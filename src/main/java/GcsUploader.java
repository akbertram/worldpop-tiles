import com.google.cloud.storage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GcsUploader implements AutoCloseable {

  private static final int BATCH_SIZE = 50;
  private final String bucketName;
  private final String objectNamePrefix;
  private final ConcurrentLinkedQueue<MbTiles.Tile> uploadQueue = new ConcurrentLinkedQueue<>();

  private boolean closed = false;

  private List<Thread> uploaderThreads = new ArrayList<>();

  public GcsUploader(String bucketName, String tileSetName) {
    this.bucketName = bucketName;
    this.objectNamePrefix = tileSetName + "/";
  }

  public void start() {
    // Allocate a relatively large number of threads
    // for uploading as we will spend most of the time waiting for network
    // operations to complete.

    int nThreads = Runtime.getRuntime().availableProcessors() * 3;
    for (int i = 0; i < nThreads; i++) {
      Thread thread = new Thread(this::processQueue);
      thread.setName("GCS Uploader " + i);
      thread.start();
      uploaderThreads.add(thread);
    }
  }


  public void upload(MbTiles.Tile tile) {
    uploadQueue.offer(tile);
  }

  private void processQueue() {
    Storage storage = StorageOptions.getDefaultInstance().getService();

    StringBuilder name = new StringBuilder(objectNamePrefix);

    while(true) {
      MbTiles.Tile tile = uploadQueue.poll();
      if(tile == null) {
        // If this uploader has been closed, and there are no more
        // entries in the queue, then we can stop and upload any remainders
        if(closed) {
          break;
        }
        // Pause and wait for a new tile to be
        // placed into the uploader queue
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // If we are interrupted, stop immediately without
          // taking any further action
          return;
        }
        // Try again
        continue;
      }

      name.setLength(objectNamePrefix.length());
      name.append(tile.zoom);
      name.append('/');
      name.append(tile.x);
      name.append('/');
      name.append(tile.y);
      name.append(".png");

      BlobInfo blobInfo = Blob.newBuilder(BlobId.of(bucketName, name.toString()))
        .setContentType("image/png")
        .build();

      storage.create(blobInfo, tile.image, 0, tile.image.length);

    }
  }

  /**
   * Closes the uploader and waits for all uploading threads to finish.
   */
  public void close() throws InterruptedException {
    closed = true;
    for (Thread thread : uploaderThreads) {
      thread.join();
    }
  }

}
