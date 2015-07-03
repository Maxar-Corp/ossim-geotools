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

   DirectoryContext newContext(File directory, Boolean memoryContext=false)
   {
      String connectionName = memoryContext?"":"DIR_WATCH_DB"
      DirectoryContext result
      File fullPath = directory
      DirectoryContext context

      HashMap params = [
              id:UUID.randomUUID().toString(),
              tableName:"watch",
              connectionName:connectionName,
              //fullPath:fullPath,
              directory:directory,
              memoryContext:memoryContext
      ]

      // generate unique database
      //
      if(memoryContext)
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