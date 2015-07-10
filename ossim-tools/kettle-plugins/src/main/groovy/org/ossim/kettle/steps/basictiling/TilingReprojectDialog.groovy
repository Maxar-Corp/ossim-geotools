package org.ossim.kettle.steps.basictiling

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.ossim.kettle.types.OssimValueMetaBase
import org.ossim.kettle.utilities.SwtUtilities
import org.pentaho.di.core.Const
import org.pentaho.di.core.row.ValueMetaInterface
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.ui.trans.step.BaseStepDialog

/**
 * Created by gpotts on 6/22/15.
 */
class TilingReprojectDialog extends BaseStepDialog implements
        StepDialogInterface
{
   private TilingReprojectMeta input
   private KettleSwtBuilder swt;

   public TilingReprojectDialog(Shell parent, Object baseStepMeta,
                          TransMeta transMeta, String stepname)
   {
      super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
      input = (TilingReprojectMeta)baseStepMeta;
   }
   public String open()
   {
      Shell parent = getParent();
      Display display = parent.getDisplay();
      swt = new KettleSwtBuilder()
      shell = swt.shell(parent) {
         migLayout(layoutConstraints:"wrap 1", columnConstraints: "[grow]")
         group(id:"stepNameGroup") {
            migLayout(layoutConstraints: "insets 2", columnConstraints: "[] [grow,50:200:200]")
            label Messages.getString("BasicTilingDialog.Stepname.Label")

            text(id: "stepName", layoutData: "span,growx", text: stepname) {
               onEvent(type: 'Modify') { input.setChanged() }
            }
            label Messages.getString("TilingReprojectDialog.SourceEpsg.Label")
            cCombo(id: "sourceEpsgField",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }
            label Messages.getString("TilingReprojectDialog.SourceAoi.Label")
            cCombo(id: "sourceAoiField",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_GEOMETRY_2D]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }
            label Messages.getString("TilingReprojectDialog.SourceMinLevel.Label")
            cCombo(id: "sourceMinLevelField",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_INTEGER]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }
            label Messages.getString("TilingReprojectDialog.SourceMaxLevel.Label")
            cCombo(id: "sourceMaxLevelField",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_INTEGER]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }
            label Messages.getString("TilingReprojectDialog.TargetEpsg.Label")
            cCombo(id: "targetEpsgField",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }

            group(layoutData: "grow, span, wrap") {
               migLayout(layoutConstraints: "insets 2", columnConstraints: "[grow]")
               button(id: "okButton", Messages.getString("BasicTilingDialog.ok.Label"),
                       layoutData: "align center,split 2") {
                  onEvent(type: "Selection") { ok() }
               }
               button(id: "cancelButton", Messages.getString("BasicTilingDialog.cancel.Label"), layoutData: "") {
                  onEvent(type: "Selection") { cancel() }
               }
            }
         }
      }
      changed = input.hasChanged();
      //setModalDialog(true)
      shell.text = Messages.getString("BasicTilingDialog.Shell.Title")
      getData(); // initialize data fields
      setSize(); // shrink and fit dialog to fit inputs
      input.setChanged(changed);

      shell.doMainloop()

      return stepname;

   }
   private getData()
   {
      swt.sourceEpsgField.text     = input.sourceEpsgField?:""
      swt.sourceAoiField.text      = input.sourceAoiField?:""
      swt.sourceMinLevelField.text = input.sourceMinLevelField?:""
      swt.sourceMaxLevelField.text = input.sourceMaxLevelField?:""
      swt.targetEpsgField.text     = input.targetEpsgField?:""
   }
   private void cancel()
   {
      stepname=null;

      dispose();
   }
   private void ok()
   {
      if (Const.isEmpty(swt.stepName.text)) return;

      input.setChanged(changed)

      stepname                  = swt.stepName.text
      input.sourceEpsgField     = swt.sourceEpsgField.text
      input.sourceAoiField      = swt.sourceAoiField.text
      input.sourceMinLevelField = swt.sourceMinLevelField.text
      input.sourceMaxLevelField = swt.sourceMaxLevelField.text
      input.targetEpsgField     = swt.targetEpsgField.text

      dispose();
   }
}
