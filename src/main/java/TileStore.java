import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

public interface TileStore extends AutoCloseable {

  /**
   * Writes an image to the tile store. Can be called from any thread.
   */
  void write(int zoom, int x, int y, RenderedImage image);

  /**
   * Ensures that all tile writes have completed.
   */
  void flush() throws InterruptedException;

  /**
   * Gets a reader that can be used from the current thread.
   */
  BufferedImage[] read(int zoom, int startX, int startY, int tileSpan);

}
