package tilecache.wmts

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by sbortman on 4/17/15.
 */
@ToString(includeNames = true, includeSuper = true)
@Validateable
class GetCapabilitiesCommand extends WmtsCommand
{
}
