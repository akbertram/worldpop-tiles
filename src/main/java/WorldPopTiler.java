import java.util.concurrent.ForkJoinPool;

public class WorldPopTiler {

  public static void main(String[] args) throws Exception {

    CountrySet countrySet = new CountrySet();
    int baseZoomLevel = 11;

    try(TileStore store = new MbTiles(countrySet, baseZoomLevel)) {
      renderBaseTiles(countrySet, store, baseZoomLevel);
      downsample(store, baseZoomLevel);
    }
  }

  private static void renderBaseTiles(CountrySet countrySet, TileStore store, int zoomLevel) throws Exception {


    TileSet tileSet = new TileSet(zoomLevel);

    Progress.starting(tileSet);

    System.out.println("Zoom level " + tileSet.zoomLevel);

    ColorGradient gradient = new ColorGradient(100);

    ForkJoinPool.commonPool().invoke(new TileRenderTask(tileSet, countrySet, gradient, store, 0, 0,
      (int) tileSet.tileCount));

    Progress.tilesDone();
  }

  private static void downsample(TileStore store, int baseZoomLevel) throws Exception {

    for (int zoom = baseZoomLevel - 1; zoom >= 0; zoom--) {
      System.out.println("Downsampling zoom level " + zoom + "...");
      ForkJoinPool.commonPool().invoke(new DownsampleTask(store, zoom, 0, 0, (int)Math.pow(2, zoom)));
    }
  }

}
