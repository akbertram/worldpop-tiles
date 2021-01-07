import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Country {

  private final File file;
  private final Tiling tiling;
  private final double longitudePerPixel;
  private final double latitudePerPixel;
  private final double topNorth;
  private final double bottomSouth;
  private final double leftWest;
  private final double rightEast;

  private final TileRect tileRect;

  private final int width;
  private final int height;

  public Country(Tiling tiling, File file) {
    this.file = file;
    this.tiling = tiling;
    Dataset dataset = gdal.Open(file.getAbsolutePath());
    width = dataset.GetRasterXSize();
    height = dataset.GetRasterYSize();

    double adfGeoTransform[] = dataset.GetGeoTransform();
    longitudePerPixel = adfGeoTransform[1];
    latitudePerPixel = -adfGeoTransform[5];

    topNorth = adfGeoTransform[3];
    bottomSouth = topNorth - (height * latitudePerPixel);
    leftWest = adfGeoTransform[0];
    rightEast = leftWest + (width * longitudePerPixel);

    tileRect = tiling.geographicRectToTileRect(topNorth, bottomSouth, leftWest, rightEast);

    dataset.delete();
  }

  public Tiling getTiling() {
    return tiling;
  }

  public File getFile() {
    return file;
  }

  public double longitudeToPixel(double longitude) {
    return (longitude - leftWest) / longitudePerPixel;
  }

  public double latitudeToPixel(double longitude) {
    return (topNorth - longitude) / latitudePerPixel;
  }

  public double pixelToLongitude(double x) {
    return leftWest + (x * longitudePerPixel);
  }

  public double pixelToLatitude(double y) {
    return topNorth + (y * latitudePerPixel);
  }

  java.util.List<TileRect> divideIntoBatches() {

    // We are assuming that the input tiffs have blocks of 1 pixel high. For this reason,
    // it makes sense to divide them into batches of horizontal bands, depending on the width
    // of the image

    int batchSize = 1024 / tileRect.getTileCountX();
    if(batchSize < 1) {
      batchSize = 1;
    }

    List<TileRect> rects = new ArrayList<>();
    for(int top=tileRect.getTopTile();top < tileRect.getBottomTile(); top+= batchSize) {
      int tileCountY = batchSize;
      if(top + tileCountY - 1 > tileRect.getBottomTile()) {
        tileCountY = tileRect.getBottomTile() - top + 1;
      }
      rects.add(new TileRect(tileRect.getLeftTile(), top, tileRect.getTileCountX(), tileCountY));
    }
    return rects;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public double approximatePixelAreaMetersAt(double longitude, double latitude) {
    double x1 = Tiling.longitudeToMeters(longitude);
    double x2 = Tiling.longitudeToMeters(longitude + longitudePerPixel);
    double y1 = Tiling.latitudeToMeters(latitude);
    double y2 = Tiling.latitudeToMeters(latitude + latitudePerPixel);

    return (x2 - x1) * (y2 - y1);
  }
}
