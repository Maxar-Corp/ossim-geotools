package org.ossim.common

import grails.validation.Validateable
import groovy.transform.ToString
import org.ossim.common.CaseInsensitiveBind

/**
 * Created by gpotts on 4/7/15.
 */
@Validateable
@ToString(includeNames = true)
class FetchDataCommand implements CaseInsensitiveBind
{
   Integer rows   = 10
   Integer page   = 1
   String  sortBy = 'id'
   String  order  = 'desc'
   String  filter

   String getOrder()
   {
      String result = order

      if(!order)
      {
         def splitArray = sortBy.split("\\+")
         if(splitArray.size() == 2)
         {
            if(splitArray[1] == 'D')
            {
               result = "desc"
            }
            else if(splitArray[1] == 'A')
            {
               result = "asc"
            }
         }
      }

      result
   }
   String getSortBy()
   {
      String result

      // just in case we are geoserver style sorting
      if(sortBy)
      {
         result = sortBy.split('\\+')[0]
      }

      result
   }
}
