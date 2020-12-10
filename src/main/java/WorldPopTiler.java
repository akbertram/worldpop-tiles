import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;

import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.util.concurrent.ForkJoinPool;

public class WorldPopTiler {

  public static void main(String[] args) throws Exception {
    File file = new File("tif/ppp_2020_1km_Aggregated_mercator.tif");

    AbstractGridFormat format = GridFormatFinder.findFormat( file );
    GridCoverage2DReader reader = format.getReader( file );

    String[] coverageNames = reader.getGridCoverageNames();

    TileSet tileSet = new TileSet(8);

    Progress.starting(tileSet);

    GridCoverage2D coverage = reader.read(coverageNames[0], null);

    System.out.println("Zoom level " + tileSet.zoomLevel);

    ColorGradient gradient = new ColorGradient();

    ForkJoinPool.commonPool().invoke(new TileRenderTask(tileSet, coverage, gradient, 0, 0, (int)tileSet.tileCount));
  }

}
