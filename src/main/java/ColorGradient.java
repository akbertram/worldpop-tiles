import java.awt.*;
import java.io.PrintStream;

/**
 * Maps population density to a color gradient.
 *
 * <p>This gradient is based on the work of Duncan A. Smith (2017) in
 * Visualising world population density as an interactive multi-scale
 * map using the global human settlement population layer,
 * Journal of Maps, 13:1, 117-123, DOI: 10.1080/17445647.2017.1400476
 *
 * https://www.tandfonline.com/doi/full/10.1080/17445647.2017.1400476</p>
 */
public class ColorGradient {

  public static final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);

  public static final Color[] COLORS = new Color[] {
    new Color(255, 255, 255, 0),  // water
    new Color(255, 255, 255),     // 0-20
    new Color(242, 251, 242),     // 20-100
    new Color(207, 242, 229),     // 100-400
    new Color(137, 228, 230),     // 400-1k
    new Color(8,   200, 217),     // 1k-2k
    new Color(0,   133, 203),     // 2k-3.5k
    new Color(0,   107, 183),     // 3.5k-5.5k
    new Color(0,    75, 163),     // 5.5k-7.5k
    new Color(0,    44, 163),     // 7.5k-9.5k
    new Color(94,   25, 143),     // 9.5k-12k
    new Color(132,   0, 132),     // 12k-15k
    new Color(180,   0,  73),     // 15k-18k
    new Color(238,   0,  60),     // 18k-22k
    new Color(255,   0,   0),     // 22k-28k
    new Color(255,  98,   0),     // 28k-40k
    new Color(255, 159,   0),     // 40k-80k
    new Color(255, 159,   0)      // 80k+
  };

  public static int populationToColorIndex(int popDensity) {

    if(popDensity < 0) {
      return 0;
    }
    if(popDensity < 20) {
      return 1;
    }
    if(popDensity < 100) {
      return 2;
    }
    if(popDensity < 400) {
      return 3;
    }
    if(popDensity < 1000) {
      return 4;
    }
    if(popDensity < 2000) {
      return 5;
    }
    if(popDensity < 3500) {
      return 6;
    }
    if(popDensity < 5500) {
      return 7;
    }
    if(popDensity < 7500) {
      return 8;
    }
    if(popDensity < 9500) {
      return 9;
    }
    if(popDensity < 12_000) {
      return 10;
    }
    if(popDensity < 15_000) {
      return 11;
    }
    if(popDensity < 18_000) {
      return 12;
    }
    if(popDensity < 22_000) {
      return 13;
    }
    if(popDensity < 28_000) {
      return 14;
    }
    if(popDensity < 40_000) {
      return 15;
    }
    if(popDensity < 80_000) {
      return 16;
    }
    return 17;
  }

  public static void main(String[] args) {
    int height = COLORS.length * 10;
    PrintStream svg = System.out;

    for (int i = 1; i < COLORS.length; i++) {
      svg.println(String.format("<rect x=\"0\" y=\"%d\" width=\"10\" height=\"10\" fill=\"#%02X%02X%02X\"/>",
        height - (i * 10),
        COLORS[i].getRed(),
        COLORS[i].getGreen(),
        COLORS[i].getBlue()));
    }
  }
}
