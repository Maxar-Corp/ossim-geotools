package org.ossim.kettle.steps.createovrhst;

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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.ossim.kettle.utilities.SwtUtilities

public class CreateOvrHstDialog extends BaseStepDialog implements
		StepDialogInterface {


	private CreateOvrHstMeta input;

	private TextVar wFieldname;
	private def swt
	public CreateOvrHstDialog(Shell parent, Object baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (CreateOvrHstMeta)baseStepMeta;
	}
	
	public String open() {
		Shell parent    = getParent();
		Display display = parent.getDisplay();
		swt = new groovy.swt.SwtBuilder()
		shell = swt.shell(parent){
		    migLayout(layoutConstraints:"wrap 2", columnConstraints: "[] [grow,:200:]")
			label Messages.getString("CreateOvrHstDialog.Stepname.Label")
			
			text(id:"stepName", layoutData:"span,growx", text: stepname){
				onEvent(type:'Modify') { input.setChanged() }
			}
			label Messages.getString("CreateOvrHstDialog.Fieldname.Label")
			
			cCombo(id:"fileFieldName", 
				   items:SwtUtilities.previousStepFields(transMeta, stepname), 
				   layoutData:"span,growx,wrap")
			{
				onEvent(type:'FocusIn') { input.setChanged(); }
			}
			button("Ok", layoutData:"align center,skip 1,split 2"){
				onEvent(type:"Selection"){ok()}
			}
			button("Cancel", layoutData:""){
				onEvent(type:"Selection"){cancel()}
			}
		}
		shell.text = Messages.getString("CreateOvrHstDialog.Shell.Title")

		changed = input.hasChanged();

		getData(); // initialize data fields
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		swt.stepName.selectAll();
		swt.fileFieldName.text = Const.NVL(input.getFieldName(), "");
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(swt.stepName.text)) return;

		stepname = swt.stepName.text;
		
		input.setFieldName(swt.fileFieldName.text);
		
		dispose();
	}

}


