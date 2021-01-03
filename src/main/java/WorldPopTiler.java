import org.gdal.gdal.gdal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class WorldPopTiler {

  public static void main(String[] args) throws Exception {
    int baseZoomLevel = 11;

    gdal.AllRegister();

    Tiling tiling = new Tiling(11);

    File sourceDir = new File("tif_country");
    TileFileStore store = new TileFileStore(new File("tiles"));

    List<TileBatch> batches = new ArrayList<>();

//    for (File file : sourceDir.listFiles()) {
//      if (file.getName().endsWith(".tif")) {
//        Country country = new Country(tiling, file);
//        for (TileRect rect : country.divideIntoBatches()) {
//          TileBatch batch = new TileBatch(country, store, rect);
//          batch.render();
//        }
//      }
//    }

    downsample(store, 11);

  }

  private static void downsample(TileStore store, int baseZoomLevel) throws Exception {

    for (int zoom = baseZoomLevel - 1; zoom >= 0; zoom--) {
      Tiling tiling = new Tiling(zoom);
      System.out.println("Downsampling zoom level " + zoom + "...");
      ForkJoinPool.commonPool().invoke(new DownsampleTask(tiling, store, zoom, 0, 0, (int)Math.pow(2, zoom)));

      System.out.println("Flushing tile writes...");
      store.flush();
    }
  }

}
