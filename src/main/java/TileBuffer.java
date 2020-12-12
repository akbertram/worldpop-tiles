import java.awt.image.BufferedImage;
import java.io.IOException;

public class TileBuffer  {

  public static final ThreadLocal<TileBuffer> THREAD_LOCAL = new ThreadLocal<>();

  public final BufferedImage image;
  public final int[] pixels;

  private final int[] gridX;

  private TileBuffer() {
    this.image = new BufferedImage(TileSet.PIXELS_PER_TILE, TileSet.PIXELS_PER_TILE, BufferedImage.TYPE_INT_ARGB);
    this.pixels = new int[TileSet.PIXELS_PER_TILE * TileSet.PIXELS_PER_TILE];
    this.gridX = new int[TileSet.PIXELS_PER_TILE];
  }

  public static TileBuffer get() {
    TileBuffer buffer = THREAD_LOCAL.get();
    if(buffer == null) {
      buffer = new TileBuffer();
      THREAD_LOCAL.set(buffer);
    }
    return buffer;
  }

  public BufferedImage renderTile(Reprojection reprojection, SourceSubset source, ColorGradient gradient, int tileX, int tileY) throws IOException {


    int pixelIndex = 0;
    boolean empty = true;

    int startX = tileX * TileSet.PIXELS_PER_TILE;
    int startY = tileY * TileSet.PIXELS_PER_TILE;

    for (int y = 0; y < TileSet.PIXELS_PER_TILE; y++) {
      int gridY = reprojection.gridY[startY + y];

      for (int x = 0; x < TileSet.PIXELS_PER_TILE; x++) {
        int gridX = reprojection.gridX[startX + x];
        int pop = source.get(gridX, gridY);

        if (pop < 0) {
          pixels[pixelIndex] = ColorGradient.TRANSPARENT;
        } else {
          pixels[pixelIndex] = gradient.color(pop);
          empty = false;
        }
        pixelIndex++;
      }
    }

    if(empty) {
      return null;

    } else {
      image.setRGB(0, 0,
        TileSet.PIXELS_PER_TILE,
        TileSet.PIXELS_PER_TILE,
        pixels,
        0, /* offset in array */
        TileSet.PIXELS_PER_TILE /* scan size */);

      return image;
    }
  }
}
