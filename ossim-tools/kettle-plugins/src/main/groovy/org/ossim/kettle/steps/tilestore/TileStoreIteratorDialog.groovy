package org.ossim.kettle.steps.tilestore

import joms.geotools.tileapi.hibernate.TileCacheHibernate
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.CCombo
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Item
import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Table
import org.eclipse.swt.widgets.TableItem
import org.eclipse.swt.widgets.Text
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.ossim.kettle.steps.tilestore.Messages
import org.ossim.kettle.types.OssimValueMetaBase
import org.ossim.kettle.utilities.SwtUtilities
import org.ossim.omar.utilities.KettleUtilities
import org.pentaho.di.core.Const
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.namedcluster.model.NamedCluster
import org.pentaho.di.core.row.ValueMetaInterface
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog
import org.pentaho.di.ui.core.widget.ColumnInfo
import org.pentaho.di.ui.trans.step.BaseStepDialog
import org.pentaho.di.ui.core.namedcluster.NamedClusterWidget;

/**
 * Created by gpotts on 5/14/15.
 */
class TileStoreIteratorDialog  extends BaseStepDialog implements
        StepDialogInterface
{
   private TileStoreIteratorMeta input;
   private def swt;
   private DatabaseMeta databaseMeta
   private def layers
   public TileStoreIteratorDialog(Shell parent, Object baseStepMeta,
                                TransMeta transMeta, String stepname) {
      super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
      input = (TileStoreIteratorMeta)baseStepMeta;
   }
   public String open()
   {
      Shell parent = getParent();
      Display display = parent.getDisplay();
      swt = new KettleSwtBuilder()
      def lsSelect = { event ->
         SelectionEvent selectionEvent = event as SelectionEvent
         //println selectionEvent.widget.text
         input.setChanged()

      } as SelectionListener
      def lsMod = {
         event ->
            def tableView = event.source.parent.parent
             def widgetSource = event.source

            if (widgetSource instanceof Text)
            {
               //String newValue = "${event.widget.text}".toString()
              // String key = item.getText(1)

              // outputFieldNames."${key}" = newValue
            }
            else if(widgetSource instanceof CCombo)
            {
               def row = tableView?.getCurrentRownr()
               Table table = event.source.parent as Table
               TableItem item = table.getItem(row)
               def key = "${widgetSource.text}"
               def renameValue = input.outputFieldNames."${key}"
               item.setText(1, key)
               item.setText(2, renameValue)
               //def indexOfValue = ccombo.indexOf(ccombo.text)
             //  def renameValue = outputFieldNames."${key}"

             //  println "RENAME VALUE ==== ${renameValue}"
             //  item.setText(1, key)
             //  item.setText(2, renameValue)
            }
      } as ModifyListener
      ColumnInfo[] colinf = new ColumnInfo[2];
      colinf[0] =
              new ColumnInfo(Messages.getString("TileStoreIteratorDialog.ColumnInfo.Fieldname"),
                      ColumnInfo.COLUMN_TYPE_CCOMBO, input.outputFieldNames.collect { k, v -> v }.sort() as String[],
                      true);
      colinf[1] =
              new ColumnInfo(Messages.getString("TileStoreIteratorDialog.ColumnInfo.RenameTo"),
                      ColumnInfo.COLUMN_TYPE_TEXT, false);
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
            composite(id:"connectionLayoutId", style:"none", layoutData:"span,growx") {
               migLayout(layoutConstraints: "inset 0", columnConstraints: "[][][][]")
               label Messages.getString("TileStoreWriterDialog.Connection.Label")
               cCombo(id: "connectionList", layoutData: "growx", items: transMeta.databaseNames) {
                  onEvent(type: 'Selection') {
                     int idx = swt.connectionList.indexOf(swt.connectionList.text);
                     databaseMeta = transMeta.getDatabase(idx)
                     // get and set list of layers

                  }
               }
               button(id: "editConnection",
                       text: Messages.getString("TileStoreWriterDialog.EditConnection.Label"),
                       layoutData: "growx") {
                  onEvent(type: "Selection") {
                     int idx = swt.connectionList.indexOf(swt.connectionList.text);
                     if (databaseMeta) {
                        DatabaseDialog cid = getDatabaseDialog(shell);
                        cid.setDatabaseMeta(databaseMeta);
                        cid.setModalDialog(true);
                        if (cid.open() != null) {
                           input.setChanged()
                        }
                     }
                  }
               }
               button(id: "newConnection",
                       text: Messages.getString("TileStoreWriterDialog.NewConnection.Label"),
                       layoutData: "growx,wrap") {
                  onEvent(type: "Selection") {
                     DatabaseMeta databaseMetaTemp = new DatabaseMeta();
                     databaseMetaTemp.shareVariablesWith(transMeta);
                     DatabaseDialog cid = getDatabaseDialog(shell);
                     cid.setDatabaseMeta(databaseMetaTemp);
                     cid.setModalDialog(true);
                     if (cid.open() != null) {
                        databaseMeta = databaseMetaTemp
                        transMeta.addDatabase(databaseMeta);
                        swt.connectionList.removeAll()
                        swt.connectionList.items = transMeta.databaseNames
                        int idx = swt.connectionList.indexOf(databaseMeta.name);
                        idx < 0 ?: swt.connectionList.select(idx)
                     }
                  }
               }
            }
            composite(style:"none", layoutData:"span,growx") {
               migLayout(layoutConstraints:"inset 0", columnConstraints: "[grow]")
               myNamedClusterWidget(id: "namedClusterWidgetId", showLabel:true,
                       selectionListener:lsSelect, style:"none") {
               }
            }

            group(id:"accumulGroupId", text:"Accumulo Connection", style:"none", layoutData:"span,growx") {
               migLayout(layoutConstraints:"inset 0", columnConstraints: "[][grow,50:100:200]")
               label Messages.getString("TileStoreWriterDialog.InstanceName.Label")
               text(id: "accumuloInstance", layoutData: "span,growx") {
                  onEvent(type: 'Modify') { input.setChanged() }
               }
               label Messages.getString("TileStoreWriterDialog.Username.Label")
               text(id: "accumuloUsername", layoutData: "span,growx") {
                  onEvent(type: 'Modify') { input.setChanged() }
               }
               label Messages.getString("TileStoreWriterDialog.Password.Label")
               text(id: "accumuloPassword", layoutData: "span,growx", style:"PASSWORD") {
                  onEvent(type: 'Modify') { input.setChanged() }
               }
               label Messages.getString("TileStoreWriterDialog.PasswordVerify.Label")
               text(id: "accumuloPasswordVerify", layoutData: "span,growx", style:"PASSWORD") {
                  onEvent(type: 'Modify') { input.setChanged() }
               }
            }
            label Messages.getString("TileStoreIteratorDialog.LayerName.Label")
            cCombo(id: "layerName",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }
            label Messages.getString("TileStoreIteratorDialog.Aoi.Label")
            cCombo(id: "aoi",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [OssimValueMetaBase.TYPE_GEOMETRY_2D]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }
            label Messages.getString("TileStoreIteratorDialog.AoiEpsg.Label")
            cCombo(id: "aoiEpsg",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }
            label Messages.getString("TileStoreIteratorDialog.MinLevel.Label")
            cCombo(id: "minLevel",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_INTEGER,
                                                                                 ValueMetaInterface.TYPE_STRING]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }
            //text(id:"clampMinLevel"){
            //   onEvent(type:'Modify') { input.setChanged() }
            //}
            label Messages.getString("TileStoreIteratorDialog.MaxLevel.Label")
            cCombo(id: "maxLevel",
                    items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_INTEGER,
                                                                                 ValueMetaInterface.TYPE_STRING]),
                    layoutData: "span,growx")
                    {
                       onEvent(type: 'Modify') { input.setChanged(); }
                    }
         }
         group(text:"Output Fields", layoutData:"grow, spanx") {
            migLayout(layoutConstraints: "insets 2", columnConstraints: "[grow]")
            tableView(id: "fieldSelection",
                    transMeta: transMeta,
                    nrRows: 1,
                    columnInfo: colinf,
                    style: "BORDER,FULL_SELECTION,MULTI,V_SCROLL,H_SCROLL",
                    propsUi: props,
                    layoutData: "height 200:200:200, span,growx, wrap",
                    modifyListener: lsMod)
            group(layoutData:"span,growx"){
              migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow]")
              button(id: "getAllFields",
                      text: Messages.getString("TileStoreIteratorDialog.getAllFields.label"),
                      layoutData:"align center,skip 1,split 2") {
                 onEvent(type: "Selection") {
                    loadAllFields()
                 }
              }
              button(id: "clearAllFields",
                      layoutData: "",
                      text:Messages.getString("TileStoreIteratorDialog.clearAllFields.label")) {
                 onEvent(type: "Selection") {
                    clearAllFields()
                 }
              }
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
      shell.text = Messages.getString("TileStoreIteratorDialog.Shell.Title")
      getData(); // initialize data fields
      setSize(); // shrink and fit dialog to fit inputs
      input.setChanged(changed);

      shell.doMainloop()

      return stepname;
   }
   def getLayerList()
   {
      if(layers) return layers as String[]
      layers = []
      if(databaseMeta)
      {
         def map = KettleUtilities.convertDatabaseMetaToMap(databaseMeta)
         map.dbCreate="update"

         TileCacheHibernate hibernate = new TileCacheHibernate()
         hibernate.initialize(map)

         def layerInfo = hibernate.applicationContext.getBean("tileCacheLayerInfoDAO")

         layerInfo.list().each{
            layers << it.name
         }

         hibernate?.shutdown()
      }

      layers as String[]
   }
   public void getData()
   {
      databaseMeta = input.tileStoreCommon.databaseMeta
      swt.stepName.selectAll();
      if(databaseMeta?.name)
      {
         int idx = swt.connectionList.indexOf(databaseMeta.name);
         idx<0?:swt.connectionList.select(idx)
      }

      swt.accumuloInstance.text = input.tileStoreCommon.accumuloInstance?:""
      swt.accumuloUsername.text = input.tileStoreCommon.accumuloUsername?:""
      swt.accumuloPassword.text = input.tileStoreCommon.accumuloPassword?:""
      swt.accumuloPasswordVerify.text = input.tileStoreCommon.accumuloPassword?:""
      swt.layerName.text = input.layerName?:""
      swt.aoi.text = input.aoi?:""
      swt.aoiEpsg.text = input.aoiEpsg?:""
      swt.minLevel.text = input.minLevel?:""
      swt.maxLevel.text = input.maxLevel?:""

      if(input.tileStoreCommon.clusterName)
      {
         swt.namedClusterWidgetId.setSelectedNamedCluster(input.tileStoreCommon.clusterName)
      }

      loadSelectedFields();
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
   private void cancel()
   {
      stepname=null;

      dispose();
   }
   private void clearAllFields() {
      def tableView = swt.fieldSelection
      tableView.table.clearAll()
      tableView.table.setItemCount(1)
      tableView.table.getItem(0).setText(0, "1")
   }

   private void ok()
   {
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

      input.layerName = swt.layerName.text?:""
      input.aoi = swt.aoi.text?:""
      input.aoiEpsg = swt.aoiEpsg.text?:""
      input.minLevel = swt.minLevel.text?:""
      input.maxLevel = swt.maxLevel.text?:""

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
