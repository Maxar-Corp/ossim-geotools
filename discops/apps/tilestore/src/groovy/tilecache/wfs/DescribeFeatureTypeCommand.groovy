package tilecache.wfs

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by sbortman on 4/23/15.
 */
@Validateable
@ToString( includeNames = true, includeSuper = true )

class DescribeFeatureTypeCommand extends WfsCommand
{
  String typeName

  static constraints = {
    typeName( nullable: false )
  }
}
