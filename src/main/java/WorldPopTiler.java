import com.google.common.base.Strings;
import org.gdal.gdal.gdal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class WorldPopTiler {

  public static void main(String[] args) throws Exception {

    gdal.AllRegister();

    String sourceDirName = System.getenv("SOURCE_DIR");
    if(Strings.isNullOrEmpty(sourceDirName)) {
      System.err.println("The SOURCE_DIR environment variable is not set.");
      System.exit(-1);
      return;
    }

    File sourceDir = new File(sourceDirName);
    if(!sourceDir.exists()) {
      System.err.println("The SOURCE_DIR (" + sourceDir.getAbsolutePath() + ") does not exist.");
      System.exit(-1);
      return;
    }

    Tiling tiling = new Tiling(11);
    GcsUploader uploader = GcsUploader.fromEnvironment();
    TileStore tileStore = new TileFileStore(new File("tiles"), uploader);

    WriteBuffer base = renderBaseLayer(sourceDir, tiling, tileStore);

    downsample(tileStore, base);

    tileStore.close();
  }

  private static WriteBuffer renderBaseLayer(File sourceDir, Tiling tiling, TileStore tileStore) throws InterruptedException {
    WriteBuffer base = tileStore.newWriteBuffer(11);

    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    for (File file : sourceDir.listFiles()) {
      if (file.getName().endsWith(".tif")) {

        System.err.println("Starting " + file.getName());

        Country country = new Country(tiling, file);

        List<TileBatch> batches = new ArrayList<>();
        for (TileRect rect : country.divideIntoBatches()) {
          TileBatch batch = new TileBatch(country, base, rect);
          batches.add(batch);
        }

        executor.invokeAll(batches);
      }
    }
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.SECONDS);

    base.doneWriting();
    return base;
  }

  private static void downsample(TileStore store, WriteBuffer base) {
    WriteBuffer up = base;
    for (int zoom = base.getZoomLevel() - 1; zoom >= 0; zoom--) {
      WriteBuffer down = store.newWriteBuffer(zoom);
      Tiling tiling = new Tiling(zoom);
      System.out.println("Downsampling zoom level " + zoom + "...");
      ForkJoinPool.commonPool().invoke(new DownsampleTask(tiling, up, down, 0, 0, (int)Math.pow(2, zoom)));

      down.doneWriting();
      up.doneReading();
      up = down;
    }
  }

}
