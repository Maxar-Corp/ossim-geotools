package org.ossim.kettle.steps.basictiling

import geoscript.layer.Pyramid;
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
import org.eclipse.swt.widgets.Text
import org.ossim.kettle.types.OssimValueMetaBase;
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

public class BasicTilingDialog extends BaseStepDialog implements
        StepDialogInterface {

   private BasicTilingMeta input;
   private def swt;
   String originField
   String tileNameMaskField
   String projectionTypeField
   String tileWidthField
   String tileHeightField
   String clampMinLevelField
   String clampMaxLevelField
   String clampGeometryField
   String filenameField
   String fileEntryField
   private def fields = ["origin":"originField",
                         "tile_mask":"tileNameMaskField",
                         "projection":"projectionTypeField",
                         "tile_width":"tileWidthField",
                         "tile_height":"tileHeightField",
                         "min_level":"clampMinLevelField",
                         "max_level":"clampMaxLevelField",
                         "clamp_geometry":"clampGeometryField",
                         "filename":"filenameField",
                         "entry":"fileEntryField"]

   public BasicTilingDialog(Shell parent, Object baseStepMeta,
                            TransMeta transMeta, String stepname) {
      super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
      input = (BasicTilingMeta)baseStepMeta;
   }
   public String open() {

      Shell parent    = getParent();
      Display display = parent.getDisplay();
      swt = new KettleSwtBuilder()
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
              new ColumnInfo( Messages.getString("BasicTilingDialog.ColumnInfo.Fieldname" ),
                      ColumnInfo.COLUMN_TYPE_CCOMBO, input.outputFieldNames.collect{k,v->v}.sort() as String [],
                      true );
      colinf[1] =
              new ColumnInfo( Messages.getString("BasicTilingDialog.ColumnInfo.RenameTo" ),
                      ColumnInfo.COLUMN_TYPE_TEXT, false );
      shell = swt.shell(parent){
         migLayout(layoutConstraints:"insets 2, wrap 1", columnConstraints: "[grow]")
         // migLayout(layoutConstraints:"wrap 2", columnConstraints: "[] [grow,:200:]")
         //gridLayout(numColumns: 2)

         composite(layoutData:"growx, spanx, wrap"){
            label Messages.getString("BasicTilingDialog.Stepname.Label")
            migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")

            //text(id:"stepName", text: stepname ,layoutData:"span, growx"){
            text(id:"stepName", layoutData:"span,growx", text: stepname){
               onEvent(type:'Modify') { input.setChanged() }
            }
            label Messages.getString("BasicTilingDialog.TileNameMask.Label")

            //text(id:"stepName", text: stepname ,layoutData:"span, growx"){
            text(id:"tileNameMask", layoutData:"span,growx"){
               onEvent(type:'Modify') { input.setChanged() }
            }
            label (id:"clampWktLabel", text:"Clamp Wkt")
            text(id:"clampWkt", layoutData:"span,growx")
                    {
                       onEvent(type: "Modify"){input.setChanged() }
                    }
            //cCombo(id:"clampWktField",
            //        items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_GEOMETRY_2D,
            //                                                                    ValueMetaInterface.TYPE_STRING]),
            //        layoutData:"span,growx")
            //        {
            //           onEvent(type:'Modify') { input.setChanged(); }
            //        }
            label (id:"clampWktEpsgLabel", text:"Clamp Wkt EPSG")
            text(id:"clampWktEpsg", layoutData:"span,growx")
                    {
                       onEvent(type: "Modify"){input.setChanged() }
                    }
           // cCombo(id:"clampWktEpsg",
           //         items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING]),
           //         layoutData:"span,growx")
           //         {
           //            onEvent(type:'Modify') { input.setChanged(); }
           //         }
            label Messages.getString("BasicTilingDialog.MinLevel.Label")
            text(id:"clampMinLevel"){
               onEvent(type:'Modify') { input.setChanged() }
            }
            label Messages.getString("BasicTilingDialog.MaxLevel.Label")
            text(id:"clampMaxLevel"){
               onEvent(type:'Modify') { input.setChanged() }
            }
            label Messages.getString("BasicTilingDialog.TilingProjectionType.Label")
            cCombo(id:"projectionType",
                    items:["EPSG:4326","EPSG:3857"],
                    text:"EPSG:4326",
                    //style:"READ_ONLY",
                    layoutData:"span,growx")
                    {
                       onEvent(type:'Modify') { input.setChanged(); }
                    }
            label Messages.getString("BasicTilingDialog.Origin.Label")
            cCombo(id:"origin",
                    items:["LOWER_LEFT", "UPPER_LEFT"],
                    text:"LOWER_LEFT",
                    style:"READ_ONLY",
                    layoutData:"span,growx")
                    {
                       onEvent(type:'Modify') { input.setChanged(); }
                    }
            label(Messages.getString("BasicTilingDialog.TileWidth.Label"), layoutData:"split 2")
            text(id:"tileWidth", text:"512"){
               onEvent("Modify"){
                  if(swt.tileWidth.text)
                  {
                     def w = swt.tileWidth.text.toInteger()
                     swt.tileHeight.text = "${w}".toString()//"${w/2}".toString()
                  }
                  input.setChanged()
               }
            }
            label(Messages.getString("BasicTilingDialog.TileHeight.Label"), layoutData:"split 2")
            text(id:"tileHeight", text:"256")
         }
         group("Geometry", layoutData:"grow, spanx, wrap", style:"SHADOW_NONE,NO_BACKGROUND"){
            migLayout(layoutConstraints:"insets 2, wrap 1", columnConstraints: "[grow]")
            radioButton (text:"From File Fields", layoutData:"split 2", selection:true){
               onEvent(type:"Selection"){
                  swt.stackLayout.topControl = swt.inputFilenameGroup
                  swt.inputFilenameGroup.visible = true
                  swt.inputBoundsWktGroup.visible = false
               }
            }
            radioButton (text:"Wkt ", layoutData:"wrap", enabled:false){
               onEvent(type:"Selection"){
                  swt.stackLayout.topControl = swt.inputBoundsWktGroup
                  swt.inputFilenameGroup.visible = false
                  swt.inputBoundsWktGroup.visible = true
               }
            }

            composite(style:"none", layoutData:"span,growx"){
               stackLayout(id:"stackLayout");
               composite(id:"inputFilenameGroup", style:"none", layoutData:"insets 0, span,growx"){
                  migLayout(layoutConstraints:"insets 0, wrap 2", columnConstraints: "[][grow]")
                  label (id:"inputFileNameFieldLabel", text:"Filename Field")
                  cCombo(id:"inputFilenameField",
                          items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING]),
                          layoutData:"span,growx")
                          {
                             onEvent(type:'Modify') { input.setChanged(); }
                          }
                  label (text:"Entry Field")
                  cCombo(id:"inputEntryField",
                          items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_INTEGER]),
                          layoutData:"span,growx")
                          {
                             onEvent(type:'Modify') { input.setChanged(); }
                          }
                  checkBox(id:"mosaicInputCheckbox", text:"Mosaic Input", layoutData:"span,growx")
               }
               /*
               composite(id:"inputBoundsWktGroup", style:"none", layoutData:"span,growx"){
                 migLayout(layoutConstraints:"insets 0, wrap 2", columnConstraints: "[][grow]")
                  label (id:"inputWktFieldLabel", text:"Input wkt")
                  cCombo(id:"inputWktField",
                          items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING]),
                          layoutData:"span,growx")
                          {
                             onEvent(type:'Modify') { input.setChanged(); }
                          }
                  label (id:"inputEpsgFieldLabel", text:"epsg")
                  cCombo(id:"inputEpsgField",
                          items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING]),
                          layoutData:"span,growx")
                          {
                             onEvent(type:'Modify') { input.setChanged(); }
                          }

                 //canvas(id:"boxSelectionCanvas", layoutData:"span, grow"){
                 //	onEvent("MouseMove"){ event->
                 //		println "MOUSE MOVED!!!!"
                 //	}
                 //}
                 //label (id:"testLabel2", text:"Not implemented yet",layoutData:"span,growx")
               }
               */
            }
         }
         group(text:"Output Fields", layoutData:"grow, spanx"){
            migLayout(layoutConstraints:"insets 2", columnConstraints: "[grow]")
            tableView(id:"fieldSelection",
                    transMeta:transMeta,
                    nrRows:1,
                    columnInfo:colinf,
                    style:"BORDER,FULL_SELECTION,MULTI,V_SCROLL,H_SCROLL",
                    propsUi:props,
                    layoutData:"height 100:100:200, span,growx, wrap",
                    modifyListener:lsMod)
            button(id:"loadSummaryFieldsButton", layoutData:"align center, split 4", text: Messages.getString("BasicTilingDialog.addSummaryFields.label")){
               onEvent(type:"Selection"){
                  loadSummaryFields()
               }
            }
            button(id:"loadTileFieldsButton", text: Messages.getString("BasicTilingDialog.addTileFields.label")){
               onEvent(type:"Selection"){
                  loadTileFields()
               }
            }
            button(id:"getAllFields", text: Messages.getString("BasicTilingDialog.getAllFields.label")){
               onEvent(type:"Selection"){
                  loadAllFields()
               }
            }
            button(id:"clearAllFields", Messages.getString("BasicTilingDialog.clearAllFields.label")){
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
      swt.stackLayout.topControl = swt.inputFilenameGroup
      shell.text = Messages.getString("BasicTilingDialog.Shell.Title")
      getData(); // initialize data fields
      setSize(); // shrink and fit dialog to fit inputs
      input.setChanged(changed);

      shell.doMainloop()

      return stepname;
   }

   public void getData()
   {
      swt.stepName.selectAll();
      swt.tileNameMask.text  = input.tileIdNameMask as String
      swt.tileWidth.text     = "${input.targetTileWidth}".toString()
      swt.tileHeight.text    = "${input.targetTileHeight}".toString()
      swt.inputFilenameField.text       = input.inputFilenameField?:""
      swt.inputEntryField.text          = input.inputEntryField?:""
      swt.projectionType.text           = input.projectionType.toString()
      swt.mosaicInputCheckbox.selection = input.mosaicInput
      swt.clampMinLevel.text = input.clampMinLevel!=null?input.clampMinLevel.toString():""
      swt.clampMaxLevel.text = input.clampMaxLevel!=null?input.clampMaxLevel.toString():""
      swt.clampWkt.text = input.clampWkt!=null?input.clampWkt.toString():""
      swt.clampWktEpsg.text = input.clampWktEpsg!=null?input.clampWktEpsg.toString():""

      loadSelectedFields();

      switch(input.getOriginAsInteger())
      {
         case Pyramid.Origin.TOP_LEFT:
            swt.origin.text = "UPPER_LEFT"
            break
         case Pyramid.Origin.BOTTOM_LEFT:
            swt.origin.text = "LOWER_LEFT"
            break
         default:
            swt.origin.text = "LOWER_LEFT"
            break
      }


   }

   private void cancel()
   {
      stepname=null;
      //input.setChanged(changed);

      dispose();
   }
   private void loadSummaryFields()
   {
      loadFields(input.summaryFieldNameMappings)
   }
   private void loadTileFields()
   {
      loadFields(input.tileFieldNameMappings)
   }
   private void loadAllFields()
   {
      loadFields(input.outputFieldNames)
   }
   private void loadSelectedFields()
   {
      def fieldMap = [:]
      input.selectedFieldNames.each{fieldName->
         def value = input.outputFieldNames."${fieldName}"
         if(value)
         {
            fieldMap << ["${fieldName}":value]
         }
      }

      loadFields(fieldMap)
   }
   private void loadFields(def fields) {
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
      if(!fields.size())
      {
         tableView.table.setItemCount(1)
      }
   }
   private void clearAllFields() {
      def tableView = swt.fieldSelection
      tableView.table.clearAll()
      tableView.table.setItemCount(1)
      tableView.table.getItem(0).setText(0, "1")
   }

   private void ok()
   {
      if (Const.isEmpty(swt.stepName.text)) return;

      stepname = swt.stepName.text
      input.tileIdNameMask= swt.tileNameMask.text as String

      input.selectedFieldNames = [] as Set
      def tableView = swt.fieldSelection
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
               if(rename)
               {
                  input.outputFieldNames."${item.getText(1)}" = rename
               }
            }
         }
      }
      if(swt.clampMinLevel.text)
      {
         input.clampMinLevel = swt.clampMinLevel.text
      }
      else
      {
         input.clampMinLevel = null
      }

      input.clampWkt     = swt.clampWkt.text
      input.clampWktEpsg = swt.clampWktEpsg.text

      if(swt.clampMaxLevel.text)
      {
         input.clampMaxLevel = swt.clampMaxLevel.text
      }
      else
      {
         input.clampMaxLevel = null
      }
      input.mosaicInput         = swt.mosaicInputCheckbox.selection
      input.origin              = swt.origin.text
      if(swt.tileWidth.text) input.targetTileWidth   = swt.tileWidth.text.toInteger()
      if(swt.tileHeight.text) input.targetTileHeight = swt.tileHeight.text.toInteger()
      input.inputFilenameField = swt.inputFilenameField.text
      input.inputEntryField    = swt.inputEntryField.text
      input.setProjectionType(swt.projectionType.text)
      dispose();
   }

}

