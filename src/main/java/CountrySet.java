import com.google.common.base.Strings;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.Envelope2D;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CountrySet {

  private final Quadtree quadtree;
  private final List<Country> sources = new ArrayList<>();

  public CountrySet() throws IOException {

    File baseDir;
    if(!Strings.isNullOrEmpty(System.getenv("SOURCE_DIR"))) {
      baseDir = new File(System.getenv("SOURCE_DIR"));
    } else {
      baseDir = new File("tif_country");
    }

    quadtree = new Quadtree();

    for (File file : baseDir.listFiles()) {
      if(file.getName().endsWith(".tif")) {
        AbstractGridFormat format = GridFormatFinder.findFormat(file);
        GridCoverage2DReader reader = format.getReader(file);

        String[] coverageNames = reader.getGridCoverageNames();

        GridCoverage2D coverage = reader.read(coverageNames[0], null);

        Country source = new Country(file, coverage);
        quadtree.insert(toJtsEnvelope(coverage.getEnvelope2D()), source);
        sources.add(source);
      }
    }
  }

  public Rectangle2D.Double getGeographicBounds() {
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;

    for (Country country : sources) {
      minX = Math.min(minX, country.getGeographicBounds().getMinX());
      minY = Math.min(minY, country.getGeographicBounds().getMinY());
      maxX = Math.max(maxX, country.getGeographicBounds().getMaxX());
      maxY = Math.max(maxY, country.getGeographicBounds().getMaxY());
    }

    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  private Envelope toJtsEnvelope(Envelope2D coverageEnvelope) {
    return new Envelope(
      coverageEnvelope.getMinX(),
      coverageEnvelope.getMaxX(),
      coverageEnvelope.getMinY(),
      coverageEnvelope.getMaxY());
  }

  public List<Country> findOverlappingCountries(Envelope2D bounds) {
    List<Country> matchingQuadtree = quadtree.query(toJtsEnvelope(bounds));
    List<Country> matching = new ArrayList<>();
    for (Country country : matchingQuadtree) {
      if(country.getGeographicBounds().getBounds2D().intersects(bounds.getBounds2D())) {
        matching.add(country);
      }
    }
    return matching;
  }

  public boolean isEmpty(Envelope2D bounds) {
    return findOverlappingCountries(bounds).isEmpty();
  }
}
