package org.ossim.kettle.steps.geopackagewriter

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.ossim.kettle.types.OssimValueMetaBase
import org.ossim.kettle.utilities.SwtUtilities
import org.pentaho.di.core.Const
import org.pentaho.di.core.row.value.ValueMetaBase
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.ui.trans.step.BaseStepDialog

/**
 * Created by gpotts on 5/27/15.
 */
class GeoPkgWriterDialog extends BaseStepDialog implements
        StepDialogInterface
{
   private GeoPkgWriterMeta input;
   private def swt;

   public GeoPkgWriterDialog(Shell parent, Object baseStepMeta,
                             TransMeta transMeta, String stepname)
   {
      super(parent, (BaseStepMeta) baseStepMeta, transMeta, stepname);
      input = (GeoPkgWriterMeta) baseStepMeta;
   }

   public String open()
   {
      Shell parent = getParent();
      Display display = parent.getDisplay();
      swt = new KettleSwtBuilder()
      shell = swt.shell(parent) {
         migLayout(layoutConstraints: "insets 2, wrap 1", columnConstraints: "[grow]")
         group(layoutData: "span,growx") {
            migLayout(layoutConstraints: "insets 2, wrap 2", columnConstraints: "[] [grow]")
            //gridLayout(numColumns: 2)
            label Messages.getString("GeoPackageWriterDialog.Stepname.Label")

            //text(id:"stepName", text: stepname ,layoutData:"span, growx"){
            text(id: "stepName", layoutData: "span,growx", text: stepname) {
               onEvent(type: 'Modify') { input.setChanged() }
            }
         }
         group(id: "tileGroupId", text: "Tile definitions", style: "none", layoutData: "span,growx") {
            migLayout(layoutConstraints: "insets 2, wrap 2", columnConstraints: "[] [grow]")
            label "Tile Level"
            cCombo(id: "tileLevelField",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_INTEGER]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') {
                          changed=true
                       }
                    }
            label "Tile Row"
            cCombo(id: "tileRowField",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_INTEGER]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') {
                          changed=true
                       }
                    }
            label "Tile Col"
            cCombo(id: "tileColField",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_INTEGER]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') {
                          changed=true
                       }
                    }
            label "Tile Image"
            cCombo(id: "tileImageField",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_IMAGE, OssimValueMetaBase.TYPE_CLONABLE_IMAGE]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') {
                          changed=true
                       }
                    }
         }
         group(id: "geopackageDefinitionGroupId", text: "Geopackage Definitions", style: "none", layoutData: "span,growx") {
            migLayout(layoutConstraints: "insets 2, wrap 2", columnConstraints: "[] [grow]")
            label "Group Id"
            cCombo(id:"groupField",
                    items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_STRING, OssimValueMetaBase.TYPE_INTEGER]),
                    layoutData:"span,growx")
                    {
                       onEvent(type:'Modify') {
                          changed=true
                       }
                    }

            label "Filename"
            cCombo(id:"filenameField",
                    items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_STRING]),
                    style:"READ_ONLY",
                    layoutData:"span,growx")
                    {
                       onEvent(type:'Modify') {
                          changed=true
                       }
                    }
            label "Layer Name"
            cCombo(id:"layerNameField",
                    items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_STRING]),
                    layoutData:"span,growx")
                    {
                       onEvent(type:'Modify') {
                          input.setChanged()
                       }
                    }
            label "Epsg Code"
            cCombo(id:"epsgCodeField",
                    items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_STRING]),
                    layoutData:"span,growx")
                    {
                       onEvent(type:'Modify') {
                          changed=true
                       }
                    }
            label "Min Level"
            cCombo(id:"minLevelField",
                    items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_INTEGER]),
                    layoutData:"span,growx")
                    {
                       onEvent(type:'Modify') {
                          changed=true
                       }
                    }
            label "Max Level"
            cCombo(id:"maxLevelField",
                    items:SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_INTEGER]),
                    style:"READ_ONLY",
                    layoutData:"span,growx")
                    {
                       onEvent(type:'Modify') {
                          changed=true
                       }
                    }
            label "Writer Mode"
            cCombo(id:"writerMode",
                    items:["mixed", "jpeg", "png", "pnga"],
                    layoutData:"span,growx")
                    {
                       onEvent(type:'Modify') {
                          changed=true
                       }
                    }
         }

         group(layoutData: "span,growx") {
            migLayout(layoutConstraints: "insets 2, wrap 2", columnConstraints: "[] [grow]")
            button("Ok", layoutData: "align center,skip 1,split 2") {
               onEvent(type: "Selection") { ok() }
            }
            button("Cancel", layoutData: "") {
               onEvent(type: "Selection") { cancel() }
            }

         }

      }
      changed = input.hasChanged();

      shell.text = Messages.getString("GeoPackageWriterDialog.Shell.Title")
      getData(); // initialize data fields
      setSize(); // shrink and fit dialog to fit inputs
      input.setChanged(changed)
      shell.doMainloop()

      return stepname;
   }

   public void getData()
   {
      swt.stepName.selectAll()

      swt.tileLevelField.text = input.tileLevelField?:""
      swt.tileRowField.text   = input.tileRowField?:""
      swt.tileColField.text   = input.tileColField?:""
      swt.tileImageField.text = input.tileImageField?:""
      swt.groupField.text     = input.groupField?:""
      swt.filenameField.text  = input.filenameField?:""
      swt.layerNameField.text = input.layerNameField?:""
      swt.epsgCodeField.text  = input.epsgCodeField?:""
      swt.minLevelField.text  = input.minLevelField?:""
      swt.maxLevelField.text  = input.maxLevelField?:""
      swt.writerMode.text     = input.writerMode?:""
   }
   private void cancel()
   {
      stepname=null
      //input.setChanged(changed);

      dispose()
   }

   private void ok()
   {
      if (Const.isEmpty(swt.stepName.text)) return;

      input.setChanged(changed)

      stepname                  = swt.stepName.text
      input.tileLevelField = swt.tileLevelField.text
      input.tileRowField   = swt.tileRowField.text
      input.tileColField   = swt.tileColField.text
      input.tileImageField = swt.tileImageField.text
      input.groupField     = swt.groupField.text
      input.filenameField  = swt.filenameField.text
      input.layerNameField = swt.layerNameField.text
      input.epsgCodeField  = swt.epsgCodeField.text
      input.minLevelField  = swt.minLevelField.text
      input.maxLevelField  = swt.maxLevelField.text
      input.writerMode     = swt.writerMode.text
      dispose()
   }
}