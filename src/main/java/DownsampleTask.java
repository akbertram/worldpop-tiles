import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class DownsampleTask extends RecursiveAction {

  private final TileStore tileStore;
  private final int zoomLevel;
  private final int tileStartX;
  private final int tileStartY;
  private final int tileSpan;

  public DownsampleTask(TileStore tileStore, int zoomLevel, int tileStartX, int tileStartY, int tileSpan) {
    this.tileStore = tileStore;
    this.zoomLevel = zoomLevel;
    this.tileStartX = tileStartX;
    this.tileStartY = tileStartY;
    this.tileSpan = tileSpan;
  }

  @Override
  protected void compute() {
    if(tileSpan == 1) {
      downSample();
    } else {
      int halfSpan = this.tileSpan / 2;
      ForkJoinTask.invokeAll(
        new DownsampleTask(tileStore, zoomLevel, tileStartX, tileStartY, halfSpan),
        new DownsampleTask(tileStore, zoomLevel, tileStartX + halfSpan, tileStartY, halfSpan),
        new DownsampleTask(tileStore, zoomLevel, tileStartX, tileStartY + halfSpan, halfSpan),
        new DownsampleTask(tileStore, zoomLevel, tileStartX + halfSpan, tileStartY + halfSpan, halfSpan));
    }
  }

  private void downSample() {

    int sx = tileStartX * 2;
    int sy = tileStartY * 2;

    TileReader reader = tileStore.getReader();
    BufferedImage[] images = new BufferedImage[4];
    int imageCount = 0;
    int i = 0;
    for (int x = 0; x < 2; x++) {
      for (int y = 0; y < 2; y++) {
        BufferedImage image = reader.read(zoomLevel + 1, sx + x, sy + y);
        if(image != null) {
          images[i] = image;
          imageCount++;
        }
        i++;
      }
    }

    if(imageCount == 0) {
      return;
    }

    CompositeBuffer buffer = CompositeBuffer.get();
    BufferedImage downsampledImage = buffer.render(images);

    tileStore.write(zoomLevel, tileStartX, tileStartY, downsampledImage);
  }
}
