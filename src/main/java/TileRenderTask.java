import org.geotools.geometry.Envelope2D;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class TileRenderTask extends RecursiveAction {

  private final TileSet tileSet;
  private final Countries coverage;
  private final ColorGradient gradient;
  private final int tileStartX;
  private final int tileStartY;
  private final int tileSpan;

  public TileRenderTask(TileSet tileSet, Countries coverage, ColorGradient gradient,
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

      Envelope2D bounds = tileSet.getGeographicBounds(tileStartX, tileStartY, tileSpan);
      if(coverage.isEmpty(bounds)) {
        Progress.progress(0, tileSpan * tileSpan);
        return;
      }

      int halfSpan = this.tileSpan / 2;
      ForkJoinTask.invokeAll(
        new TileRenderTask(tileSet, coverage, gradient, tileStartX, tileStartY, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStartX + halfSpan, tileStartY, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStartX, tileStartY + halfSpan, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStartX + halfSpan, tileStartY + halfSpan, halfSpan));
    }
  }

  private void renderBatch() throws TransformException, IOException {

    Envelope2D batchGeographicBounds = tileSet.getGeographicBounds(tileStartX, tileStartY, tileSpan);

    Reprojection reprojection = Reprojection.get();
    TileBuffer tileBuffer = TileBuffer.get();
    tileBuffer.clear();

    // Find all the countries that overlap with this batch and
    // render them to the tile buffer

    List<SourceImage> sources = coverage.findOverlappingCountries(batchGeographicBounds);
    for (SourceImage source : sources) {
      SourceSubset subset = source.extractImage(batchGeographicBounds);
      if(subset != null) {
        reprojection.precomputeGridIndexes(tileSet, tileStartX, tileStartY, subset);

        for (int tileX = 0; tileX < tileSpan; tileX++) {
          for (int tileY = 0; tileY < tileSpan; tileY++) {
            tileBuffer.renderTile(reprojection, subset, gradient, tileX, tileY);
          }
        }
      }
    }

    // Now write out any non-empty tiles

    for (int tileX = 0; tileX < tileSpan; tileX++) {
      File tileDir = new File("build/test/" + tileSet.zoomLevel + "/" + (tileStartX + tileX));
      tileDir.mkdirs();
      for (int tileY = 0; tileY < tileSpan; tileY++) {
        BufferedImage image = tileBuffer.image(tileX, tileY);
        if(image != null) {
          ImageIO.write(image, "png", new File(tileDir, (tileStartY + tileY) + ".png"));
        }
      }
    }

    Progress.progress(tileSpan * tileSpan, 0);
  }
}
