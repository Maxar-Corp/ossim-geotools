package org.ossim.kettle.groovyswt

import org.eclipse.swt.events.SelectionListener
import org.pentaho.di.ui.core.namedcluster.NamedClusterWidget

import java.util.Map
import org.codehaus.groovy.GroovyException
import groovy.swt.factory.AbstractSwtFactory
import org.eclipse.swt.SWT;
import org.pentaho.di.ui.core.widget.ColumnInfo

class KettleSwtFactory extends  AbstractSwtFactory
{

    protected Class beanClass;

    protected int defaultStyle = SWT.NONE;

    /**
     * @param beanClass
     */
   public KettleSwtFactory(Class beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * @param beanClass
     * @param style
     */
    public KettleSwtFactory(Class beanClass, int style) {
        this.beanClass = beanClass;
        this.defaultStyle = style;
    }
   /**
     * Create a new instance
     */
	public Object newInstance(FactoryBuilderSupport builder, Object name,
			Object value, Map attributes) throws InstantiationException,
			IllegalAccessException 
  {
    	//println "${attributes}\n${builder}\n"
    	//println "NAME: ${name}\n"
    	//println "*"*40
    	def result = null
		Object parent = builder.getCurrent();
		
		// if no parent given, check if value can be the parent
		if (value != null
			&& ! (value instanceof String)
			&& ! (value instanceof GString)) 
		{
			parent = value;
		}
		
		String styleProperty = (String) attributes.remove("style");
		int style = this.defaultStyle;
		if (styleProperty != null) {
			style = groovy.swt.SwtUtils.parseStyle(SWT.class, styleProperty);
		}

		Object parentWidget = groovy.swt.SwtUtils.getParentWidget(parent, attributes);

    switch(name)
    {
      case "tableView":
        result = createTableView(parent, style, attributes)
        break
      case "myNamedClusterWidget":
        result = createNamedClusterWidget(parent, style, attributes)
        break
      case "columnInfo":
    //  println "HERE AT COLUMNINFO!!!!!!!"
            // this must be a setting or child
        if(parentWidget instanceof org.pentaho.di.ui.core.widget.TableView)
        {
          def columnName = attributes.remove("name")
          def columnType = parseColumnInfoType(attributes.remove("type"))

          if((columnName != null) && (columnType != null))
          {
            result = new org.pentaho.di.ui.core.widget.ColumnInfo(columnName, columnType)
          }
        }
      //println "DONE HERE AT COLUMNINFO!!!!!!!"
        break
      default:
        break
    }
   	result
  }
  protected def parseColumnInfoType(def value){
    def result = ColumnInfo.COLUMN_TYPE_NONE

    switch(value?.trim().toUpperCase())
    {
      case "COLUMN_TYPE_TEXT":
        result = ColumnInfo.COLUMN_TYPE_TEXT
      break
      case "COLUMN_TYPE_CCOMBO":
        result = ColumnInfo.COLUMN_TYPE_CCOMBO
      break
      case "COLUMN_TYPE_BUTTON":
        result = ColumnInfo.COLUMN_TYPE_BUTTON
      break
      case "COLUMN_TYPE_ICON":
        result = ColumnInfo.COLUMN_TYPE_ICON
      break
      case "COLUMN_TYPE_FORMAT":
        result = ColumnInfo.COLUMN_TYPE_FORMAT
      break
      default:
      break
    }

    result
  }
  protected Object createNamedClusterWidget(Object parent, int style, def attributes) throws InstantiationException
  {
    Object result = null
    def selectionListener = attributes.remove("selectionListener")
    def showLabel = attributes.remove("showLabel")

    if(attributes?.showLabel!=null) showLabel = attributes?.showLabel
    result = new NamedClusterWidget(parent, showLabel)
    if(selectionListener)
    {
      result.addSelectionListener(selectionListener)
    }

    result
  }
  protected Object createTableView(Object parent, int style, def attributes) throws InstantiationException 
  {
  	def result = null
  	def transMeta = attributes.remove("transMeta")
  	def columnInfo = attributes.remove("columnInfo")
  	def nrRows = attributes.remove("nrRows")
  	def propsUi = attributes.remove("propsUi")
  	def readOnly = attributes.remove("readOnly")
  	def modifyListener = attributes.remove("modifyListener")

  	if(!transMeta)
  	{
  		throw new InstantiationException("Attribute 'transMeta' must be present and pointing to a TransMeta object")
  	}
  	if(!columnInfo)
  	{
  		throw new InstantiationException("Attribute 'columnInfo' must be present and pointing to a ColumnInfo[] object")
  	}
  	if(!nrRows)
  	{
  		throw new InstantiationException("Attribute 'nrRows' must be present and specify the initial number of rows")
  	}
  //	if(!attributes.modifyListener)
  //	{
  //		throw new InstantiationException("Attribute 'modifyListener' must be present and specify ModifyListener object")
  //	}
  	if(!propsUi)
  	{
  		throw new InstantiationException("Attribute 'propsUi' must be present and specify PropsUI object")
  	}
 	
   	if(attributes.readOnly)
   	{
   		result = new org.pentaho.di.ui.core.widget.TableView(transMeta,
   																			parent,
   																			style,
   																			columnInfo,
   																			nrRows,
   																			readOnly,
   																			modifyListener,//attributes.modifyListener,
   																			propsUi)
   	}
   	else
   	{
   		result = new org.pentaho.di.ui.core.widget.TableView(transMeta,
   																			parent,
   																			style,
   																			columnInfo,
   																			nrRows,
   																			modifyListener,//attributes.modifyListener,
   																			propsUi)
   	}
  	result
  }
}
