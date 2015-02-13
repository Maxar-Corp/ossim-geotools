package tilecache.app

import tilecache.api.TileStore
import tilecache.common.MemoryTileStore

class Application
{
  public static void main(String[] args)
  {
    TileStore tileStore = new MemoryTileStore()
    byte[] data = []
    String key = tileStore.putTile( data )

    data = tileStore.getTile( key )

    println 'Howdy!'
  }
}