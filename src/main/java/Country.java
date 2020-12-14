import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.geometry.Envelope2D;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;

public class Country {

  private static final ThreadLocal<int[]> THREAD_LOCAL_BUFFER = new ThreadLocal<>();
  public static final int MAX_ERROR_COUNT = 3;

  private final File file;
  private final GridCoverage2D coverage;
  private final RenderedImage image;
  private final GridEnvelope2D gridRange;

  private int errorCount = 0;

  public Country(File file, GridCoverage2D coverage) {
    this.file = file;
    this.coverage = coverage;
    this.image = coverage.getRenderableImage(0, 1).createDefaultRendering();
    this.gridRange = coverage.getGridGeometry().getGridRange2D();
  }

  public Envelope2D getGeographicBounds() {
    return coverage.getEnvelope2D();
  }

  public CountrySubset extractImage(Envelope2D bounds) throws TransformException {

    if(errorCount >= MAX_ERROR_COUNT) {
      return null;
    }

    GridEnvelope2D gridBounds = coverage.getGridGeometry().worldToGrid(bounds);

    int left = gridBounds.x;
    int top = gridBounds.y;
    int width = gridBounds.width;
    int height = gridBounds.height;

    int offsetX = 0;
    int offsetY = 0;

    if(left < 0) {
      offsetX = -left;
      left = 0;
      width -= offsetX;
    }

    if(top < 0) {
      offsetY = -top;
      top = 0;
      height -= offsetY;
    }

    if(left + width > gridRange.getSpan(0)) {
      width = gridRange.getSpan(0) - left;
    }

    if(top + height > gridRange.getSpan(1)) {
      height = gridRange.getSpan(1) - top;
    }

    if(width < 0 || height < 0) {
      return null;
    }

    // Iterate over the tiles that overlap with this range
    Raster raster;
    try {
      raster = image.getData(new Rectangle(left, top, width, height));
    } catch (Exception e) {
      System.out.println("Exception reading from " + file.getName());
      e.printStackTrace();
      errorCount++;
      if(errorCount >= MAX_ERROR_COUNT) {
        System.out.println("Too many errors reading from " + file.getName() + ", will not attempt further.");
      }
      return null;
    }

    // Try to reuse the buffer if big enough
    int[] buffer = THREAD_LOCAL_BUFFER.get();
    if(buffer == null || buffer.length < (width * height)) {
      buffer = new int[width * height];
      THREAD_LOCAL_BUFFER.set(buffer);
    }

    int[] pixels = raster.getPixels(left, top, width, height, buffer);

    return new CountrySubset(coverage, left, top, width, height, pixels);
  }

}
