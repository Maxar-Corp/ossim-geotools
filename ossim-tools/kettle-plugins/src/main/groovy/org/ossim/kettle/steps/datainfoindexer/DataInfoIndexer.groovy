package org.ossim.kettle.steps.datainfoindexer

import org.hibernate.Query
import org.hibernate.Session;
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
import org.ossim.omar.hibernate.Hibernate;
import org.ossim.omar.hibernate.domain.io.RasterDataSetXmlReader
import org.ossim.omar.hibernate.domain.io.VideoDataSetXmlReader
import org.ossim.omar.hibernate.domain.io.XmlIoHints
import org.ossim.core.SynchOssimInit

public class DataInfoIndexer extends BaseStep implements StepInterface
{
	private DataInfoIndexerData data;
	private DataInfoIndexerMeta meta;
	private def hibernate;
	private def session;
	private def count = 0
	private def outputRows = []
	private def repository
	public DataInfoIndexer(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis)
	{
		super(s,stepDataInterface,c,t,dis);
	}
	private void outputResults(){
		outputRows.each{outputRow->
			putRow(data.outputRowMeta, outputRow);
		}
		outputRows = []
		count = 0
	}
	private void rollbackResults(){
		outputRows.each{outputRow->
			outputRow[data.outputRowMeta.size()-2] = -1 as long
			outputRow[data.outputRowMeta.size()-1] = ""
		}
		outputResults()
		count = 0
	}
	private void commitTransaction(){
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
	private def processRasterDataSetNode(def rasterDataSetNode)
	{
	   def hints = new XmlIoHints()
		def rasterDataSet = RasterDataSetXmlReader.initRasterDataSet(rasterDataSetNode, null, hints)
		Query query
		def rasterFile
		def result = [-1 as long,"raster_data_set"] as Object[]


		// if we are in update mode or check if exists mode then we will look up the raster file entry
		//
		if(meta.indexingModeUpdateFlag||meta.checkIfAlreadyExistsFlag&&rasterDataSet?.mainFile)
		{
			try{
				query = session.createQuery("FROM RasterFile where name = '${rasterDataSet.mainFile}'").setMaxResults(1)
				def queryList = query?.list()
        if(queryList)
        {
          rasterFile = queryList[0]
        }
			}
			catch(e)
			{
        //println "ERROR QUERYING THE LIST!!!!!!!!!!!!!!!!!!!!!!!!!!"
        //e.printStackTrace()
         // println "Checking data set ${e}"
			}
		}

		// if we had a lookup and already existed
		if(rasterFile)
		{
			rasterDataSet = rasterFile.rasterDataSet
			if(rasterDataSet)
			{
				if(repository) rasterDataSet?.repository = repository
				if(meta.indexingModeUpdateFlag)
				{
					RasterDataSetXmlReader.initRasterDataSet(rasterDataSetNode, rasterDataSet, hints)
					try
					{
            session.saveOrUpdate(rasterDataSet)
            result[0] = rasterDataSet.id as long
          }
					catch(e)
					{
          //  println e
						e.printStackTrace()
					}
				}
			}
		}
		else if(meta.indexingModeAddFlag)
		{
			if(rasterDataSet)
			{
				if(repository) rasterDataSet?.repository = repository
				try
				{
          session.saveOrUpdate(rasterDataSet)
					result[0] = rasterDataSet.id as long
				}
				catch(e)
				{
            e.printStackTrace()
				}
			}  
		}

		result
	}
	private def processVideoDataSetNode(def videoDataSetNode)
	{
	   def hints = new XmlIoHints()
		def videoDataSet = VideoDataSetXmlReader.initVideoDataSet(videoDataSetNode, null, hints)
		def query 
		def videoFile
		def result = [-1 as long,"video_data_set"] as Object[]
	
		// if we are in update mode or check if exists mode then we will look up the raster file entry
		//
		if(meta.indexingModeUpdateFlag||meta.checkIfAlreadyExistsFlag&&videoDataSet?.mainFile)
		{
			query = session.createQuery("FROM VideoFile where name = '${videoDataSet.mainFile}'").setMaxResults(1)
			videoFile = query?.list()[0]
		}

		if(videoFile)
		{
			videoDataSet = videoFile.videoDataSet
			if(repository) videoDataSet?.repository = repository
			if(meta.indexingModeUpdateFlag)
			{
				if(videoDataSet) 
				{
					VideoDataSetXmlReader.initVideoDataSet(videoDataSetNode, videoDataSet, hints)
					session.saveOrUpdate(videoDataSet)
					result[0] = videoDataSet.id as long
				}
			}
		}
		else if(meta.indexingModeAddFlag)
		{
			if(repository) videoDataSet?.repository = repository
			if(videoDataSet) session.save(videoDataSet); 
			result[0] = videoDataSet.id as long
		}

		result

	}
	private def getRepository(def columnValue)
	{
		def queryRepo
		if(!columnValue) repository = null
		else if(columnValue.isInteger())
		{
			def id = columnValue.toInteger()
			if(repository) 
			{
				if(repository.id != id)
				{
					queryRepo = session.createQuery("from Repository where id = ?").setMaxResults(1)
				}
			}
			else
			{
				queryRepo = session.createQuery("from Repository where id = ?").setMaxResults(1)
			}
			queryRepo?.setLong(0, id as long)
		}
		else
		{
			if(repository) 
			{
				if(repository.repositoryBaseDir != columnValue)
				{
					queryRepo = session.createQuery("from Repository where repositoryBaseDir = ?").setMaxResults(1)
				}
			}
			else
			{
				queryRepo = session.createQuery("from Repository where repositoryBaseDir = ?").setMaxResults(1)
			}
			queryRepo?.setString(0, columnValue)
		}

		if(queryRepo)
		{
			try{
				repository = null
				def queryList = queryRepo?.list()
				if(queryList)
				{
					repository = queryList[0]
				}
			}
			catch(e)
			{
			}
		}
		repository
   }
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] r=getRow();    // get row, set busy!
		if(r == null)
		{
    		commitTransaction()
		   setOutputDone();
		   return false;
		} 
		if(!session)
		{
			throw new KettleException("No session present")
		}  
		if(first)
		{
		 first = false;
		 data.outputRowMeta = getInputRowMeta().clone();
		 meta.getFields(data.outputRowMeta, getStepname(), null, null, this); 
		// System.out.println("=============================== " +  ); 
		}

		def result
		int idx =  getInputRowMeta().indexOfValue(meta.dataInfoFieldName);
		int repoIdx =  getInputRowMeta().indexOfValue(meta.repositoryField);
		def repoColumnValue = repoIdx>=0?getInputRowMeta().getString(r,repoIdx):"";
		def dataInfo;
		def hints = new XmlIoHints()
		if(idx >= 0 ) dataInfo = getInputRowMeta().getString(r,idx);
		if(dataInfo)
		{
			if(hibernate.applicationContext)
			{
		   def dataInfoXml = new XmlSlurper().parseText(dataInfo);

			try{
				if(count == 0)	session.beginTransaction();
				repository = getRepository(repoColumnValue)
				if(dataInfoXml.name() == "oms")
				{                	
					for (def rasterDataSetNode in dataInfoXml?.dataSets?.RasterDataSet )
					{
						result = processRasterDataSetNode(rasterDataSetNode)
					}
				// index videos
					for (def videoDataSetNode in dataInfoXml?.dataSets?.VideoDataSet )
					{
						result = processVideoDataSetNode(videoDataSetNode)
					}
				}
				else if(dataInfoXml.name() == "RasterDataSet")
				{
					result = processRasterDataSetNode(dataInfoXml)
					//def rasterDataSetNoderDataSet = RasterDataSetXmlReader.initRasterDataSet(dataInfoXml, null, hints)
					//session.saveOrUpdate(rasterDataSet); 
				}
				else if(dataInfoXml.name() == "VideoDataSet")
				{
					result = processVideoDataSetNode(dataInfoXml)
				}
				++count
				if(meta.outputResultFlag)
				{
					outputRows << RowDataUtil.addRowData(r, 
		 	                                             data.outputRowMeta.size()-(result.size()), 
		 	                                             result as Object []);
				}
				if (count % meta.batchSize== 0) 
				{
          //println "COMMITING TRANSACTION---------------------"
					commitTransaction()
				}
				
		    }
		    catch(e)
		    {
		    	e.printStackTrace()
		    	rollbackResults()
		    }
			}
		}

     if ((linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);  // Some basic logging every 5000 rows.

		return true;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		SynchOssimInit.initialize()
		meta = (DataInfoIndexerMeta)smi;
		data = (DataInfoIndexerData)sdi;
		if(!hibernate)
		{
			hibernate = new Hibernate()
			hibernate.initialize(meta.databaseMeta)
		}

		session?.close()
		session = hibernate?.sessionFactory?.openSession()
		return super.init(smi, sdi);
	}
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
    try{
      meta = (DataInfoIndexerMeta)smi;
      data = (DataInfoIndexerData)sdi;
      hibernate?.shutdown()
      session?.close()
      session = null
    }
    catch(def e)
    {
      println e
    }
    finally{
      super.dispose(smi, sdi);
    }
	}
}
