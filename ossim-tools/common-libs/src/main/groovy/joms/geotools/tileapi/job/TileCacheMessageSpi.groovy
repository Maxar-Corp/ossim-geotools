package joms.geotools.tileapi.job

import org.ossim.oms.job.Message

/**
 * Created by gpotts on 3/3/15.
 */
class TileCacheMessageSpi
{
  boolean accepts(Object jsonObject)
  {

    return (jsonObject.type == "TileCacheMessage")
  }

  Message getMessageInstance(Object jsonObj)
  {
    TileCacheMessage result = new TileCacheMessage()

    if(jsonObj)
    {
      result.fromJson(jsonObj)
    }

    result
  }
}
