package ossim.diskcache

import grails.validation.Validateable

/**
 * Created by gpotts on 4/7/15.
 */
@Validateable
class FetchDataCommand
{
   Integer rows = 10
   Integer page = 1
   String sort = 'id'
   String order = 'desc'
   String filter
}
