package org.ossim.kettle.steps.dirwalk

import groovy.io.FileType
import groovy.io.FileVisitResult
import org.ossim.kettle.steps.dirwatch.DirWatchData.FileDoneCompareType
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.row.RowMeta
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.*

import java.nio.file.FileVisitor
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp

class DirWalk extends BaseStep implements StepInterface
{
   private DirWalkMeta meta = null
   private DirWalkData data = null
   public DirWalk(StepMeta stepMeta, StepDataInterface stepDataInterface,
                  int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   private String getFieldValueAsString(String fieldValue,
                                        def r,
                                        DirWalkMeta meta,
                                        DirWalkData data)
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

   private void scan(HashMap settings)
   {
      def traverseSettings = [:]

      if(settings.directory)
      {
         File fileToTraverse = settings.directory as File
         if(settings.wildcard) traverseSettings.nameFilter = ~/${settings.wildcard}/
         if(settings.wildcardExclude) traverseSettings.excludeNameFilter = ~/${settings.wildcardExclude}/
         if(settings.recurseDirectories) traverseSettings.maxDepth = null
         else traverseSettings.maxDepth = 0
         String fileType = settings.fileType?:""
         fileType = fileType.toLowerCase()
         if(fileType.contains("all")) traverseSettings.type = FileType.ANY
         else if(fileType.contains("file")) traverseSettings.type = FileType.FILES
         else if(fileType.contains("dir")) traverseSettings.type = FileType.DIRECTORIES
         else traverseSettings.type = FileType.FILES

         fileToTraverse.traverse(traverseSettings){file->
            if((isStopped())&&(this.status == BaseStepData.StepExecutionStatus.STATUS_HALTING))
            {
               return FileVisitResult.TERMINATE
            }
            putRow(data.outputRowMeta, [file.toString()] as Object[]);
         }
      }
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      boolean result = true
      Object[] r = getRow();

      if (first)
      {
         first=false
         data.outputRowMeta = new RowMeta()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
      }


      // if we get new rows and we are getting our directory information to watch from the
      // input fields then let's add a new watch context
      //
      if(meta.fileInputFromField)
      {
         if(r)
         {
            String  directory          = getFieldValueAsString(meta.fieldFilename, r, meta, data) as File
            String  wildcard           = getFieldValueAsString(meta.fieldWildcard, r, meta, data)
            String  wildcardExclude    = getFieldValueAsString(meta.fieldWildcardExclude, r, meta, data)
            Boolean recurseDirectories = getFieldValueAsString(meta.fieldRecurseSubfolders, r, meta, data) as Boolean
            String  fileType           = getFieldValueAsString(meta.fieldFileType, r, meta, data)

            def settings = [
                    directory:directory as File,
                    wildcard:wildcard,
                    wildcardExclude:wildcardExclude,
                    recurseDirectories:recurseDirectories,
                    fileType:fileType?:"FILE"
            ]
            scan(settings)

            if((isStopped())&&(this.status == BaseStepData.StepExecutionStatus.STATUS_HALTING))
            {
               setOutputDone()
               return false
            }
            result = true
         }
         else
         {
            setOutputDone()
            result = false
         }
      }
      else
      {
         meta?.fileDefinitions?.each{fileDefinition->
            String recurceSubfoldersString = environmentSubstitute(fileDefinition.recurseSubfolders?:"")
            String directory               = environmentSubstitute(fileDefinition.filename?:"") as File
            String wildcard                = environmentSubstitute(fileDefinition.wildcard?:"")
            String wildcardExclude         = environmentSubstitute(fileDefinition.wildcardExclude?:"")
            Boolean recurseDirectories     = recurceSubfoldersString?recurceSubfoldersString.toBoolean():true
            String fileType                = environmentSubstitute(fileDefinition.fileType?:"FILE")
            def settings = [
                    directory:directory as File,
                    wildcard:wildcard,
                    wildcardExclude:wildcardExclude,
                    recurseDirectories:recurseDirectories,
                    fileType:fileType
            ]
            scan(settings)
            if((isStopped())&&(this.status == BaseStepData.StepExecutionStatus.STATUS_HALTING))
            {
               setOutputDone()
               return false
            }


         }
         setOutputDone()
         result = false;
      }

      result
   }

   public boolean init(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = (DirWalkData) sdi
      meta = (DirWalkMeta) smi

      return super.init(smi, sdi)
   }

   public void dispose(StepMetaInterface smi, StepDataInterface sdi)
   {
      data = null
      meta = null

      super.dispose(smi, sdi)
   }

}
