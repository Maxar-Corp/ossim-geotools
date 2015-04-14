package tilecache

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 1/14/15.
 */
@Validateable
@ToString( includeNames = true )
class GetTileCommand implements CaseInsensitiveBind
{
  String hashId
  String family
  String qualifier
  String table
  String format
}
