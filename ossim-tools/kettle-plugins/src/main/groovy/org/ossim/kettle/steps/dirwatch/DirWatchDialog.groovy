package org.ossim.kettle.steps.dirwatch

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.pentaho.di.core.Const
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.ui.trans.step.BaseStepDialog

class DirWatchDialog extends BaseStepDialog implements
        StepDialogInterface
{
   private DirWatchMeta input
   private KettleSwtBuilder swt;

   public DirWatchDialog(Shell parent, Object baseStepMeta,
                          TransMeta transMeta, String stepname)
   {
      super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
      input = (DirWatchMeta)baseStepMeta;
   }
   public String open()
   {
      Shell parent = getParent();
      swt = new KettleSwtBuilder()
      shell = swt.shell(parent) {
         migLayout(layoutConstraints:"wrap 1", columnConstraints: "[grow]")
         group(id:"stepNameGroup") {
            migLayout(layoutConstraints: "insets 2", columnConstraints: "[] [grow,50:200:200]")
            label Messages.getString("DirWatchDialog.Stepname.Label")

            text(id: "stepName", layoutData: "span,growx", text: stepname) {
               onEvent(type: 'Modify') { input.setChanged() }
            }
            label Messages.getString("DirWatchDialog.ExampleTemplateFieldName.Label")

            text(id: "exampleTemplateFieldName", layoutData: "span,growx", text: "") {
               onEvent(type: 'Modify') { input.setChanged() }
            }
            group(layoutData: "grow, span, wrap") {
               migLayout(layoutConstraints: "insets 2", columnConstraints: "[grow]")
               button(id: "okButton", Messages.getString("DirWatchDialog.ok.Label"),
                       layoutData: "align center,split 2") {
                  onEvent(type: "Selection") { ok() }
               }
               button(id: "cancelButton", Messages.getString("DirWatchDialog.cancel.Label"), layoutData: "") {
                  onEvent(type: "Selection") { cancel() }
               }
            }
         }
      }
      changed = input.hasChanged();
      //setModalDialog(true)
      shell.text = Messages.getString("DirWatchDialog.Shell.Title")
      getData(); // initialize data fields
      setSize(); // shrink and fit dialog to fit inputs
      input.setChanged(changed);

      shell.doMainloop()

      return stepname;

   }
   private getData()
   {
      swt.exampleTemplateFieldName.text = input.exampleTemplateFieldName?:""
   }
   private void cancel()
   {
      stepname=null;

      dispose();
   }
   private void ok()
   {
      if (Const.isEmpty(swt.stepName.text)) return;

      stepname = swt.stepName.text
      input.exampleTemplateFieldName = swt.exampleTemplateFieldName.text

      dispose();
   }
}
