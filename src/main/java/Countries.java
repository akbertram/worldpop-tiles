import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.Envelope2D;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Countries {

  private final Quadtree quadtree;
  private final List<SourceImage> sources = new ArrayList<>();

  public Countries(File baseDir) throws IOException {

    quadtree = new Quadtree();

    for (File file : baseDir.listFiles()) {
      AbstractGridFormat format = GridFormatFinder.findFormat( file );
      GridCoverage2DReader reader = format.getReader( file );

      String[] coverageNames = reader.getGridCoverageNames();

      GridCoverage2D coverage = reader.read(coverageNames[0], null);

      SourceImage source = new SourceImage(coverage);
      quadtree.insert(toJtsEnvelope(coverage.getEnvelope2D()), source);
      sources.add(source);
    }
  }

  private Envelope toJtsEnvelope(Envelope2D coverageEnvelope) {
    return new Envelope(
      coverageEnvelope.getMinX(),
      coverageEnvelope.getMaxX(),
      coverageEnvelope.getMinY(),
      coverageEnvelope.getMaxY());
  }

  public List<SourceImage> findOverlappingCountries(Envelope2D bounds) {
    List<SourceImage> matching = new ArrayList<>();
    for (SourceImage sourceImage : sources) {
      if(sourceImage.getGeographicBounds().getBounds2D().intersects(bounds.getBounds2D())) {
        matching.add(sourceImage);
      }
    }
    return matching;
  }

  public boolean isEmpty(Envelope2D bounds) {
    return findOverlappingCountries(bounds).isEmpty();
  }
}
