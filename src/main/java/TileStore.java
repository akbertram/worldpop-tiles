public interface TileStore extends AutoCloseable {


  WriteBuffer newWriteBuffer(int zoomLevel);


}
