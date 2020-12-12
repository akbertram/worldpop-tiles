import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class TileSet {

  public static final double RADIUS = 6378137;

  public static final double PROJ_MIN = - Math.PI * RADIUS;
  public static final double PROJ_MAX = + Math.PI * RADIUS;

  public static final double PROJ_SIZE = PROJ_MAX - PROJ_MIN;

  public static final int PIXELS_PER_TILE = 256;

  public final double tileCount;
  public final int zoomLevel;
  public final double metersPerTile;
  public final double pixelsPerMeter;
  public final int worldSizePixels;
  public final CoordinateReferenceSystem sourceCRS;
  public final CoordinateReferenceSystem targetCRS;
  public final double metersPerPixel;

  public TileSet(int zoomLevel) throws FactoryException {
    this.sourceCRS = CRS.decode("EPSG:4326");
    this.targetCRS = CRS.decode("EPSG:3857");

    this.tileCount = Math.pow(2, zoomLevel);
    this.zoomLevel = zoomLevel;
    this.metersPerTile = PROJ_SIZE / tileCount;
    this.worldSizePixels = (int) (tileCount * PIXELS_PER_TILE);
    this.pixelsPerMeter = worldSizePixels / PROJ_SIZE;
    this.metersPerPixel = PROJ_SIZE / worldSizePixels;
  }

  public static double metersToLatitude(double aY) {
    return Math.toDegrees(Math.atan(Math.exp(aY / RADIUS)) * 2 - Math.PI/2);
  }
  public static double metersToLongitude(double aX) {
    return (aX / RADIUS) * 180.0 / Math.PI;
  }

  /**
   * The position, in meters, of the tile's left edge.
   */
  public double meterTileLeft(int tileX) {
    return PROJ_MIN + (tileX * metersPerTile);
  }

  public double meterTileRight(int tileX) {
    return meterTileLeft(tileX) + metersPerTile;
  }

  /**
   * The position, in meters, of the tile's top edge.
   */
  public double meterTileTop(int tileY) {
    return PROJ_MAX - (tileY * metersPerTile);
  }

  /**
   * The position, in meters, of the tile's top edge.
   */
  public double meterTileBottom(int tileY) {
    return meterTileTop(tileY) - metersPerTile;
  }

  public Envelope2D getGeographicBounds(int tileX, int tileY, int tileCount) {
    double left = metersToLongitude(meterTileLeft(tileX));
    double right = metersToLongitude(meterTileRight(tileX + tileCount));
    double top = metersToLatitude(meterTileTop(tileY));
    double bottom = metersToLatitude(meterTileBottom(tileY + tileCount));

    return new Envelope2D(sourceCRS, left, bottom, right - left, top - bottom);
  }
}
