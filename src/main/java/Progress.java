import java.util.concurrent.atomic.AtomicLong;

public class Progress {

  private static final int REPORT_INTERVAL_MILLIS;

  static {
    if(System.getenv("BUILD_NUMBER") != null) {
      REPORT_INTERVAL_MILLIS = 60_000;
    } else {
      REPORT_INTERVAL_MILLIS = 5_000;
    }
  }

  public static final AtomicLong COUNT = new AtomicLong(0);
  public static final AtomicLong RENDERED_COUNT = new AtomicLong(0);

  public static long startTime;
  public static long totalTiles;
  public static boolean done;

  public static void starting(TileSet tileSet) {
    startTime = System.currentTimeMillis();
    totalTiles = (long) tileSet.tileCount * (long)tileSet.tileCount;
    Thread reporter = new Thread(() -> {
      while(true) {
        try {
          Thread.sleep(REPORT_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
          return;
        }
        if(done) {
          return;
        }
        reportProgress();
      }
    });
    reporter.setName("Progress reporter");
    reporter.start();
  }

  public static void progress(int rendered, int skipped) {
    RENDERED_COUNT.addAndGet(rendered);
    COUNT.addAndGet(rendered + skipped);
  }

  private static void reportProgress() {
    long count = COUNT.get();
    double percentComplete = (double) count / (double) totalTiles * 100d;
    double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000L;
    double tilesPerSecond = (double) RENDERED_COUNT.get() / elapsedSeconds;

    long remainingTiles = totalTiles - count;
    double remainingSeconds = remainingTiles / tilesPerSecond;
    double remainingMinutes = remainingSeconds / 60d;

    String remaining;
    if(remainingMinutes < 90) {
      remaining = Math.round(remainingMinutes) + " minutes remaining";
    } else {
      remaining = Math.round(remainingMinutes / 60d) + " hours remaining";
    }

    System.out.printf("%d tiles rendered (%.2f%%) at %.2f tiles/second, %s%n",
      count, percentComplete, tilesPerSecond, remaining);
  }

  public static void tilesDone() {
    done = true;
    reportProgress();
  }
}
