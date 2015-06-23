package org.ossim.kettle.steps.dirwatch

import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class DirWatch extends BaseStep implements StepInterface
{
   private DirWatchMeta meta = null;
   private DirWatchData data = null;

   public DirWatch(StepMeta stepMeta, StepDataInterface stepDataInterface,
                         int copyNr, TransMeta transMeta, Trans trans) {
      super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
   }
   public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
   {
      Object[] r = getRow();
      if (r==null)
      {
         setOutputDone()
         return false
      }

      if (first)
      {

         /*
         Class.forName("org.h2.Driver");
         Connection conn = DriverManager.getConnection("jdbc:h2:~/DIR_WATCH_DB");
         Statement stat = conn.createStatement();
         // this line would initialize the database
         // from the SQL script file 'init.sql'
         // stat.execute("runscript from 'init.sql'");

         stat.execute("create table IF NOT EXISTS watch(id int primary key, name TEXT, last_modified TIMESTAMP)");
         stat.execute("MERGE INTO watch values(1, 'Hello','2015-10-10 12:34:45.1234')");
         ResultSet rs;
         rs = stat.executeQuery("select * from watch");
         while (rs.next()) {
            println "Name: ${rs.getString("name")}, Last modified = ${rs.getTimestamp("last_modified")}"

         }
         stat.close();
         conn.close();
           */

         first=false

         data.outputRowMeta = getInputRowMeta().clone()
         meta.getFields(data.outputRowMeta, getStepname(), null, null, this)
      }

      // For this template I am just copying the input row to the output row
      // You can pass your own information to the output
      //
      putRow(data.outputRowMeta, r);

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
      data = null
      meta = null

      super.dispose(smi, sdi)
   }

}
