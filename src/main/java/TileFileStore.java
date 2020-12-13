import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TileFileStore implements TileStore, TileReader {

  private final File baseDir;
  private final Set<String> directoriesCreated = new HashSet<>();

  public TileFileStore(File baseDir) {
    this.baseDir = baseDir;
  }

  @Override
  public void write(int zoom, int x, int y, RenderedImage image) {
    String tileDir = baseDir.getPath() + "/" + zoom + "/" + x;
    if(directoriesCreated.add(tileDir)) {
      mkdirs(tileDir);
    }
    File imageFile = new File(tileDir, y + ".png");
    try {
      ImageIO.write(image, "png", imageFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public TileReader getReader() {
    return this;
  }

  private void mkdirs(String tileDir) {
    File file = new File(tileDir);
    file.mkdirs();
  }

  @Override
  public void close() {
  }

  @Override
  public BufferedImage read(int zoom, int x, int y) {
    File file = new File(baseDir, zoom + "/" + x + "/" + y + ".png");
    if(file.exists()) {
      try {
        return ImageIO.read(file);
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }
}
