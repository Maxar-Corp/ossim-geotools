package org.ossim.kettle.steps.dirwatch

import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement


class DirWatchData extends BaseStepData implements StepDataInterface
{
   RowMetaInterface outputRowMeta
   File databaseLocation
   String databaseName = "DIR_WATCH_DB"
   File fullPath
   String driverClass = "org.h2.Driver"
   Connection conn

   DirWatchData()
   {
      super();
   }

   void initDb(File location)
   {
      Boolean newDB = false
      databaseLocation = location

      fullPath = new File(location, databaseName)
      conn?.close()
      if(!fullPath.exists())
      {
         File testFile = new File(databaseLocation, "${fullPath}.h2.db")


         if(!testFile.exists())
         {
            newDB = true
         }

         Class.forName(driverClass);
         conn = DriverManager.getConnection("jdbc:h2:${fullPath}");
         Statement stat = conn.createStatement();

         if(newDB)
         {
            stat.execute("create table IF NOT EXISTS watch(id bigint auto_increment, filename TEXT primary key, filesize BIGINT, last_modified TIMESTAMP, notified BOOLEAN)");
            stat.execute("CREATE INDEX IF NOT EXISTS idx_last_modified ON watch(last_modified)");
            stat.execute("CREATE INDEX IF NOT EXISTS idx_filename ON watch(filename)");
            stat.execute("CREATE INDEX IF NOT EXISTS idx_filesize ON watch(filesize)");
         }
         stat?.close()
      }
   }

   void closeDb()
   {
      conn?.close()
   }

}