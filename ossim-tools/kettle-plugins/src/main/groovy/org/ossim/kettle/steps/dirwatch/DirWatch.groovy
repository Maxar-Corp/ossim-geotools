package org.ossim.kettle.steps.dirwatch

import groovy.io.FileType
import org.ossim.kettle.steps.dirwatch.DirWatchData.FileDoneCompareType
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
   private DirWatchMeta meta = null
   private DirWatchData data = null
   private Long lastTimeChecked
   private Integer batchReadSize = 50
   private Boolean needsToScan = false
   private Double secondsBetweenScan = 10
   private Double secondsToFileDoneCompare = 10
   private Double lastScanTime = 0
   private FileDoneCompareType fileDoneCompareType
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

   private Boolean fileDoneTest(File file, BigInteger lastSizeCheck)
   {
      Boolean result = false

      switch(fileDoneCompareType)
      {
         case FileDoneCompareType.MODIFIED_TIME:
            Long modifiedMillis = file.lastModified()
            Double delta = (new Date().time - modifiedMillis)/1000
            result = delta >= secondsToFileDoneCompare
            break
         case FileDoneCompareType.FILE_SIZE:
            result = file.length() == lastSizeCheck
            break
         default:
            result = true
      }

      result
   }
   /*
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
         if(context.wildcard)
         {
            Boolean matches = file.toString() ==~ ~/${context.wildcard}/
            skipFile        = !matches
         }
         else if(context.wildcardExclude)
         {
            Boolean matches = file.toString() ==~ ~/${context.wildcardExclude}/
            skipFile        = matches
         }
      }

      if(!skipFile)
      {
         ResultSet rs;
         rs = stat.executeQuery("select * from ${context.tableName} where filename='${file}'".toString());
         if(!rs.first())
         {
            Long currentFileLength = file.length()
            pstmt.setString(1,file.toString())
            pstmt.setBigDecimal(2,currentFileLength)
            pstmt.setBigDecimal(3,currentFileLength)
            pstmt.setTimestamp(4, new Timestamp(file.lastModified()))//new Timestamp(new Date().time))
            pstmt.setTimestamp(5, new Timestamp(new Date().time))//new Timestamp(new Date().time))
            pstmt.setBoolean(6, false)
            pstmt.executeUpdate()//"INSERT INTO watch(filename, filesize, last_modified, notified) values('${file}',${file.length()},'${timeStamp}', false)".toString());
         }
         else if(!rs.getBoolean("notified"))
         {
            Long currentFileLength = file.length()
            rs.updateTimestamp("last_modified", new Timestamp(file.lastModified()))
            rs.updateBigDecimal("filesize",currentFileLength)

            Double delta = (new Date().time - rs.getTimestamp("last_checked").time)/1000
            if(delta >= secondsToFileDoneCompare)
            {
               if(fileDoneTest(file, rs.getBigDecimal("last_filesize") as BigInteger))
               {
                  rs.updateBoolean("notified", true)

                  putRow(data.outputRowMeta, [file] as Object[]);
               }

               rs.updateTimestamp("last_checked", new Timestamp(new Date().time))
            }
            // last_modified is here till I support change compares.  Right now we are
            // looking for timestamp modifications
            //
            rs.updateBigDecimal("last_filesize", currentFileLength)
            rs.updateRow()
         }
         else
         {
         }
      }
   }
   */
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
         if(context.wildcard)
         {
            Boolean matches = file.toString() ==~ ~/${context.wildcard}/
            skipFile        = !matches
         }
         else if(context.wildcardExclude)
         {
            Boolean matches = file.toString() ==~ ~/${context.wildcardExclude}/
            skipFile        = matches
         }
      }

      if(!skipFile)
      {
         ResultSet rs;
         rs = stat.executeQuery("select * from ${context.tableName} where filename='${file}'".toString());
         if(!rs.first())
         {
            Long currentFileLength = file.length()
            pstmt.setString(1,file.toString())
            pstmt.setBigDecimal(2,currentFileLength)
            pstmt.setBigDecimal(3,currentFileLength)
            pstmt.setTimestamp(4, new Timestamp(file.lastModified()))//new Timestamp(new Date().time))
            pstmt.setTimestamp(5, new Timestamp(new Date().time))//new Timestamp(new Date().time))
            pstmt.setBoolean(6, false)
            pstmt.executeUpdate()//"INSERT INTO watch(filename, filesize, last_modified, notified) values('${file}',${file.length()},'${timeStamp}', false)".toString());
         }
         else if(!rs.getBoolean("notified"))
         {
            Long currentFileLength = file.length()
            rs.updateTimestamp("last_modified", new Timestamp(file.lastModified()))
            rs.updateBigDecimal("filesize",currentFileLength)


            //Double delta = (new Date().time - rs.getTimestamp("last_checked").time)/1000
            //if(delta >= secondsToFileDoneCompare)
            //{
            //   if(fileDoneTest(file, rs.getBigDecimal("last_filesize") as BigInteger))
            //   {
            //      rs.updateBoolean("notified", true)
            //
            //      putRow(data.outputRowMeta, [file] as Object[]);
            //   }
            //
            //   rs.updateTimestamp("last_checked", new Timestamp(new Date().time))
            //}
            // last_modified is here till I support change compares.  Right now we are
            // looking for timestamp modifications
            //
            //rs.updateBigDecimal("last_filesize", currentFileLength)
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
         String INSERT_RECORD    = "insert into ${context.tableName}(filename, filesize, last_filesize, last_modified, last_checked, notified) values(?, ?, ?, ?, ?, ?)";
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
      try{
         while(keepGoing)
         {
            nResults = 0
            rs = stat.executeQuery("select * from ${context.tableName} ORDER BY id LIMIT ${batchReadSize} OFFSET ${offset}");

            while(rs.next())
            {
               File file = rs.getString("filename") as File
               if(!file.exists())
               {
                  rs.deleteRow()
                  logDebug("Unindexing ${file}.  File no longer exists.")
               }
               else
               {
                  Boolean notified = rs.getBoolean("notified")
                  // now lets see if we need to skip and unindex
                  //
                  if(!notified)
                  {

                     Boolean unindexFile = false
                     if(context.wildcard)
                     {
                        Boolean matches = file.toString() ==~ ~/${context.wildcard}/
                        unindexFile        = !matches
                     }
                     else if(context.wildcardExclude)
                     {
                        Boolean matches = file.toString() ==~ ~/${context.wildcardExclude}/
                        unindexFile        = matches
                     }
                     if(unindexFile)
                     {
                        rs.deleteRow()
                        logDebug("Unindexing ${file}.  File no longer matches wildcard settings")
                     }
                     else
                     {
                        // now check for notification only if it's time to
                        Double delta = (new Date().time - rs.getTimestamp("last_checked").time)/1000
                        if(delta >= secondsToFileDoneCompare)
                        {
                           if (fileDoneTest(file, rs.getBigDecimal("last_filesize") as BigInteger))
                           {
                              rs.updateBoolean("notified", true)

                              putRow(data.outputRowMeta, [file] as Object[]);
                           }
                           rs.updateTimestamp("last_checked", new Timestamp(new Date().time))
                           rs.updateBigDecimal("last_filesize", file.length())

                           rs.updateRow()
                        }
                     }
                  }
               }
               ++nResults
            }

            if(nResults < batchReadSize) keepGoing = false
            offset += nResults
         }
      }
      catch(e)
      {
         //println e
         logDebug("${e}".toString())
      }
      stat?.close()
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow();

      if (first)
      {
         String fileDoneCompareTypeString =  environmentSubstitute(meta.fileDoneCompareType)?:FileDoneCompareType.MODIFIED_TIME.toString()
         fileDoneCompareType = FileDoneCompareType."${fileDoneCompareTypeString}"
         secondsBetweenScan  = environmentSubstitute(meta.secondsBetweenScans).toDouble()
         secondsToFileDoneCompare = environmentSubstitute(meta.secondsToFileDoneCompare).toDouble()
         lastTimeChecked = System.currentTimeMillis()

         if(meta.fileInputFromField)
         {
            if(r == null)
            {
               setOutputDone()
               return false
            }
         }
         else
         {
             meta.fileDefinitions.each{fileDefinition->
                String recurceSubfoldersString = environmentSubstitute(fileDefinition.recurseSubfolders?:"")
                String useMemoryDatabaeString  = environmentSubstitute(fileDefinition.useMemoryDatabase?:"")
                String directoryToWatch        = environmentSubstitute(fileDefinition.filename?:"") as File
                String wildcard                = environmentSubstitute(fileDefinition.wildcard?:"")
                String wildcardExclude         = environmentSubstitute(fileDefinition.wildcardExclude?:"")
                Boolean recurseDirectories     = recurceSubfoldersString?recurceSubfoldersString.toBoolean():true
                Boolean useMemoryDatabase      = useMemoryDatabaeString?useMemoryDatabaeString.toBoolean():true
                def settings = [
                        directory:directoryToWatch as File,
                        wildcard:wildcard,
                        wildcardExclude:wildcardExclude,
                        recurseDirectories:recurseDirectories,
                        useMemoryDatabase:useMemoryDatabase
                ]
                data.newContext(settings)
             }
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
         if(meta.fileInputFromField)
         {
            String  directoryToWatch   = getFieldValueAsString(meta.fieldFilename, r, meta, data) as File
            String  wildcard           = getFieldValueAsString(meta.fieldWildcard, r, meta, data)
            String  wildcardExclude    = getFieldValueAsString(meta.fieldWildcardExclude, r, meta, data)
            Boolean recurseDirectories = getFieldValueAsString(meta.fieldRecurseSubfolders, r, meta, data) as Boolean

            def settings = [
                    directory:directoryToWatch as File,
                    wildcard:wildcard,
                    wildcardExclude:wildcardExclude,
                    recurseDirectories:recurseDirectories,
                    useMemoryDatabase:true
            ]
            data.newContext(settings)
         }
      }

      // scan for files
      Double deltaTime = (System.currentTimeMillis() - lastScanTime) / 1000
      if(deltaTime > secondsBetweenScan)
      {
         needsToScan = true
      }

      if(needsToScan)
      {
         needsToScan = false
         data.managedDirectories.each{String k, DirectoryContext context->
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
