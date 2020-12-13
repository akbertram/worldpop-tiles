import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

public class WorldPopTiler {

  public static void main(String[] args) throws Exception {

    int baseZoomLevel = 11;
    renderBaseTiles(baseZoomLevel);
    downsample(baseZoomLevel);
  }

  private static void renderBaseTiles(int zoomLevel) throws IOException, FactoryException {

    Countries countries = new Countries(new File("tif_country"));

    TileSet tileSet = new TileSet(zoomLevel);

    Progress.starting(tileSet);

    System.out.println("Zoom level " + tileSet.zoomLevel);

    ColorGradient gradient = new ColorGradient(100);

    ForkJoinPool.commonPool().invoke(new TileRenderTask(tileSet, countries, gradient, 0, 0,
      (int)tileSet.tileCount));

    Progress.tilesDone();
  }

  private static void downsample(int baseZoomLevel) {
    for (int zoom = baseZoomLevel - 1; zoom >= 0; zoom--) {
      System.out.println("Downsampling zoom level " + zoom + "...");
      ForkJoinPool.commonPool().invoke(new Downsampler(zoom, 0, 0, (int)Math.pow(2, zoom)));
    }
  }

}
