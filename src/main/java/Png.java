import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Png {

  public static final ThreadLocal<ByteArrayOutputStream> BUFFER = new ThreadLocal<>();

  public static byte[] toPngBytes(RenderedImage image) {
    ByteArrayOutputStream buffer = BUFFER.get();
    if(buffer == null) {
      buffer = new ByteArrayOutputStream(1024);
      BUFFER.set(buffer);
    } else {
      buffer.reset();
    }

    try {
      ImageIO.write(image, "png", buffer);
      return buffer.toByteArray();

    } catch (IOException e) {
      e.printStackTrace();
      return new byte[0];
    }
  }

  public static BufferedImage fromBytes(byte[] content) {
    try {
      return ImageIO.read(new ByteArrayInputStream(content));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
