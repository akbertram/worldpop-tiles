import java.awt.*;

public class ColorGradient {

  public static final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);

  public static final Color[] COLORS = new Color[] {
    new Color(255, 255, 255, 0),
    new Color(255, 255, 240),
    new Color(255, 255, 204),
    new Color(255, 237, 160),
    new Color(254, 217, 118),
    new Color(254, 178, 76),
    new Color(253, 141, 60),
    new Color(252, 78, 42),
    new Color(227, 26, 28),
    new Color(177, 0, 38)
  };

  public static int populationToColorIndex(short pop) {
    if(pop < 0) {
      return 0;
    }
    if(pop < 1) {
      return 1;
    }
    if(pop < 4) {
      return 2;
    }
    if(pop < 8) {
      return 3;
    }
    if(pop < 12) {
      return 4;
    }
    if(pop < 20) {
      return 5;
    }
    if(pop < 50) {
      return 6;
    }
    if(pop < 100) {
      return 7;
    }
    if(pop < 3000) {
      return 8;
    }
    return 9;
  }

}
