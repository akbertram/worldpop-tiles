
class GeoRect {
  private final double north;
  private final double south;
  private final double east;
  private final double west;

  public GeoRect(double north, double south, double east, double west) {
    this.north = north;
    this.south = south;
    this.east = east;
    this.west = west;
  }

  public double getNorth() { return north; }
  public double getSouth() { return south; }
  public double getEast() { return east; }
  public double getWest() { return west; }
  public double getLeft() { return west; }
  public double getRight() { return east; }
  public double getTop() { return north; }
  public double getBottom() { return south; }

  public double getCenterX() {
    return (east + west) / 2d;
  }

  public double getCenterY() {
    return (north + south) / 2d;
  }
}
