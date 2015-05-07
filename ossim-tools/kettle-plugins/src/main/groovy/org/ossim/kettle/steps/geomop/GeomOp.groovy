package org.ossim.kettle.steps.geomop

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource
import org.springframework.beans.factory.xml.XmlBeanFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.springframework.context.support.GenericApplicationContext
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader
import org.pentaho.di.core.row.RowMeta
import org.ossim.kettle.steps.geomop.GeomOpData.GeomOpType
import geoscript.proj.Projection
import javax.imageio.ImageIO;
/*
 * Created on 2-jun-2003
 *
 */

public class GeomOp extends BaseStep implements StepInterface
{
   private GeomOpData data
	private GeomOpMeta meta
	private def param1
	private def param2
	private def isSingleRow
	def geom
  //joms.oms.DataInfo dataInfo;
	
	GeomOp(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis)
	{
		super(s,stepDataInterface,c,t,dis);
	}
	private String getFieldValueAsString(String fieldValue, def r, GeomOpMeta meta, GeomOpData data)
	{
		String result = fieldValue

		if(fieldValue && r)
		{
			if(fieldValue.startsWith("\${"))
			{
				result = environmentSubstitute(fieldValue?:"")
			}
			else
			{
				Integer fieldIndex   =  getInputRowMeta().indexOfValue(fieldValue)
				if(fieldIndex >= 0)
				{
					result = getInputRowMeta().getString(r,fieldIndex)
				}
			}
		}

		result
	}

	boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] r=getRow();    // get row, set busy!
		//println p		def readFormats = []

		if(r == null)
		{
			//println "DOING THE LAST ROW!!!!!!!!!!!!!!!"
			// put final result to row if in the mode that combines all rows
			//
			if(isSingleRow&&data.outputRowMeta)
			{
         	Object[] outputRow = RowDataUtil.addValueData(null, 0, geom);
        		putRow(data.outputRowMeta, outputRow);     // copy row to possible alternate rowset(s).
			}

			setOutputDone();

			return false;
		}   

		if(first)
		{
			first = false;
			if(isSingleRow)
			{
				data.outputRowMeta = new RowMeta()//getInputRowMeta().clone();
			}
			else
			{
				data.outputRowMeta = getInputRowMeta().clone()
			}
			//data.outputRowMeta.clear()
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
         if(meta.operationType == GeomOpType.PROJECTION_TRANSFORM)
         {
            param1 = new Projection(getFieldValueAsString(meta.param1,r, meta, data))
            param2 = new Projection(getFieldValueAsString(meta.param2,r, meta, data))
         }
		}
		def rowMeta = getInputRowMeta()
		int input1Idx = rowMeta.indexOfValue(meta.inputGeomField1)
		int input2Idx = rowMeta.indexOfValue(meta.inputGeomField2)

		try{
			if(isSingleRow)
			{
				if(input1Idx >= 0)
				{
					if(meta.operationType == GeomOpType.UNION)
					{
			    		if(!geom)
			    		{
			    			geom = r[input1Idx]
						}
						else
						{
			    			if(r[input1Idx])
			    			{
			    				geom = geom.union(r[input1Idx])
			    			}
						}
					}
				}
			}
			else
			{
				def resultArray = []
				switch(meta.operationType)
				{
					case GeomOpType.UNION:
						def value = null
						if(input1Idx >= 0 && input2Idx >= 0)
						{
							if(!r[input2Idx]&&r[input1Idx])
							{
								value = r[input1Idx]
							}
							else if(r[input2Idx]&&!r[input1Idx])
							{
								value = r[input2Idx]
							}
							else
							{
								value = r[input1Idx].union(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.PROJECTION_TRANSFORM:
						def value = null
                  if(input1Idx >= 0)
						{
							if(r[input1Idx])
							{
	    						try{
	    							value = param1.transform(geoscript.geom.Geometry.wrap(r[input1Idx]), param2)//Projection.transform(geoscript.geom.Geometry.wrap(r[input1Idx]),
									value = value.g
	    						}
	    						catch(e)
	    						{
	    							println e
	    							value = null
	    						}
							}
						}
						resultArray << value
						break
					case GeomOpType.INTERSECTION:
						def value = null
						if(input1Idx >= 0 && input2Idx >= 0)
						{
							if(r[input2Idx]&&r[input1Idx])
							{
								value =  r[input1Idx].intersection(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.DIFFERENCE:
						def value = null
						if(input1Idx >= 0 && input2Idx >= 0)
						{
							if(r[input2Idx]&&r[input1Idx])
							{
								value = r[input1Idx].difference(r[input2Idx])
							}
							else if(!r[input2idx])
							{
								if(r[input1idx])
								{
									value = r[input1Idx]
								}
							}
						}
						resultArray << value
						break
					case GeomOpType.CONVEX_HULL:
						def value = null
						if(input1Idx >= 0)
						{
							if(r[input1Idx])
							{
								value = r[input1Idx].convexHull()
							}
						}
						resultArray << value
						break
					case GeomOpType.DISTANCE:
						def value = 0.0
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].distance(r[input2Idx])
							}
						}
						if(value != null)
							resultArray << (Number)value
						break
					case GeomOpType.AREA:
						if((input1Idx >= 0)&&r[input1Idx])
						{
							resultArray << r[input1Idx].area
						}
						break
					case GeomOpType.BOUNDARY:
						def value = null
						if(input1Idx >= 0&&r[input1Idx])
						{
							value = r[input1Idx].boundary
						}
						resultArray << value
						break
					case GeomOpType.CENTROID:
						def value = null
						if(input1Idx >= 0&&r[input1Idx])
						{
							value = r[input1Idx].centroid
						}
						resultArray << value
						break
					case GeomOpType.ENVELOPE:
						break
					case GeomOpType.INTERSECTS:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].intersects(r[input2Idx])
							}
						}
						resultArray << value

						break
					case GeomOpType.EXACTLY_EQUALS:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].equalsExact(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.EQUALS:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].equals(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.CONTAINS:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].contains(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.COVERED_BY:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].coveredBy(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.COVERS:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].covers(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.CROSSES:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].crosses(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.IS_SIMPLE:
						def value = true
						if(input1Idx >= 0)
						{
							if(r[input1Idx])
							{
								value = r[input1Idx].isSimple()
							}
						}
						resultArray << value
						break
					case GeomOpType.OVERLAPS:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].overlaps(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.WITHIN:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].within(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.DISJOINT:
						def value = true
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].disjoint(r[input2Idx])
							}
						}
						resultArray << value
						break
					case GeomOpType.TOUCHES:
						def value = false
						if((input1Idx >= 0)&&(input2Idx>=0))
						{
							if(r[input1Idx]&&r[input2Idx])
							{
								value = r[input1Idx].touches(r[input2Idx])
							}
						}
						resultArray << value
						break
					default:
						resultArray = []
						break
				}
				if(resultArray)
				{
					Object[] outputRow = RowDataUtil.addRowData(r, 
						                                          data.outputRowMeta.size()-(resultArray.size()), 
						                                          resultArray as Object []);
					putRow(data.outputRowMeta, outputRow);
				}
			}
		}
		catch(e)
		{
			
			throw new KettleException("Unable to perform operation ${meta.operationType}: ${e}")
		}
		if ((linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.
		return true;
	}
		
   boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (GeomOpMeta)smi;
		data = (GeomOpData)sdi;
		geom = null
		isSingleRow = meta.isSingleRow()

		return super.init(smi, sdi);
	}

	void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (GeomOpMeta)smi;
		data = (GeomOpData)sdi;
		super.dispose(smi, sdi);
	}
}
