package org.ossim.kettle.steps.createovrhst;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Runtime;
import java.lang.Process;

public class CreateOvrHst extends BaseStep implements StepInterface {
   private CreateOvrHstMeta meta = null;
   private CreateOvrHstData data = null;

	
	public CreateOvrHst(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		// TODO Auto-generated constructor stub
	}

	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (CreateOvrHstMeta) smi;
		data = (CreateOvrHstData) sdi;
		
		Object[] row = getRow();
		if (row==null) {
			setOutputDone();
			return false;
		}
		
		if (first) 
      {
			first=false;
			
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			// Find the index of the short_filename column, throw an exception if it's not there
	        //
		}
      int filenameIndex = data.outputRowMeta.indexOfValue("filename"); // returns -1 if index not found
      if (filenameIndex<0) {
         throw new KettleException("filename not found in the input -- previous step must provide it");
      } 
        // Check for the files we want to process
        //
        String filename = getInputRowMeta().getString(row, filenameIndex);
        String ossim_cmd = "ossim-preproc -o --ch "+filename;
        
        try {
	   	    Runtime runtime = Runtime.getRuntime();
     		logBasic("Executing: "+ossim_cmd);
            Process process = runtime.exec(ossim_cmd);
    	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    	        String line;    
    	        while ((line = bufferedReader.readLine()) !=null)  logBasic(line);
    	        
    	        putRow( data.outputRowMeta, row);
         
        } catch (IOException e) {
    	         logBasic("Oops...exec() didn't work. Ignoring: "+filename);
             e.printStackTrace();
   	    }

      	return true; // finished with this row, process the next row
	}
  public boolean init(StepMetaInterface smi, StepDataInterface sdi)
  {
    data = (CreateOvrHstData) sdi;
    meta = (CreateOvrHstMeta) smi;

    return super.init(smi, sdi);
  }

  public void dispose(StepMetaInterface smi, StepDataInterface sdi)
  {
      data = null;
      meta = null;

      super.dispose(smi, sdi);
  }

  public void run()
  {
    try
    {
      while (processRow(meta, data) && !isStopped());
    }
    catch(Exception e)
    {
      logError("Unexpected error : "+e.toString());
      logError(Const.getStackTracker(e));
      setErrors(1);
      stopAll();
    }
    finally
    {
      dispose(meta, data);
      logBasic("Finished, processing "+linesRead+" rows");
      markStop();
    }
  }
}
