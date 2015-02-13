package joms.geotools.tileapi

/**
 * Created by gpotts on 2/3/15.
 */

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;

public class TileCacheAccumuloFormat extends AbstractGridFormat implements Format {
  /** Logger. */
  private final static Logger LOGGER = Logging
          .getLogger(TileCacheAccumuloFormat.class.getPackage().getName());

  /** Control the transparency of the output coverage. */

  public static final ParameterDescriptor<Color> OUTPUT_TRANSPARENT_COLOR = new DefaultParameterDescriptor<Color>(
          "OutputTransparentColor", Color.class, null, null);
  /** Control the background values for the output coverage */

//        public static final ParameterDescriptor<Color> BACKGROUND_COLOR = new DefaultParameterDescriptor<Color>(
//                "BackgroundColor", Color.class, null, Color.BLACK);



  /**
   * Creates an instance and sets the metadata.
   */
  public TileCacheAccumuloFormat() {
    setInfo();

  }

  /*
public static URL getURLFromSource(Object source) {
  URL sourceURL = null;
   if (source == null) {
     return null;
   }

   URL sourceURL = null;

   try {
     if (source instanceof File) {
       File file = (File) source;
       String path = file.getPath();
       if (path.contains("pgraster:/")) {
         path = path.substring(path.indexOf("pgraster:/"));
         sourceURL = JDBCPGRasterConfigurationBuilder.createConfiguration(path, null);
       } else {
         sourceURL = file.toURI().toURL();
       }
     } else if (source instanceof URL) {
       sourceURL = (URL) source;
     } else if (source instanceof String) {
       String path = ((String)source);
       if (path.contains("pgraster:/")) {
         path = path.substring(path.indexOf("pgraster:/"));
         sourceURL = JDBCPGRasterConfigurationBuilder.createConfiguration(path, null);
       } else {

         final File tempFile = new File((String) source);

         if (tempFile.exists()) {
           sourceURL = tempFile.toURI().toURL();
         } else {
           sourceURL = new URL(URLDecoder.decode((String) source,
                   "UTF8"));

           // if (sourceURL.getProtocol().equals("file") == false) {
           // return null;
           // }
         }
       }
     }
   } catch (Exception e) {
     if (LOGGER.isLoggable(Level.FINE)) {
       LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
     }

     return null;
   }

   return sourceURL;

    println "NEED TO ADD SOME STUFF TO GET CONNECTION THINGS"
    "" as URL
  }
   */

  /**
   * Sets the metadata information.
   */
  private void setInfo() {
    HashMap<String, String> info = new HashMap<String, String>();

    info.put("name", "TileCacheAccumulo");
    info.put("description", "Mosaicking tiles stored in accumulo");
    info.put("vendor", "OSSIM");
    info.put("docURL", "");
    info.put("version", "1.0");
    mInfo = info;

    // reading parameters
    readParameters = new ParameterGroup(
            new DefaultParameterDescriptorGroup(mInfo,
                     [READ_GRIDGEOMETRY2D,
                      OUTPUT_TRANSPARENT_COLOR, BACKGROUND_COLOR ] as GeneralParameterDescriptor[]));

    // reading parameters
    writeParameters = null;
  }

  /**
   * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object)
   */
  @Override
  public TileCacheAccumuloReader getReader(Object source) {
    return getReader(source, null);
  }

  /**
   *
   */
  @Override
  public GridCoverageWriter getWriter(Object destination) {
    throw new UnsupportedOperationException(
            "This plugin does not support writing.");
  }

  /**
   * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object
          *      input)
   */
  @Override
  public boolean accepts(Object source, Hints hints) {
    if (source == null) {
      return false;
    }

    def config = new TileCacheConfig()

    try{
      config.readXml(source)

      return true
    }
    catch(e)
    {
    }
    if(source instanceof File)

    return config.layer!=null;
  }

  /**
   * @see AbstractGridFormat#getReader(Object, Hints)
   */
  @Override
  public TileCacheAccumuloReader getReader(Object source, Hints hints) {
    try {
      return new TileCacheAccumuloReader(source, hints);
    } catch (Exception e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
      }

      return null;
    }
  }

  /**
   * Throw an exception since this plugin is readonly.
   *
   * @return nothing.
   */
  @Override
  public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
    throw new UnsupportedOperationException("Unsupported method.");
  }

  @Override
  public GridCoverageWriter getWriter(Object destination, Hints hints) {
    throw new UnsupportedOperationException("Unsupported method.");
  }
}
