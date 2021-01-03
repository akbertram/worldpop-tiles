import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.util.concurrent.atomic.AtomicLong;

public class TileBatch {

  private static final AtomicLong tileCount = new AtomicLong(0);
  private static final long startTime = System.currentTimeMillis();

  private final Country country;
  private final Tiling tiling;
  private final TileStore tileStore;
  private final TileRect tileRect;

  private int sourceLeft;
  private int sourceTop;
  private int sourceWidth;
  private int sourceHeight;

  private final int targetWidth;
  private final int targetHeight;

  private int[] projectionX;
  private int[] projectionY;
  private short[] sourceArray;

  public TileBatch(Country country, TileStore tileStore, TileRect rect) {
    this.country = country;
    this.tileStore = tileStore;
    this.tileRect = rect;
    this.tiling = country.getTiling();

    // Find the geographic bounds of this tile range (in degrees)
    GeoRect geoRect = tiling.TileRectToGeoRect(rect);

    // Now map these geographic bounds to a rectangle within the original country image (in pixels)
    sourceLeft = (int) Math.floor(country.longitudeToPixel(geoRect.getLeft()));
    sourceTop = (int) Math.floor(country.latitudeToPixel(geoRect.getTop()));
    int sourceRight = (int) Math.ceil(country.longitudeToPixel(geoRect.getRight()));
    int sourceBottom = (int) Math.ceil(country.latitudeToPixel(geoRect.getBottom()));

    // The source rectangle might lay outside the bounds of the image
    // So adjust...

    if(sourceLeft < 0) {
      sourceLeft = 0;
    }
    if(sourceRight > country.getWidth()) {
      sourceRight = country.getWidth();
    }
    if(sourceTop < 0) {
      sourceTop = 0;
    }
    if(sourceBottom > country.getHeight()) {
      sourceBottom = country.getHeight();
    }

    sourceWidth = sourceRight - sourceLeft;
    sourceHeight = sourceBottom - sourceTop;
    targetWidth = tileRect.getTileCountX() * Tiling.PIXELS_PER_TILE;
    targetHeight = tileRect.getTileCountY() * Tiling.PIXELS_PER_TILE;
  }

  void render() {
    readImage();
    project();
    renderTiles();
    sourceArray = null;
    tileCount.addAndGet(tileRect.getTileCount());
    PrintStatistics();
  }

  static void PrintStatistics() {
    double elapsedMillis = System.currentTimeMillis() - startTime;
    double elpasedSeconds = elapsedMillis / 1000d;
    double tilesRendered = tileCount.get();
    double tilesPerSecond = tilesRendered / elpasedSeconds;

    System.err.println(String.format("Rendering rate: %.3f tiles/second", tilesPerSecond));
  }

  private double longitudeToPixel(double longitude) {
    return country.longitudeToPixel(longitude) - sourceLeft;
  }

  private double latitudeToPixel(double latitude) {
    return country.latitudeToPixel(latitude) - sourceTop;
  }

  private short getPopulation(int x, int y) {
    if(x < 0 || y < 0 || x >= sourceWidth || y >= sourceHeight) {
      return -1;
    } else {
      return sourceArray[((y * sourceWidth) + x)];
    }
  }

  private void project() {

    projectionX = new int[targetWidth];
    projectionY = new int[targetHeight];

    double meterX = tiling.meterTileLeft(tileRect.getLeftTile());
    for (int i = 0; i < targetWidth; i++) {
      double longitude = Tiling.metersToLongitude(meterX);
      projectionX[i] = (int) Math.round(longitudeToPixel(longitude));
      meterX += tiling.getMetersPerPixel();
    }

    double meterY = tiling.meterTileTop(tileRect.getTopTile());
    for (int i = 0; i < targetHeight; i++) {
      double latitude = Tiling.metersToLatitude(meterY);
      projectionY[i] = (int) Math.round(latitudeToPixel(latitude));
      meterY -= tiling.getMetersPerPixel();
    }
  }

  private void readImage() {

    Dataset dataset = gdal.Open(country.getFile().getAbsolutePath());
    Band band = dataset.GetRasterBand(1);

    sourceArray = new short[sourceWidth * sourceHeight];
    band.ReadRaster(sourceLeft, sourceTop, sourceWidth, sourceHeight,
      gdalconstConstants.GDT_Int16, sourceArray);

    dataset.delete();
  }

  private void renderTiles() {
    for (int tileX = 0; tileX < tileRect.getTileCountX(); tileX++) {
      for (int tileY = 0; tileY < tileRect.getTileCountY(); tileY++) {
        renderTile(tileX, tileY);
      }
    }

  }

  private void renderTile(int tileX, int tileY) {

    boolean empty = true;

    TileImage tileBuffer = TileImage.getBuffer();
    tileBuffer.clear();

//
//    if(!tileStore.tryRead(tiling.zoomLevel, tileRect.getLeftTile() + tileX, tileRect.getTopTile() + tileY, pixelBuffer)) {
//      Arrays.fill(pixelBuffer, ColorGradient.TRANSPARENT);
//    }

    int startX = tileX * Tiling.PIXELS_PER_TILE;
    int startY = tileY * Tiling.PIXELS_PER_TILE;
    int pixelIndex = 0;

    for (int y = 0; y < Tiling.PIXELS_PER_TILE; y++) {
      int gridY = projectionY[startY + y];

      for (int x = 0; x < Tiling.PIXELS_PER_TILE; x++) {
        int gridX = projectionX[startX + x];
        short pop = getPopulation(gridX, gridY);

        if (pop >= 0) {
          tileBuffer.setColorIndex(pixelIndex, ColorGradient.populationToColorIndex(pop));
          empty = false;
        }
        pixelIndex ++;
      }
    }

    if(!empty) {
      tileStore.write(tiling.zoomLevel, tileRect.getLeftTile() + tileX, tileRect.getTopTile() + tileY, tileBuffer);
    }
  }

}
