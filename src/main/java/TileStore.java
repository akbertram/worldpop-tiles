import com.google.common.collect.Sets;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class TileStore {

  private final File baseDir;
  private final Set<String> directoriesCreated = Sets.newConcurrentHashSet();

  public TileStore(File baseDir) {
    this.baseDir = baseDir;
  }

  public void write(int zoomLevel, int x, int y, RenderedImage image) {
    String tileDir = baseDir.getPath() + "/" + zoomLevel + "/" + x;
    if(!directoriesCreated.contains(tileDir)) {
      mkdirs(tileDir);
      directoriesCreated.add(tileDir);
    }
    File imageFile = new File(tileDir, y + ".png");
    try {
      ImageIO.write(image, "png", imageFile);
    } catch (IOException e) {
      System.err.println("Failed to write " + imageFile.getAbsolutePath());
      e.printStackTrace();
    }
  }

  public BufferedImage read(int zoomLevel, int x, int y) {
    File inFile = file(zoomLevel, x, y);
    if(!inFile.exists()) {
      return null;
    }
    try {
      return ImageIO.read(inFile);
    } catch (IOException e) {
      System.err.println("Error reading " + inFile);
      e.printStackTrace();
      return null;
    }
  }

  private File file(int zoom, int startX, int startY) {
    return new File(baseDir, zoom + "/" + startX + "/" + startY + ".png");
  }

  private void mkdirs(String tileDir) {
    File file = new File(tileDir);
    file.mkdirs();
  }

  public File getZoomDirectory(int zoomLevel) {
    return new File(baseDir, Integer.toString(zoomLevel));
  }
}
