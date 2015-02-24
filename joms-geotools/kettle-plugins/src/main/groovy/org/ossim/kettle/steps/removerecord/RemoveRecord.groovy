package org.ossim.kettle.steps.removerecord;

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
import org.pentaho.di.core.row.RowMeta
import org.springframework.validation.FieldError
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource
import org.springframework.beans.factory.xml.XmlBeanFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader
import org.springframework.context.support.GenericApplicationContext
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader
import org.ossim.omar.hibernate.Hibernate;
import org.ossim.omar.hibernate.domain.io.RasterDataSetXmlReader
import org.ossim.omar.hibernate.domain.io.VideoDataSetXmlReader
import org.ossim.omar.hibernate.domain.io.XmlIoHints
import org.hibernate.ScrollMode

public class RemoveRecord extends BaseStep implements StepInterface
{
	private RemoveRecordData data
	private RemoveRecordMeta meta
	private def hibernate
	private def session
	private def query
	private def records
	private def count = 0
	private def outputRows = []

	public RemoveRecord(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis)
	{
		super(s,stepDataInterface,c,t,dis);
	}

	private outputResults(){
		outputRows.each{outputRow->
			putRow(data.outputRowMeta, outputRow);
		}
		outputRows = []
		count = 0
	}
	private rollbackResults(){
		session?.transaction?.rollback();
		outputRows.each{outputRow->
			outputRow[data.outputRowMeta.size()-1] = false
		}
		outputResults()
		count = 0
	}
	private commitTransaction(){
		try{
			if(session?.transaction?.isActive())
			{
				session?.transaction?.commit()
				session?.flush()
				session?.clear();
			}
			outputResults()
		}
		catch(def e)
		{
			session?.transaction?.rollback()
			rollbackResults()
		}
	}


	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		def result = false
		Object[] r=getRow();    // get row, set busy!

		if(!session)
		{
			throw new KettleException("No session present")
		}

		if(!r)
		{
			// commit any items that are left and output
    		commitTransaction()
		   setOutputDone();

		   return false;
		}  
		if(first)
		{
			first = false;
			//data.outputRowMeta = new RowMeta()
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this); 
		}
	   int keyFieldIdx = getInputRowMeta().indexOfValue(meta.keyFieldName);

		if(count == 0)	session.beginTransaction();

      incrementLinesInput();
		++count

		if(keyFieldIdx >=0)
		{
			try{
				def tableName 

				if(meta.tableNameFromFieldFlag)
				{
	   			int tableNameIdx = getInputRowMeta().indexOfValue(meta.tableNameFromField);
	   			if(tableNameIdx >=0)
	   			{
	   				tableName = getInputRowMeta().getString(r, tableNameIdx)
	   				tableName = Hibernate.sqlTableNameToHibernate(tableName)
	   			}
	   			else
	   			{
	   				throw "Table name field ${meta.tableNameFromField} not found\n".toString()
	   			}
				}
				else
				{
					tableName = meta.tableName
				}
	    		def key = getInputRowMeta().getString(r,keyFieldIdx);
	    		String queryString = "FROM ${tableName} WHERE ${meta.columnName} = ${key}".toString()
				def query = session.createQuery(queryString).setMaxResults(1)
				def recordList = query.list()
				if(recordList)
				{
					recordList.each{session.delete(it)}
					result = true
				}					
			}
			catch(def e)
			{
				//println e
				result = false;
			}
		}

		if(meta.outputResultFlag)
		{
			outputRows << RowDataUtil.addValueData(r, 
 	                                             data.outputRowMeta.size()-1, 
 	                                             result);
		}


		if (count % meta.batchSize== 0) { commitTransaction()}

      if (checkFeedback(getLinesInput())) 
      {
      	if(log.isBasic()) logBasic("linenr " + getLinesInput());
      }

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (RemoveRecordMeta)smi;
		data = (RemoveRecordData)sdi;
		if(!hibernate)
		{
			hibernate = new Hibernate()
			hibernate.initialize(meta.databaseMeta)
		}
		session?.close()
		session = hibernate?.sessionFactory?.openSession()
		count = 0

		return super.init(smi, sdi);
	}
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (RemoveRecordMeta)smi;
		data = (RemoveRecordData)sdi;
		hibernate?.shutdown()
		session?.close()
		session = null
		super.dispose(smi, sdi);
	}

}