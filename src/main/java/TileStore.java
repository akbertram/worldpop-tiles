import java.awt.image.RenderedImage;

public interface TileStore extends AutoCloseable {

  /**
   * Writes an image to the tile store. Can be called from any thread.
   */
  void write(int zoom, int x, int y, RenderedImage image);

  /**
   * Gets a reader that can be used from the current thread.
   */
  TileReader getReader();
}
