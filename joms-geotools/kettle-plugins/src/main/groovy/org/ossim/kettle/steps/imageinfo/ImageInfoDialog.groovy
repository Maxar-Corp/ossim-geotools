package org.ossim.kettle.steps.imageinfo;

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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.custom.CTabFolder
import org.eclipse.swt.custom.CTabItem
import org.eclipse.swt.custom.CCombo

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

public class ImageInfoDialog extends BaseStepDialog implements
		StepDialogInterface {

	private ImageInfoMeta input;
	private def swt;

	public ImageInfoDialog(Shell parent, Object baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (ImageInfoMeta)baseStepMeta;
	}
	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

 

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
	   						def renameValue          = input.fieldNames."${key}"

	   						item.setText(1, key)
	   						item.setText(2, renameValue)
			   			}
   				   } as ModifyListener

		ColumnInfo[] colinf = new ColumnInfo[2];
		colinf[0] =
		  new ColumnInfo( Messages.getString("ImageInfoDialog.ColumnInfo.Fieldname" ),
		      ColumnInfo.COLUMN_TYPE_CCOMBO, input.fieldNames.collect{k,v->v}.sort() as String [], 
		      true );
		colinf[1] =
		  new ColumnInfo( Messages.getString("ImageInfoDialog.ColumnInfo.RenameTo" ),
		      ColumnInfo.COLUMN_TYPE_TEXT, false );

		swt = new KettleSwtBuilder()
		shell = swt.shell(parent){
			migLayout(layoutConstraints:"wrap 1", columnConstraints: "[grow]")
			group(id:"stepNameGroup"){
				migLayout(layoutConstraints:"insets 2", columnConstraints: "[] [grow,50:200:200]")
				label Messages.getString("ImageInfoDialog.Stepname.Label")
			
				text(id:"stepName", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("ImageInfoDialog.inputFilenameField.Label")
				cCombo(id:"inputFilenameField", 
					   items:SwtUtilities.previousStepFields(transMeta, stepname), 
					   layoutData:"span,growx")
				{
					onEvent(type:'FocusIn') { input.setChanged(); }
				}
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
				button(id:"getAllFields", layoutData:"align center, split 2", text: Messages.getString("ImageInfoDialog.getAllFields.label")){
					onEvent(type:"Selection"){
						loadAllFields()
					}
				}
				button(id:"clearAllFields", Messages.getString("ImageInfoDialog.clearAllFields.label")){
					onEvent(type:"Selection"){
						clearAllFields()
					}
				}
			}

			group(layoutData:"grow, span, wrap"){
				migLayout(layoutConstraints:"insets 2", columnConstraints: "[grow]")
				button(id:"okButton", Messages.getString("ImageInfoDialog.ok.Label"), 
					   layoutData:"align center,split 2"){
					onEvent(type:"Selection"){ok()}
				}
				button(id:"cancelButton", Messages.getString("ImageInfoDialog.cancel.Label"), layoutData:""){
					onEvent(type:"Selection"){cancel()}
				}
			}
		}
		changed = input.hasChanged();
		//setModalDialog(true)
		shell.text = Messages.getString("ImageInfoDialog.Shell.Title")
		getData(); // initialize data fields
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;

	}
	private void loadAllFields() {
		def fields = input.fieldNames
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
		def fields = input.fieldNames
		def selectedFields = input.selectedFieldNames
		def tableView = swt.fieldSelection
		tableView.table.clearAll()
		tableView.table.setItemCount(selectedFields.size()+1)
		def idx = 0
		if(selectedFields)
		{
			selectedFields.each{key->
				def value = input.fieldNames."${key}"
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
		swt.stepName.selectAll();
		swt.inputFilenameField.text = input.inputFilenameField as String
		loadSelectedFields();

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
		if (Const.isEmpty(swt.stepName.text)) return;
		def tableView = swt.fieldSelection
		input.inputFilenameField = swt.inputFilenameField.text

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
						input.fieldNames."${item.getText(1)}" = rename
					}
				}
			}
		}
	
		dispose();
	}

}

