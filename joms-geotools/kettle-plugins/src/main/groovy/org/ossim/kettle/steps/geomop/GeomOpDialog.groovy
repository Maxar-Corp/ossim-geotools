package org.ossim.kettle.steps.geomop;

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
import org.eclipse.swt.custom.CCombo
import org.ossim.kettle.steps.geomop.GeomOpData.GeomOpType

import org.ossim.kettle.types.OssimValueMetaBase

public class GeomOpDialog extends BaseStepDialog implements
		StepDialogInterface {

	private GeomOpMeta input;
	private def swt;
	private def stepData

	public GeomOpDialog(Shell parent, Object baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (GeomOpMeta)baseStepMeta;
	}
	public String open() {

		stepData = input.stepData
		Shell parent    = getParent();
		Display display = parent.getDisplay();
		swt = new groovy.swt.SwtBuilder()
		
		shell = swt.shell(parent){
		    migLayout(layoutConstraints:"wrap 2", columnConstraints: "[] [grow,:200:]")
	    	//gridLayout(numColumns: 2)
			label Messages.getString("GeomOpDialog.Stepname.Label")
			
			//text(id:"stepName", text: stepname ,layoutData:"span, growx"){
			text(id:"stepName", layoutData:"span,growx", text: stepname){
				onEvent(type:'Modify') { input.setChanged() }
			}
			label Messages.getString("GeomOpDialog.OutputFieldName.Label")
			
			//text(id:"stepName", text: stepname ,layoutData:"span, growx"){
			text(id:"outputFieldName", layoutData:"span,growx"){
				onEvent(type:'Modify') { input.setChanged() }
			}
			group(Messages.getString("GeomOpDialog.OperationSettings.Label"),
				   id:"geometryOpGroup", layoutData:"span, grow, wrap"){
				migLayout(layoutConstraints:"insets 2", columnConstraints: "[][]")
				label Messages.getString("GeomOpDialog.OperationType.Label")
				cCombo(id:"operationType", 
					   items:GeomOpType.valuesAsString(), 
					   layoutData:"span,growx,split 5")
				{
					onEvent(type:'Modify') { 
						operationTypeChanged()
						input.setChanged() 
					}
				}
				label ("Param1: ", id:"param1Label", visible:false)
				text(id:"param1", visible:false)

				label ("Param2: ", id:"param2Label", visible:false)
				text(id:"param2", visible:false)

				label Messages.getString("GeomOpDialog.inputGeomField1.Label")
				cCombo(id:"inputGeomField1", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_GEOMETRY_2D]), 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
				label (Messages.getString("GeomOpDialog.inputGeomField2.Label"), id:"inputGeomField2Label")
				cCombo(id:"inputGeomField2", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_GEOMETRY_2D]), 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify'){ input.setChanged(); }
				}
			}
			group(id:"buttonsGroup", layoutData:"span, grow, wrap"){
				migLayout(layoutConstraints:"insets 2", columnConstraints: "[grow]")
				button(id:"okButton", "Ok", 
					   layoutData:"align center, split 2"){
					onEvent(type:"Selection"){ok()}
				}
				button(id:"cancelButton", "Cancel", layoutData:""){
					onEvent(type:"Selection"){cancel()}
				}
			}
		}
		changed = input.hasChanged();

		shell.text = Messages.getString("GeomOpDialog.Shell.Title")
		getData(); // initialize data fields
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;
	}
	private void operationTypeChanged()
	{
		def tempText = swt.operationType.text
		if(!tempText) return
		if(!GeomOpType.supportsTwoInputs(GeomOpType."${tempText}"))
		{
			swt.inputGeomField2.enabled      = false
			swt.inputGeomField2Label.enabled = false
		}
		else
		{
			swt.inputGeomField2.enabled      = true
			swt.inputGeomField2Label.enabled = true
		}
		if(GeomOpType."${tempText}" == GeomOpType.PROJECTION_TRANSFORM)
		{
			swt.param1Label.visible = true
			swt.param1.visible      = true
			swt.param2Label.visible = true
			swt.param2.visible      = true
		
			swt.param1Label.text  = "EPSG In:"
			swt.param2Label.text  = "EPSG Out:"
		}
		else
		{
			swt.param1Label.visible = false
			swt.param1.visible      = false
			swt.param2Label.visible = false
			swt.param2.visible      = false
		}
	}
	public void getData()
	{
		swt.stepName.selectAll();
		swt.outputFieldName.text = input.outputFieldName?:""
		swt.inputGeomField1.text = input.inputGeomField1?:""
		swt.inputGeomField2.text = input.inputGeomField2?:""
		swt.operationType.text   = input.operationType?input.operationType.toString():""
		swt.param1.text = input.param1?:""
		swt.param2.text = input.param2?:""

		operationTypeChanged()
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

		stepname = swt.stepName.text

		if(!"${swt.outputFieldName.text}")
		{
			// ERROR
			return
		}
		if(!"${swt.operationType.text}")
		{
			//ERROR
			return 
		} 
		if(!"${swt.inputGeomField1.text}")
		{
			//ERROR
			return
		}			
		def p1 = "${swt.param1.text}".toLowerCase().trim()
		def p2 = "${swt.param2.text}".toLowerCase().trim()


		switch(GeomOpType."${swt.operationType.text}")
		{
			case GeomOpType.UNION:
				if((!"${swt.inputGeomField1.text}")&&(!"${swt.inputGeomField2.text}"))
				{
					//ERROR
					return
				}			
				break
			case GeomOpType.PROJECTION_TRANSFORM:
				if(!p1 || !p2)
				{
					// error
					return
				}
				if(!p1.contains("epsg"))
				{
					// error
					return
				}
				if(!p2.contains("epsg"))
				{
					return
					// error
				}
				break
			default:
				break
		}
		input.inputGeomField1 = swt.inputGeomField1.text
		input.inputGeomField2 = swt.inputGeomField2.enabled?swt.inputGeomField2.text:""
		input.outputFieldName = swt.outputFieldName.text
		input.operationType   = GeomOpType."${swt.operationType.text}"

		input.param1          = p1
		input.param2          = p2

		dispose();
	}

}

