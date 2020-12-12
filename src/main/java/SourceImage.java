import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.geometry.Envelope2D;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;

public class SourceImage {

  private static ThreadLocal<int[]> THREAD_LOCAL_BUFFER = new ThreadLocal<>();

  private final GridCoverage2D coverage;
  private final RenderedImage image;
  private final GridEnvelope2D gridRange;

  public SourceImage(GridCoverage2D coverage) {
    this.coverage = coverage;
    this.image = coverage.getRenderableImage(0, 1).createDefaultRendering();
    this.gridRange = coverage.getGridGeometry().getGridRange2D();

  }

  public SourceSubset extractImage(Envelope2D bounds) throws TransformException {
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
    Raster raster = image.getData(new Rectangle(left, top, width, height));

    // Try to reuse the buffer if big enough
    int[] buffer = THREAD_LOCAL_BUFFER.get();
    if(buffer == null || buffer.length < (width * height)) {
      buffer = new int[width * height];
      THREAD_LOCAL_BUFFER.set(buffer);
    }

    int[] pixels = raster.getPixels(left, top, width, height, buffer);

    return new SourceSubset(coverage, left, top, width, pixels);
  }

}
