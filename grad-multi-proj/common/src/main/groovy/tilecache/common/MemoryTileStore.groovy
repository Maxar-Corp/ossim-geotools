package tilecache.common

import tilecache.api.TileStore

class MemoryTileStore implements TileStore
{

  @Override
  String putTile(byte[] data)
  {
    return null
  }

  @Override
  byte[] getTile(String key)
  {
    return new byte[0]
  }
}