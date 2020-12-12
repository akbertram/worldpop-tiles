import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.media.jai.Interpolation;
import java.awt.*;
import java.awt.geom.AffineTransform;

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
  public final CoordinateReferenceSystem targetCRS;
  public final AffineTransform2D pixelsToMeters2D;

  public TileSet(int zoomLevel) throws FactoryException {
    this.targetCRS = CRS.decode("EPSG:3857");

    this.tileCount = Math.pow(2, zoomLevel);
    this.zoomLevel = zoomLevel;
    this.tileSizeInMeters = PROJ_SIZE / tileCount;
    this.worldSizePixels = (int) (tileCount * tileSizePixels);
    this.pixelsPerMeter = worldSizePixels / PROJ_SIZE;

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
    return (GridCoverage2D) Operations.DEFAULT.resample(coverage, null, geometry,
      Interpolation.getInstance(Interpolation.INTERP_NEAREST));
  }
}
