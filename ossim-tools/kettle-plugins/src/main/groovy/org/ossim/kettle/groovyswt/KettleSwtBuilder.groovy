package org.ossim.kettle.groovyswt
import groovy.swt.SwtBuilder
import org.pentaho.di.ui.core.widget.TableView
import org.pentaho.di.ui.core.widget.ColumnInfo
import groovy.swt.impl.ShellImpl
import org.eclipse.swt.SWT;

class KettleSwtBuilder extends SwtBuilder
{
	void registerBasicWidgets() 
	{
		super.registerBasicWidgets()

		registerFactory("tableView", 
			             new KettleSwtFactory(TableView.class))
		registerFactory("columnInfo", 
			             new KettleSwtFactory(org.pentaho.di.ui.core.widget.ColumnInfo.class))
		registerFactory("columnInfo", 
			             new KettleSwtFactory(org.pentaho.di.ui.core.widget.ColumnInfo.class))
	}
}
