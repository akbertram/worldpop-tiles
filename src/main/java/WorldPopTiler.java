import java.util.concurrent.ForkJoinPool;

public class WorldPopTiler {

  public static void main(String[] args) throws Exception {

    CountrySet countrySet = new CountrySet();
    int baseZoomLevel = 11;

    try(TileStore store = new GcsStore()) {
      renderBaseTiles(countrySet, store, baseZoomLevel);
      downsample(countrySet, store, baseZoomLevel);
    }
  }

  private static void renderBaseTiles(CountrySet countrySet, TileStore store, int zoomLevel) throws Exception {


    TileSet tileSet = new TileSet(zoomLevel);

    Progress.starting(tileSet);

    System.out.println("Zoom level " + tileSet.zoomLevel);

    ColorGradient gradient = new ColorGradient(100);

    TileRenderQueue queue = new TileRenderQueue(tileSet, countrySet, gradient, store);
    queue.render();

    Progress.tilesDone();

    System.out.println("Flushing tile writes...");
    store.flush();
  }

  private static void downsample(CountrySet countrySet, TileStore store, int baseZoomLevel) throws Exception {

    for (int zoom = baseZoomLevel - 1; zoom >= 0; zoom--) {
      TileSet tileSet = new TileSet(zoom);
      System.out.println("Downsampling zoom level " + zoom + "...");
      ForkJoinPool.commonPool().invoke(new DownsampleTask(countrySet, tileSet, store, zoom, 0, 0, (int)Math.pow(2, zoom)));

      System.out.println("Flushing tile writes...");
      store.flush();
    }
  }

}
