import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

public class TileBuffer  {

  public static final ThreadLocal<TileBuffer> THREAD_LOCAL = new ThreadLocal<>();

  public final BufferedImage image;
  public final int[][] pixelBuffers;

  private final BitSet nonEmpty = new BitSet();

  private TileBuffer() {
    this.image = new BufferedImage(TileSet.PIXELS_PER_TILE, TileSet.PIXELS_PER_TILE, BufferedImage.TYPE_INT_ARGB);
    this.pixelBuffers = new int[Reprojection.BATCH_SIZE * Reprojection.BATCH_SIZE][TileSet.PIXELS_PER_TILE * TileSet.PIXELS_PER_TILE];
    for (int i = 0; i < pixelBuffers.length; i++) {
      pixelBuffers[i] = new int[TileSet.PIXELS_PER_TILE * TileSet.PIXELS_PER_TILE];
    }
  }

  public static TileBuffer get() {
    TileBuffer buffer = THREAD_LOCAL.get();
    if(buffer == null) {
      buffer = new TileBuffer();
      THREAD_LOCAL.set(buffer);
    }
    return buffer;
  }

  public void clear() {
    nonEmpty.clear();
    for (int i = 0; i < pixelBuffers.length; i++) {
      Arrays.fill(pixelBuffers[i], ColorGradient.TRANSPARENT);
    }
  }

  public void renderTile(Reprojection reprojection, CountrySubset source, ColorGradient gradient, int tileX, int tileY) throws IOException {

    int pixelIndex = 0;
    boolean empty = true;

    int[] buffer = buffer(tileX, tileY);

    int startX = tileX * TileSet.PIXELS_PER_TILE;
    int startY = tileY * TileSet.PIXELS_PER_TILE;

    for (int y = 0; y < TileSet.PIXELS_PER_TILE; y++) {
      int gridY = reprojection.gridY[startY + y];

      for (int x = 0; x < TileSet.PIXELS_PER_TILE; x++) {
        int gridX = reprojection.gridX[startX + x];
        int pop = source.get(gridX, gridY);

        if (pop >= 0) {
          buffer[pixelIndex] = gradient.color(pop);
          empty = false;
        }
        pixelIndex++;
      }
    }

    if(!empty) {
      nonEmpty.set(tileIndex(tileX, tileY));
    }
  }

  private int[] buffer(int tileX, int tileY) {
    return pixelBuffers[tileIndex(tileX, tileY)];
  }

  private int tileIndex(int tileX, int tileY) {
    return (tileY * Reprojection.BATCH_SIZE) + tileX;
  }

  public BufferedImage image(int tileX, int tileY) {

    int tileIndex = tileIndex(tileX, tileY);

    if(nonEmpty.get(tileIndex)) {

      image.setRGB(0, 0,
        TileSet.PIXELS_PER_TILE,
        TileSet.PIXELS_PER_TILE,
        pixelBuffers[tileIndex],
        0, /* offset in array */
        TileSet.PIXELS_PER_TILE /* scan size */);
      return image;
    } else {
      return null;
    }
  }

}
