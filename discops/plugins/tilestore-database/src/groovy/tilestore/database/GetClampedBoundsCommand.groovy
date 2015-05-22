package tilestore.database

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 5/21/15.
 */
@Validateable
@ToString( includeNames = true )
class GetClampedBoundsCommand implements CaseInsensitiveBind
{
   String  layerName
   Double  res
   Integer resLevels
   String  resUnits // meters or degrees


   static constraints = {
      layerName nullable: false
      res nullable: false
      resLevels nullable: false
      resUnits nullable: true
   }

   HashMap toMap()
   {
      [
              layerName:layerName,
              res:res,
              resLevels:resLevels,
              resUnits:resUnits
      ]
   }
}
