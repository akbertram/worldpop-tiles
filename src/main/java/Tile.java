public class Tile {

  public Tile() {
  }

  public Tile(int zoom, int x, int y, byte[] image) {
    this.zoom = zoom;
    this.x = x;
    this.y = y;
    this.image = image;
  }

  public int zoom;
  public int x;
  public int y;
  public byte[] image;
}
