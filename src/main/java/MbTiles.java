import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MbTiles implements TileStore {

  private final Connection connection;
  private final Thread writerThread;
  private final ThreadLocal<ByteArrayOutputStream> threadLocalImageBuffer = new ThreadLocal<>();
  private final ThreadLocal<ThreadLocalReader> threadLocalReader = new ThreadLocal<>();

  private boolean closed = false;

  private static class Tile {
    private int zoom;
    private int x;
    private int y;
    private byte[] image;
  }

  private ConcurrentLinkedQueue<Tile> writeQueue = new ConcurrentLinkedQueue<>();


  public MbTiles(CountrySet countrySet, int baseZoomLevel) throws ClassNotFoundException, SQLException {

    File databaseFile = new File("bgd.mbtiles");

    Class.forName("org.sqlite.JDBC");

    SQLiteConfig config = new SQLiteConfig();
    config.setOpenMode(SQLiteOpenMode.READWRITE);
    config.setOpenMode(SQLiteOpenMode.CREATE);
    config.setOpenMode(SQLiteOpenMode.NOMUTEX);

    connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath(), config.toProperties());
    execute("CREATE TABLE IF NOT EXISTS metadata (name text, value text)");
    execute("CREATE UNIQUE INDEX IF NOT EXISTS name on metadata (name);");
    execute("CREATE TABLE IF NOT EXISTS tiles (zoom_level integer, tile_column integer, tile_row integer, tile_data blob)");
    execute("CREATE UNIQUE INDEX IF NOT EXISTS tile_index on tiles (zoom_level, tile_column, tile_row);");
    setMetadata("name", "WorldPop");
    setMetadata("format", "png");
    Rectangle2D.Double bounds = countrySet.getGeographicBounds();
    setMetadata("bounds", String.format("%.2f, %.2f, %.2f, %.2f",
      bounds.getMinX(),
      bounds.getMinY(),
      bounds.getMaxX(),
      bounds.getMaxY()));
    setMetadata("attribution", "WorldPop.org");
    setMetadata("description", "Gridded population density, 2020");
    setMetadata("type", "baselayer");
    setMetadata("version", "1");
    setMetadata("minzoom", "0");
    setMetadata("maxzoom", Integer.toString(baseZoomLevel));

    writerThread = new Thread(() -> {

      PreparedStatement stmt = null;
      try {
        stmt = connection.prepareStatement("INSERT OR REPLACE INTO tiles (zoom_level,tile_column,tile_row,tile_data) VALUES(?,?,?,?)");
      } catch (SQLException e) {
        e.printStackTrace();
        return;
      }

      while(true) {
        Tile tile = writeQueue.poll();
        if(tile == null) {
          if(closed) {
            break;
          }
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            break;
          }
        } else {
          try {
            stmt.setInt(1, tile.zoom);
            stmt.setInt(2, tile.x);
            stmt.setInt(3, Tms.toTmsY(tile.zoom, tile.y));
            stmt.setBytes(4, tile.image);
            stmt.execute();
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }
      try {
        stmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    });
    writerThread.start();
  }

  private void setMetadata(String attributeName, String value) throws SQLException {
    PreparedStatement insert = connection.prepareStatement("INSERT OR REPLACE INTO metadata (name, value) VALUES (?, ?)");
    insert.setString(1, attributeName);
    insert.setString(2, value);
    insert.execute();
  }

  private void execute(String sql) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.execute();
  }

  @Override
  public void write(int zoom, int x, int y, RenderedImage image) {

    ByteArrayOutputStream baos = threadLocalImageBuffer.get();
    if(baos == null) {
      baos = new ByteArrayOutputStream(1024);
      threadLocalImageBuffer.set(baos);
    } else {
      baos.reset();
    }

    try {
      ImageIO.write(image, "png", baos);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    Tile tile = new Tile();
    tile.zoom = zoom;
    tile.x = x;
    tile.y = y;
    tile.image = baos.toByteArray();

    writeQueue.offer(tile);
  }

  public ThreadLocalReader getReader() {
    ThreadLocalReader reader = this.threadLocalReader.get();
    if(reader == null) {
      reader = new ThreadLocalReader();
      threadLocalReader.set(reader);
    }
    return reader;
  }

  public void close() throws InterruptedException {
    closed = true;
    writerThread.join();
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private class ThreadLocalReader implements TileReader {

    private final PreparedStatement statement;

    public ThreadLocalReader() {
      try {
        statement = connection.prepareStatement("SELECT tile_data FROM tiles WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?");
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    public BufferedImage read(int zoom, int x, int y) {
      try {
        statement.setInt(1, zoom);
        statement.setInt(2, x);
        statement.setInt(3, Tms.toTmsY(zoom, y));
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
          try (InputStream in = rs.getBinaryStream(1)) {
            return ImageIO.read(in);
          } catch (IOException e) {
            e.printStackTrace();
            return null;
          }
        } else {
          return null;
        }
      } catch (SQLException e) {
        e.printStackTrace();
        return null;
      }
    }
  }
}
