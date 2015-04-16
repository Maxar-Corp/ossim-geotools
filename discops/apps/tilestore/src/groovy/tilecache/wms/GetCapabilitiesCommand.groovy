package tilecache.wms

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by sbortman on 4/15/15.
 */

@Validateable
@ToString( includeNames = true, includeSuper = true )
class GetCapabilitiesCommand extends WmsCommand //implements CaseInsensitiveBind
{
}
