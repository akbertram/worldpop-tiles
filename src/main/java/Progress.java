import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Progress {

  public static final AtomicLong COUNT = new AtomicLong(0);

  public static long startTime;
  public static long totalTiles;

  public static void starting(TileSet tileSet) {
    startTime = System.currentTimeMillis();
    totalTiles = (long) tileSet.tileCount * (long)tileSet.tileCount;
  }

  public static void tileCompleted() {
    long count = COUNT.incrementAndGet();
    if(count % 100L == 0) {
      double percentComplete = (double) count / (double) totalTiles * 100d;
      double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000L;
      double tilesPerSecond = (double) count / (double)elapsedSeconds;
      System.out.printf("%d tiles rendered (%.2f%%) at %.2f tiles/second%n", count, percentComplete, tilesPerSecond);
    }
  }
}
