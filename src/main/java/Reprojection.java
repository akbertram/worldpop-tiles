public class Reprojection {

  private static final ThreadLocal<Reprojection> THREAD_LOCAL = new ThreadLocal<>();

  public static final int BATCH_SIZE = 16;

  public final int[] gridX;
  public final int[] gridY;

  private Reprojection() {
    gridX = new int[BATCH_SIZE * TileSet.PIXELS_PER_TILE];
    gridY = new int[BATCH_SIZE * TileSet.PIXELS_PER_TILE];
  }

  public static Reprojection get() {
    Reprojection buffer = THREAD_LOCAL.get();
    if(buffer == null) {
      buffer = new Reprojection();
      THREAD_LOCAL.set(buffer);
    }
    return buffer;
  }

  /**
   * Precompute the mapping from output tile pixels to the source grid.
   *
   * This essential reprojects and resamples the image with the nearest-neighbor algorithm.
   */
  public void precomputeGridIndexes(TileSet tileSet, int tileStartX, int tileStartY, CountrySubset source) {

    double meterX = tileSet.meterTileLeft(tileStartX);
    for (int i = 0; i < gridX.length; i++) {
      double longitude = TileSet.metersToLongitude(meterX);
      gridX[i] = source.longitudeToGridX(longitude);
      meterX += tileSet.metersPerPixel;
    }

    double meterY = tileSet.meterTileTop(tileStartY);
    for (int i = 0; i < gridY.length; i++) {
      double latitude = TileSet.metersToLatitude(meterY);
      gridY[i] = source.latitudeToGridY(latitude);
      meterY -= tileSet.metersPerPixel;
    }
  }
}
