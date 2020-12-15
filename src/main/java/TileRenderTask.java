import org.geotools.geometry.Envelope2D;
import org.opengis.referencing.operation.TransformException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

public class TileRenderTask implements Runnable {

  private final TileSet tileSet;
  private final CountrySet coverage;
  private final ColorGradient gradient;
  private final TileStore tileStore;
  private final int tileStartX;
  private final int tileStartY;

  public TileRenderTask(TileSet tileSet, CountrySet coverage, ColorGradient gradient,
                        TileStore tileStore, int tileStartX, int tileStartY) {
    this.tileSet = tileSet;
    this.coverage = coverage;
    this.gradient = gradient;
    this.tileStore = tileStore;
    this.tileStartX = tileStartX;
    this.tileStartY = tileStartY;
  }

  public void run() {
    try {
      renderBatch();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void renderBatch() throws IOException {
    Envelope2D batchGeographicBounds = tileSet.getGeographicBounds(tileStartX, tileStartY, Reprojection.BATCH_SIZE);

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

        for (int tileX = 0; tileX < Reprojection.BATCH_SIZE; tileX++) {
          for (int tileY = 0; tileY < Reprojection.BATCH_SIZE; tileY++) {
            tileBuffer.renderTile(reprojection, subset, gradient, tileX, tileY);
          }
        }
      }
    }


    // Now write out any non-empty tiles

    for (int tileX = 0; tileX < Reprojection.BATCH_SIZE; tileX++) {
      for (int tileY = 0; tileY < Reprojection.BATCH_SIZE; tileY++) {
        BufferedImage image = tileBuffer.image(tileX, tileY);
        if(image != null) {
          tileStore.write(tileSet.zoomLevel, tileStartX + tileX, tileStartY + tileY, image);
        }
      }
    }

    Progress.progress(Reprojection.BATCH_SIZE * Reprojection.BATCH_SIZE, 0);
  }
}
