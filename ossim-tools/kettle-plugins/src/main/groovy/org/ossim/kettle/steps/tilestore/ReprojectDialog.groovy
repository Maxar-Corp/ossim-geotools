package org.ossim.kettle.steps.tilestore

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.namedcluster.model.NamedCluster
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta

/**
 * Created by gpotts on 7/6/15.
 */
class ReprojectDialog extends TileStoreCommonDialog
{
   ReprojectMeta input
   private DatabaseMeta databaseMeta
   public ReprojectDialog(Shell parent, Object baseStepMeta,
                                  TransMeta transMeta, String stepname) {
      super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
      input = (ReprojectMeta)baseStepMeta;
   }
   public String open()
   {
      Shell parent = getParent();
      Display display = parent.getDisplay();
      kettleSwtBuilder()
      shell = swt.shell(parent) {
         migLayout(layoutConstraints: "insets 2, wrap 1", columnConstraints: "[grow]")
         composite(layoutData: "growx, spanx, wrap") {
            migLayout(layoutConstraints: "insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")

            stepnameClosure()
            databaseConnectionClosure()
            namedClusterWidgetClosure()
            accumuloConnectionClosure()
         }

         okCancelClosure()

      }
      changed = input.hasChanged();
      shell.text = Messages.getString("TileStoreReprojectDialog.Shell.Title")
      getData(); // initialize data fields
      setSize(); // shrink and fit dialog to fit inputs
      input.setChanged(changed);

      shell.doMainloop()

      return stepname;
   }
   private void getData()
   {
      databaseMeta = input.tileStoreCommon.databaseMeta
      swt.stepName.selectAll();
      if(databaseMeta?.name)
      {
         int idx = swt.connectionList.indexOf(databaseMeta.name);
         idx<0?:swt.connectionList.select(idx)
      }

      swt.accumuloInstance.text       = input.tileStoreCommon.accumuloInstance?:""
      swt.accumuloUsername.text       = input.tileStoreCommon.accumuloUsername?:""
      swt.accumuloPassword.text       = input.tileStoreCommon.accumuloPassword?:""
      swt.accumuloPasswordVerify.text = input.tileStoreCommon.accumuloPassword?:""

      if(input.tileStoreCommon.clusterName)
      {
         swt.namedClusterWidgetId.setSelectedNamedCluster(input.tileStoreCommon.clusterName)
      }
   }

   private void cancel()
   {
      stepname=null;

      dispose();
   }
   private void ok()
   {
      NamedCluster namedCluster =  swt.namedClusterWidgetId.selectedNamedCluster

      if(namedCluster)
      {
         input.tileStoreCommon.clusterName      = namedCluster.name
         input.tileStoreCommon.zookeeperHosts   = namedCluster.zooKeeperHost
         input.tileStoreCommon.zookeeperPort    = namedCluster.zooKeeperPort

      }
      input.tileStoreCommon.databaseMeta = databaseMeta

      input.tileStoreCommon.accumuloInstance = swt.accumuloInstance.text
      input.tileStoreCommon.accumuloUsername = swt.accumuloUsername.text
      input.tileStoreCommon.accumuloPassword = swt.accumuloPassword.text

      dispose();

   }
}
