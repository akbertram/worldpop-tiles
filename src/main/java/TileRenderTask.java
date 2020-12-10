import org.geotools.coverage.grid.GridCoverage2D;

import javax.imageio.ImageIO;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class TileRenderTask extends RecursiveAction {
  private final TileSet tileSet;
  private final GridCoverage2D coverage;
  private final ColorGradient gradient;
  private final int tileStartX;
  private final int tileStartY;
  private final int tileSpan;

  public TileRenderTask(TileSet tileSet, GridCoverage2D coverage, ColorGradient gradient,
                        int tileStartX, int tileStartY, int tileSpan) {
    this.tileSet = tileSet;
    this.coverage = coverage;
    this.gradient = gradient;
    this.tileStartX = tileStartX;
    this.tileStartY = tileStartY;
    this.tileSpan = tileSpan;
  }

  @Override
  protected void compute() {
    if(tileSpan <= 8) {
      try {
        renderTiles();
      } catch (Throwable e) {
        e.printStackTrace();
      }
    } else {
      int halfSpan = this.tileSpan / 2;
      ForkJoinTask.invokeAll(
        new TileRenderTask(tileSet, coverage, gradient, tileStartX, tileStartY, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStartX + halfSpan, tileStartY, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStartX, tileStartY + halfSpan, halfSpan),
        new TileRenderTask(tileSet, coverage, gradient, tileStartX + halfSpan, tileStartY + halfSpan, halfSpan));
    }
  }

  private void renderTiles() {

    GridCoverage2D projected = tileSet.project(coverage, tileStartX, tileStartY, tileSpan);

    RenderedImage image = projected.getRenderableImage(0, 1).createDefaultRendering();
    Raster raster = image.getData();

    TileBuffer buffer = new TileBuffer(tileSet, gradient);

    for (int tileX = tileStartX; tileX < tileStartX + tileSpan; tileX++) {
      File tileDir = new File("build/test/" + tileSet.zoomLevel + "/" + tileX);
      tileDir.mkdirs();

      for (int tileY = tileStartY; tileY < tileStartY + tileSpan; tileY++) {

        if (buffer.render(raster, tileX - tileStartX, tileY - tileStartY)) {

          File tileFile = new File(tileDir, tileY + ".png");
          boolean written = false;
          try {
            written = ImageIO.write(buffer.getImage(), "png", tileFile);
          } catch (IOException e) {
            System.err.println("Exception writing tile " + tileX + "x" + tileY);
            e.printStackTrace();
          }
          if (!written) {
            throw new RuntimeException("not written");
          }
        }

        Progress.tileCompleted();
      }
    }
  }
}
