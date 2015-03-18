package org.ossim.kettle.steps.datainfo;

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
import org.ossim.core.SynchOssimInit

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
/*
 * Created on 2-jun-2003
 *
 */

public class DataInfo extends BaseStep implements StepInterface
{
  private DataInfoData data;
	private DataInfoMeta meta;
  //joms.oms.DataInfo dataInfo;
	
	DataInfo(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis)
	{
		super(s,stepDataInterface,c,t,dis);
	}
	
	boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
    Object[] r=getRow();    // get row, set busy!

    if(r == null)
    {
       setOutputDone();
       return false;
    }   

    if(first)
    {
          //Class imageReaderClass=Class.forName("javax.imageio.spi.ImageReaderSpi");

     // def imageReaders = javax.imageio.ImageIO.getImageReadersBySuffix("")
   //     IIORegistry registry = IIORegistry.getDefaultInstance();
   //   def providers=registry.getServiceProviders(ImageReaderSpi.class, true);
   //   println "*"*40
   //   providers?.each{
   //     println it
   //   }
   //   println "*"*40

       first = false;
       data.outputRowMeta = getInputRowMeta().clone();
       meta.getFields(data.outputRowMeta, getStepname(), null, null, this); 
    }
    def fileFieldName = meta.fileFieldName;


    int idx =  getInputRowMeta().indexOfValue(fileFieldName);


    String filename = null;

    if(idx >= 0 ) filename = getInputRowMeta().getString(r,idx);


    if(filename != null)
    {
       joms.oms.DataInfo dataInfo = new joms.oms.DataInfo();
       if ( dataInfo.open( filename ) )
       {
          String valueString = dataInfo.getInfo();
          valueString = valueString.replaceAll("(\\r|\\n)", "");

         // System.out.println(valueString);
         ValueMetaAndData value = new ValueMetaAndData(fileFieldName, valueString);
         Object extraValue = value.getValueData();
         Object[] outputRow = RowDataUtil.addValueData(r, data.outputRowMeta.size()-1, extraValue);
         putRow(data.outputRowMeta, outputRow);     // copy row to possible alternate rowset(s).
        //System.out.println( dataInfo.getInfo() );
       }
       else
       {
          logBasic("Could not open " + filename);
         // System.out.println("DataInfoTest::printInfo Could not open: " + filename);
       }
       dataInfo.close();
       dataInfo.delete();
       dataInfo = null;
    }


    if ((linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.
		return true;
	}
		
  boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
    SynchOssimInit.initialize()
    
    meta = (DataInfoMeta)smi;
    data = (DataInfoData)sdi;

    return super.init(smi, sdi);
	}

	void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
    meta = (DataInfoMeta)smi;
    data = (DataInfoData)sdi;
    super.dispose(smi, sdi);
	}
}
