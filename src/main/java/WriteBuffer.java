import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

public interface WriteBuffer {

  void write(int x, int y, RenderedImage image);

  BufferedImage read(int x, int y);

  int getZoomLevel();

  void doneWriting();

  void doneReading();
}
