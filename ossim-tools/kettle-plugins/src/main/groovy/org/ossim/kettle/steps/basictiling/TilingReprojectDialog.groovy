package org.ossim.kettle.steps.basictiling

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.pentaho.di.core.Const
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
            label org.ossim.kettle.steps.plugintemplate.Messages.getString("PluginTemplateDialog.Stepname.Label")

            text(id: "stepName", layoutData: "span,growx", text: stepname) {
               onEvent(type: 'Modify') { input.setChanged() }
            }
            label org.ossim.kettle.steps.plugintemplate.Messages.getString("PluginTemplateDialog.ExampleTemplateFieldName.Label")

            text(id: "exampleTemplateFieldName", layoutData: "span,growx", text: "") {
               onEvent(type: 'Modify') { input.setChanged() }
            }
            group(layoutData: "grow, span, wrap") {
               migLayout(layoutConstraints: "insets 2", columnConstraints: "[grow]")
               button(id: "okButton", org.ossim.kettle.steps.plugintemplate.Messages.getString("PluginTemplateDialog.ok.Label"),
                       layoutData: "align center,split 2") {
                  onEvent(type: "Selection") { ok() }
               }
               button(id: "cancelButton", org.ossim.kettle.steps.plugintemplate.Messages.getString("PluginTemplateDialog.cancel.Label"), layoutData: "") {
                  onEvent(type: "Selection") { cancel() }
               }
            }
         }
      }
      changed = input.hasChanged();
      //setModalDialog(true)
      shell.text = org.ossim.kettle.steps.plugintemplate.Messages.getString("PluginTemplateDialog.Shell.Title")
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
