import org.geotools.geometry.Envelope2D;
import org.opengis.referencing.operation.TransformException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class TileRenderTask extends RecursiveAction {

  private final TileSet tileSet;
  private final CountrySet coverage;
  private final ColorGradient gradient;
  private final TileStore tileStore;
  private final int tileStartX;
  private final int tileStartY;
  private final int tileSpan;

  public TileRenderTask(TileSet tileSet, CountrySet coverage, ColorGradient gradient,
                        TileStore tileStore, int tileStartX, int tileStartY, int tileSpan) {
    this.tileSet = tileSet;
    this.coverage = coverage;
    this.gradient = gradient;
    this.tileStore = tileStore;
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
        new TileRenderTask(tileSet, coverage, gradient, tileStore, tileStartX, tileStartY, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStore, tileStartX + halfSpan, tileStartY, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStore, tileStartX, tileStartY + halfSpan, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStore, tileStartX + halfSpan, tileStartY + halfSpan, halfSpan));
    }
  }

  private void renderBatch() throws TransformException, IOException {

    Envelope2D batchGeographicBounds = tileSet.getGeographicBounds(tileStartX, tileStartY, tileSpan);

    Reprojection reprojection = Reprojection.get();
    TileBuffer tileBuffer = TileBuffer.get();
    tileBuffer.clear();

    // Find all the countries that overlap with this batch and
    // render them to the tile buffer

    List<Country> sources = coverage.findOverlappingCountries(batchGeographicBounds);
    for (Country source : sources) {
      CountrySubset subset = source.extractImage(batchGeographicBounds);
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
      for (int tileY = 0; tileY < tileSpan; tileY++) {
        BufferedImage image = tileBuffer.image(tileX, tileY);
        if(image != null) {
          tileStore.write(tileSet.zoomLevel, tileStartX + tileX, tileStartY + tileY, image);
        }
      }
    }

    Progress.progress(tileSpan * tileSpan, 0);
  }
}
