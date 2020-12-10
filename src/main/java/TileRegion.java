import java.awt.*;

public class TileRegion  {
  public final int imageStartX;
  public final int imageStartY;
  public final int imageOffsetX;
  public final int imageOffsetY;
  public final int tileWidth;
  public final int tileHeight;

  public TileRegion(int imageStartX, int imageStartY, int imageOffsetX, int imageOffsetY, int tileWidth, int tileHeight) {
    this.imageStartX = imageStartX;
    this.imageStartY = imageStartY;
    this.imageOffsetX = imageOffsetX;
    this.imageOffsetY = imageOffsetY;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
  }

  public boolean isEmpty() {
    return tileWidth <= 0 || tileHeight <= 0;
  }

  public final Rectangle relativeToSourceImage() {
    return new Rectangle(imageStartX, imageStartY, tileWidth, tileHeight);
  }

}
