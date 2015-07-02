package org.ossim.kettle.steps.dirwatch

import groovy.io.FileType
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.row.RowMeta
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp

class DirWatch extends BaseStep implements StepInterface
{
   private DirWatchMeta meta = null;
   private DirWatchData data = null;
   private File directoryToWatch
   private Boolean needsToScan = false
   private Long timeBetweenScans = 10
   private Long timeDeltaForNotification = 30
   private Long lastScanTime = 0
   public DirWatch(StepMeta stepMeta, StepDataInterface stepDataInterface,
                         int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   private String getFieldValueAsString(String fieldValue,
                                        def r,
                                        DirWatchMeta meta,
                                        DirWatchData data)
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
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow();

      if (first)
      {
         if (r==null)
         {
            setOutputDone()
            return false
         }

         directoryToWatch = getFieldValueAsString(meta.fieldFilename, r,meta,data) as File

         if(directoryToWatch)
         {
            data.initDb(directoryToWatch)

         }

         first=false

         data.outputRowMeta = new RowMeta()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
      }

      // For this template I am just copying the input row to the output row
      // You can pass your own information to the output
      //
      //putRow(data.outputRowMeta, r);

      // scan for files

      Long deltaTime = (System.currentTimeMillis() - lastScanTime) / 1000
      if(deltaTime > timeBetweenScans)
      {
         needsToScan = true
      }

      if(data.conn&&needsToScan)
      {
         needsToScan = false
         if(directoryToWatch?.exists())
         {
            Statement stat = data.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String INSERT_RECORD = "insert into watch(filename, filesize,last_modified, notified) values(?, ?, ?, ?)";
            PreparedStatement pstmt = data.conn.prepareStatement(INSERT_RECORD);
            directoryToWatch.eachFileRecurse(FileType.FILES) {file->
               if(!file?.name?.startsWith(data.databaseName))
               {
                  ResultSet rs;
                  rs = stat.executeQuery("select * from watch where filename='${file}'".toString());
                  if(!rs.first())
                  {
                     Boolean notified = false
                     Long modifiedMillis = file.lastModified()
                     Double delta = (new Date().time - modifiedMillis)/1000
                     if(delta >= timeDeltaForNotification)
                     {
                        notified = true
                        putRow(data.outputRowMeta, [file] as Object[]);
                     }
                     //String timeStamp = new Date(file.lastModified()).format("yyyy-MM-dd hh:mm:ss.ssss")
                     //stat.execute("MERGE INTO watch(filename, last_modified, notified) values('Hello','2015-10-10 12:34:45.1234', false)");
                     pstmt.setString(1,file.toString())
                     pstmt.setBigDecimal(2,file.length())
                     pstmt.setTimestamp(3, new Timestamp(file.lastModified()))//new Timestamp(new Date().time))
                     pstmt.setBoolean(4, notified)
                     pstmt.executeUpdate()//"INSERT INTO watch(filename, filesize, last_modified, notified) values('${file}',${file.length()},'${timeStamp}', false)".toString());
                  }
                  else if(!rs.getBoolean("notified"))
                  {
                     rs.updateTimestamp("last_modified", new Timestamp(file.lastModified()))
                     rs.updateBigDecimal("filesize",file.length())
                     Long modifiedMillis = file.lastModified()

                     Double delta = (new Date().time - modifiedMillis)/1000
                     if(delta >= timeDeltaForNotification)
                     {
                        rs.updateBoolean("notified", true)

                        putRow(data.outputRowMeta, [file] as Object[]);
                     }
                     rs.updateRow()
                  }
                  else
                  {
                    // println "FILE HAS BEEN NOTIFIED"
                  }
               }
            }

            stat.close()
            pstmt?.close()
         }

         lastScanTime = System.currentTimeMillis()

         // now purge any invalid files that were indexed
         //
         Statement stat = data.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

         ResultSet rs;
         Long offset=0
         Long nResults = 0
         Boolean keepGoing = true
         while(keepGoing)
         {
            nResults = 0
            rs = stat.executeQuery("select * from watch ORDER BY id LIMIT 50 OFFSET ${offset}");

            while(rs.next())
            {
               File f = rs.getString("filename") as File
               if(!f.exists())
               {
                  println "FILE NO LONG EXISTS ${f}"
                  rs.deleteRow()
               }
               ++nResults
            }

            if(nResults < 50) keepGoing = false
            offset += nResults
         }

         stat.close()
      }

      true
   }

   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = (DirWatchData) sdi
      meta = (DirWatchMeta) smi

      return super.init(smi, sdi)
   }

   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      data.conn?.close()

      data = null
      meta = null

      super.dispose(smi, sdi)
   }

}
