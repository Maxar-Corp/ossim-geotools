package org.ossim.kettle.steps.jobmessage;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.ossim.kettle.utilities.SwtUtilities
import org.pentaho.di.ui.core.widget.ColumnInfo
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.eclipse.swt.widgets.TableItem
import org.eclipse.swt.custom.CCombo
import org.eclipse.swt.widgets.MessageBox;
import org.ossim.core.MultiResolutionTileGenerator
import org.ossim.core.RabbitType

public class ProductMessageInputDialog extends BaseStepDialog implements
		StepDialogInterface {

	private ProductMessageInputMeta input;
	private def swt;
	private def stepsWeCanWatch = [] as String[];

	public ProductMessageInputDialog(Shell parent, Object baseStepMeta,
			                   TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (ProductMessageInputMeta)baseStepMeta;
	}
	public String open() {

		Shell parent    = getParent();
		Display display = parent.getDisplay();
		swt = new KettleSwtBuilder()
		//colinf[0] =
		//  new ColumnInfo( Messages.getString("JobMessageInputDialog.ColumnInfo.ExchangeType" ),
		//      ColumnInfo.COLUMN_TYPE_CCOMBO, RabbitType.ExchangeType.valuesAsString() as String [], 
		//      true );

   	def lsMod = { 
	   					event -> input.setChanged()
	   					def ccombo = event.source
	   					if(ccombo instanceof CCombo)
	   					{
		   					def tableView = event.source.parent.parent
		    					def row  = tableView?.getCurrentRownr()
		   					def item = event?.source?.parent?.getItem(row)
	   						//def indexOfValue = ccombo.indexOf(ccombo.text)
	   						def key = "${ccombo.text}"
	   						def renameValue          = input.outputFieldNames."${key}"

	   						item.setText(1, key)
	   						item.setText(2, renameValue)
			   			}
   				   } as ModifyListener

		ColumnInfo[] colinf = new ColumnInfo[2];
		colinf[0] =
		  new ColumnInfo( Messages.getString("JobMessageInputDialog.ColumnInfo.Fieldname" ),
		      ColumnInfo.COLUMN_TYPE_CCOMBO, input.outputFieldNames.collect{k,v->v}.sort() as String [], 
		      true );
		colinf[1] =
		  new ColumnInfo( Messages.getString("JobMessageInputDialog.ColumnInfo.RenameTo" ),
		      ColumnInfo.COLUMN_TYPE_TEXT, false );

  // 	def monitorStepsMod = {
//	   	event -> input.setChanged()
	   	//def src = event.source
//	   } as ModifyListener
//		ColumnInfo[] messageHandlerColinfo = new ColumnInfo[1];
//		messageHandlerColinfo[0] =
//		  new ColumnInfo( Messages.getString("JobMessageInputDialog.MessageHandlerColumnInfo.StepToMonitor" ),
//		      ColumnInfo.COLUMN_TYPE_CCOMBO, getStepsWeCanWatch(), 
//		      true );


		shell = swt.shell(parent){
			migLayout(layoutConstraints:"insets 2, wrap 1", columnConstraints: "[grow]")
		   // migLayout(layoutConstraints:"wrap 2", columnConstraints: "[] [grow,:200:]")
	    	//gridLayout(numColumns: 2)
			
	    	composite(layoutData:"growx, spanx, wrap"){
		   	migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")

				label Messages.getString("JobMessageInputDialog.Stepname.Label")
				text(id:"stepName", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}
			//	label Messages.getString("JobMessageInputDialog.MessageFieldName.Label")
		//		text(id:"messageFieldName", layoutData:"span,growx", text: ""){
	//				onEvent(type:'Modify') { input.setChanged() }
//				}

				label Messages.getString("JobMessageInputDialog.Host.Label")
				text(id:"host", layoutData:"span,growx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("JobMessageInputDialog.Port.Label")
				text(id:"port", layoutData:"span,growx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("JobMessageInputDialog.Username.Label")
				text(id:"username", layoutData:"span,growx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
				}

				label Messages.getString("JobMessageInputDialog.Password.Label")
				text(id:"password", layoutData:"span,growx", text: "", style:"PASSWORD"){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("JobMessageInputDialog.VerifyPassword.Label")
				text(id:"verifyPassword", layoutData:"span,growx", text: "", style:"PASSWORD"){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("JobMessageInputDialog.JobQueueName.Label")
			   	//migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] []")
				text(id:"jobQueueName", layoutData:"growx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
				}
				/*
				label Messages.getString("JobMessageInputDialog.JobQueueType.Label")
				cCombo(id:"jobQueueType", 
					   items:RabbitType.ExchangeType.valuesAsString() as String [], 
					   layoutData:"growx,spanx")
				{
					onEvent(type:'FocusIn') { input.setChanged(); }
				}
				*/
				label Messages.getString("JobMessageInputDialog.JobQueueStatusName.Label")
				text(id:"jobQueueStatusName", layoutData:"growx, spanx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
				}
/*				label Messages.getString("JobMessageInputDialog.JobQueueStatusType.Label")
				cCombo(id:"jobQueueStatusType", 
					   items:RabbitType.ExchangeType.valuesAsString() as String [], 
					   layoutData:"growx,spanx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
				*/
	    	}
			group(id:"outputFields",layoutData:"grow, spanx, wrap"){
				migLayout(layoutConstraints:"insets 2", columnConstraints: "[grow]")
				tableView(id:"fieldSelection",
							 transMeta:transMeta,
							 nrRows:1,
							 columnInfo:colinf,
							 style:"BORDER,FULL_SELECTION,MULTI,V_SCROLL,H_SCROLL",
							 propsUi:props,
//							 layoutData:"height 100:100:200, w 200:200:200, span,growx, wrap",
							 layoutData:"height 100:100:200, span,growx, wrap",
							 modifyListener:lsMod)
				button(id:"getAllFields", layoutData:"align center, split 2", text: Messages.getString("JobMessageInputDialog.getAllFields.label")){
					onEvent(type:"Selection"){
						loadAllFields()
					}
				}
				button(id:"clearAllFields", Messages.getString("JobMessageInputDialog.clearAllFields.label")){
					onEvent(type:"Selection"){
						clearAllFields()
					}
				}
			}
			composite(layoutData:"grow, span, wrap", style:"none"){
				migLayout(layoutConstraints:"insets 2", columnConstraints: "[grow]")
				button("Ok", layoutData:"align center,split 2"){
					onEvent(type:"Selection"){ok()}
				}
				button("Cancel", layoutData:""){
					onEvent(type:"Selection"){cancel()}
				}
			}
		}
		changed = input.hasChanged();
		shell.text = Messages.getString("JobMessageInputDialog.Shell.Title")
		getData(); // initialize data fields
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;
	}
	private void loadAllFields() {
		def fields = input.outputFieldNames
		def tableView = swt.fieldSelection
		tableView.table.clearAll()
		tableView.table.setItemCount(fields.size())
		def idx = 0
		fields.each{k,v->
			TableItem item = tableView.table.getItem(idx);
			item.setText(0, "${idx+1}" as String);
			item.setText(1, k);
			item.setText(2, v);
			++idx
		}
	}
	private void clearAllFields() {
		def tableView = swt.fieldSelection
		tableView.table.clearAll()
		tableView.table.setItemCount(1)
		tableView.table.getItem(0).setText(0, "1")
	}
	private void loadSelectedFields(){
		def fields = input.outputFieldNames
		def selectedFields = input.selectedFieldNames
		def tableView = swt.fieldSelection
		tableView.table.clearAll()
		tableView.table.setItemCount(selectedFields.size()+1)
		def idx = 0
		if(selectedFields)
		{
			selectedFields.each{key->
				def value = input.outputFieldNames."${key}"
				if(!value) value = ""

				TableItem item = tableView.table.getItem(idx)
				item.setText(0, "${idx+1}" as String)
				item.setText(1, key)
				item.setText(2, value)
				++idx
			}			
		}
		TableItem item = tableView.table.getItem(idx)
		item.setText(0, "${idx+1}" as String)
	}

	public void getData()
	{
//		swt.messageFieldName.text 	= input.messageFieldName
		swt.host.text 					 = input.host
		swt.port.text 					 = input.port
		swt.username.text 			 = input.username
		swt.password.text 			 = input.password
		swt.verifyPassword.text 	 = input.password

		swt.jobQueueName.text       = input.jobQueueName
		//swt.jobQueueType.text       = input.jobQueueType.toString()
		swt.jobQueueStatusName.text = input.jobQueueStatusName
		//swt.jobQueueStatusType.text = input.jobQueueStatusType.toString()

		swt.stepName.selectAll();
		loadSelectedFields();
	}
	private void cancel()
	{
		stepname=null;
		//input.setChanged(changed);

		dispose();
	}
	private void ok()
	{
		if (Const.isEmpty(swt.stepName.text)) return;

		stepname = swt.stepName.text

		if("${swt.password.text}" != "${swt.verifyPassword.text}")
		{
			def mb     = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.message = Messages.getString("JobMessageInputDialog.PasswordDontMatch.DialogMessage"); 
			mb.text    = Messages.getString("JobMessageInputDialog.PasswordDontMatch.DialogTitle") 
			mb.open()
			return
		}
		def tableView = swt.fieldSelection
		input.selectedFieldNames = [] as Set
		stepname = swt.stepName.text
		def itemCount = tableView.table.itemCount
		if(itemCount)
		{
			(0..itemCount-1).each{idx->
				TableItem item = tableView.table.getItem(idx);
				def itemText = "${item.getText(1)}".trim()

				if(itemText)
				{
					input.selectedFieldNames << item.getText(1)
					def rename = "${item.getText(2)}".trim() as String
					//println "WILL DO RENAME: ${item.getText(1)} = ${rename}"
					if(rename)
					{
						input.outputFieldNames."${item.getText(1)}" = rename
					}
				}
			}
		}

		input.port 					 = swt.port.text
		input.host 					 = swt.host.text
		input.username 			 = swt.username.text
		input.password 			 = swt.password.text

		//input.jobQueueType       = RabbitType.ExchangeType."${swt.jobQueueType.text}"
		input.jobQueueName       = swt.jobQueueName.text
		//input.jobQueueStatusType = RabbitType.ExchangeType."${swt.jobQueueStatusType.text}"
		input.jobQueueStatusName = swt.jobQueueStatusName.text

//		input.messageFieldName 	= swt.messageFieldName.text

		dispose();
	}

}

