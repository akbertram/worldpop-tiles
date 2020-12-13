import java.awt.*;
import java.awt.image.BufferedImage;

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

  public BufferedImage render(BufferedImage[] images) {
    graphics.clearRect(0, 0, 256, 256);

    render(0,     0, images[0]); // (0, 0)
    render(0,   128, images[1]); // (0, 1)
    render(128,   0, images[2]); // (1, 0)
    render(128, 128, images[3]); // (1, 1)

    return image;
  }

  private void render(int x, int y, BufferedImage image) {
    if(image != null) {
      graphics.drawImage(image, x, y, 128, 128, null);
    }
  }
}
