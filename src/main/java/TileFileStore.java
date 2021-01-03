import com.google.common.collect.Sets;
import com.google.common.io.Files;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TileFileStore implements TileStore {

  private final File baseDir;
  private final GcsUploader uploader;

  private ConcurrentLinkedQueue<Integer> directoryQueue = new ConcurrentLinkedQueue<>();
  private Thread directoryReaderThread;
  private boolean done;

  public TileFileStore(File baseDir, GcsUploader uploader) {
    this.baseDir = baseDir;
    this.uploader = uploader;
    this.directoryReaderThread = new Thread(this::uploadZoomLevels);
    this.directoryReaderThread.setName("WriteBuffer reader");
    this.directoryReaderThread.start();
  }

  @Override
  public WriteBuffer newWriteBuffer(int zoomLevel) {

    return new WriteBuffer() {
      private final Set<String> directoriesCreated = Sets.newConcurrentHashSet();

      @Override
      public void write(int x, int y, RenderedImage image) {
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

      @Override
      public BufferedImage read(int x, int y) {
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

      @Override
      public int getZoomLevel() {
        return zoomLevel;
      }

      @Override
      public void doneWriting() {
        directoryQueue.offer(zoomLevel);
      }

      @Override
      public void doneReading() {
      }
    };
  }


  private void uploadZoomLevels() {
    while(true) {
      Integer zoomLevel = directoryQueue.poll();
      if(zoomLevel == null) {
        if(done) {
          return;
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            return;
          }
        }
      } else {
        uploadZoomLevel(zoomLevel);
      }
    }
  }

  private void uploadZoomLevel(int zoomLevel) {
    File zoomDir = new File(baseDir, Integer.toString(zoomLevel));

    File[] dirs = zoomDir.listFiles();
    if(dirs == null) {
      return;
    }
    for (File dir : dirs) {
      if(dir.isDirectory()) {
        int x = Integer.parseInt(dir.getName());

        File[] tiles = dir.listFiles();
        if(tiles != null) {
          for (File tileFile : tiles) {
            String tileName = tileFile.getName();
            String tileNameWithoutExtension = tileName.substring(0, tileName.length() - ".png".length());
            int y = Integer.parseInt(tileNameWithoutExtension);
            try {
              Tile tile = new Tile(zoomLevel, x, y, Files.toByteArray(tileFile));
              uploader.upload(tile);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
  }


  private File file(int zoom, int startX, int startY) {
    return new File(baseDir, zoom + "/" + startX + "/" + startY + ".png");
  }

  private void mkdirs(String tileDir) {
    File file = new File(tileDir);
    file.mkdirs();
  }

  @Override
  public void close() throws InterruptedException {
    done = true;
    directoryReaderThread.join();
    uploader.close();
  }

}
