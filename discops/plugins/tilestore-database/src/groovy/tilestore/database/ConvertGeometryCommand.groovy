package tilestore.database

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

@Validateable
@ToString( includeNames = true )
class ConvertGeometryCommand implements CaseInsensitiveBind
{
   String sourceEpsg
   String targetEpsg
   String geometry

}
