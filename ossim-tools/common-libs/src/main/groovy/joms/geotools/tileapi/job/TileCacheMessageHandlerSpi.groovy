package joms.geotools.tileapi.job

import org.ossim.oms.job.MessageHandlerSpi
import org.ossim.oms.job.MessageHandler

/**
 * Created by gpotts on 3/3/15.
 */
class TileCacheMessageHandlerSpi implements MessageHandlerSpi
{
  boolean accepts(Object obj)
  {

    return (obj.type == "TileCacheMessage")
  }

  MessageHandler getMessageHandlerInstance(Object obj)
  {
    TileCacheMessageHandler result = new TileCacheMessage()

    if(obj)
    {
      result.fromJson(obj)
    }

    result
  }
}
