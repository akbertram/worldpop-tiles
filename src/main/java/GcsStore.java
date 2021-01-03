import com.google.cloud.storage.*;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

public class GcsStore implements TileStore {

  public static final int MAX_ZOOM = 16;

  private final Storage client;
  private final String bucketName;
  private final String tileSetName;

  private GcsUploader uploader;

  public GcsStore() {
    this.client = StorageOptions.getDefaultInstance().getService();
    bucketName = "worldpoptiles";
    tileSetName = "v1/2020";
    this.uploader = new GcsUploader(client, bucketName, tileSetName);
  }

  @Override
  public void write(int zoom, int x, int y, RenderedImage image) {
    uploader.upload(new Tile(zoom, x, y, Png.toPngBytes(image)));
  }

  @Override
  public BufferedImage[] read(int zoom, int startX, int startY, int tileSpan) {
    StorageBatch batch = client.batch();

    StorageBatchResult<Blob> blobs[] = new StorageBatchResult[tileSpan * tileSpan];
    BufferedImage images[] = new BufferedImage[tileSpan * tileSpan];

    int index = 0;

    String objectNamePrefix = tileSetName + "/" + zoom + "/";

    for (int x = 0; x < tileSpan; x++) {
      for (int y = 0; y < tileSpan; y++) {
        blobs[index] = batch.get(BlobId.of(bucketName, objectNamePrefix + (startX + x) + "/" + (startY + y) + ".png"));
        index++;
      }
    }
    for (int i = 0; i < blobs.length; i++) {
      StorageBatchResult<Blob> batchResult = blobs[i];
      if(batchResult != null) {
        Blob blob = batchResult.get();
        if(blob != null) {
          byte[] content = blob.getContent();
          images[i] = Png.fromBytes(content);
        }
      }
    }

    return images;
  }

  @Override
  public void flush() throws InterruptedException {
    GcsUploader previousUploader = this.uploader;
    this.uploader = new GcsUploader(client, bucketName, tileSetName);
    previousUploader.close();
  }

  @Override
  public void close() throws Exception {
    uploader.close();
  }
}
