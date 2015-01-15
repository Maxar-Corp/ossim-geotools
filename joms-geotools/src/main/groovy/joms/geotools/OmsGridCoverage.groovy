package joms.geotools

import org.geotools.coverage.GridSampleDimension
import org.geotools.coverage.grid.GridCoverage2D
import org.geotools.coverage.grid.GridGeometry2D
import org.geotools.factory.Hints
import org.opengis.coverage.grid.GridCoverage
import org.opengis.coverage.grid.GridEnvelope
import org.opengis.referencing.crs.CoordinateReferenceSystem

import javax.media.jai.PlanarImage
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage

/**
 * Created by gpotts on 12/12/14.
 */
class OmsGridCoverage extends GridCoverage2D
{
/*
  public OmsGridCoverage(CharSequence name=null, BufferedImage image=null, GridGeometry2D gridGeometry=null,
                         GridSampleDimension[] bands=null, GridCoverage[] sources=null,
                         Map<?, ?> properties=null, Hints hints=null) throws IllegalArgumentException

  {
    super(
            name,
            PlanarImage.wrapRenderedImage(image),
            gridGeometry,
            bands,
            sources,
            properties,
            hints
    )
  }
  */
   public OmsGridCoverage(CharSequence name=null, RenderedImage image=null, GridGeometry2D gridGeometry=null,
                         GridSampleDimension[] bands=null, GridCoverage[] sources=null,
                         Map<?, ?> properties=null, Hints hints=null) throws IllegalArgumentException

  {
    super(
            name,
            PlanarImage.wrapRenderedImage(image),
            gridGeometry,
            bands,
            sources,
            properties,
            hints
    )


  }

}
