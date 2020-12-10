import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Arrays;

public class TileBuffer  {

  private final BufferedImage image;
  private int[] countArray;
  private int[] transparentArray;
  private final int tileSizePixels;
  private final ColorGradient gradient;

  public TileBuffer(TileSet tileSet, ColorGradient gradient) {
    this.tileSizePixels = tileSet.getTileSizePixels();
    this.gradient = gradient;
    this.image = new BufferedImage(tileSizePixels, tileSizePixels, BufferedImage.TYPE_INT_ARGB);
    this.countArray = new int[tileSizePixels * tileSizePixels];
    int rgb = Color.RED.getRGB();
    for (int i = 0; i < tileSizePixels; i++) {
      image.setRGB(i, 100, rgb);
    }
    transparentArray = new int[tileSizePixels * tileSizePixels];
    Arrays.fill(transparentArray, ColorGradient.TRANSPARENT);
  }

  public BufferedImage getImage() {
    return image;
  }

  public boolean render(Raster raster, int tileX, int tileY) {
    int[] buffer = this.countArray;

    int tileStartX = (int)raster.getBounds().getX() + tileX * tileSizePixels;
    int tileEndY = raster.getHeight() - (tileY * tileSizePixels);
    int tileStartY = (int)raster.getBounds().getY() + tileEndY - tileSizePixels;

    // Load the population counts into the buffer
    raster.getPixels(
      tileStartX,
      tileStartY,
      tileSizePixels,
      tileSizePixels,
      buffer);

    // Recolor
    boolean empty = true;
    int numPixels = buffer.length;
    for (int i = 0; i < numPixels; i++) {
      int pop = buffer[i];
      if(pop < 0) {
        buffer[i] = ColorGradient.TRANSPARENT;
      } else {
        buffer[i] = gradient.color(pop);
        empty = false;
      }
    }

    if(empty) {
      return false;
    }

    image.setRGB(0, 0,
      tileSizePixels,
      tileSizePixels,
      buffer,
      0, /* offset in array */
      tileSizePixels /* scan size */);

    return true;
  }

}
