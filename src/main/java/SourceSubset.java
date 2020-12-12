import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.coverage.grid.GridEnvelope;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SourceSubset {

  private final double pixelsPerDegreeLongitude;
  private final double pixelsPerDegreeLatitude;
  private final double latitudeNorth;
  private final double longitudeWest;
  private final int left;
  private final int top;
  private final int scanLine;
  private final int[] pixels;

  public SourceSubset(GridCoverage2D coverage, int left, int top, int scanLine, int[] pixels) {
    this.left = left;
    this.top = top;
    this.scanLine = scanLine;
    this.pixels = pixels;
    GridEnvelope gridRange = coverage.getGridGeometry().getGridRange();
    pixelsPerDegreeLongitude = gridRange.getSpan(0) / coverage.getEnvelope2D().getWidth();
    pixelsPerDegreeLatitude = gridRange.getSpan(1) / coverage.getEnvelope2D().getHeight();
    longitudeWest = coverage.getEnvelope2D().getMinX();
    latitudeNorth = coverage.getEnvelope2D().getMaxY();
  }

  public int get(int x, int y) {
    try {
      return pixels[(y * scanLine) + x];
    } catch (ArrayIndexOutOfBoundsException e) {
      return -1;
    }
  }

  public int longitudeToGridX(double longitude) {
    return (int)Math.round((longitude - longitudeWest) * pixelsPerDegreeLongitude) - left;
  }

  public int latitudeToGridY(double latitude) {
    return (int)Math.round((latitudeNorth - latitude) * pixelsPerDegreeLatitude) - top;
  }
}
