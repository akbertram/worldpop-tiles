import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TileFileStore implements TileStore {

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
  public void flush() throws InterruptedException {

  }

  @Override
  public BufferedImage[] read(int zoom, int startX, int startY, int tileSpan) {

    BufferedImage[] images = new BufferedImage[tileSpan * tileSpan];

    int index = 0;
    for (int x = 0; x < tileSpan; x++) {
      for (int y = 0; y < tileSpan; y++) {
        images[index++] = tryRead(file(zoom, startX + x, startY + y));
      }
    }
    return images;
  }

  private File file(int zoom, int startX, int startY) {
    return new File(baseDir, zoom + "/" + startX + "/" + startY + ".png");
  }

  private BufferedImage tryRead(File file) {
    if(file.exists()) {
      try {
        return ImageIO.read(file);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  private void mkdirs(String tileDir) {
    File file = new File(tileDir);
    file.mkdirs();
  }

  @Override
  public void close() {
  }

}
