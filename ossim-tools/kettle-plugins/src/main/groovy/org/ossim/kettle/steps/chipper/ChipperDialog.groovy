package org.ossim.kettle.steps.chipper

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
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.MessageBox;

public class ChipperDialog extends BaseStepDialog implements
		StepDialogInterface {

	private ChipperMeta input;
	private def swt;
	private def fields = ["filename":"inputFilenameField",
				     			 "entry":"inputEntryField", 
				             "minx":"inputTileMinXField", 
				             "miny":"inputTileMinYField", 
				             "maxx":"inputTileMaxXField",
				             "maxy":"inputTileMaxYField", 
				             "epsg":"inputEpsgCodeField", 
				             "width":"inputTileWidthField", 
				             "height":"inputTileHeightField"]

	public ChipperDialog(Shell parent, Object baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (ChipperMeta)baseStepMeta;
	}
	public String open() {

		Shell parent    = getParent();
		Display display = parent.getDisplay();
		swt = new KettleSwtBuilder()
   	def lsMod = { 
	   					event -> input.setChanged()
   				   } as ModifyListener
		ColumnInfo[] colinf = new ColumnInfo[2];
		colinf[0] =
		  new ColumnInfo( Messages.getString("ChipperDialog.ColumnInfo.Parameter" ),
		      ColumnInfo.COLUMN_TYPE_TEXT, false, 
		                                    true );
		colinf[1] =
		  new ColumnInfo( Messages.getString("ChipperDialog.ColumnInfo.InputField" ),
		      ColumnInfo.COLUMN_TYPE_CCOMBO, SwtUtilities.previousStepFields(transMeta, stepname), 
		      true );

		shell = swt.shell(parent){
			migLayout(layoutConstraints:"wrap 1", columnConstraints: "[grow]")
			group(layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow]")
		    	//gridLayout(numColumns: 2)
				label Messages.getString("ChipperDialog.Stepname.Label")
				
				//text(id:"stepName", text: stepname ,layoutData:"span, growx"){
				text(id:"stepName", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("ChipperDialog.ImageResult.Label")
				
				//text(id:"stepName", text: stepname ,layoutData:"span, growx"){
				text(id:"imageResultField", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}
				label Messages.getString("ChipperDialog.ImageStatus.Label")
				
				//text(id:"stepName", text: stepname ,layoutData:"span, growx"){
				text(id:"imageStatusField", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}

			}

			group(layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[grow]")
				label(layoutData:"", 
					text: Messages.getString("ChipperDialog.histogramOperationType.Label"))
				cCombo(id:"histogramOperationType", 
					   items:["none", "auto-minmax", "std-stretch-1", "std-stretch-2", "std-stretch-3"], 
					   style:"READ_ONLY",
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { 
						input.setChanged() 
					}
				}
				label(layoutData:"",  text: Messages.getString("ChipperDialog.resampleFilterType.Label"))
				cCombo(id:"resampleFilterType", 
					   items:["nearest neighbor", "bilinear", "cubic", "bessel", "blackman",
					   		 "bspline", "catrom", "gaussian", "hanning", "hamming", "hermite",
					   		 "lanczos", "mitchell", "quadratic", "sinc"], 
					   style:"READ_ONLY",
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { 
						input.setChanged() 
					}
				}

			}
			group(layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2, wrap 1", columnConstraints: "[grow]")
				tableView(id:"fieldMappings",
							 transMeta:transMeta,
							 nrRows:5,
							 columnInfo:colinf,
							 style:"BORDER,FULL_SELECTION,MULTI,V_SCROLL,H_SCROLL",
							 propsUi:props,
							 //layoutData:"height 100:100:200, w 200:200:200, span,wrap",
							 layoutData:"span, growx",
							 modifyListener:lsMod)
			}
			group(layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow]")
				button("Ok", layoutData:"align center,skip 1,split 2"){
					onEvent(type:"Selection"){ok()}
				}
				button("Cancel", layoutData:""){
					onEvent(type:"Selection"){cancel()}
				}

			}
		}
		changed = input.hasChanged();

		shell.text = Messages.getString("ChipperDialog.Shell.Title")
		getData(); // initialize data fields
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;
	}

	public void getData()
	{
		swt.stepName.selectAll();
		swt.histogramOperationType.text = input.histogramOperationType
		swt.resampleFilterType.text = input.resampleFilterType
		swt.imageResultField.text = input.imageResultField
		swt.imageStatusField.text = input.imageStatusField

		def tableView = swt.fieldMappings
		tableView.table.clearAll()
		tableView.table.setItemCount(fields.size())
		def idx = 0
		fields.each{k,v->
			TableItem item = tableView.table.getItem(idx);
			item.setText(1, k);
			item.setText(2, input."${v}");

			++idx
		}

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
		if (Const.isEmpty(swt.stepName.text)){
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.message = Messages.getString("ChipperDialog.StepNameEmpty.DialogMessage"); //$NON-NLS-1$
			mb.text = Messages.getString("ChipperDialog.StepNameEmpty.DialogTitle") //$NON-NLS-1$
			mb.open()
			return;
		} 

		input.imageResultField = swt.imageResultField.text
		input.imageStatusField = swt.imageStatusField.text 

		input.histogramOperationType = swt.histogramOperationType.text
		input.resampleFilterType = swt.resampleFilterType.text

		def tableView = swt.fieldMappings
		
		tableView.table.items.each{item->
			def key = fields."${item.getText(1)}"
			def value = item.getText(2)
			input."${key}" = value
		}

		stepname = swt.stepName.text
	
		dispose();
	}

}

