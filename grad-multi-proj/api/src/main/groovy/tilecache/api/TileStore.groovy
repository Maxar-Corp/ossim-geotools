package tilecache.api

interface TileStore
{
  String putTile(byte[] data)

  byte[] getTile(String key)
}