package org.ossim.kettle.steps.stageraster;

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
import org.eclipse.swt.widgets.Group;
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


public class StageRasterDialog extends BaseStepDialog implements
		StepDialogInterface {

	private StageRasterMeta input;
	private def swt;
	private boolean toggleFlag=true;
	public StageRasterDialog(Shell parent, Object baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (StageRasterMeta)baseStepMeta;
	}
	
	public String open() {

		Shell parent    = getParent();
		Display display = parent.getDisplay();
		swt = new groovy.swt.SwtBuilder()

		shell = swt.shell(parent){
			migLayout(layoutConstraints:"wrap 1", columnConstraints: "[]")
			group(text:"Inputs", layoutData:"growx") {
			    migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")
		    	//gridLayout(numColumns: 2)
				label Messages.getString("StageRasterDialog.Stepname.Label")
				
				//text(id:"stepName", text: stepname ,layoutData:"span, growx"){
				text(id:"stepName", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("StageRasterDialog.FileFieldname.Label")
				
				cCombo(id:"fileFieldName", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname), 
					   layoutData:"span,growx")
				{
					onEvent(type:'FocusIn') { input.setChanged(); }
				}
				label Messages.getString("StageRasterDialog.EntryFieldname.Label")
				
				cCombo(id:"entryFieldName", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname), 
					   layoutData:"span,growx")
				{
					onEvent(type:'FocusIn') { input.setChanged(); }
				}
				checkBox(id:"outputResultCheckbox",
					text:Messages.getString("StageRasterDialog.outputResultCheckbox.Label"),
					selection:true){
					onEvent(type:"Selection"){
						input.setChanged()
						swt.resultColumnName.enabled = swt.outputResultCheckbox.selection
					}
				}
				text(id:"resultColumnName", text:"", layoutData:"growx, wrap")
				{
					onEvent(type:'Modify') { input.setChanged() }
				}

				checkBox(id:"outputOmsInfoFlag",
					text:Messages.getString("StageRasterDialog.outputOmsInfoFlagCheckbox.Label"),
					selection:true){
					onEvent(type:"Selection"){
						input.setChanged()
						swt.omsInfoFieldName.enabled = swt.outputOmsInfoFlag.selection
					}
				}
				text(id:"omsInfoFieldName", text:"", layoutData:"growx, wrap")
				{
					onEvent(type:'Modify') { input.setChanged() }
				}
			}
			group("Settings", layoutData:"growx"){
			   migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")
				label "Histogram type:"
				cCombo(id:"histogramType", items:["NONE", "NORMAL", "FAST"]){
					onEvent(type:"Selection"){
						input.setChanged()
					}
				}
				label "Overview type:"
				cCombo(id:"overviewType", items:["ossim_tiff_box",
					                             "ossim_tiff_nearest",
					                             "ossim_kakadu_nitf_j2k"], layoutData:"wrap"){
					onEvent(type:"Selection"){
						input.setChanged()
						updateEnableDisableFields()
					}
				}
				label "Compression Type:"
				cCombo(id:"compressionType", items:["NONE",
																"JPEG",
																"PACKBITS",
																"DEFLATE"], 
						 layoutData:"wrap"){
					onEvent(type:"Selection"){
						input.setChanged()
						updateEnableDisableFields()
					}
				}

				label "Compression Quality (0-100):"
				text id:"compressionQuality", text:"100", layoutData:"growx, wrap"
			}
			composite(layoutData:"growx"){
			   migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")
				button("Ok", layoutData:"align center,skip 1,split 2"){
					onEvent(type:"Selection"){ok()}
				}
				button("Cancel", layoutData:""){
					onEvent(type:"Selection"){cancel()}
				}
			}
		}
		changed = input.hasChanged();
		shell.text = Messages.getString("StageRasterDialog.Shell.Title")
		getData(); // initialize data fields
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;
	}
	private void updateEnableDisableFields(){
		if(swt.compressionType.text == "JPEG" || swt.overviewType.text.contains("kakadu"))
		{
			swt.compressionQuality.enabled = true
		}
		else
		{
			swt.compressionQuality.enabled = false
		}
		if(swt.overviewType.text.contains("tiff"))
		{
			swt.compressionType.enabled = true
		}
		else
		{
			swt.compressionType.enabled = false;
		}
	}
	public void getData()
	{
		swt.stepName.selectAll();

		swt.fileFieldName.text = Const.NVL(input.fileFieldName, "")
		swt.entryFieldName.text = Const.NVL(input.entryFieldName, "")
		swt.outputResultCheckbox.selection = input.outputResultFlag
		swt.resultColumnName.enabled = swt.outputResultCheckbox.selection
		swt.resultColumnName.text    = input.resultFieldName

		swt.histogramType.text       = input.histogramType
		swt.overviewType.text        = input.overviewType
		swt.compressionType.text     = input.compressionType
		swt.compressionQuality.text  = "${input.compressionQuality}"

		swt.outputOmsInfoFlag.selection = input.outputOmsInfoFlag
		swt.omsInfoFieldName.enabled    = swt.outputOmsInfoFlag.selection
		swt.omsInfoFieldName.text       = input.omsInfoFieldName

		updateEnableDisableFields()
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);

		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(swt.stepName.text) ||
			Const.isEmpty(swt.compressionQuality.text)) return;

		stepname = swt.stepName.text
		
		input.fileFieldName           = swt.fileFieldName.text
		input.entryFieldName          = swt.entryFieldName.text
		input.outputResultFlag        = swt.outputResultCheckbox.selection
		input.resultFieldName         = swt.resultColumnName.text
		input.histogramType           = swt.histogramType.text
		input.overviewType            = swt.overviewType.text
		input.compressionType         = swt.compressionType.text
		input.compressionQuality      = swt.compressionQuality.text.toInteger()
		input.outputOmsInfoFlag       = swt.outputOmsInfoFlag.selection
		input.omsInfoFieldName        = swt.omsInfoFieldName.text

		dispose();
	}

}
