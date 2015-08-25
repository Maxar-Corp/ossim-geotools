package org.ossim.kettle.steps.tilestore

import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.TableItem
import org.ossim.kettle.utilities.SwtUtilities
import org.pentaho.di.core.database.DatabaseMeta
//import org.pentaho.di.core.namedcluster.model.NamedCluster


import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.ui.core.widget.ColumnInfo

//import org.pentaho.big.data.api.cluster.NamedCluster;

import org.ossim.kettle.common.NamedCluster


/**
 * Created by gpotts on 7/6/15.
 */
class ReprojectDialog extends TileStoreCommonDialog
{
   ReprojectMeta input
   private DatabaseMeta databaseMeta
   private def fields = ["layers":"inputLayersField",
                         "minx":"inputTileMinXField",
                         "miny":"inputTileMinYField",
                         "maxx":"inputTileMaxXField",
                         "maxy":"inputTileMaxYField",
                         "epsg":"inputEpsgCodeField",
                         "width":"inputTileWidthField",
                         "height":"inputTileHeightField",
   ]

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
      def lsMod = {
         event -> changed = true
      } as ModifyListener
      ColumnInfo[] colinf = new ColumnInfo[2];
      colinf[0] =
              new ColumnInfo( Messages.getString("TileStoreReprojectDialog.ColumnInfo.Parameter" ),
                      ColumnInfo.COLUMN_TYPE_TEXT, false,
                      true );
      colinf[1] =
              new ColumnInfo( Messages.getString("TileStoreReprojectDialog.ColumnInfo.InputField" ),
                      ColumnInfo.COLUMN_TYPE_CCOMBO, SwtUtilities.previousStepFields(transMeta, stepname),
                      true );
      shell = swt.shell(parent) {
         migLayout(layoutConstraints: "insets 2, wrap 1", columnConstraints: "[grow]")
         composite(layoutData: "growx, spanx, wrap") {
            migLayout(layoutConstraints: "insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")

            stepnameClosure()
            databaseConnectionClosure()
            namedClusterWidgetClosure()
            accumuloConnectionClosure()
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
   }

   private void cancel()
   {
      stepname=null;

      dispose();
   }
   private void ok()
   {
      def namedCluster =  swt.namedClusterWidgetId.selectedNamedCluster

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

      def tableView = swt.fieldMappings

      tableView.table.items.each{item->
         def key = fields."${item.getText(1)}"
         def value = item.getText(2)
         input."${key}" = value
      }

      dispose();

   }
}
