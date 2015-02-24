package org.ossim.kettle.steps.amqp;

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

public class RabbitMQOutputDialog extends BaseStepDialog implements
		StepDialogInterface {

	private RabbitMQOutputMeta input;
	private def swt;

	public RabbitMQOutputDialog(Shell parent, Object baseStepMeta,
			                   TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (RabbitMQOutputMeta)baseStepMeta;
	}
	public String open() {

		Shell parent    = getParent();
		Display display = parent.getDisplay();
		swt = new KettleSwtBuilder()
		//colinf[0] =
		//  new ColumnInfo( Messages.getString("RabbitMQOutputDialog.ColumnInfo.ExchangeType" ),
		//      ColumnInfo.COLUMN_TYPE_CCOMBO, RabbitType.ExchangeType.valuesAsString() as String [], 
		//      true );

		shell = swt.shell(parent){
			migLayout(layoutConstraints:"insets 2, wrap 1", columnConstraints: "[grow]")
		   // migLayout(layoutConstraints:"wrap 2", columnConstraints: "[] [grow,:200:]")
	    	//gridLayout(numColumns: 2)
			
	    	composite(layoutData:"growx, spanx, wrap"){
		   	migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")

				label Messages.getString("RabbitMQInputDialog.Stepname.Label")
				text(id:"stepName", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("RabbitMQInputDialog.MessageFieldName.Label")
				cCombo(id:"messageFieldName", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, null),
					   text:"LOWER_LEFT",
					   style:"READ_ONLY", 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}

				label Messages.getString("RabbitMQInputDialog.Host.Label")
				text(id:"host", layoutData:"span,growx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("RabbitMQInputDialog.Port.Label")
				text(id:"port", layoutData:"span,growx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("RabbitMQInputDialog.Username.Label")
				text(id:"username", layoutData:"span,growx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
				}

				label Messages.getString("RabbitMQInputDialog.Password.Label")
				text(id:"password", layoutData:"span,growx", text: "", style:"PASSWORD"){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("RabbitMQInputDialog.VerifyPassword.Label")
				text(id:"verifyPassword", layoutData:"span,growx", text: "", style:"PASSWORD"){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("RabbitMQOutputDialog.RoutinKey.Label")
				text(id:"routingKey", layoutData:"span,growx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("RabbitMQOutputDialog.ExchangeName.Label")
				text(id:"exchangeName", layoutData:"span,growx", text: ""){
					onEvent(type:'Modify') { input.setChanged() }
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
		shell.text = Messages.getString("RabbitMQOutputDialog.Shell.Title")
		getData(); // initialize data fields
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;
	}

	public void getData()
	{
		swt.messageFieldName.text 	= input.messageFieldName?:""
		swt.host.text 					= input.host?:""
		swt.port.text 					= input.port?:""
		swt.username.text 			= input.username?:""
		swt.password.text 			= input.password?:""
		swt.verifyPassword.text 	= input.password?:""
		swt.routingKey.text 			= input.routingKey?:""
		swt.exchangeName.text 		= input.exchangeName?:""

		swt.stepName.selectAll();
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
			mb.message = Messages.getString("RabbitMQInputDialog.PasswordDontMatch.DialogMessage"); 
			mb.text    = Messages.getString("RabbitMQInputDialog.PasswordDontMatch.DialogTitle") 
			mb.open()
			return
		}

		input.port 					= swt.port.text
		input.host 					= swt.host.text
		input.username 			= swt.username.text
		input.password 			= swt.password.text
		input.messageFieldName 	= swt.messageFieldName.text
		input.routingKey 			= swt.routingKey.text
		input.exchangeName 		= swt.exchangeName.text

		dispose();
	}

}

