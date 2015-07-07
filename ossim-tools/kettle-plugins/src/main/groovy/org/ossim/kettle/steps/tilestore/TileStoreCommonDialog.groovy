package org.ossim.kettle.steps.tilestore

import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.widgets.Shell
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog
import org.pentaho.di.ui.trans.step.BaseStepDialog

/**
 * Created by gpotts on 5/22/15.
 */
class TileStoreCommonDialog extends BaseStepDialog implements
        StepDialogInterface
{
   def swt;
   def lsSelect = { event ->
      SelectionEvent selectionEvent = event as SelectionEvent
      //println selectionEvent.widget.text
      input.setChanged()

   } as SelectionListener
   def stepnameClosure = {
      label Messages.getString("TileStoreCommon.Stepname.Label")
      //text(id:"stepName", text: stepname ,layoutData:"span, growx"){
      text(id: "stepName", layoutData: "span,growx", text: stepname) {
         onEvent(type: 'Modify') { input.setChanged() }
      }
   }
   def databaseConnectionClosure = {
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
                     input.setChanged()
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
               if (cid.open() != null)
               {
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
   }
   def accumuloConnectionClosure = {
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
   }
   def namedClusterWidgetClosure={
      composite(style:"none", layoutData:"span,growx") {
         migLayout(layoutConstraints:"inset 0", columnConstraints: "[grow]")
         myNamedClusterWidget(id: "namedClusterWidgetId", showLabel:true,
                 selectionListener:lsSelect, style:"none") {
         }
      }
   }
   def okCancelClosure = {
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
   public TileStoreCommonDialog(Shell parent, Object baseStepMeta,
                                  TransMeta transMeta, String stepname)
   {
      super(parent, (BaseStepMeta) baseStepMeta, transMeta, stepname);
   }
   def kettleSwtBuilder()
   {
      swt = new KettleSwtBuilder()

      stepnameClosure.delegate=swt
      databaseConnectionClosure.delegate=swt
      accumuloConnectionClosure.delegate=swt
      okCancelClosure.delegate=swt
      namedClusterWidgetClosure.delegate=swt

      swt
   }
   String open()
   {
      stepname
   }
}
