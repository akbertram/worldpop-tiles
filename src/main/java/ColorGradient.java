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

  public static int populationToColorIndex(int popDensity) {

    // This scale is based on natural logarithmic scale, where
    // each breakpoint k is ~ equal to e^((k-1)/2)*100

    if(popDensity < 0) {
      return 0;
    }
    if(popDensity < 100) {
      return 1;
    }
    if(popDensity < 165) {
      return 2;
    }
    if(popDensity < 270) {
      return 3;
    }
    if(popDensity < 450) {
      return 4;
    }
    if(popDensity < 740) {
      return 5;
    }
    if(popDensity < 1200) {
      return 6;
    }
    if(popDensity < 2000) {
      return 7;
    }
    if(popDensity < 3300) {
      return 8;
    }
    return 9;
  }

  public static void main(String[] args) {
    for (int i = 1; i < COLORS.length; i++) {
      System.out.println(String.format("<rect x=\"0\" y=\"%d\" width=\"10\" height=\"10\" fill=\"#%02X%02X%02X\"/>",
        90 - (i * 10),
        COLORS[i].getRed(),
        COLORS[i].getGreen(),
        COLORS[i].getBlue()));
    }
  }
}
