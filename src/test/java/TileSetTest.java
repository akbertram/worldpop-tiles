import org.geotools.geometry.DirectPosition2D;
import org.junit.jupiter.api.Test;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;

import java.awt.geom.NoninvertibleTransformException;

import static org.hamcrest.MatcherAssert.assertThat;

class TileSetTest {

  @Test
  public void test() throws FactoryException, NoninvertibleTransformException {

    TileSet tileSet = new TileSet(0);
    DirectPosition2D pixels = new DirectPosition2D(0, 0);
    DirectPosition2D meters = new DirectPosition2D();

    tileSet.pixelsToMeters2D.transform(pixels, (DirectPosition)meters);

    System.out.println(TileSet.PROJ_MIN);
    System.out.println("origin => " + meters);
//    System.out.println(meters.getX() / TileSet.PROJ_MIN);

//    assertThat(meters.getX(), Matchers.closeTo(TileSet.PROJ_MIN, 1));
//    assertThat(meters.getY(), Matchers.closeTo(TileSet.PROJ_MIN, 1));

    pixels = new DirectPosition2D(255, 255);

    tileSet.pixelsToMeters2D.transform(pixels, (DirectPosition)meters);

    System.out.println("max => " + meters);

//    assertThat(meters.getX(), Matchers.closeTo(TileSet.PROJ_MAX, 1));
//    assertThat(meters.getY(), Matchers.closeTo(TileSet.PROJ_MAX, 1));
  }

  @Test
  public void testLatLng() throws FactoryException {

    // Alexandria http://a.tile.stamen.com/toner/9/298/209.png

    TileSet tileSet = new TileSet(9);

    double x = tileSet.meterTileLeft(298);
    double y = tileSet.meterTileTop(209);

    double longitude = TileSet.x2lon(x);
    double latitude = TileSet.y2lat(y);

    System.out.println(longitude + ", " + latitude);


  }
}