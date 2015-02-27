package org.ossim.kettle.steps.amqp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.ossim.kettle.utilities.SwtUtilities
import org.pentaho.di.ui.core.widget.ColumnInfo
import org.ossim.kettle.groovyswt.KettleSwtBuilder
import org.eclipse.swt.widgets.TableItem
import org.eclipse.swt.custom.CCombo
import org.eclipse.swt.widgets.MessageBox;
import org.ossim.core.MultiResolutionTileGenerator
import org.ossim.core.RabbitType

public class RabbitMQInputDialog extends BaseStepDialog implements
        StepDialogInterface {

  private RabbitMQInputMeta input;
  private def swt;
  private def stepsWeCanWatch = [] as String[];

  public RabbitMQInputDialog(Shell parent, Object baseStepMeta,
                             TransMeta transMeta, String stepname) {
    super(parent, baseStepMeta, transMeta, stepname);
    input = (RabbitMQInputMeta)baseStepMeta;

  }
  public String open() {

    Shell parent    = getParent();
    Display display = parent.getDisplay();
    swt = new KettleSwtBuilder()
    //colinf[0] =
    //  new ColumnInfo( Messages.getString("RabbitMQInputDialog.ColumnInfo.ExchangeType" ),
    //      ColumnInfo.COLUMN_TYPE_CCOMBO, RabbitType.ExchangeType.valuesAsString() as String [],
    //      true );


    def queueMod = {
      event -> input.setChanged()
        def src = event.source
        if(src instanceof CCombo)
        {
          /*
          def tableView = src?.parent?.parent
           def row  = tableView?.getCurrentRownr()
          def item = src?.parent?.getItem(row)
          //def indexOfValue = src.indexOf(src.text)
          def key = "${src.text}"


          //def renameValue          = input.fieldNames."${key}"

          //item.setText(1, key)
          //item.setText(2, renameValue)
          */
        }
    } as ModifyListener

    // 	def monitorStepsMod = {
//	   	event -> input.setChanged()
    //def src = event.source
//	   } as ModifyListener
//		ColumnInfo[] messageHandlerColinfo = new ColumnInfo[1];
//		messageHandlerColinfo[0] =
//		  new ColumnInfo( Messages.getString("RabbitMQInputDialog.MessageHandlerColumnInfo.StepToMonitor" ),
//		      ColumnInfo.COLUMN_TYPE_CCOMBO, getStepsWeCanWatch(), 
//		      true );

    ColumnInfo[] queueColinfo = new ColumnInfo[8];
    queueColinfo[0] =
            new ColumnInfo( Messages.getString("RabbitMQInputDialog.ColumnInfo.ExchangeType" ),
                    ColumnInfo.COLUMN_TYPE_CCOMBO, RabbitType.ExchangeType.valuesAsString() as String [],
                    true );
    queueColinfo[1] =
            new ColumnInfo( Messages.getString("RabbitMQInputDialog.ColumnInfo.RoutingName" ),
                    ColumnInfo.COLUMN_TYPE_TEXT, false );
    queueColinfo[2] =
            new ColumnInfo( Messages.getString("RabbitMQInputDialog.ColumnInfo.ExchangeName" ),
                    ColumnInfo.COLUMN_TYPE_TEXT, false );
    queueColinfo[3] =
            new ColumnInfo( Messages.getString("RabbitMQInputDialog.ColumnInfo.Durable" ),
                    ColumnInfo.COLUMN_TYPE_CCOMBO, ["true", "false"] as String [],true );
    queueColinfo[4] =
            new ColumnInfo( Messages.getString("RabbitMQInputDialog.ColumnInfo.Exclusive" ),
                    ColumnInfo.COLUMN_TYPE_CCOMBO, ["true", "false"] as String [],true );
    queueColinfo[5] =
            new ColumnInfo( Messages.getString("RabbitMQInputDialog.ColumnInfo.AutoDelete" ),
                    ColumnInfo.COLUMN_TYPE_CCOMBO, ["true", "false"] as String [],true );
    queueColinfo[6] =
            new ColumnInfo( Messages.getString("RabbitMQInputDialog.ColumnInfo.CreateQueue" ),
                    ColumnInfo.COLUMN_TYPE_CCOMBO, ["true", "false"] as String [],true );
    queueColinfo[7] =
            new ColumnInfo( Messages.getString("RabbitMQInputDialog.ColumnInfo.CreateExchange" ),
                    ColumnInfo.COLUMN_TYPE_CCOMBO, ["true", "false"] as String [],true );

    shell = swt.shell(parent){
      migLayout(layoutConstraints:"insets 2, wrap 1", columnConstraints: "[grow]")
      // migLayout(layoutConstraints:"wrap 2", columnConstraints: "[] [grow,:200:]")
      //gridLayout(numColumns: 2)

      composite(layoutData:"growx, spanx, wrap"){
        migLayout(layoutConstraints:"insets 2, wrap 2", columnConstraints: "[] [grow,:200:]")

        label Messages.getString("RabbitMQInputDialog.Stepname.Label")
        text(id:"stepName", layoutData:"span,growx", text: stepname){
          onEvent(type:'Modify') { input.setChanged() }
        }
        label Messages.getString("RabbitMQInputDialog.MessageFieldName.Label")
        text(id:"messageFieldName", layoutData:"span,growx", text: ""){
          onEvent(type:'Modify') { input.setChanged() }
        }

        label Messages.getString("RabbitMQInputDialog.Host.Label")
        text(id:"host", layoutData:"span,growx", text: ""){
          onEvent(type:'Modify') { input.setChanged() }
        }
        label Messages.getString("RabbitMQInputDialog.Port.Label")
        text(id:"port", layoutData:"span,growx", text: ""){
          onEvent(type:'Modify') { input.setChanged() }
        }
        label Messages.getString("RabbitMQInputDialog.Username.Label")
        text(id:"username", layoutData:"span,growx", text: ""){
          onEvent(type:'Modify') { input.setChanged() }
        }

        label Messages.getString("RabbitMQInputDialog.Password.Label")
        text(id:"password", layoutData:"span,growx", text: "", style:"PASSWORD"){
          onEvent(type:'Modify') { input.setChanged() }
        }
        label Messages.getString("RabbitMQInputDialog.VerifyPassword.Label")
        text(id:"verifyPassword", layoutData:"span,growx", text: "", style:"PASSWORD"){
          onEvent(type:'Modify') { input.setChanged() }
        }
        checkBox(id:"stopIfNoMoreMessages",
                text:"Stop If No More Messages:",//Messages.getString("StageRasterDialog.outputResultCheckbox.Label"),
                selection:false){
          onEvent(type:"Selection"){
            input.setChanged()
            //swt.resultColumnName.enabled = swt.outputResultCheckbox.selection
          }
        }
        label ""
        label "Stop After N Messages:"
        text(id:"stopAfterNMessages"){
          onEvent(type: "Modify"){
            input.setChanged()
          }
        }
        label Messages.getString("RabbitMQInputDialog.MessageHandlerColumnInfo.StepToMonitor")
        cCombo(id:"messageHandlerSteps",
                items:getStepsWeCanWatch(),
                layoutData:"span,growx")
                {
                  onEvent(type:'FocusIn') { input.setChanged(); }
                }
      }
      group(id:"queueGroup",layoutData:"grow, spanx, wrap"){
        migLayout(layoutConstraints:"insets 2", columnConstraints: "[grow]")
        tableView(id:"queues",
                transMeta:transMeta,
                nrRows:1,
                columnInfo:queueColinfo,
                style:"BORDER,FULL_SELECTION,MULTI,V_SCROLL,H_SCROLL",
                propsUi:props,
                layoutData:"height 100:100:200, span,growx, wrap",
                modifyListener:queueMod)
        button(id:"clearAll", Messages.getString("RabbitMQInputDialog.clearAll.Label")){
          onEvent(type:"Selection"){
            clearAll()
          }
        }
      }

      /*	group(id:"messageHandlerGroup",layoutData:"grow, spanx, wrap"){
          migLayout(layoutConstraints:"insets 2", columnConstraints: "[grow]")
          tableView(id:"messageHandlerSteps",
                 transMeta:transMeta,
                 nrRows:1,
                 columnInfo:messageHandlerColinfo,
                 style:"BORDER,FULL_SELECTION,MULTI,V_SCROLL,H_SCROLL",
                 propsUi:props,
                 layoutData:"height 100:100:200, span,growx, wrap",
                 modifyListener:monitorStepsMod)
          button(id:"clearAllMessageHandlerSteps", Messages.getString("RabbitMQInputDialog.clearAll.Label")){
            onEvent(type:"Selection"){
              clearAllMessageHandlerSteps()
            }
          }
        }
  */
      composite(layoutData:"grow, span, wrap", style:"none"){
        migLayout(layoutConstraints:"insets 2", columnConstraints: "[grow]")
        button("Ok", layoutData:"align center,split 2"){
          onEvent(type:"Selection"){ok()}
        }
        button("Cancel", layoutData:""){
          onEvent(type:"Selection"){cancel()}
        }
      }
    }
    changed = input.hasChanged();
    shell.text = Messages.getString("RabbitMQInputDialog.Shell.Title")
    getData(); // initialize data fields
    setSize(); // shrink and fit dialog to fit inputs
    input.setChanged(changed);

    shell.doMainloop()

    return stepname;
  }
  private loadAllFields()
  {
    loadAllQueueFields()
    loadAllMessageHandlerFields()
  }
  public void getData()
  {
    swt.messageFieldName.text = input.messageFieldName
    swt.host.text 					  = input.host
    swt.port.text 					  = input.port
    swt.username.text 			  = input.username
    swt.password.text 			  = input.password
    swt.verifyPassword.text 	= input.password
    swt.stopIfNoMoreMessages.selection = input.stopIfNoMoreMessages
    swt.stopAfterNMessages.text       = input.stopAfterNMessages.toString()

    loadAllFields()

    swt.stepName.selectAll();
  }
  private clearAll()
  {
    def tableView = swt.queues
    tableView.table.clearAll()
    tableView.table.setItemCount(1)
    tableView.table.getItem(0).setText(0, "1")

  }
//	private clearAllMessageHandlerSteps()
//	{
//		def tableView = swt.messageHandlerSteps
//		tableView.table.clearAll()
//		tableView.table.setItemCount(1)
//		tableView.table.getItem(0).setText(0, "1")
//	}
  private String[] getStepsWeCanWatch()
  {

    def entries=transMeta.getStepNames() as String []
    entries = entries  - [stepname]
    //String[] nextSteps=transMeta.getNextStepNames(stepMeta);

    //previousSteps.eachWithIndex{previousStep, i->
    //	if(previousStep != stepname)
    //	{
    //		entries << previousStep
    //	}
    //}
    entries as String[]
  }

  private void loadAllMessageHandlerFields() {
    def fields = input.messageHandlerSteps
    if(fields)
    {
      swt.messageHandlerSteps.text = fields[0].toString()
    }
    //def tableView = swt.messageHandlerSteps
    //tableView.table.clearAll()
    //tableView.table.setItemCount(fields.size())
    //fields.eachWithIndex{messageHandler, i->
    //		def item = tableView.table.getItem(i);
//			item?.setText(0, "${i}" as String);
//			item?.setText(1, messageHandler)
//		}
//		if(!fields.size())
//		{
//			tableView.table.setItemCount(1)
//			TableItem item = tableView.table.getItem(0)
//			item.setText(0, "0" as String)
//		}
  }
  private void loadAllQueueFields() {
    def fields = input.queueProperties
    def tableView = swt.queues
    tableView.table.clearAll()
    tableView.table.setItemCount(fields.size())
    fields.eachWithIndex{q, i->
      def item = tableView.table.getItem(i);
      item?.setText(0, "${i}" as String);
      item?.setText(1, q.exchangeType?q.exchangeType.toString():"DIRECT")
      item?.setText(2, q.routingName?q.routingName.toString():"")
      item?.setText(3, q.exchangeName?q.exchangeName.toString():"")
      item?.setText(4, q.durable!=null?q.durable.toString():"true")
      item?.setText(5, q.exclusive!=null?q.exclusive.toString():"false")
      item?.setText(6, q.autoDelete!=null?q.autoDelete.toString():"false")
      item?.setText(7, q.createQueue!=null?q.createQueue.toString():"false")
      item?.setText(8, q.createExchange!=null?q.createExchange.toString():"false")
    }

    if(!fields.size())
    {
      tableView.table.setItemCount(1)
      TableItem item = tableView.table.getItem(0)
      item.setText(0, "0" as String)
    }
  }
  private void cancel()
  {
    stepname=null;
    //input.setChanged(changed);

    dispose();
  }
  private void ok()
  {
    if (Const.isEmpty(swt.stepName.text)) return;

    stepname = swt.stepName.text

    if("${swt.password.text}" != "${swt.verifyPassword.text}")
    {
      def mb     = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
      mb.message = Messages.getString("RabbitMQInputDialog.PasswordDontMatch.DialogMessage");
      mb.text    = Messages.getString("RabbitMQInputDialog.PasswordDontMatch.DialogTitle")
      mb.open()
      return
    }
    def qInfo = []
    def tableView = swt.queues
    def itemCount = tableView.table.itemCount
    (0..<itemCount).each{idx->
      TableItem item = tableView.table.getItem(idx);
      def exchangeType = "${item.getText(1)}".trim()
      def routingName  = "${item.getText(2)}".trim()
      def exchangeName = "${item.getText(3)}".trim()
      def durable      = "${item.getText(4)}".trim()
      def exclusive    = "${item.getText(5)}".trim()
      def autoDelete   = "${item.getText(6)}".trim()
      def createQueue   = "${item.getText(7)}".trim()
      def createExchange   = "${item.getText(8)}".trim()
      def canSet = false;
      if(createQueue == "") createQueue = "false";
      if(createExchange == "") createExchange = "false";

      switch(exchangeType)
      {
        case "DIRECT":
          if(routingName) canSet = true
          durable    = "true"
          exclusive  = "false"
          autoDelete = "false"
          break
        case "TOPIC":
          if(exchangeName&&routingName) canSet = true
          if(durable == "")    durable = "false"
          if(exclusive == "")  exclusive = "false"
          if(autoDelete == "") autoDelete = "false";

          break
        default:
          break
      }
      if(canSet)
      {
        qInfo << [routingName: routingName,
                  exchangeName:exchangeName,
                  exchangeType:RabbitType.ExchangeType."${exchangeType}",
                  durable:durable?.toBoolean(),
                  exclusive:exclusive?.toBoolean(),
                  autoDelete:autoDelete?.toBoolean(),
                  createQueue:createQueue?.toBoolean(),
                  createExchange:createExchange?.toBoolean()
        ]

      }
    }

    //def messageHandlerView = swt.messageHandlerSteps
    //def  messageHandlerItemCount = messageHandlerView.table.itemCount
    //input.messageHandlerSteps = []
    //(0..<messageHandlerItemCount).each{idx->
    //	TableItem item = messageHandlerView.table.getItem(idx);
    //	def step = "${item.getText(1)}"
    //	if(step)
    //		input.messageHandlerSteps << step
    //}

    input.messageHandlerSteps = []
    if("${swt.messageHandlerSteps.text}".trim())  input.messageHandlerSteps << swt.messageHandlerSteps.text
    input.queueProperties	= qInfo
    input.port 					= swt.port.text
    input.host 					= swt.host.text
    input.username 			= swt.username.text
    input.password 			= swt.password.text
    input.messageFieldName 	= swt.messageFieldName.text

    input.stopIfNoMoreMessages = swt.stopIfNoMoreMessages.selection
    if(swt.stopAfterNMessages.text)
    {
      input.stopAfterNMessages   = swt.stopAfterNMessages.text.toInteger()
    }
    dispose();
  }

}

