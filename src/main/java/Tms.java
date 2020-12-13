public class Tms {

  private static int tileCount[] = new int[16];

  static {
    tileCount[0] = 1;
    for (int i = 1; i < 16; i++) {
      tileCount[i] = tileCount[i - 1] * 2;
    }
  }

  public static int toTmsY(int zoomLevel, int y) {
    return tileCount[zoomLevel] - y - 1;
  }
}
