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
  String outputFormat
  String resultType
  String filter

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

  Integer maxFeatures = 10
  Integer startIndex = 0

  static constraints = {
    typeName( nullable: false )
    maxFeatures( nullable: true )
    startIndex( nullable: true )
    outputFormat( nullable: true )
    resultType( nullable: true )
    propertyName( nullable: true )
    bbox( nullable: true )
    filter( nullable: true )
  }
  def convertSortByToArray()
  {
    def result = [];


    if ( !sortBy )
    {
      return null
    };
    def arrayOfValues = sortBy.split( "," )
    def idx = 0;
    arrayOfValues.each { element ->
      def splitParam = element.split( /\+|:/ );
      if ( splitParam.length == 1 )
      {
        result << [splitParam]
      }
      else
      {
        if ( splitParam[1].toLowerCase() == "a" )
        {
          result << [splitParam[0], "ASC"]
        }
        else
        {
          result << [splitParam[0], "DESC"]
        }
      }
    }
    result;
  }
}
