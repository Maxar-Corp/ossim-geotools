package org.ossim.kettle.steps.tilestore

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.ossim.kettle.utilities.SwtUtilities
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.row.ValueMetaInterface
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.ui.trans.step.BaseStepDialog

/**
 * Created by gpotts on 5/18/15.
 */
class TileStoreReaderDialog extends TileStoreCommonDialog
{
   private TileStoreReaderMeta input;
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
      swt = kettleSwtBuilder()
      def previousStepStrings = SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING])
      def previousStepInteger = SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_INTEGER])
      shell = swt.shell(parent) {
         migLayout(layoutConstraints: "insets 2, wrap 1", columnConstraints: "[grow]")
         // migLayout(layoutConstraints:"wrap 2", columnConstraints: "[] [grow,:200:]")
         //gridLayout(numColumns: 2)

         composite(layoutData: "growx, spanx, wrap") {
            migLayout(layoutConstraints: "insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")

             stepnameClosure()
             databaseConnectionClosure()
             namedClusterWidgetClosure()
             accumuloConnectionClosure()

            label Messages.getString("TileStoreReaderDialog.LayerName.Label")
            cCombo(id: "layerName", layoutData: "growx",
                    items: previousStepStrings) {
               onEvent(type: 'Selection') {
                  input.changed = true
                  // get and set list of layers

               }
            }
            label Messages.getString("TileStoreReaderDialog.HashId.Label")
            cCombo(id: "hashId", layoutData: "growx",
                    items: previousStepStrings) {
               onEvent(type: 'Selection') {
                  input.changed = true
               }
            }
         }
         okCancelClosure()
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
      swt.stepname       = stepname
      swt.layerName.text = input.layerName?:""
      swt.hashId.text    = input.hashId?:""
   }
   private void cancel()
   {
      stepname=null;

      dispose();
   }
   private void ok()
   {

      if (!swt.stepName.text) return;

      stepname = swt.stepName.text
      input.layerName = swt.layerName.text
      input.hashId     = swt.hashId.text

      dispose();
   }
}
