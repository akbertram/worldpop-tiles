import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

public class WorldPopTiler {

  public static void main(String[] args) throws Exception {
    renderBaseTiles();
    downsample();
  }

  private static void renderBaseTiles() throws IOException, FactoryException {
    File file = new File("tif/ppp_2020_1km_Aggregated_mercator.tif");

    AbstractGridFormat format = GridFormatFinder.findFormat( file );
    GridCoverage2DReader reader = format.getReader( file );

    String[] coverageNames = reader.getGridCoverageNames();

    TileSet tileSet = new TileSet(9);

    Progress.starting(tileSet);

    GridCoverage2D coverage = reader.read(coverageNames[0], null);

    System.out.println("Zoom level " + tileSet.zoomLevel);

    ColorGradient gradient = new ColorGradient();

    ForkJoinPool.commonPool().invoke(new TileRenderTask(tileSet, coverage, gradient, 0, 0, (int)tileSet.tileCount));
  }


  private static void downsample() {
    for (int zoom = 8; zoom >= 0; zoom--) {
      ForkJoinPool.commonPool().invoke(new Downsampler(zoom, 0, 0, (int)Math.pow(2, zoom)));
    }
  }

}
