package joms.geotools

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader
import org.geotools.coverage.grid.io.AbstractGridFormat
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams
import org.geotools.factory.Hints
import org.geotools.parameter.ParameterGroup
import org.opengis.coverage.grid.GridCoverageWriter
import org.geotools.parameter.DefaultParameterDescriptorGroup;

import joms.oms.DataInfo
import org.opengis.parameter.GeneralParameterDescriptor

/**
 * Created by sbortman on 10/22/14.
 */
class OmsGridFormat extends AbstractGridFormat
{
  OmsGridFormat() {
    setInfo();
  }

  @Override
  AbstractGridCoverage2DReader getReader(Object source, Hints hints = null)
  {
    return new OmsGridReader(source, hints)
  }

  @Override
  boolean accepts(Object source, Hints hints)
  {
    return DataInfo.readInfo( source?.toString() ) != null
  }
  protected void setInfo() {
    final HashMap<String, String> info = new HashMap<String, String>();
    info.put("name", "OSSIM");
    info.put("description", "OSSIM Coverage Format");
    info.put("vendor", "OSSIM");
    info.put("docURL", ""); // TODO: set something
    info.put("version", "1.0");
    mInfo = Collections.unmodifiableMap(info);

    // writing parameters
     writeParameters = null;

    // reading parameters
     readParameters = new ParameterGroup(
             new DefaultParameterDescriptorGroup(mInfo,
                     [AbstractGridFormat.READ_GRIDGEOMETRY2D,
                       AbstractGridFormat.USE_JAI_IMAGEREAD,
                       AbstractGridFormat.SUGGESTED_TILE_SIZE ] as GeneralParameterDescriptor[]));
  }
  @Override
  GeoToolsWriteParams getDefaultImageIOWriteParameters()
  {
    return null
  }

  @Override
  GridCoverageWriter getWriter(Object destination, Hints hints = null)
  {
    return null
  }
}
