public class Tiling {

  public static final double RADIUS = 6378137;

  public static final double PROJ_MIN = - Math.PI * RADIUS;
  public static final double PROJ_MAX = + Math.PI * RADIUS;

  public static final double PROJ_SIZE = PROJ_MAX - PROJ_MIN;

  public static final int PIXELS_PER_TILE = 256;

  public static final double originShift = 2 * Math.PI * RADIUS / 2.0;

  public final double tileCount;
  public final int zoomLevel;
  public final double metersPerTile;
  public final double pixelsPerMeter;
  public final int worldSizePixels;
  public final double metersPerPixel;

  public Tiling(int zoomLevel) {
    this.tileCount = tileCount(zoomLevel);
    this.zoomLevel = zoomLevel;
    this.metersPerTile = PROJ_SIZE / tileCount;
    this.worldSizePixels = (int) (tileCount * PIXELS_PER_TILE);
    this.pixelsPerMeter = worldSizePixels / PROJ_SIZE;
    this.metersPerPixel = PROJ_SIZE / worldSizePixels;
  }

  public static int tileCount(int zoomLevel) {
    return (int)Math.pow(2, zoomLevel);
  }

  public double getMetersPerPixel() {
    return metersPerPixel;
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

  public static double latitudeToMeters(double latitude) {
    double my = Math.log( Math.tan((90 + latitude) * Math.PI / 360.0 )) / (Math.PI / 180.0);
    my = my * originShift / 180.0;
    return my;
  }

  public static double longitudeToMeters(double longitude) {
    return longitude * originShift / 180.0;
  }

  /**
   * The position, in meters, of the tile's top edge.
   */
  public double meterTileBottom(int tileY) {
    return meterTileTop(tileY) - metersPerTile;
  }

  TileRect geographicRectToTileRect(double topNorth, double bottomSouth, double leftWest, double rightEast) {

    int topTile = metersToTileY(latitudeToMeters(topNorth));
    int bottomTile = metersToTileY(latitudeToMeters(bottomSouth));
    int leftTile = metersToTileX(longitudeToMeters(leftWest));
    int rightTile = metersToTileX(longitudeToMeters(rightEast));

    return new TileRect(leftTile, topTile, rightTile - leftTile + 1, bottomTile - topTile + 1);
  }

  GeoRect tileRectToGeoRect(TileRect rect) {
    double longitudeWestLeft = metersToLongitude(meterTileLeft(rect.getLeftTile()));
    double latitudeNorth = metersToLatitude(meterTileTop(rect.getTopTile()));
    double longitudeEastRight = metersToLongitude(meterTileRight(rect.getRightTile()));
    double latitudeSouth = metersToLatitude(meterTileBottom(rect.getBottomTile()));

    return new GeoRect(latitudeNorth, latitudeSouth, longitudeEastRight, longitudeWestLeft);
  }

  public int metersToTileX(double metersX) {
    return (int)Math.floor((metersX - PROJ_MIN) / metersPerTile);
  }

  public int metersToTileY(double metersY) {
    return (int)Math.floor( (PROJ_MAX - metersY) / metersPerTile);
  }

}
