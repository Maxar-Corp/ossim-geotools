package ossim.diskcache

import groovy.transform.ToString

/**
 * Created by gpotts on 4/7/15.
 */
@ToString(includeNames = true)
class UpdateCommand
{
   Long id

   // used as a key if specified
   String directory
   /**
    * This can be of type SUB_DIRECTORY or of type DEDICATED.
    *
    * If the type is SUB_DIRECTORY then sizes are calculated by scanning the contents
    * and if it's DEDICATED then the entire device is used as the cache and calculating remaining size is much more efficient.
    *
    */
   String directoryType
   Integer maxSize
   Integer currentSize

   /**
    * This is an ISO8601 Period format for defining lengths of time
    * any job can be cached.  If you want jobs to stay around for
    * 7 days then the format is P7D (7 days) or P1W ( one week).
    *
    * This can be used by other processes to expire data as it ages
    *
    */
   String  expirePeriod
}
