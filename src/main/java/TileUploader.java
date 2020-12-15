import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;

public class TileUploader {

  public static void main(String[] args) throws SQLException, InterruptedException, FileNotFoundException {
    try(GcsUploader uploader = new GcsUploader("worldpop-test-1", "bgd-test")) {
      uploader.start();

      MbTiles.forEachTile(new File("worldpop.mbtiles"), tile -> {
        uploader.upload(tile);
      });
    }
  }
}
