package joms.geotools.tileapi

import java.util.Collections;
import java.util.Map;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;

/**
 * Implementation of the GridCoverageFormat service provider interface for
 * mosaicing of georeferenced images and image pyramids stored in a jdbc
 * database
 *
 * @author mcr
 * @since 2.5
 *
 *
 *
 * @source $URL$
 */
public class TileCacheAccumuloFormatFactory implements GridFormatFactorySpi {
  /**
   * Tells me if this plugin will work on not given the actual installation.
   *
   * <p>
   * Dependecies are mostly from JAI and ImageIO so if they are installed you
   * should not have many problems.
   *
   * @return False if something's missing, true otherwise.
   */
  public boolean isAvailable() {
    boolean available = true;

    try {
    //  println "TESTING AVAILABLE IN TileCacheAccumuloFormatFactory"
      Class.forName("joms.geotools.tileapi.TileCacheAccumuloReader");
    } catch (ClassNotFoundException cnf) {
      available = false;
    }

    return available;
  }

  /**
   * @see GridFormatFactorySpi#createFormat().
   */
  public TileCacheAccumuloFormat createFormat() {
    return new TileCacheAccumuloFormat();
  }

  /**
   * Returns the implementation hints. The default implementation returns an
   * empty map.
   *
   * @return An empty map.
   */
  public Map getImplementationHints() {
    return Collections.EMPTY_MAP;
  }
}
