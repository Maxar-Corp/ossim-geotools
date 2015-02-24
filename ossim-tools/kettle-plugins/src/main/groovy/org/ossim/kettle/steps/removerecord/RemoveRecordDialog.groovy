package org.ossim.kettle.steps.removerecord;

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
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.ossim.omar.utilities.KettleUtilities
import org.ossim.kettle.utilities.SwtUtilities
import org.eclipse.swt.widgets.MessageBox;
import org.ossim.omar.hibernate.Hibernate;
import org.hibernate.ScrollMode
import org.pentaho.di.ui.core.widget.ColumnInfo
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.widgets.TableItem

public class RemoveRecordDialog extends BaseStepDialog implements
		StepDialogInterface {
	private static Class<?> PKG = RemoveRecordMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private RemoveRecordMeta input
	private def databaseMeta
	private def swt
	private gotPreviousFields = false

	RemoveRecordDialog(Shell parent, Object baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (RemoveRecordMeta)baseStepMeta;
	}
	String open() {
		Shell parent    = getParent();
		Display display = parent.getDisplay();
		swt = new KettleSwtBuilder()

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
   	def lsMod = { event -> input.setChanged()
   						println "${event.class}\n${event}"
   				   } as ModifyListener

		def ciKey = new ColumnInfo[3];
		ciKey[0] = new ColumnInfo("Table Field",  ColumnInfo.COLUMN_TYPE_CCOMBO, ["id"] as String[], false); //$NON-NLS-1$
		ciKey[1] = new ColumnInfo("Comparator",   ColumnInfo.COLUMN_TYPE_TEXT,["="]as String[], true)//ColumnInfo.COLUMN_TYPE_CCOMBO, ["="] as String[]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		ciKey[2] = new ColumnInfo("Input Field",  ColumnInfo.COLUMN_TYPE_CCOMBO, SwtUtilities.previousStepFields(transMeta, stepname) as String[], false); //$NON-NLS-1$

		shell = swt.shell(parent){
			//rowLayout(type:"VERTICAL")
			//swt.composite(it){
			    migLayout(layoutConstraints:"", columnConstraints: "[grow]")

				group(layoutData:"grow, span, wrap"){
					migLayout(layoutConstraints:"insets 2", columnConstraints: "[] [grow,50:100:200] [] []")
					label Messages.getString("RemoveRecordDialog.stepname.Label")
					text(id:"stepName", layoutData:"span,growx", text: stepname){
						onEvent(type:'Modify') { input.setChanged() }
					}
					label Messages.getString("RemoveRecordDialog.connection.Label")
					cCombo(id:"connectionList", layoutData:"growx", items:transMeta.databaseNames){
						onEvent(type:'Selection'){
						   int idx = swt.connectionList.indexOf(swt.connectionList.text);
						   databaseMeta = transMeta.getDatabase(idx)
						}
					}	
					button(id:"editConnection", 
						   text:Messages.getString("RemoveRecordDialog.editConnection.Label"),
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
						   text:Messages.getString("RemoveRecordDialog.newConnection.Label"),
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
						text:Messages.getString("RemoveRecordDialog.outputResultCheckbox.Label"),
						selection:true){
						onEvent(type:"Selection"){
							input.setChanged()
							swt.resultFieldName.enabled = swt.outputResultCheckbox.selection
						}
					}
					text(id:"resultFieldName", text:"", layoutData:"growx, wrap")
					{
						onEvent(type:'Modify') { input.setChanged() }
					}
					label Messages.getString("RemoveRecordDialog.BatchSize.Label")
					text(id:"batchSize", 
						   //items://SwtUtilities.previousStepFields(transMeta, stepname), 
						   layoutData:"span,growx")
					{
						onEvent(type:'Modify') { input.setChanged() }
						//onEvent(type:'FocusIn') { input.setChanged(); }
					}

			   }// group common settings

				group(id:"useInputGroup",layoutData:"grow, spanx, wrap"){
					migLayout(layoutConstraints:"insets 2", columnConstraints: "[] [grow,50:100:200] [] []")
					label Messages.getString("RemoveRecordDialog.TableName.Label")
					cCombo(id:"tableName", 
						items:["RasterEntry", "RasterEntryFile", "VideoDataSet","VideoFile","RasterDataSet", "RasterFile"],
						   //items://SwtUtilities.previousStepFields(transMeta, stepname), 
						   layoutData:"span,growx, wrap")
					{
						onEvent(type:'Modify') { input.setChanged() 
						}
						//onEvent(type:'FocusIn') { input.setChanged(); }
					}
					checkBox(text:Messages.getString("RemoveRecordDialog.TableNameFromFieldCheckbox.Label"),
					           id:"tableNameFromFieldCheckbox",
						layoutData:"span, growx, wrap"){
						onEvent(type:'Selection'){
							input.setChanged()
							showAndHideFields()
						}
					}
					label Messages.getString("RemoveRecordDialog.TableNameFromField.Label")
					cCombo(id:"tableNameFromField", 
						    items:SwtUtilities.previousStepFields(transMeta, stepname), 
						    layoutData:"grow,span,wrap"){
						onEvent(type:'Modify'){
							input.setChanged()
						}
					}
					tableView(id:"fieldMapping",
						transMeta:transMeta,
								 nrRows:1,
								 columnInfo:ciKey,
								 style:"BORDER,FULL_SELECTION,MULTI,V_SCROLL,H_SCROLL",
								 propsUi:props,
								 layoutData:"flowx,span,growx",
								 modifyListener:lsMod){
						//columnInfo(name:"What", type:"COLUMN_TYPE_CCOMBO"){

						//}
					}
			   }
				group(layoutData:"grow, span, wrap"){
					migLayout(layoutConstraints:"insets 2", columnConstraints: "[] [grow,50:100:200] [] []")
					button(id:"okButton", Messages.getString("RemoveRecordDialog.ok.Label"), 
						   layoutData:"align center,skip 1,split 2"){
						onEvent(type:"Selection"){ok()}
					}
					button(id:"cancelButton", Messages.getString("RemoveRecordDialog.cancel.Label"), layoutData:""){
						onEvent(type:"Selection"){cancel()}
					}

				}
		}
		props.setLook(shell)

		changed = input.hasChanged();

		shell.text = Messages.getString("RemoveRecordDialog.Shell.Title")
		getData(); // initialize data fields
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
		swt.outputResultCheckbox.selection = input.outputResultFlag
		swt.resultFieldName.enabled        = swt.outputResultCheckbox.selection
		swt.resultFieldName.text           = input.resultFieldName
		swt.batchSize.text  = "${input.batchSize}".toString()
		swt.tableName.text  = input.tableName
		swt.tableNameFromFieldCheckbox.selection = input.tableNameFromFieldFlag
		swt.tableNameFromField.text = input.tableNameFromField
		def tableView = swt.fieldMapping
		tableView.table.clearAll()
		tableView.table.setItemCount(1)
		TableItem item = tableView.table.getItem(0);
		item.setText(1, input.columnName);
		item.setText(2, "=");
		item.setText(3, input.keyFieldName);


		showAndHideFields()
	}
	private showAndHideFields()
	{
		if(swt.tableNameFromFieldCheckbox.selection)
		{
			swt.tableName.enabled          = false
			swt.tableNameFromField.enabled = true
		}
		else
		{
			swt.tableName.enabled          = true
			swt.tableNameFromField.enabled = false

		}
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
	private def testHibernate()
	{
		if(!databaseMeta.driverClass.contains("postgres"))
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.message = Messages.getString("RemoveRecordDialog.InvalidConnection.DialogMessage"); //$NON-NLS-1$
			mb.text = Messages.getString("RemoveRecordDialog.InvalidConnection.DialogTitle") //$NON-NLS-1$
			mb.open()
			return false
		}
		else
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

				def mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.message = Messages.getString("RemoveRecordDialog.InvalidSession.DialogMessage"); 
				mb.text    = Messages.getString("RemoveRecordDialog.InvalidSession.DialogTitle") 
				mb.open()
				session?.close()
				testHibernate?.shutdown()

				return false
			}
			finally{
				session?.close()
				testHibernate.shutdown()
			}
		}

		true
	}
	private void ok()
	{
		def batchSize      = swt.batchSize.text.toInteger()
		if(batchSize < 1)
		{
			displayError(Messages.getString("RemoveRecordDialog.BadBatchSize.DialogMessage"),
				          Messages.getString("RemoveRecordDialog.BadBatchSize.DialogTitle"))
			return 
		}
		if (Const.isEmpty(swt.stepName.text)||!testHibernate()) return;
		input.setDatabaseMeta(databaseMeta)
		stepname               = swt.stepName.text
		input.outputResultFlag = swt.outputResultCheckbox.selection
		input.resultFieldName  = swt.resultFieldName.text

		input.tableName              = swt.tableName.text
		input.batchSize              = batchSize>0?batchSize:1
		input.tableNameFromFieldFlag = swt.tableNameFromFieldCheckbox.selection
		input.tableNameFromField     = swt.tableNameFromField.text


		def table= swt.fieldMapping.table
		if(table.itemCount>0)
		{
			TableItem item = table.getItem(0)
			input.columnName = item.getText(1)
			input.keyFieldName = item.getText(3)
		}

		dispose();
	}

	private void getInfo(RemoveRecordMeta meta, boolean preview)
	{
		meta.setDatabaseMeta(databaseMeta)
		meta.outputResultFlag = swt.outputResultCheckbox.selection
		meta.resultFieldName = swt.resultFieldName.text
		meta.previewMode = preview
	}

	private void preview()
	{
		if (Const.isEmpty(swt.stepName.text)||!testHibernate()) return;

		def stepName = swt?.stepName?.text
	  // Create the table input reader step...
	  RemoveRecordMeta oneMeta = new RemoveRecordMeta();
	  getInfo(oneMeta, true);
	  
	  TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, 
	  	                                                                         oneMeta, 
	  	                                                                         swt?.stepName?.text);
	  
	  EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), 
	  	                                    Messages.getString("RemoveRecordDialog.EnterPreviewSize"), 
	                                       Messages.getString("RemoveRecordDialog.NumberOfRowsToPreview")); //$NON-NLS-1$ //$NON-NLS-2$
	  int previewSize = numberDialog.open();
	  if (previewSize>0)
	  {
	      TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, 
	      	                                                                        [stepName] as String[], 
	      	                                                                        [previewSize] as Integer [] );
	      progressDialog.open();

	      Trans trans = progressDialog.getTrans();
	      String loggingText = progressDialog.getLoggingText();

	      if (!progressDialog.isCancelled())
	      {
	          if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
	          {
	          	EnterTextDialog etd = new EnterTextDialog(shell, 
	          		"Error",  
	          			"Preview Error", loggingText, true );
	          	etd.setReadOnly();
	          	etd.open();
	          } 
	          else
	          {
	              PreviewRowsDialog prd =new PreviewRowsDialog(shell, 
	              	                                            transMeta, 
	              	                                            SWT.NONE, 
	              	                                            stepName, 
	              	                                            progressDialog.getPreviewRowsMeta(stepName), 
	              	                                            progressDialog.getPreviewRows(stepName), loggingText);
	              prd.open();
	          }
	      }
	      
	      
		}
	}

}

