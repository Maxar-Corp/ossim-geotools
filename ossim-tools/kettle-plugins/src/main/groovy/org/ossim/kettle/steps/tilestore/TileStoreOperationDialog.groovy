package org.ossim.kettle.steps.tilestore

import joms.geotools.tileapi.hibernate.TileCacheHibernate
import org.eclipse.swt.SWT
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.TableItem
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.ossim.kettle.types.OssimValueMetaBase
import org.ossim.kettle.utilities.SwtUtilities
import org.ossim.omar.utilities.KettleUtilities
import org.pentaho.di.core.Const
import org.pentaho.di.core.database.DatabaseMeta
//import org.pentaho.di.core.namedcluster.model.NamedCluster
//import org.pentaho.big.data.api.cluster.NamedCluster;

import org.pentaho.di.core.row.ValueMetaInterface
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog
import org.pentaho.di.ui.core.widget.ColumnInfo
import org.pentaho.di.ui.trans.step.BaseStepDialog

/**
 * Created by gpotts on 3/24/15.
 */
class TileStoreOperationDialog extends BaseStepDialog implements
        StepDialogInterface
{
   private TileStoreOperationMeta input;
   private def swt;
   private DatabaseMeta databaseMeta
   private def layers = []
   private def createLayerColumnFields = ["minx":"layerMinx",
                                     "miny":"layerMiny",
                                     "maxx":"layerMaxx",
                                     "maxy":"layerMaxy",
                                     "wkt_bounds":"layerWktBounds",
                                     "epsg":"layerEpsg",
                                     "min_level":"layerMinLevel",
                                     "max_level":"layerMaxLevel",
                                     "epsg":"layerEpsg",
                                     "tile_width":"layerTileWidth",
                                     "tile_height":"layerTileHeight"
   ]

   public TileStoreOperationDialog(Shell parent, Object baseStepMeta,
                                TransMeta transMeta, String stepname) {
      super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
      input = (TileStoreOperationMeta)baseStepMeta;
   }
   public String open() {
      Shell parent    = getParent();
      Display display = parent.getDisplay();
      swt = new KettleSwtBuilder()
      def lsSelect = { event ->
         SelectionEvent selectionEvent = event as SelectionEvent
         //println selectionEvent.widget.text
         input.setChanged()

      } as SelectionListener
      //NamedCluster nc = namedClusterWidget.getSelectedNamedCluster();
      def createLayerColMod = {
         event -> input.setChanged()
      } as ModifyListener
      ColumnInfo[] createLayerColInfo = new ColumnInfo[2];
      createLayerColInfo[0] =
              new ColumnInfo( Messages.getString("TileStoreWriterDialog.ColumnInfo.Parameter" ),
                      ColumnInfo.COLUMN_TYPE_TEXT, false,
                      true );
      createLayerColInfo[1] =
              new ColumnInfo( Messages.getString("TileStoreWriterDialog.ColumnInfo.InputField" ),
                      ColumnInfo.COLUMN_TYPE_CCOMBO, SwtUtilities.previousStepFields(transMeta, stepname),
                      true );

      shell = swt.shell(parent){
         migLayout(layoutConstraints:"", columnConstraints: "[grow]")
         //gridLayout(numColumns: 2)
         composite(style:"none", layoutData:"span,growx") {
            migLayout(layoutConstraints:"inset 0", columnConstraints: "[][grow,50:100:200]")
            label Messages.getString("TileStoreWriterDialog.Stepname.Label")
            text(id: "stepName", layoutData: "span,growx", text: stepname) {
               onEvent(type: 'Modify') { input.setChanged() }
            }
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
            myNamedClusterWidget(id: "namedClusterWidgetId", showLabel:true, selectionListener:lsSelect, style:"none") {
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

         group("Layer Name", layoutData:"grow, spanx, wrap", style:"SHADOW_NONE,NO_BACKGROUND"){
            migLayout(layoutConstraints:"insets 2, wrap 1", columnConstraints: "[grow]")
            radioButton (id:"layerFromFieldGroupButton", text:"From input field", layoutData:"split 2", selection:true){
               onEvent(type:"Selection"){
                  swt.stackLayout.topControl = swt.layerFromFieldGroup
                  swt.layerFromFieldGroup.visible = true
                  swt.layerFromTextGroup.visible = false
               }
            }
            radioButton (id:"layerFromTextGroupButton", text:"Entered", layoutData:"wrap"){
               onEvent(type:"Selection"){
                  swt.stackLayout.topControl = swt.layerFromTextGroup
                  swt.layerFromFieldGroup.visible = false
                  swt.layerFromTextGroup.visible = true
                  def tempText = swt.layerName.text
                  swt.layerName.removeAll()
                  swt.layerName.items = getLayerList()
                  def v = layers.find{it==tempText}
                  if(v) swt.layerName.text = tempText
               }
            }

            composite(style:"none", layoutData:"span,growx"){
               stackLayout(id:"stackLayout");
               composite(id:"layerFromFieldGroup", style:"none", layoutData:"insets 0, span,growx"){
                  migLayout(layoutConstraints:"insets 0, wrap 2", columnConstraints: "[][grow]")
                  label (id:"layerFieldLabel", text:"Layer Field")
                  cCombo(id:"layerFieldName",
                          items:SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaInterface.TYPE_STRING]),
                          layoutData:"span,growx")
                          {
                             onEvent(type:'Modify') { input.setChanged(); }
                          }
               }
               composite(id:"layerFromTextGroup", style:"none", layoutData:"span,growx"){
                  migLayout(layoutConstraints:"insets 0, wrap 2", columnConstraints: "[][grow]")
                  label "Layer Name"
                  cCombo(id: "layerName", layoutData: "span,growx") {
                     onEvent(type: 'Modify') { input.setChanged() }
                  }
                  //canvas(id:"boxSelectionCanvas", layoutData:"span, grow"){
                  //	onEvent("MouseMove"){ event->
                  //		println "MOUSE MOVED!!!!"
                  //	}
                  //}
                  //label (id:"testLabel2", text:"Not implemented yet",layoutData:"span,growx")
               }
            }
         }

         group("Operation", layoutData:"grow, spanx, wrap", style:"SHADOW_NONE,NO_BACKGROUND") {
            migLayout(layoutConstraints:"insets 0, wrap 2", columnConstraints: "[] [grow]")
            label "Operation type field"
            cCombo(id: "operationTypeField",
                    layoutData: "span,growx",
                    text:TileStoreCommonData.TileStoreOpType.CREATE_LAYER.toString(),
                     items:TileStoreCommonData.TileStoreOpType.valuesAsString()){
               onEvent(type: 'Modify') {
                  changeStackLayoutOperation(swt.operationTypeField.text)
                  input.setChanged()
               }
            }
            composite(style:"none", layoutData:"span,growx"){
               stackLayout(id:"stackLayoutOperation");
               composite(id:"CREATE_LAYER", style:"none", layoutData:"insets 0, span,growx"){
                  migLayout(layoutConstraints:"insets 0, wrap 1", columnConstraints: "[grow]")
                  tableView(id:"createLayerFields",
                          transMeta:transMeta,
                          nrRows:11,
                          columnInfo:createLayerColInfo,
                          style:"BORDER,FULL_SELECTION,MULTI,V_SCROLL,H_SCROLL",
                          propsUi:props,
                          //layoutData:"height 100:100:200, w 200:200:200, span,wrap",
                          layoutData:"span, growx",
                          modifyListener:createLayerColMod)
               }
               composite(id:"nullLayoutOperationGroupId", style:"none", layoutData:"insets 0, span,growx") {
                  migLayout(layoutConstraints:"insets 0, wrap 1", columnConstraints: "[grow]")
                  label "No Extra parameters required for selection"
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
      changeStackLayoutOperation("CREATE_LAYER")
      shell.text = Messages.getString("TileStoreOperationDialog.Shell.Title")
      getData(); // initialize data fields
      setSize(); // shrink and fit dialog to fit inputs
      input.setChanged(changed);

      shell.doMainloop()

      return stepname;
   }

   void changeStackLayoutOperation(String type)
   {

      def opType = TileStoreCommonData.TileStoreOpType."${type.toUpperCase()}"

      switch(opType)
      {
         case TileStoreCommonData.TileStoreOpType.CREATE_LAYER:
            swt.stackLayoutOperation.topControl = swt.CREATE_LAYER
            swt.CREATE_LAYER.visible = true
            swt.nullLayoutOperationGroupId.visible = false
            break
         default:
            swt.stackLayoutOperation.topControl = swt.nullLayoutOperationGroupId
            swt.CREATE_LAYER.visible = false
            swt.nullLayoutOperationGroupId.visible = true
            break
      }

   }
   void clearFields()
   {
      def tableView = swt.createLayerFields
      tableView.table.clearAll()
      tableView.table.setItemCount(createLayerColumnFields.size())
      def idx = 0
      createLayerColumnFields.each{k,v->
         TableItem item = tableView.table.getItem(idx);
         item.setText(1, k);
         item.setText(2, "");
         ++idx
      }
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
      if(input.layerFieldName)
      {
         swt.layerFromFieldGroupButton.selection = true
         swt.layerFromTextGroupButton.selection = false
         swt.stackLayout.topControl = swt.layerFromFieldGroup
         swt.layerFieldName.text = input.layerFieldName
      }
      else if(input.layerName)
      {
         swt.layerFromFieldGroupButton.selection = false
         swt.layerFromTextGroupButton.selection = true
         swt.stackLayout.topControl = swt.layerFromTextGroup
         swt.layerName.items = getLayerList()
         swt.layerName.text  = input.layerName
      }
      else if(input.layerFieldName)
      {
         swt.layerFromFieldGroupButton.selection = true
         swt.layerFromTextGroupButton.selection = false
         swt.stackLayout.topControl = swt.layerFromFieldGroup
         swt.layerFieldName.text = input.layerFieldName?:""
      }
      else
      {
         swt.layerFromFieldGroupButton.selection = false
         swt.layerFromTextGroupButton.selection = true
         swt.stackLayout.topControl = swt.layerFromTextGroup
         swt.layerName.items = getLayerList()
         swt.layerName.text  = ""
      }
      if(input.tileStoreCommon.clusterName)
      {
         swt.namedClusterWidgetId.selectedNamedCluster = input.tileStoreCommon.clusterName
      }

      changeStackLayoutOperation(input.operationType.toString())

      clearFields()
   }
   void cancel()
   {
      //input.setChanged(changed);

      dispose();
   }

   void ok()
   {
      if (Const.isEmpty(swt.stepName.text))
      {
         def mb     = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
         mb.message = Messages.getString("Stepname can't be empty");
         mb.text    = Messages.getString("Stepname Empty")
         mb.open()

         return
      };
      if(swt.accumuloPassword.text != swt.accumuloPasswordVerify.text)
      {
         def mb     = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
         mb.message = Messages.getString("TileStoreWriterDialog.PasswordDontMatch.DialogMessage");
         mb.text    = Messages.getString("TileStoreWriterDialog.PasswordDontMatch.DialogTitle")
         mb.open()
         return
      }

      stepname = swt.stepName.text
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

      /**
       * If Layer field name is not empty then the input field will contain the layer to write to
       * else we will ue the layerName field
       */
      if(swt.layerFromFieldGroupButton.selection)
      {
         input.layerFieldName = swt.layerFieldName.text
         input.layerName = ""
      }
      else
      {
         input.layerName =swt.layerName.text
         input.layerFieldName = ""
      }
      input.operationType =  TileStoreCommonData.TileStoreOpType."${swt.operationTypeField.text}"

      dispose();
   }
}
