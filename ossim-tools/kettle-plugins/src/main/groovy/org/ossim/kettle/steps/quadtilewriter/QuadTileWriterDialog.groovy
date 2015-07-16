package org.ossim.kettle.steps.quadtilewriter

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
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.pentaho.di.ui.core.widget.ColumnInfo
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.widgets.TableItem
import org.ossim.kettle.types.OssimValueMetaBase
import org.pentaho.di.core.row.value.ValueMetaBase

public class QuadTileWriterDialog extends BaseStepDialog implements
		StepDialogInterface {

	private QuadTileWriterMeta input;
	private def swt;

	public QuadTileWriterDialog(Shell parent, Object baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (QuadTileWriterMeta)baseStepMeta;
	}
	public String open() {

		Shell parent    = getParent();
		Display display = parent.getDisplay();
		swt = new KettleSwtBuilder()
		shell = swt.shell(parent){
			migLayout(layoutConstraints:"wrap 1", columnConstraints: "[grow]")
			group(layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow]")
				label Messages.getString("QuadTileWriterDialog.Stepname.Label")
				text(id:"stepName", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}
			}

			group(text: "Input Options", id:"inputOptionsGroup",layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow]")
				label Messages.getString("QuadTileWriterDialog.InputTile.Label")
				cCombo(id:"inputTileField", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_CLONABLE_IMAGE, OssimValueMetaBase.TYPE_IMAGE]), 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
				label Messages.getString("QuadTileWriterDialog.InputTileStatus.Label")
				cCombo(id:"inputTileStatusField", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_STRING]), 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
				label Messages.getString("QuadTileWriterDialog.InputTileLevel.Label")
				cCombo(id:"inputTileLevelField", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_INTEGER]), 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
				label Messages.getString("QuadTileWriterDialog.InputTileRow.Label")
				cCombo(id:"inputTileRowField", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_INTEGER]), 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
				label Messages.getString("QuadTileWriterDialog.InputTileCol.Label")
				cCombo(id:"inputTileColField", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_INTEGER]), 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
			}
			group(text:"Output Options", id:"outputOptionsGroup", layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow]")
				label Messages.getString("QuadTileWriterDialog.OutputType.Label")
				cCombo(id:"outputType", 
					   items:QuadTileWriterData.OutputType.outputTypeList, 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
				label Messages.getString("QuadTileWriterDialog.OutputFileNameMask.Label")
				text(id:"outputFileNameMask", layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}					
				label Messages.getString("QuadTileWriterDialog.RootOutputDirectory.Label")
				cCombo(id:"inputRootOutputDirField", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_STRING]), 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
				label Messages.getString("QuadTileWriterDialog.FilenameOverride.Label")
				cCombo(id:"inputFilenameOverrideField", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_STRING]), 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}
			}
			group(layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2", columnConstraints: "[][grow]")
				button("Ok", layoutData:"align center,skip 1,split 2"){
					onEvent(type:"Selection"){ok()}
				}
				button("Cancel", layoutData:""){
					onEvent(type:"Selection"){cancel()}
				}
			}
		}
		changed = input.hasChanged();

		shell.text = Messages.getString("QuadTileWriterDialog.Shell.Title")
		getData(); // initialize data fields
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;
	}

	public void getData()
	{
		swt.stepName.selectAll();
		swt.inputTileField.text = Const.NVL((String)input.inputTileField, "")
		swt.outputFileNameMask.text = Const.NVL((String)input.outputFileNameMask, "")
		swt.inputTileStatusField.text = Const.NVL((String)input.inputTileStatusField, "")
		swt.inputRootOutputDirField.text = Const.NVL((String)input.inputRootOutputDirField, "")
		swt.inputFilenameOverrideField.text = Const.NVL((String)input.inputFilenameOverrideField, "")
		swt.inputTileLevelField.text = Const.NVL((String)input.inputTileLevelField, "")
		swt.inputTileRowField.text = Const.NVL((String)input.inputTileRowField, "")
		swt.inputTileColField.text = Const.NVL((String)input.inputTileColField, "")
		swt.outputType.text = Const.NVL((String)input.outputType.toString(), "")
		//swt.fileFieldName.text = Const.NVL((String)input.fileFieldName, "")
		//swt.infoFieldName.text = Const.NVL((String)input.omsInfoFieldName, "")
	}
	
	private void cancel()
	{
		stepname=null;
		//input.setChanged(changed);

		dispose();
	}
	
	private void ok()
	{
		if (!swt.stepName.text) return;

		stepname = swt.stepName.text
		input.inputTileField = swt.inputTileField.text
		input.outputFileNameMask = swt.outputFileNameMask.text
		input.inputTileStatusField = swt.inputTileStatusField.text
		input.inputRootOutputDirField = swt.inputRootOutputDirField.text
		input.inputFilenameOverrideField = swt.inputFilenameOverrideField.text
		input.inputTileLevelField = swt.inputTileLevelField.text
		input.inputTileRowField = swt.inputTileRowField.text
		input.inputTileColField = swt.inputTileColField.text

		input.outputType = QuadTileWriterData.OutputType."${swt.outputType.text}"
//		input.omsInfoFieldName =swt.infoFieldName.text
	
		dispose();
	}

}

