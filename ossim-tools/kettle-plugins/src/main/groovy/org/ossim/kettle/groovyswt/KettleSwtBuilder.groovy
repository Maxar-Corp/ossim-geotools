package org.ossim.kettle.groovyswt
import groovy.swt.SwtBuilder
import org.pentaho.di.ui.core.namedcluster.NamedClusterWidget
import org.pentaho.di.ui.core.widget.TableView
import org.pentaho.di.ui.core.widget.ColumnInfo
import groovy.swt.impl.ShellImpl
import org.eclipse.swt.SWT;

class KettleSwtBuilder extends SwtBuilder
{
	void registerBasicWidgets() 
	{
		super.registerBasicWidgets()

    registerFactory("myNamedClusterWidget",
            new KettleSwtFactory(NamedClusterWidget.class))
    registerFactory("tableView",
            new KettleSwtFactory(TableView.class))
		registerFactory("columnInfo",
            new KettleSwtFactory(ColumnInfo.class))

	}
}
