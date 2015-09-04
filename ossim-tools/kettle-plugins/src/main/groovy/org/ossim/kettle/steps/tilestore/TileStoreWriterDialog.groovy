package org.ossim.kettle.steps.tilestore

import joms.geotools.tileapi.hibernate.TileCacheHibernate
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.CCombo
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.TableItem
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.ossim.kettle.steps.tilestore.Messages
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
 * Created by gpotts on 3/19/15.
 */
class TileStoreWriterDialog extends BaseStepDialog implements
        StepDialogInterface
{
   private TileStoreWriterMeta input;
   private def swt;
   private DatabaseMeta databaseMeta
   private def layers = []
   private def fields = ["minx":"tileMinXFieldName",
                         "miny":"tileMinYFieldName",
                         "maxx":"tileMaxXFieldName",
                         "maxy":"tileMaxYFieldName",
                         "epsg":"epsgCodeFieldName",
                         "level":"tileLevelFieldName",
                         "row":"tileRowFieldName",
                         "col":"tileColFieldName",
                         "image":"imageFieldName",
                         "status":"imageStatusFieldName"
                         ]

   public TileStoreWriterDialog(Shell parent, Object baseStepMeta,
                                TransMeta transMeta, String stepname) {
      super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
      input = (TileStoreWriterMeta)baseStepMeta;
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
      def lsMod = {
         event -> input.setChanged()
      } as ModifyListener
      ColumnInfo[] colinf = new ColumnInfo[2];
      colinf[0] =
              new ColumnInfo( Messages.getString("TileStoreWriterDialog.ColumnInfo.Parameter" ),
                      ColumnInfo.COLUMN_TYPE_TEXT, false,
                      true );
      colinf[1] =
              new ColumnInfo( Messages.getString("TileStoreWriterDialog.ColumnInfo.InputField" ),
                      ColumnInfo.COLUMN_TYPE_CCOMBO, SwtUtilities.previousStepFields(transMeta, stepname),
                      true );

      shell = swt.shell(parent){
         migLayout(layoutConstraints:"", columnConstraints: "[grow]")
         //gridLayout(numColumns: 2)
         composite(style:"none", layoutData:"span,growx") {
            migLayout(layoutConstraints:"inset 0", columnConstraints: "[][grow,50:100:200]")
            label Messages.getString("TileStoreCommonDialog.Stepname.Label")
            text(id: "stepName", layoutData: "span,growx", text: stepname) {
               onEvent(type: 'Modify') { input.setChanged() }
            }
            label Messages.getString("TileStoreCommonDialog.PassInputFields.Label")
            checkBox(id: "passInputFields",
                    text: "",
                    selection: true,
                    layoutData: "span, growx, wrap") {
               onEvent(type: "Selection") {
                  changed=true
               }
            }
         }
         composite(id:"connectionLayoutId", style:"none", layoutData:"span,growx") {
            migLayout(layoutConstraints: "inset 0", columnConstraints: "[][][][]")
            label Messages.getString("TileStoreCommonDialog.Connection.Label")
            cCombo(id: "connectionList", layoutData: "growx", items: transMeta.databaseNames) {
               onEvent(type: 'Selection') {
                  int idx = swt.connectionList.indexOf(swt.connectionList.text);
                  databaseMeta = transMeta.getDatabase(idx)
                  // get and set list of layers

               }
            }
            button(id: "editConnection",
                    text: Messages.getString("TileStoreCommonDialog.EditConnection.Label"),
                    layoutData: "growx") {
               onEvent(type: "Selection") {
                  int idx = swt.connectionList.indexOf(swt.connectionList.text);
                  if (databaseMeta) {
                     DatabaseDialog cid = getDatabaseDialog(shell);
                     cid.setDatabaseMeta(databaseMeta);
                     cid.setModalDialog(true);
                     if (cid.open() != null) {
                        changed=true
                     }
                  }
               }
            }
            button(id: "newConnection",
                    text: Messages.getString("TileStoreCommonDialog.NewConnection.Label"),
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
            label Messages.getString("TileStoreCommonDialog.InstanceName.Label")
            text(id: "accumuloInstance", layoutData: "span,growx") {
               onEvent(type: 'Modify') { input.setChanged() }
            }
            label Messages.getString("TileStoreCommonDialog.Username.Label")
            text(id: "accumuloUsername", layoutData: "span,growx") {
               onEvent(type: 'Modify') { input.setChanged() }
            }
            label Messages.getString("TileStoreCommonDialog.Password.Label")
            text(id: "accumuloPassword", layoutData: "span,growx", style:"PASSWORD") {
               onEvent(type: 'Modify') { input.setChanged() }
            }
            label Messages.getString("TileStoreCommonDialog.PasswordVerify.Label")
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
            composite(id:"fieldMappingsButtonGroupId", style:"none", layoutData: "span, growx"){
               migLayout(layoutConstraints:"insets 0, wrap 2", columnConstraints: "[][grow]")
               button("Clear", layoutData:"align center,skip 1,split 2")  {
                  onEvent(type:"Selection"){
                      clearFields()
                  }
               }
               button("Auto Populate"){
                  onEvent(type:"Selection"){
                      autoPopulateFields()
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
      swt.stackLayout.topControl = swt.layerFromFieldGroup
      shell.text = Messages.getString("TileStoreWriterDialog.Shell.Title")
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
   void clearFields()
   {
      def tableView = swt.fieldMappings
      tableView.table.clearAll()
      tableView.table.setItemCount(fields.size())
      def idx = 0
      fields.each{k,v->
         TableItem item = tableView.table.getItem(idx);
         item.setText(1, k);
         item.setText(2, "");

         ++idx
      }
   }
   void autoPopulateFields()
   {
      def inputFields = SwtUtilities.previousStepFields(transMeta, stepname)

      def tableView = swt.fieldMappings
      tableView.table.clearAll()
      tableView.table.setItemCount(fields.size())

      inputFields.each{inputField->
         String tempName = inputField.toLowerCase()

         def idx = 0
         fields.each { k,v ->

            TableItem item = tableView.table.getItem(idx);
            if(!item?.getText(2))
            {
               if(tempName.contains(k.toLowerCase()))
               {
                  item.setText(1, k);
                  item.setText(2, inputField);
               }
               else
               {
                  item.setText(1, k);
                  item.setText(2, "");
               }
            }
            ++idx
         }
      }
   }
   public void getData()
   {
      databaseMeta = input.tileStoreCommon.databaseMeta
      swt.stepName.selectAll();
      swt.passInputFields.selection = input.passInputFields

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
      def tableView = swt.fieldMappings

      tableView.table.items.each{item->
         def key = fields."${item.getText(1)}"
         def value = item.getText(2)
         input."${key}" = value
      }

      input.passInputFields = swt.passInputFields.selection
      input.setChanged(changed)

      dispose();
   }
}
