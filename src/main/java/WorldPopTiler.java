import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.Envelope2D;
import org.opengis.referencing.FactoryException;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

public class WorldPopTiler {

  public static void main(String[] args) throws Exception {
    renderBaseTiles();
//    downsample();
  }

  private static void renderBaseTiles() throws IOException, FactoryException {
    File file = new File("tif/ppp_2020_1km_Aggregated.tif");

    AbstractGridFormat format = GridFormatFinder.findFormat( file );
    GridCoverage2DReader reader = format.getReader( file );

    String[] coverageNames = reader.getGridCoverageNames();


    GridCoverage2D coverage = reader.read(coverageNames[0], null);

    TileSet tileSet = new TileSet(9);

    Point2D latLng = new Point2D.Double();

    ColorGradient gradient = new ColorGradient();

    float[] pop = new float[1];
    int[] pixels = new int[256 * 256];
    int pixelIndex = 0;

    double leftMeterX = tileSet.meterTileLeft(298);
    double meterX;
    double meterY = tileSet.meterTileTop(209);

    Envelope2D coverageEnvelope = coverage.getEnvelope2D();
    boolean fullyContained = coverageEnvelope.contains(tileSet.getTileGeographicBounds(298, 209));

    for (int y = 0; y < 256; y++) {
      meterX = leftMeterX;
      for (int x = 0; x < 256; x++) {
        latLng.setLocation(TileSet.x2lon(meterX), TileSet.y2lat(meterY));
        if(fullyContained || coverageEnvelope.contains(latLng)) {
          pop = coverage.evaluate(latLng, pop);
          if (pop[0] == Float.MAX_VALUE) {
            pixels[pixelIndex] = ColorGradient.TRANSPARENT;
          } else {
            pixels[pixelIndex] = gradient.color((int) pop[0]);
          }
        } else {
          pixels[pixelIndex] = ColorGradient.TRANSPARENT;
        }
        pixelIndex++;
        meterX += tileSet.metersPerPixel;
      }
      meterY -= tileSet.metersPerPixel;
    }

    BufferedImage bufferedImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
    bufferedImage.setRGB(0, 0, 256, 256, pixels, 0, 256);

    ImageIO.write(bufferedImage, "png", new File("/tmp/test.png"));
  }


  private static void downsample() {
    for (int zoom = 8; zoom >= 0; zoom--) {
      ForkJoinPool.commonPool().invoke(new Downsampler(zoom, 0, 0, (int)Math.pow(2, zoom)));
    }
  }

}
