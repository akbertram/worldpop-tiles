import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class DownsampleTask extends RecursiveAction {

  private final CountrySet countrySet;
  private final TileSet tileSet;
  private final TileStore tileStore;
  private final int zoomLevel;
  private final int tileStartX;
  private final int tileStartY;
  private final int tileSpan;

  public DownsampleTask(CountrySet countrySet, TileSet tileSet, TileStore tileStore, int zoomLevel, int tileStartX, int tileStartY, int tileSpan) {
    this.countrySet = countrySet;
    this.tileSet = tileSet;
    this.tileStore = tileStore;
    this.zoomLevel = zoomLevel;
    this.tileStartX = tileStartX;
    this.tileStartY = tileStartY;
    this.tileSpan = tileSpan;
  }

  @Override
  protected void compute() {

    // Check to see if this region is empty
    if(countrySet.isEmpty(tileSet.getGeographicBounds(tileStartX, tileStartY, tileSpan))) {
      return;
    }

    if(tileSpan == 1) {
      downSample();
    } else {
      int halfSpan = this.tileSpan / 2;
      ForkJoinTask.invokeAll(
        new DownsampleTask(countrySet, tileSet, tileStore, zoomLevel, tileStartX, tileStartY, halfSpan),
        new DownsampleTask(countrySet, tileSet, tileStore, zoomLevel, tileStartX + halfSpan, tileStartY, halfSpan),
        new DownsampleTask(countrySet, tileSet, tileStore, zoomLevel, tileStartX, tileStartY + halfSpan, halfSpan),
        new DownsampleTask(countrySet, tileSet, tileStore, zoomLevel, tileStartX + halfSpan, tileStartY + halfSpan, halfSpan));
    }
  }

  private void downSample() {

    int sx = tileStartX * 2;
    int sy = tileStartY * 2;

    BufferedImage[] images = tileStore.read(zoomLevel + 1, sx, sy, 2);

    CompositeBuffer buffer = CompositeBuffer.get();
    BufferedImage downsampledImage = buffer.render(images);
    if(downsampledImage != null) {
      tileStore.write(zoomLevel, tileStartX, tileStartY, downsampledImage);
    }
  }
}
