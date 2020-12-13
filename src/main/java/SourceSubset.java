import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.coverage.grid.GridEnvelope;

public class SourceSubset {

  private final double pixelsPerDegreeLongitude;
  private final double pixelsPerDegreeLatitude;
  private final double latitudeNorth;
  private final double longitudeWest;
  private final int left;
  private final int top;
  private final int width;
  private final int height;
  private final int[] pixels;

  public SourceSubset(GridCoverage2D coverage, int left, int top, int width, int height, int[] pixels) {
    this.left = left;
    this.top = top;
    this.width = width;
    this.height = height;
    this.pixels = pixels;
    GridEnvelope gridRange = coverage.getGridGeometry().getGridRange();
    pixelsPerDegreeLongitude = gridRange.getSpan(0) / coverage.getEnvelope2D().getWidth();
    pixelsPerDegreeLatitude = gridRange.getSpan(1) / coverage.getEnvelope2D().getHeight();
    longitudeWest = coverage.getEnvelope2D().getMinX();
    latitudeNorth = coverage.getEnvelope2D().getMaxY();
  }

  public int get(int x, int y) {
    if(x < 0 || y < 0 || x >= width || y >= height) {
      return -1;
    } else {
      float pop = pixels[(y * width) + x];
      return (int)pop;
    }
  }

  public int longitudeToGridX(double longitude) {
    return (int)Math.round((longitude - longitudeWest) * pixelsPerDegreeLongitude) - left;
  }

  public int latitudeToGridY(double latitude) {
    return (int)Math.round((latitudeNorth - latitude) * pixelsPerDegreeLatitude) - top;
  }
}
