package org.ossim.kettle.steps.datainfoindexer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.CCombo;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.ossim.omar.utilities.KettleUtilities
import org.ossim.kettle.utilities.SwtUtilities
import org.eclipse.swt.widgets.MessageBox;
import org.ossim.omar.hibernate.Hibernate;
import org.hibernate.ScrollMode

public class DataInfoIndexerDialog extends BaseStepDialog implements
		StepDialogInterface {

	private DataInfoIndexerMeta input
	private def databaseMeta
	private def swt
	private gotPreviousFields = false

	public DataInfoIndexerDialog(Shell parent, Object baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (DataInfoIndexerMeta)baseStepMeta;
	}
	public String open() {

		Shell parent    = getParent();
		Display display = parent.getDisplay();
		swt = new groovy.swt.SwtBuilder()

		RowMetaInterface inputfields = null;
		try
		{
			inputfields = transMeta.getPrevStepFields(stepname);
		}
		catch(KettleException ke)
		{
			inputfields = new RowMeta();
			new ErrorDialog(shell, BaseMessages.getString(PKG, "FilterRowsDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "FilterRowsDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
		shell = swt.shell(parent){
			//rowLayout(type:"VERTICAL")
			//swt.composite(it){
			    migLayout(layoutConstraints:"", columnConstraints: "[] [grow,50:100:200] [] []")
		    	//gridLayout(numColumns: 2)
				label Messages.getString("DataInfoIndexerDialog.stepname.Label")
				text(id:"stepName", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("DataInfoIndexerDialog.dataInfoFieldName.Label")
				cCombo(id:"dataInfoFieldName", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname), 
					   layoutData:"span,growx")
				{
					onEvent(type:'FocusIn') { input.setChanged(); }
				}
				label Messages.getString("DataInfoIndexerDialog.connection.Label")
				cCombo(id:"connectionList", layoutData:"growx", items:transMeta.databaseNames){
					onEvent(type:'Selection'){
					   int idx = swt.connectionList.indexOf(swt.connectionList.text);
					   databaseMeta = transMeta.getDatabase(idx)
					}
				}	
				button(id:"editConnection", 
					   text:Messages.getString("DataInfoIndexerDialog.editConnection.Label"),
					layoutData:"growx"){
					onEvent(type:"Selection"){
						int idx = swt.connectionList.indexOf(swt.connectionList.text);
						if(databaseMeta)
						{
							DatabaseDialog cid = getDatabaseDialog(shell);
							cid.setDatabaseMeta(databaseMeta);
							cid.setModalDialog(true);
							if (cid.open() != null) {
								input.setChanged()
							}
						}
					}
				}
				button(id:"newConnection", 
					   text:Messages.getString("DataInfoIndexerDialog.newConnection.Label"),
				       layoutData:"growx,wrap"){
					onEvent(type:"Selection"){
						DatabaseMeta databaseMetaTemp = new DatabaseMeta();
						databaseMetaTemp.shareVariablesWith(transMeta);
						DatabaseDialog cid = getDatabaseDialog(shell);
						cid.setDatabaseMeta(databaseMetaTemp);
						cid.setModalDialog(true);
						if (cid.open() != null) {
							databaseMeta = databaseMetaTemp
							transMeta.addDatabase(databaseMeta);
							swt.connectionList.removeAll()
							swt.connectionList.items = transMeta.databaseNames
							int idx = swt.connectionList.indexOf(databaseMeta.name);
							idx<0?:swt.connectionList.select(idx)
						}
					}
				}
				checkBox(id:"outputResultCheckbox",
					text:Messages.getString("DataInfoIndexerDialog.outputResultCheckbox.Label"),
					selection:true){
					onEvent(type:"Selection"){
						input.setChanged()
						enableDisableControls()
						//swt.resultColumnName.enabled = swt.outputResultCheckbox.selection
					}
				}

				label(text:Messages.getString("DataInfoIndexerDialog.resultIdFieldName.Label"), 
					layoutData:"split 2")

				//text(id:"resultColumnName", text:"", layoutData:"growx, wrap")
				text(id:"resultIdFieldName", text:"", layoutData:"growx")
				{
					onEvent(type:'Modify') { input.setChanged() }
				}
				label(text:Messages.getString("DataInfoIndexerDialog.resultTableFieldName.Label"), 
					   layoutData:"split 2")

				text(id:"resultTableFieldName", text:"", layoutData:"growx, wrap")
				{
					onEvent(type:'Modify') { input.setChanged() }
				}

				label(text:Messages.getString("DataInfoIndexerDialog.indexingMode.Label"))
				checkBox(id:"indexingModeAddFlag", text:Messages.getString("DataInfoIndexerDialog.indexingModeAdd.Label"),
							layoutData:"split 2")
				checkBox(id:"indexingModeUpdateFlag",text:Messages.getString("DataInfoIndexerDialog.indexingModeUpdate.Label"),
							layoutData:"wrap")
				
				label Messages.getString("DataInfoIndexerDialog.checkIfExists.Label") 
				checkBox(id:"checkIfAlreadyExistsFlag", text:"", layoutData:"wrap")

				label Messages.getString("DataInfoIndexerDialog.batchSize.Label")
				text(id:"batchSize", text:"", layoutData:"growx, wrap")
				{
					onEvent(type:'Modify') { input.setChanged() }
				}

				label Messages.getString("DataInfoIndexerDialog.repositoryField.Label")
				cCombo(id:"repositoryField", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname), 
					   layoutData:"span,growx")
				{
					onEvent(type:'FocusIn') { input.setChanged(); }
				}

				checkBox(id:"validateCheckbox",
					text:Messages.getString("DataInfoIndexerDialog.validateCheckbox.Label"),
					selection:true){
				}
				button(Messages.getString("DataInfoIndexerDialog.ok.Label"), 
					   layoutData:"align center,skip 1,split 2"){
					onEvent(type:"Selection"){ok()}
				}
				button(Messages.getString("DataInfoIndexerDialog.cancel.Label"), layoutData:""){
					onEvent(type:"Selection"){cancel()}
				}
			//}
		}
		changed = input.hasChanged();

		shell.text = Messages.getString("DataInfoIndexerDialog.Shell.Title")
		getData(); // initialize data fields
		enableDisableControls();
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;
	}

	public void getData()
	{
		databaseMeta = input.getDatabaseMeta()
		swt.stepName.selectAll();
		if(databaseMeta?.name)
		{
			int idx = swt.connectionList.indexOf(databaseMeta.name);
			idx<0?:swt.connectionList.select(idx)
		}
		int idx = swt.dataInfoFieldName.indexOf(input.getDataInfoFieldName())
		if(idx >=0) swt.dataInfoFieldName.select(idx)
		swt.outputResultCheckbox.selection = input.outputResultFlag
		swt.indexingModeAddFlag.selection  = input.indexingModeAddFlag
		swt.indexingModeUpdateFlag.selection   = input.indexingModeUpdateFlag
		swt.checkIfAlreadyExistsFlag.selection = input.checkIfAlreadyExistsFlag
		swt.resultIdFieldName.text    = input.resultIdFieldName
		swt.resultTableFieldName.text = input.resultTableFieldName
		swt.repositoryField.text = input.repositoryField
		//swt.resultColumnName.enabled = swt.outputResultCheckbox.selection
		//swt.resultColumnName.text = input.resultFieldName
		swt.batchSize.text = "${input.batchSize}".toString()
	}
	private void enableDisableControls(){
		swt.resultIdFieldName.enabled    = swt.outputResultCheckbox.selection
		swt.resultTableFieldName.enabled = swt.outputResultCheckbox.selection
	}
	private void cancel()
	{
		stepname=null;
		
		input.setChanged(changed);

		dispose();
	}
	private displayError(def title, def message)
	{
		MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
		mb.message = message //$NON-NLS-1$
		mb.text = title //$NON-NLS-1$
		mb.open()
	}
	private void ok()
	{
		def canDispose = true
		if (Const.isEmpty(swt.stepName.text)) return;

		if(!databaseMeta?.driverClass?.contains("postgres"))
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.message = Messages.getString("DataInfoIndexerDialog.InvalidConnection.DialogMessage"); //$NON-NLS-1$
			mb.text = Messages.getString("DataInfoIndexerDialog.InvalidConnection.DialogTitle") //$NON-NLS-1$
			mb.open()
		}
		else if (swt.validateCheckbox.selection)  // if validation is enabled
		{
			def testHibernate
			def session
			try {
				testHibernate = new Hibernate()
				testHibernate.initialize(databaseMeta)

				def sessionFactory = testHibernate.sessionFactory
				session = sessionFactory?.openSession()
			}
			catch(def e) {
				println e.printStackTrace()

				def mb     = new MessageBox(shell, SWT.YES | SWT.NO|SWT.ICON_ERROR )
				mb.message = Messages.getString("DataInfoIndexerDialog.InvalidSession.DialogMessage") 
				mb.text    = Messages.getString("DataInfoIndexerDialog.InvalidSession.DialogTitle") 
				def buttonSelected = mb.open()
				session?.close()
				testHibernate?.shutdown()
				if(buttonSelected == SWT.NO)
				{
					// let's stop execution and do not close the editor so we can make changes.
					return
				}
			}
			finally{
				session?.close()
				testHibernate.shutdown()
			}
		}
		stepname = swt.stepName.text
		input.setDatabaseMeta(databaseMeta)
		input.setDataInfoFieldName(swt.dataInfoFieldName.text)
		input.outputResultFlag         = swt.outputResultCheckbox.selection
		input.indexingModeAddFlag      = swt.indexingModeAddFlag.selection
		input.indexingModeUpdateFlag   = swt.indexingModeUpdateFlag.selection
		input.checkIfAlreadyExistsFlag = swt.checkIfAlreadyExistsFlag.selection
		input.resultIdFieldName        = swt.resultIdFieldName.text
		input.resultTableFieldName     = swt.resultTableFieldName.text
		input.batchSize = swt.batchSize.text.toInteger()
		input.repositoryField = swt.repositoryField.text
		
		dispose();
	}

}

