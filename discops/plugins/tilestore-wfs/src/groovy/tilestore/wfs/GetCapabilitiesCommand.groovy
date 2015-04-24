package tilestore.wfs

import grails.validation.Validateable
import groovy.transform.ToString

/**
 * Created by sbortman on 4/15/15.
 */

@Validateable
@ToString( includeNames = true, includeSuper = true )
class GetCapabilitiesCommand extends WfsCommand
{
}
