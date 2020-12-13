import java.awt.image.BufferedImage;

public interface TileReader {

  BufferedImage read(int zoom, int x, int y);
}
