import org.opengis.referencing.operation.TransformException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class TileRenderTask extends RecursiveAction {

  private final TileSet tileSet;
  private final SourceImage coverage;
  private final ColorGradient gradient;
  private final int tileStartX;
  private final int tileStartY;
  private final int tileSpan;

  public TileRenderTask(TileSet tileSet, SourceImage coverage, ColorGradient gradient,
                        int tileStartX, int tileStartY, int tileSpan) {
    this.tileSet = tileSet;
    this.coverage = coverage;
    this.gradient = gradient;
    this.tileStartX = tileStartX;
    this.tileStartY = tileStartY;
    this.tileSpan = tileSpan;
  }

  @Override
  protected void compute() {
    if(tileSpan <= Reprojection.BATCH_SIZE) {
      try {
        renderBatch();
      } catch (Throwable e) {
        e.printStackTrace();
      }
    } else {
      int halfSpan = this.tileSpan / 2;
      ForkJoinTask.invokeAll(
        new TileRenderTask(tileSet, coverage, gradient, tileStartX, tileStartY, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStartX + halfSpan, tileStartY, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStartX, tileStartY + halfSpan, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStartX + halfSpan, tileStartY + halfSpan, halfSpan));
    }
  }

  private void renderBatch() throws TransformException, IOException {

    Reprojection reprojection = Reprojection.get();

    // First we need to assemble the source image in a rectangular grid
    SourceSubset source = coverage.extractImage(tileSet.getGeographicBounds(tileStartX, tileStartY, tileSpan));
    if(source == null) {
      return;
    }

    reprojection.precomputeGridIndexes(tileSet, tileStartX, tileStartY, source);

    TileBuffer tileBuffer = TileBuffer.get();

    for (int tileX = 0; tileX < tileSpan; tileX++) {
      File tileDir = new File("build/test/" + tileSet.zoomLevel + "/" + (tileStartX + tileX));
      tileDir.mkdirs();

      for (int tileY = 0; tileY < tileSpan; tileY++) {

        BufferedImage image = tileBuffer.renderTile(reprojection, source, gradient, tileX, tileY);

        if(image != null) {
          ImageIO.write(image, "png", new File(tileDir, (tileStartY + tileY) + ".png"));
        }

        Progress.tileCompleted();
      }
    }
  }
}
