import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class DownsampleTask extends RecursiveAction {

  private final Tiling tiling;
  private final int zoomLevel;
  private final TileStore store;
  private final int tileStartX;
  private final int tileStartY;
  private final int tileSpan;

  public DownsampleTask(Tiling tiling, TileStore store, int tileStartX, int tileStartY, int tileSpan) {
    this.tiling = tiling;
    this.zoomLevel = tiling.zoomLevel;
    this.store = store;
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
        new DownsampleTask(tiling, store, tileStartX, tileStartY, halfSpan),
        new DownsampleTask(tiling, store, tileStartX + halfSpan, tileStartY, halfSpan),
        new DownsampleTask(tiling, store, tileStartX, tileStartY + halfSpan, halfSpan),
        new DownsampleTask(tiling, store, tileStartX + halfSpan, tileStartY + halfSpan, halfSpan));
    }
  }

  private void downSample() {

    int sx = tileStartX * 2;
    int sy = tileStartY * 2;
    BufferedImage images[] = new BufferedImage[4];
    int index = 0;
    for (int x = 0; x < 2; x++) {
      for (int y = 0; y < 2; y++) {
        images[index] = store.read(zoomLevel + 1, sx + x, sy + y);
        index++;
      }
    }

    CompositeBuffer buffer = CompositeBuffer.get();
    BufferedImage downsampledImage = buffer.render(images);
    if(downsampledImage != null) {
      store.write(zoomLevel, tileStartX, tileStartY, downsampledImage);
    }
  }
}
