import com.google.cloud.storage.*;
import com.google.common.base.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GcsUploader implements AutoCloseable {

  private final String bucketName;
  private final String objectNamePrefix;
  private final ConcurrentLinkedQueue<File> uploadQueue = new ConcurrentLinkedQueue<>();
  private final Storage client;

  private boolean closed = false;

  private List<Thread> uploaderThreads = new ArrayList<>();
  private final Thread reporterThread;

  public GcsUploader(Storage client, String bucketName, String tileSetName) {
    this.bucketName = bucketName;
    this.objectNamePrefix = tileSetName + "/";
    this.client = client;

    // Allocate a relatively large number of threads
    // for uploading as we will spend most of the time waiting for network
    // operations to complete.

    int nThreads = 10;
    if (!Strings.isNullOrEmpty(System.getenv("GCS_THREADS"))) {
      nThreads = Integer.parseInt(System.getenv("GCS_THREADS"));
    }

    for (int i = 0; i < nThreads; i++) {
      Thread thread = new Thread(this::processUploadQueue);
      thread.setName("GCS Uploader " + i);
      thread.start();
      uploaderThreads.add(thread);
    }

    reporterThread = new Thread(() -> {
      boolean started = false;
      while(true) {
        try {
          Thread.sleep(30_000);
        } catch (InterruptedException e) {
          return;
        }
        if(started || uploadQueue.size() > 0) {
          System.err.println(uploadQueue.size() + " tiles in upload queue");
          started = true;
        }
      }
    });
    reporterThread.setName("GCS Uploader Reporter");
    reporterThread.start();
  }

  public static GcsUploader fromEnvironment() {
    Storage service = StorageOptions.getDefaultInstance().getService();
    String bucket = System.getenv("GCS_TILE_BUCKET");
    if(Strings.isNullOrEmpty(bucket)) {
      bucket = "worldpop-test-1";
    }
    String tileset = System.getenv("GCS_TILE_PREFIX");
    if(Strings.isNullOrEmpty(tileset)) {
      tileset = "dev6";
    }
    return new GcsUploader(service, bucket, tileset);
  }

  public void uploadZoomDirectory(File zoomDir) {
    for (File subDir : zoomDir.listFiles()) {
      if(subDir.isDirectory()) {
        uploadQueue.offer(subDir);
      }
    }
  }

  private void processUploadQueue() {

    StringBuilder name = new StringBuilder(objectNamePrefix);

    while(true) {
      File dir = uploadQueue.poll();
      if(dir == null) {
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

      byte buffer[] = new byte[1024 * 32];

      name.setLength(objectNamePrefix.length());
      name.append(dir.getParentFile().getName());
      name.append('/');
      name.append(dir.getName());
      name.append('/');

      int prefixLength = name.length();

      File[] files = dir.listFiles();
      if(files != null) {
        for (File file : files) {

          name.setLength(prefixLength);
          name.append(file.getName());

          int fileLength = (int)file.length();

          // We don't expect tiles larger than 32k, but just in case...
          if(fileLength > buffer.length) {
            buffer = new byte[fileLength];
          }

          readFile(buffer, file, fileLength);

          BlobInfo blobInfo = Blob.newBuilder(BlobId.of(bucketName, name.toString()))
            .setContentType("image/png")
            .build();

          client.create(blobInfo, buffer, 0, fileLength);
        }
      }
    }
  }

  private void readFile(byte[] buffer, File file, int fileLength) {
    try(FileInputStream in = new FileInputStream(file)) {
      int bytesRead = 0;
      while(bytesRead < fileLength) {
        bytesRead += in.read(buffer, bytesRead, fileLength - bytesRead);
      }
    } catch (IOException e) {
      e.printStackTrace();
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
    reporterThread.interrupt();
    reporterThread.join();
    System.err.println("Upload queue finished.");
  }

}
