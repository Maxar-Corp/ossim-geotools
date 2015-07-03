package org.ossim.kettle.steps.dirwatch

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.CCombo
import org.eclipse.swt.events.ModifyListener
import org.eclipse.swt.widgets.DirectoryDialog
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Table
import org.eclipse.swt.widgets.TableItem
import org.eclipse.swt.widgets.Text
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.ossim.kettle.types.OssimValueMetaBase
import org.ossim.kettle.utilities.SwtUtilities
import org.pentaho.di.core.Const
import org.pentaho.di.core.row.value.ValueMetaBase
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.trans.step.StepDialogInterface
import org.pentaho.di.ui.core.widget.ColumnInfo
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


      def lsMod = {
         event ->
            //def tableView = event.source.parent.parent
            //def widgetSource = event.source
      } as ModifyListener
      ColumnInfo[] colinf = new ColumnInfo[4];
      colinf[0] = new ColumnInfo("Directory", ColumnInfo.COLUMN_TYPE_TEXT, false);
      colinf[1] = new ColumnInfo("Wildcard (RegExp)", ColumnInfo.COLUMN_TYPE_TEXT, false);
      colinf[2] = new ColumnInfo("Exclude Wildcard", ColumnInfo.COLUMN_TYPE_TEXT, false);
      colinf[3] = new ColumnInfo("Include Subfolders", ColumnInfo.COLUMN_TYPE_CCOMBO, ["true", "false"] as String[], true);



      shell = swt.shell(parent) {
         migLayout(layoutConstraints: "wrap 1", columnConstraints: "[grow]")
         group(id: "stepNameGroup") {
            migLayout(layoutConstraints: "insets 1", columnConstraints: "[] [grow]")//,50:200:200]")
            label Messages.getString("DirWatchDialog.Stepname.Label")

            text(id: "stepName", layoutData: "span,growx", text: stepname) {
               onEvent(type: 'Modify') { changed = true }
            }
         }
         cTabFolder(id: "tabFolder") {
            //migLayout(layoutConstraints: "wrap 1", columnConstraints: "[grow]")
            cTabItem(id: "tabInputDefinitions", text: "Input Settings") {
               composite(){
                  migLayout(layoutConstraints: "insets 2, wrap 1", columnConstraints: "[grow]")
                  group(id:"inputFieldGroupId", text:"Input From Fields", layoutData: "growx"){
                     migLayout(layoutConstraints: "insets 1, wrap 2", columnConstraints: "[][grow]")
                     label "Input from fields?"
                     checkBox(id: "fileInputFromField",
                             text: "",//Messages.getString("RemoveRecordDialog.outputResultCheckbox.Label"),
                             selection: true,
                             layoutData: "span, growx, wrap") {
                        onEvent(type: "Selection") {
                           //input.setChanged()
                           fileInputFromFieldCheckboxModified()
                        }
                     }
                     label "Filename"
                     cCombo(id: "fieldFilename",
                             items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_STRING]),
                             layoutData: "width 100:100:200,span,growx"
                     ) {
                        onEvent(type: 'Modify') { changed = true }
                     }
                     label "Include Filter"
                     cCombo(id: "fieldWildcard",
                             items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_STRING]),
                             layoutData: "width 100:100:200, span,growx")
                             {
                                onEvent(type: 'Modify') { changed = true }
                             }
                     label "Exclude Filter"
                     cCombo(id: "fieldWildcardExclude",
                             items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_STRING]),
                             layoutData: "width 100:100:200,span,growx"
                     ) {
                        onEvent(type: 'Modify') { changed = true }
                     }
                     label "Recurse subfolders"
                     cCombo(id: "fieldRecurseSubfolders",
                             items: SwtUtilities.previousStepFields(transMeta, stepname, [ValueMetaBase.TYPE_STRING, ValueMetaBase.TYPE_BOOLEAN]),
                             layoutData: "width 100:100:200,span,growx"
                     ) {
                        onEvent(type: 'Modify') { changed = true }
                     }
                  }
                  group(){//layoutData:"span, growx"){
                     migLayout(layoutConstraints: "insets 1, wrap 1", columnConstraints: "[grow]")
                     composite(){
                        migLayout(layoutConstraints: "insets 1, wrap 2", columnConstraints: "[][grow]")
                        label "Filename"
                        text id:"filename", text:"", layoutData: "width 250:250:250, split 3"
                        button (id:"addButton", text:"Add"){
                           onEvent(type:"Selection"){
                              addRow()
                           }
                        }
                        button (id:"browseButton", text:"Browse..."){
                           onEvent(type:"Selection"){
                              DirectoryDialog dialog = new DirectoryDialog( parent, SWT.OPEN );
                              if ( swt.filename.text ) {
                                 String fpath = transMeta.environmentSubstitute( swt.filename.text );
                                 dialog.filterPath = fpath;
                              }

                              if ( dialog.open()) {
                                 String str = dialog.filterPath;
                                 swt.filename.text = str
                              }
                           }
                        }
                        label "Wildcard"
                        text id:"wildcard", text:"", layoutData:"spanx, grow"
                        label "Wildcard Exclude"
                        text id:"wildcardExclude", layoutData:"spanx, grow"
                        label "Recurse Sub Folders"
                        checkBox(id: "recurseSubfolders",
                                text: "",//Messages.getString("RemoveRecordDialog.outputResultCheckbox.Label"),
                                selection: false,
                                layoutData: "wrap") {
                           onEvent(type: "Selection") {
                              changed = true
                           }
                        }
                     }

                     tableView(id:"fieldSelection",
                             transMeta:transMeta,
                             nrRows:1,
                             columnInfo:colinf,
                             style:"BORDER,FULL_SELECTION,MULTI,V_SCROLL,H_SCROLL",
                             propsUi:props,
                             layoutData:"height 100:100:200, span 2",
                             modifyListener:lsMod)

                     composite(){
                        migLayout(layoutConstraints: "insets 1, wrap 3", columnConstraints: "[][grow]")
                        button(id:"clearFields", "Clear All"){
                           onEvent(type:"Selection"){
                              clearFields()
                           }
                        }
                        button(id:"deleteRowButton", "Delete"){
                           onEvent(type:"Selection"){
                              deleteCurrentRow()
                           }
                        }
                        button(id:"editRowButton", "Edit"){
                           onEvent(type:"Selection"){
                              editRow()
                           }
                        }
                     }
                  }
               }
            }
            cTabItem(id: "tabOutputDefinitions", text: "Output settings") {
               composite(){

               }
            }
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
      changed = input.hasChanged();
      shell.text = Messages.getString("DirWatchDialog.Shell.Title")
      getData(); // initialize data fields
      //setSize(); // shrink and fit dialog to fit inputs
      input.setChanged(changed);

      swt.tabFolder.setSelection(0)
      shell.doMainloop()

      return stepname;

   }
   private fileInputFromFieldCheckboxModified()
   {
      swt.fieldFilename.enabled = swt.fileInputFromField.selection
      swt.fieldWildcard.enabled = swt.fileInputFromField.selection
      swt.fieldWildcardExclude.enabled = swt.fileInputFromField.selection
      swt.fieldRecurseSubfolders.enabled = swt.fileInputFromField.selection

      swt.filename.enabled = !swt.fileInputFromField.selection
      swt.wildcard.enabled = !swt.fileInputFromField.selection
      swt.wildcardExclude.enabled = !swt.fileInputFromField.selection
      swt.recurseSubfolders.enabled = !swt.fileInputFromField.selection
      swt.fieldSelection.enabled = !swt.fileInputFromField.selection
      swt.clearFields.enabled = !swt.fileInputFromField.selection
      swt.addButton.enabled = !swt.fileInputFromField.selection
      swt.browseButton.enabled = !swt.fileInputFromField.selection
      swt.deleteRowButton.enabled = !swt.fileInputFromField.selection
     // swt.editRowButton.enabled = !swt.fileInputFromField.selection
   }
   private clearFields()
   {
      def tableView = swt.fieldSelection
      tableView.table.removeAll()
      //tableView.table.setItemCount(1)
      //tableView.table.getItem(0).setText(0, "1")
   }
   private void deleteCurrentRow()
   {
      def tableView = swt.fieldSelection
      def itemCount = tableView.table.itemCount

      if(itemCount>0)
      {
         def row  = tableView?.getCurrentRownr()
         if(row>=0)
         {
            tableView?.table.remove(row)
            changed = true

            resetRowNumbers()
         }

      }
   }
   private void editRow()
   {
      def tableView = swt.fieldSelection
      def itemCount = tableView.table.itemCount

      if(itemCount>0)
      {
         def row  = tableView?.getCurrentRownr()
         if(row>=0)
         {
            TableItem item = tableView.table.getItem(row);

            if(item)
            {
               swt.filename.text               = item.getText(1)
               swt.wildcard.text               = item.getText(2)
               swt.wildcardExclude.text        = item.getText(3)
               swt.recurseSubfolders.selection = "${item.getText(4)}".toBoolean()
            }
         }
      }
   }
   private void addRow(){
      if(swt.filename?.text)
      {
         def tableView = swt.fieldSelection
         def itemCount = tableView.table.itemCount
         tableView.table.setItemCount(itemCount+1)
         TableItem item = tableView.table.getItem(itemCount);

         item?.setText(0, itemCount.toString());
         item?.setText(1, swt.filename.text?:"")
         item?.setText(2, swt.wildcard?.text?:"")
         item?.setText(3, swt.wildcardExclude?.text?:"")
         item?.setText(4, swt.recurseSubfolders?.selection?"true":"false")

         changed = true
      }
   }
   private void loadFileinfoTable() {

      def fields = input.fileDefinitions
      def tableView = swt.fieldSelection

      tableView.table.removeAll()
      if(fields?.size())
      {
         tableView.table.setItemCount(fields.size())
         fields?.eachWithIndex{fieldDefinition, i->
            def item = tableView.table.getItem(i);
            item?.setText(0, i.toString());
            item?.setText(1, fieldDefinition.filename?:"")
            item?.setText(2, fieldDefinition.wildcard?:"")
            item?.setText(3, fieldDefinition.wildcardExclude?:"")
            item?.setText(4, fieldDefinition.recurseSubfolders?:"true")
         }
      }
   }
   private void resetRowNumbers()
   {
      def tableView = swt.fieldSelection
      def itemCount = tableView?.table?.itemCount
      if(itemCount)
      {
         (0..<itemCount).each { idx ->
            TableItem item = tableView.table.getItem(idx);
            item.setText(0, idx.toString())
         }
      }
   }
   private void saveFileinfoTable()
   {
      input.fileDefinitions = []
      def tableView = swt.fieldSelection
      def itemCount = tableView.table.itemCount
      (0..<itemCount).each { idx ->
         TableItem item = tableView.table.getItem(idx);
         String filename          = "${item.getText(1)}".trim()
         String wildcard          = "${item.getText(2)}"
         String wildcardExclude   = "${item.getText(3)}"
         String recurseSubfolders = "${item.getText(4)}".trim()

         if(filename.trim())
         {

            input.fileDefinitions << [filename:filename,
                    wildcard:wildcard,
                    wildcardExclude: wildcardExclude,
                    recurseSubfolders: recurseSubfolders]
         }
      }
   }
   private getData()
   {
      swt.fileInputFromField.selection = input.fileInputFromField
      swt.fieldFilename.text           = input.fieldFilename?:""
      swt.fieldWildcard.text           = input.fieldWildcard?:""
      swt.fieldWildcardExclude.text    = input.fieldWildcardExclude?:""
      swt.fieldRecurseSubfolders.text  = input.fieldRecurseSubfolders?:""

      loadFileinfoTable()

      swt.stepName.selectAll();
      fileInputFromFieldCheckboxModified()
   }
   private void cancel()
   {
      stepname=null;

      dispose();
   }
   private void ok()
   {
      if (Const.isEmpty(swt.stepName.text)) return;

      stepname                     = swt.stepName.text
      input.fileInputFromField     = swt.fileInputFromField.selection
      input.fieldFilename          = swt.fieldFilename.text
      input.fieldWildcard          = swt.fieldWildcard.text
      input.fieldWildcardExclude   = swt.fieldWildcardExclude.text
      input.fieldRecurseSubfolders = swt.fieldRecurseSubfolders.text

      saveFileinfoTable()

      input.setChanged(changed);
      dispose();
   }
}
