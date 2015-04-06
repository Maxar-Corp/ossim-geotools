package tilecache.wfs

import grails.validation.Validateable
import groovy.transform.ToString
import tilecache.CaseInsensitiveBind

/**
 */
@Validateable
@ToString( includeNames = true )
class WfsCommand implements CaseInsensitiveBind
{
   String version = "2.0.0"
   String typeName
   String maxFeatures
   String request
   String outputFormat = "json"
   String resultType

   /**
    * A list of properties may be specified for each feature type that is being queried
    *
    * comma separated list of properties to retrieve.  A value of * gets all
    * properties
    *
    */
   String propertyName
   String bbox

   /**
    * Per spec it says the default is ascending.
    *
    * The format is a comma separated list of field [A|D]
    * where A is ascending and D is descending and if not specified defaults
    * to ascending
    */
   String sortBy

   static constraints = {
      version( nullable: true )
      typeName( nullable: false )
      maxFeatures( nullable: true )
      request( nullable: false )
      outputFormat( nullable: true )
      resultType( nullable: true )
      propertyName(nullable: true)
      bbox(nullable: true)
   }
}