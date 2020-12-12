import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.media.jai.Interpolation;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class TileSet {

  public static final double RADIUS = 6378137;

  public static final double PROJ_MIN = - Math.PI * RADIUS;
  public static final double PROJ_MAX = + Math.PI * RADIUS;

  public static final double PROJ_SIZE = PROJ_MAX - PROJ_MIN;
  public static final int tileSizePixels = 256;

  public final double tileCount;
  public final int zoomLevel;
  public final double tileSizeInMeters;
  public final double pixelsPerMeter;
  public final int worldSizePixels;
  private final CoordinateReferenceSystem sourceCRS;
  public final CoordinateReferenceSystem targetCRS;
  public final AffineTransform2D pixelsToMeters2D;
  public final double metersPerPixel;

  public TileSet(int zoomLevel) throws FactoryException {
    this.targetCRS = CRS.decode("EPSG:3857");
    this.sourceCRS = CRS.decode("EPSG:4326");

    this.tileCount = Math.pow(2, zoomLevel);
    this.zoomLevel = zoomLevel;
    this.tileSizeInMeters = PROJ_SIZE / tileCount;
    this.worldSizePixels = (int) (tileCount * tileSizePixels);
    this.pixelsPerMeter = worldSizePixels / PROJ_SIZE;
    this.metersPerPixel = PROJ_SIZE / worldSizePixels;

    AffineTransform pixelToMeter = new AffineTransform();
    pixelToMeter.translate(-Math.PI * RADIUS, Math.PI * RADIUS);
    pixelToMeter.scale(PROJ_SIZE / worldSizePixels, -PROJ_SIZE / worldSizePixels);

    this.pixelsToMeters2D = new AffineTransform2D(pixelToMeter);
  }

  public int getTileSizePixels() {
    return tileSizePixels;
  }

  public GridCoverage2D project(GridCoverage2D coverage, int tileX, int tileY, int size) {

    int tileStartX = tileX * tileSizePixels;
    int tileEndY =  worldSizePixels - (tileY * tileSizePixels);
    int tileStartY = tileEndY - (size * tileSizePixels);

    GridEnvelope gridRange = new GeneralGridEnvelope(
      new Rectangle(tileStartX, tileStartY, size * tileSizePixels, size * tileSizePixels));

    GridGeometry geometry = new GridGeometry2D(gridRange, pixelsToMeters2D, targetCRS);
    return (GridCoverage2D) Operations.DEFAULT.resample(coverage, targetCRS, geometry,
      Interpolation.getInstance(Interpolation.INTERP_NEAREST));
  }

  public static double y2lat(double aY) {
    return Math.toDegrees(Math.atan(Math.exp(aY / RADIUS)) * 2 - Math.PI/2);
  }
  public static double x2lon(double aX) {
    return Math.toDegrees(aX / RADIUS);
  }

  /**
   * The position, in meters, of the tile's left edge.
   */
  public double meterTileLeft(int tileX) {
    return PROJ_MIN + (tileX * tileSizeInMeters);
  }

  public double meterTileRight(int tileX) {
    return meterTileLeft(tileX) + tileSizeInMeters;
  }

  /**
   * The position, in meters, of the tile's top edge.
   */
  public double meterTileTop(int tileY) {
    return PROJ_MAX - (tileY * tileSizeInMeters);
  }

  /**
   * The position, in meters, of the tile's top edge.
   */
  public double meterTileBottom(int tileY) {
    return meterTileTop(tileY) - tileSizeInMeters;
  }

  public Envelope2D getTileEnvelopeInMeters(int tileX, int tileY) {
    return new Envelope2D(targetCRS, meterTileLeft(tileX), meterTileBottom(tileY), tileSizeInMeters, tileSizeInMeters);
  }

  public Rectangle2D getTileGeographicBounds(int tileX, int tileY) {
    double left = x2lon(meterTileLeft(tileX));
    double right = x2lon(meterTileRight(tileX));
    double top = y2lat(meterTileTop(tileY));
    double bottom = y2lat(meterTileBottom(tileY));

    return new Rectangle2D.Double(left, bottom, right - left, top - bottom);
  }
}
