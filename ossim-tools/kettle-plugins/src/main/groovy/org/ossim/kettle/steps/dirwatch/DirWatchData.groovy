package org.ossim.kettle.steps.dirwatch

import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement


class DirWatchData extends BaseStepData implements StepDataInterface
{
   HashMap managedDirectories = [:]
   RowMetaInterface outputRowMeta

   DirWatchData()
   {
      super();
   }

   DirectoryContext newContext(HashMap settings)
   {
      String connectionName = settings?.memoryContext?"":"DIR_WATCH_DB"
      DirectoryContext result
      File directory = settings.directory as File
      File fullPath = directory
      DirectoryContext context

      HashMap params = settings

      params.id = UUID.randomUUID().toString()
      params.tableName = "watch"
      params.connectionName = connectionName
      // generate unique database
      //
      if(params?.memoryContext)
      {
         String databaseName = new String(params.id).replaceAll("-","_")

         params.url = "jdbc:h2:mem:${databaseName}"
      }
      else
      {
         fullPath = new File(directory, connectionName)
         params.url = "jdbc:h2:${fullPath}"
      }

      result = new DirectoryContext(params)
      result.init()

      managedDirectories."${params.id}" = result
    }

   void deleteContext(DirectoryContext context)
   {
      context?.close()
      managedDirectories.remove(context.id)
   }

   void closeAll()
   {
      managedDirectories.each{k,v->
         v?.close()
      }
   }

}