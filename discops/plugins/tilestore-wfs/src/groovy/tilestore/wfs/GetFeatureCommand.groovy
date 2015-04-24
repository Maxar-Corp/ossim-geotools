package tilestore.wfs

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by sbortman on 4/23/15.
 */
@Validateable
@ToString( includeNames = true, includeSuper = true )

class GetFeatureCommand extends WfsCommand
{
  String typeName
  String maxFeatures
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
    typeName( nullable: false )
    maxFeatures( nullable: true )
    outputFormat( nullable: true )
    resultType( nullable: true )
    propertyName(nullable: true)
    bbox(nullable: true)
  }
}
