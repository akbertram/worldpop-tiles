import org.geotools.geometry.Envelope2D;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TileRenderQueue {
  private final TileSet tileSet;
  private final CountrySet countrySet;
  private final ColorGradient gradient;
  private final TileStore store;
  private final ExecutorService executorService;

  private int batchCount = 0;

  public TileRenderQueue(TileSet tileSet, CountrySet countrySet, ColorGradient gradient, TileStore store) {
    this.tileSet = tileSet;
    this.countrySet = countrySet;
    this.gradient = gradient;
    this.store = store;


    int nThreads = Runtime.getRuntime().availableProcessors() - 1;
    executorService = Executors.newFixedThreadPool(nThreads);
  }

  public void render() throws InterruptedException {
    enqueue(0, 0, (int)tileSet.tileCount);

    System.out.println(batchCount + " batches submitted.");

    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.DAYS);
  }

  private void enqueue(int tileX, int tileY, int tileSpan) {

    Envelope2D bounds = tileSet.getGeographicBounds(tileX, tileY, tileSpan);
    if(countrySet.isEmpty(bounds)) {
      Progress.progress(0, tileSpan * tileSpan);

    } else if(tileSpan > Reprojection.BATCH_SIZE) {
      int halfSpan = tileSpan / 2;
      enqueue(tileX, tileY, halfSpan);
      enqueue(tileX, tileY + halfSpan, halfSpan);
      enqueue(tileX + halfSpan, tileY, halfSpan);
      enqueue(tileX + halfSpan, tileY + halfSpan, halfSpan);
    } else {
      for (int x = 0; x < tileSpan; x += Reprojection.BATCH_SIZE) {
        for (int y = 0; y < tileSpan; y += Reprojection.BATCH_SIZE) {
          executorService.submit(new TileRenderTask(tileSet, countrySet, gradient, store, tileX + x, tileY + y));
          batchCount ++;
        }
      }
    }
  }
}
