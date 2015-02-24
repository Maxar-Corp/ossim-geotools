package org.ossim.omar.utilities
import org.pentaho.di.core.database.DatabaseMeta;


class KettleUtilities
{
	static def convertDatabaseMetaToMap(def databaseMeta)
	{
		[
			username:databaseMeta.username,
			password:databaseMeta.password,
			url:databaseMeta.URL,
			port:databaseMeta.databasePortNumberString,
			driverClass:databaseMeta.driverClass,
			databaseName:databaseMeta.databaseName,
			hostName:databaseMeta.hostname,
			// reserved for future hibernate settings.  
			// For now,  validate only no schema updates
			//dbCreate:"validate"
			dbCreate:"validate"
		]
	}
}