/**
 * Defines a rectangle of tiles, using the XYZ indexing scheme.
 *
 * <p>Note that in the XYZ-tiling scheme, the origin of the tiling indexes are in the top
 * left (north west) corner of the map.</p>
 */
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

  /**
   * Returns the zero-based index of the left-most tile.
   */
  public int getLeftTile() {
    return leftTile;
  }

  /**
   * Returns the zero-based index of the right-most tile.
   */
  public int getRightTile() {
    return leftTile + tileCountX - 1;
  }


  /**
   * Returns the zero-based index of the top-most tile.
   *
   * <p>topTile &lt; bottomTile</p>
   *
   */
  public int getTopTile() {
    return topTile;
  }

  /**
   * Returns the number of tiles across.
   */
  public int getTileCountX() {
    return tileCountX;
  }

  /**
   * @return the number of tiles down.
   */
  public int getTileCountY() {
    return tileCountY;
  }

  /**
   * Returns the zero-based index of the bottom-most tile.
   *
   * <p>topTile &lt; bottomTile</p>
   *
   */
  public int getBottomTile() {
    return topTile + tileCountY - 1;
  }

  /**
   * Returns the total number of tiles in this rectangle.
   */
  public long getTileCount() {
    return tileCountX * tileCountY;
  }

  public boolean overlaps(TileRect other) {
    if(other.getRightTile() < getLeftTile()) {
      return false;
    }
    if(other.getLeftTile() > getRightTile()) {
      return false;
    }
    if(other.getBottomTile() < getTopTile()) {
      return false;
    }
    if(other.getTopTile() > getBottomTile()) {
      return false;
    }
    return true;
  }
}


