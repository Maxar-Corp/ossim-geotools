package org.ossim.kettle.steps.geopackagewriter
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * 
 * 
  */
public class GeoPackageWriterData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;

   public GeoPackageWriterData()
	{
		super();
	}
}
