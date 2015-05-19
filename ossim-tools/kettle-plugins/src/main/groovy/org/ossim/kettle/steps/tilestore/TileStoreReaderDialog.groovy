package org.ossim.kettle.steps.tilestore

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.ui.trans.step.BaseStepDialog

/**
 * Created by gpotts on 5/18/15.
 */
class TileStoreReaderDialog extends BaseStepDialog implements
        StepDialogInterface
{
   private TileStoreReaderMeta input;
   private def swt;
   private DatabaseMeta databaseMeta
   private def layers

   public TileStoreReaderDialog(Shell parent, Object baseStepMeta,
                                  TransMeta transMeta, String stepname) {
      super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
      input = (TileStoreReaderMeta)baseStepMeta;
   }
   public String open()
   {
      Shell parent = getParent();
      Display display = parent.getDisplay();
      swt = new KettleSwtBuilder()
      shell = swt.shell(parent) {
         migLayout(layoutConstraints: "insets 2, wrap 1", columnConstraints: "[grow]")
         // migLayout(layoutConstraints:"wrap 2", columnConstraints: "[] [grow,:200:]")
         //gridLayout(numColumns: 2)

         composite(layoutData: "growx, spanx, wrap") {
            migLayout(layoutConstraints: "insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")

            label Messages.getString("TileStoreCommon.Stepname.Label")
            //text(id:"stepName", text: stepname ,layoutData:"span, growx"){
            text(id: "stepName", layoutData: "span,growx", text: stepname) {
               onEvent(type: 'Modify') { input.setChanged() }
            }
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
      shell.text = Messages.getString("TileStoreReaderDialog.Shell.Title")
      getData(); // initialize data fields
      setSize(); // shrink and fit dialog to fit inputs
      input.setChanged(changed);

      shell.doMainloop()

      return stepname;
   }
   public void getData()
   {
   }
   private void cancel()
   {
      stepname=null;

      dispose();
   }
   private void ok()
   {

      dispose();
   }
}
