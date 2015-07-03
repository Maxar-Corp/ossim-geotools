package org.ossim.kettle.steps.dirwatch

import com.sun.java.util.jar.pack.Driver

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

/**
 * Created by gpotts on 7/3/15.
 */
class DirectoryContext
{
   String id
   String tableName      = "watch"
   String connectionName = ""
   String driverClass    = "org.h2.Driver"
   String url            = ""
  // File fullPath
   File directory
   Boolean memoryContext = false
   Connection conn

   private void initTable()
   {
      Statement stat = conn.createStatement();

      stat.execute("create table IF NOT EXISTS ${tableName}(id bigint auto_increment, filename TEXT primary key, filesize BIGINT, last_filesize BIGINT, last_modified TIMESTAMP, notified BOOLEAN)");
      stat.execute("CREATE INDEX IF NOT EXISTS idx_last_modified ON ${tableName}(last_modified)");
      stat.execute("CREATE INDEX IF NOT EXISTS idx_filename ON ${tableName}(filename)");
      stat.execute("CREATE INDEX IF NOT EXISTS idx_filesize ON ${tableName}(filesize)");
      stat.execute("CREATE INDEX IF NOT EXISTS idx_last_filesize ON ${tableName}(last_filesize)");

      stat?.close()
   }
   void createConnection()
   {
      close()
      conn = DriverManager.getConnection(url)
   }
   void init()
   {
      createConnection()
      initTable()
   }

   void close()
   {
      conn?.close()
      conn = null
   }
}
