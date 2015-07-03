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

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp

class DirWatch extends BaseStep implements StepInterface
{
   private DirWatchMeta meta = null;
   private DirWatchData data = null;
   private Boolean needsToScan = false
   private Long timeBetweenScans = 10
   private Long timeDeltaForNotification = 10
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
   private addContext(String directoryToWatch)
   {
      data.newContext(directoryToWatch)
   }
   private void indexOrUpdateFile(DirectoryContext context, File file,
                                  Statement stat, PreparedStatement pstmt)
   {
      Boolean skipFile = false
      if(context.connectionName&&file?.name?.startsWith(context.connectionName))
      {
         skipFile = true
      }
      else
      {
         if(context.includeRegEx)
         {
            Boolean matches = file.toString() ==~ ~/${context.includeRegEx}/
            skipFile = !matches
         }
         else if(context.excludeRegEx)
         {
            Boolean matches = file.toString() ==~ ~/${context.excludeRegEx}/
            skipFile = matches
         }
      }
      if(!skipFile)
      {
         ResultSet rs;
         rs = stat.executeQuery("select * from ${context.tableName} where filename='${file}'".toString());
         if(!rs.first())
         {
            Long currentFileLength = file.length()
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
            pstmt.setBigDecimal(2,currentFileLength)
            pstmt.setBigDecimal(3,currentFileLength)
            pstmt.setTimestamp(4, new Timestamp(file.lastModified()))//new Timestamp(new Date().time))
            pstmt.setBoolean(5, notified)
            pstmt.executeUpdate()//"INSERT INTO watch(filename, filesize, last_modified, notified) values('${file}',${file.length()},'${timeStamp}', false)".toString());
         }
         else if(!rs.getBoolean("notified"))
         {
            Long currentFileLength = file.length()
            rs.updateTimestamp("last_modified", new Timestamp(file.lastModified()))
            rs.updateBigDecimal("filesize",currentFileLength)

            // last_modified is here till I support change compares.  Right now we are
            // looking for timestamp modifications
            //
            rs.updateBigDecimal("last_filesize",currentFileLength )

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
         }
      }
   }
   private void scanDirectory(DirectoryContext context)
   {
      File directoryToWatch = context.directory as File
      if(directoryToWatch?.exists())
      {
         Statement stat = context.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
         String INSERT_RECORD = "insert into ${context.tableName}(filename, filesize, last_filesize, last_modified, notified) values(?, ?, ?, ?, ?)";
         PreparedStatement pstmt = context.conn.prepareStatement(INSERT_RECORD);

         if(context.recurseDirectories)
         {
            directoryToWatch.eachFileRecurse(FileType.FILES) {file->
               indexOrUpdateFile(context, file, stat, pstmt)
            }
         }
         else
         {
            directoryToWatch.eachFile(FileType.FILES) {file->
               indexOrUpdateFile(context, file, stat, pstmt)
            }
         }

         stat.close()
         pstmt?.close()
      }
   }
   private void synchIndexedFiles(DirectoryContext context)
   {
      // now purge any invalid files that were indexed
      //
      Statement stat = context.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

      ResultSet rs;
      Long offset=0
      Long nResults = 0
      Boolean keepGoing = true
      while(keepGoing)
      {
         nResults = 0
         rs = stat.executeQuery("select * from ${context.tableName} ORDER BY id LIMIT 50 OFFSET ${offset}");

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

      stat?.close()
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow();

      if (first)
      {
          if(r == null)
          {
             setOutputDone()
             return false
          }

         first=false

         data.outputRowMeta = new RowMeta()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
      }

      // if we get new rows and we are getting our directory information to watch from the
      // input fields then let's add a new watch context
      //
      if(r)
      {
         String directoryToWatch = getFieldValueAsString(meta.fieldFilename, r, meta, data) as File
         String includeRegEx = getFieldValueAsString(meta.fieldWildcard, r, meta, data)
         String excludeRegEx = getFieldValueAsString(meta.fieldWildcardExclude, r, meta, data)
         Boolean recurseDirectories = getFieldValueAsString(meta.fieldRecurseSubfolders, r, meta, data) as Boolean

         def settings = [
                 directory:directoryToWatch as File,
                 includeRegEx:includeRegEx,
                 excludeRegEx:excludeRegEx,
                 recurseDirectories:recurseDirectories,
                 memoryContext:true
         ]
         data.newContext(settings)
      }

      // scan for files
      Long deltaTime = (System.currentTimeMillis() - lastScanTime) / 1000
      if(deltaTime > timeBetweenScans)
      {
         needsToScan = true
      }

      if(needsToScan)
      {
         needsToScan = false
         data.managedDirectories.each{k,context->
            if(!context?.conn)
            {
               context.createConnection()
            }

            scanDirectory(context)
            lastScanTime = System.currentTimeMillis()
            synchIndexedFiles(context)
         }
      }

      // we will introdce a small delay
      // so we are not continually running
      System.sleep(1000)

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
      data.closeAll()

      data = null
      meta = null

      super.dispose(smi, sdi)
   }

}
