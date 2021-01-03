public class TileRect {
  int leftTile;
  int topTile;
  int tileCountX;
  int tileCountY;

  public TileRect(int leftTile, int topTile, int tileCountX, int tileCountY) {
    this.leftTile = leftTile;
    this.topTile = topTile;
    this.tileCountX = tileCountX;
    this.tileCountY = tileCountY;
  }

  public int getLeftTile() {
    return leftTile;
  }

  public int getRightTile() {
    return leftTile + tileCountX - 1;
  }

  public int getTopTile() {
    return topTile;
  }

  public int getTileCountX() {
    return tileCountX;
  }

  public int getTileCountY() {
    return tileCountY;
  }

  public int getBottomTile() {
    return topTile + tileCountY - 1;
  }

  public long getTileCount() {
    return tileCountX * tileCountY;
  }
}
