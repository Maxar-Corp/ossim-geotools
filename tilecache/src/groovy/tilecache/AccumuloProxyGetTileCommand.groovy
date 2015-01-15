package tilecache

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by gpotts on 1/14/15.
 */
@Validateable
@ToString(includeNames = true)
class AccumuloProxyGetTileCommand
{
  String hashId
  String family
  String qualifier
  String table
  String format

}
