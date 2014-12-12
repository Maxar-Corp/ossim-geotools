package joms.geotools

import joms.oms.Init
import org.geotools.coverage.grid.io.AbstractGridFormat
import org.geotools.coverage.grid.io.GridFormatFactorySpi

import java.awt.RenderingHints

/**
 * Created by gpotts on 12/11/14.
 */
class OmsGridFactorySpi implements GridFormatFactorySpi{
  OmsGridFactorySpi()
  {
    super()
    Init.instance().initialize()
  }
  @Override
  AbstractGridFormat createFormat() {
    return new OmsGridFormat()
  }

  @Override
  boolean isAvailable() {
    true
  }

  @Override
  Map<RenderingHints.Key, ?> getImplementationHints() {
    return null
  }
}
