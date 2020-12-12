import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CompositeBuffer {

  private static final ThreadLocal<CompositeBuffer> THREAD_LOCAL = new ThreadLocal<>();

  private final BufferedImage image;
  private final Graphics2D graphics;

  public static CompositeBuffer get() {
    CompositeBuffer buffer = THREAD_LOCAL.get();
    if(buffer == null) {
      buffer = new CompositeBuffer();
      THREAD_LOCAL.set(buffer);
    }
    return buffer;
  }

  private CompositeBuffer() {
    image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
    graphics = image.createGraphics();
    graphics.setBackground(ColorGradient.TRANSPARENT_COLOR);
    graphics.clearRect(0, 0, 256, 256);
  }

  public void render(File[] files) {
    graphics.clearRect(0, 0, 256, 256);

    render(0,     0, files[0]); // (0, 0)
    render(0,   128, files[1]); // (0, 1)
    render(128,   0, files[2]); // (1, 0)
    render(128, 128, files[3]); // (1, 1)
  }

  private void render(int x, int y, File file) {
    if(file != null) {
      BufferedImage image;
      try {
        image = ImageIO.read(file);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
      graphics.drawImage(image, x, y, 128, 128, null);
    }
  }

  public BufferedImage getImage() {
    return image;
  }
}
