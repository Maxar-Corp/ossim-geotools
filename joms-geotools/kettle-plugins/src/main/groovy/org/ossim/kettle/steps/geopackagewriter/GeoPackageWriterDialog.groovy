package org.ossim.kettle.steps.geopackagewriter
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

public class GeoPackageWriterDialog extends BaseStepDialog implements
		StepDialogInterface {

	private GeoPackageWriterMeta input;
	private def swt;

	private def fields = [		 "tile_width":"inputTileWidthField",
								 "tile_height":"inputTileHeightField",
								 "tile_row":"inputTileLocalRowField",
								 "tile_col":"inputTileLocalColField",
								 "tile_global_row":"inputTileGlobalRowField",
								 "tile_global_col":"inputTileGlobalColField",
								 "tile_level":"inputTileLevelField",
								 "image":"inputImageField",
								 "image_status":"inputImageStatusField",
								 "summary_epsg":"inputSummaryEpsgField",
								 "summary_clip_minx":"inputSummaryMinXField",
								 "summary_clip_maxx":"inputSummaryMaxXField",
								 "summary_clip_miny":"inputSummaryMinYField",
								 "summary_clip_maxy":"inputSummaryMaxYField",
								 "summary_level_info":"inputSummaryLevelInfoField"
								 ]

	public GeoPackageWriterDialog(Shell parent, Object baseStepMeta,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
		input = (GeoPackageWriterMeta)baseStepMeta;
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
		  new ColumnInfo( Messages.getString("GeoPackageWriterDialog.ColumnInfo.Parameter" ),
		      ColumnInfo.COLUMN_TYPE_TEXT, false, 
		                                    true );
		colinf[1] =
		  new ColumnInfo( Messages.getString("GeoPackageWriterDialog.ColumnInfo.InputField" ),
		      ColumnInfo.COLUMN_TYPE_CCOMBO, SwtUtilities.previousStepFields(transMeta, stepname), 
		      true );
		shell = swt.shell(parent){
			migLayout(layoutConstraints:"insets 2, wrap 1", columnConstraints: "[grow]")
			group(layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow]")
		    	//gridLayout(numColumns: 2)
				label Messages.getString("GeoPackageWriterDialog.Stepname.Label")
				
				//text(id:"stepName", text: stepname ,layoutData:"span, growx"){
				text(id:"stepName", layoutData:"span,growx", text: stepname){
					onEvent(type:'Modify') { input.setChanged() }
				}

			}

			group(layoutData:"span,growx"){
				migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow]")

				label Messages.getString("GeoPackageWriterDialog.Outputfilename.Label")

				text(id:"outputFilename", layoutData:"span,growx", text: "/tmp/test.gpkg"){
					onEvent(type:'Modify') { input.setChanged() }
				}

				label Messages.getString("GeoPackageWriterDialog.Layername.Label")

				text(id:"layerName", layoutData:"span,growx", text: "tiles"){
					onEvent(type:'Modify') { input.setChanged() }
				}

				label Messages.getString("GeoPackageWriterDialog.Imagetype.Label")

				cCombo(id:"imageType", 
					   items:["PNG", "JPG", "MIXED"],
					   text:"PNG",
					   style:"READ_ONLY", 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
				}

				label Messages.getString("GeoPackageWriterDialog.Tilingtype.Label")
				
				cCombo(id:"tilingType", 
					   items:["local", "global"],
					   text:"local",
					   style:"READ_ONLY", 
					   layoutData:"span,growx")
				{
					onEvent(type:'Modify') { input.setChanged(); }
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
							 layoutData:"height 100:100:200, w 200:200:200, span,wrap",
							 //layoutData:"span, growx",
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

		shell.text = Messages.getString("GeoPackageWriterDialog.Shell.Title")
		getData(); // initialize data fields
		setSize(); // shrink and fit dialog to fit inputs
		input.setChanged(changed);

		shell.doMainloop()

		return stepname;
	}

	public void getData()
	{
		swt.stepName.selectAll();
		swt.outputFilename.text  = input.outputFilename as String
		swt.imageType.text  = input.imageType as String
		swt.layerName.text  = input.layerName as String
		swt.tilingType.text = input.tilingType as String


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
		resizeTable(tableView.table);
	}
	private static void resizeColumn(def tableColumn)
	{
	    tableColumn.pack();

	}
	private static void resizeTable(def table)
	{
		table.columns.each{col->
			resizeColumn(col)
		}
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
		input.outputFilename = swt.outputFilename.text as String
		input.imageType      = swt.imageType.text as String
		input.layerName      = swt.layerName.text as String
		input.tilingType     = swt.tilingType.text as String

		def tableView = swt.fieldMappings
		
		tableView.table.items.each{item->
			def key = fields."${item.getText(1)}"
			def value = item.getText(2)
			input."${key}" = value
		}


		dispose();
	}

}

