import com.google.common.primitives.UnsignedBytes;

import java.awt.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.Vector;

public class TileImage implements RenderedImage {

  private static final ThreadLocal<TileImage> THREAD_LOCAL = new ThreadLocal<>();

  private final IndexColorModel colorModel;
  private final SampleModel sampleModel;
  private final DataBufferByte dataBuffer;
  private final byte[] array;
  private final Raster raster;

  public TileImage() {
    int bits = 8;
    int numColors = ColorGradient.COLORS.length;
    byte[] red = new byte[numColors];
    byte[] green = new byte[numColors];
    byte[] blue = new byte[numColors];
    for (int i = 0; i < numColors; i++) {
      red[i] = UnsignedBytes.checkedCast(ColorGradient.COLORS[i].getRed());
      green[i] = UnsignedBytes.checkedCast(ColorGradient.COLORS[i].getGreen());
      blue[i] = UnsignedBytes.checkedCast(ColorGradient.COLORS[i].getBlue());
    }
    int transparentIndex = 0;
    colorModel = new IndexColorModel(bits, numColors, red, green, blue, transparentIndex);
    sampleModel = colorModel.createCompatibleSampleModel(Tiling.PIXELS_PER_TILE, Tiling.PIXELS_PER_TILE);
    dataBuffer = (DataBufferByte) sampleModel.createDataBuffer();
    array = dataBuffer.getData();
    raster = Raster.createRaster(sampleModel, dataBuffer, new Point(0, 0));
  }

  public void setColorIndex(int pixelIndex, int color) {
    array[pixelIndex] = (byte)color;
  }

  public static TileImage getBuffer() {
    TileImage buffer = THREAD_LOCAL.get();
    if(buffer == null) {
      buffer = new TileImage();
      THREAD_LOCAL.set(buffer);
    }
    return buffer;
  }

  @Override
  public Vector<RenderedImage> getSources() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getProperty(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getPropertyNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ColorModel getColorModel() {
    return colorModel;
  }

  @Override
  public SampleModel getSampleModel() {
    return sampleModel;
  }

  @Override
  public int getWidth() {
    return Tiling.PIXELS_PER_TILE;
  }

  @Override
  public int getHeight() {
    return Tiling.PIXELS_PER_TILE;
  }

  @Override
  public int getMinX() {
    return 0;
  }

  @Override
  public int getMinY() {
    return 0;
  }

  @Override
  public int getNumXTiles() {
    return 1;
  }

  @Override
  public int getNumYTiles() {
    return 1;
  }

  @Override
  public int getMinTileX() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMinTileY() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getTileWidth() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getTileHeight() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getTileGridXOffset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getTileGridYOffset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Raster getTile(int tileX, int tileY) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Raster getData() {
    return raster;
  }

  @Override
  public Raster getData(Rectangle rect) {
    return raster;
  }

  @Override
  public WritableRaster copyData(WritableRaster raster) {
    throw new UnsupportedOperationException();
  }

  public void clear() {
    Arrays.fill(array, (byte)0);
  }

  public void set(BufferedImage image) {
    int[] existingBuffer = new int[Tiling.PIXELS_PER_TILE * Tiling.PIXELS_PER_TILE];
    image.getData().getPixels(0, 0, Tiling.PIXELS_PER_TILE, Tiling.PIXELS_PER_TILE, existingBuffer);
    for (int i = 0; i < existingBuffer.length; i++) {
      array[i] = (byte)existingBuffer[i];
    }
  }
}
