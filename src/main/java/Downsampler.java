import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class Downsampler extends RecursiveAction {

  private final int zoomLevel;
  private final int tileStartX;
  private final int tileStartY;
  private final int tileSpan;

  public Downsampler(int zoomLevel, int tileStartX, int tileStartY, int tileSpan) {
    this.zoomLevel = zoomLevel;
    this.tileStartX = tileStartX;
    this.tileStartY = tileStartY;
    this.tileSpan = tileSpan;
  }

  @Override
  protected void compute() {
    if(tileSpan == 1) {
      try {
        downSample();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      int halfSpan = this.tileSpan / 2;
      ForkJoinTask.invokeAll(
        new Downsampler(zoomLevel, tileStartX, tileStartY, halfSpan),
        new Downsampler(zoomLevel, tileStartX + halfSpan, tileStartY, halfSpan),
        new Downsampler(zoomLevel, tileStartX, tileStartY + halfSpan, halfSpan),
        new Downsampler(zoomLevel, tileStartX + halfSpan, tileStartY + halfSpan, halfSpan));
    }
  }

  private void downSample() throws IOException {

    int sx = tileStartX * 2;
    int sy = tileStartY * 2;

    File[] files = new File[4];
    int imageCount = 0;
    int i = 0;
    for (int x = 0; x < 2; x++) {
      for (int y = 0; y < 2; y++) {
        File file = new File("build/test/" + (zoomLevel + 1) + "/" + (sx + x) + "/" + (sy + y) + ".png");
        if(file.exists()) {
          files[i] = file;
          imageCount++;
        }
        i++;
      }
    }

    if(imageCount == 0) {
      return;
    }

    File targetFile = new File("build/test/" + zoomLevel + "/" + tileStartX + "/" + tileStartY + ".png");
    targetFile.getParentFile().mkdirs();

    CompositeBuffer buffer = CompositeBuffer.get();
    buffer.render(files);
    ImageIO.write(buffer.getImage(), "png", targetFile);
  }
}
