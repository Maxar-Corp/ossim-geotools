package joms.geotools.tileapi.accumulo

import org.apache.accumulo.core.Constants
import org.apache.accumulo.core.client.BatchScanner
import org.apache.accumulo.core.client.BatchWriter
import org.apache.accumulo.core.client.Scanner
import org.apache.accumulo.core.client.BatchWriterConfig
import org.apache.accumulo.core.client.ZooKeeperInstance
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.accumulo.core.data.Key
import org.apache.accumulo.core.data.Mutation
import org.apache.accumulo.core.data.Range
import org.apache.accumulo.core.data.Value
import org.apache.hadoop.io.Text
import org.springframework.beans.factory.InitializingBean

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Created by gpotts on 1/8/15.
 */
class AccumuloApi implements InitializingBean
{
  String username
  String password
  String instanceName
  String zooServers
  private instance
  private connector

  void afterPropertiesSet()
  {
    initialize()
  }

  private def encodeToByteBuffer(BufferedImage bufferedImage, String type="tiff")
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, type, out)
    out.toByteArray()
  }
  private BatchWriter createBatchWriter(String table)
  {
    def bwc = new BatchWriterConfig()
    def batchWriter
    try {
      batchWriter = connector.createBatchWriter(table, bwc);
    }
    catch (def e)
    {

    }
    batchWriter
  }
  private BatchScanner createBatchScanner(String table)
  {
    BatchScanner scanner = connector?.createBatchScanner(table, Constants.NO_AUTHS, 4);

    scanner
  }
  def initialize()
  {
    //String instanceName = "accumulo";
    //String zooServers = "accumulo.radiantblue.local"
    instance = new ZooKeeperInstance(instanceName, zooServers);
    //Connector conn
    connector = instance.getConnector("root", new PasswordToken("root"));
    //conn = inst.getConnector("root", new PasswordToken("hadoop"));

    this
  }


  def close()
  {

  }
  Scanner getRow(String table, ImageTileKey key)
  {
    // Create a scanner
    Scanner scanner = connector.createScanner(table, Constants.NO_AUTHS);
    Range range = new Range(new Text(key.rowId))
    scanner.setRange(range);
    scanner
  }
  void renameTable(String oldTableName, String newTableName)
  {
    connector.tableOperations().rename(oldTableName, newTableName)
  }
  void createTable(String table)
  {
    if(table)
    {
      if(!connector.tableOperations().exists(table))
      {
        connector.tableOperations().create(table);
      }
    }
  }
  void deleteTable(String table)
  {
    if(table)
    {
      if(connector.tableOperations().exists(table))
      {
        connector.tableOperations().delete(table)
      }
    }
  }

  void deleteRow(String table, ImageTileKey key)
  {
    deleteRow(getRow(table, key))
  }

  void deleteRow(Scanner scanner)
  {
    Mutation deleter = null;
    def batchWriter = createBatchWriter(table);

    try{
      // iterate through the keys
      for (Map.Entry<Key,Value> entry : scanner) {
        // create a mutation for the row
        if (deleter == null)
          deleter = new Mutation(entry.getKey().getRow());
        // the remove function adds the key with the delete flag set to true
        deleter.putDelete(entry.getKey().getColumnFamily(), entry.getKey().getColumnQualifier());
      }
      batchWriter?.addMutation(deleter);
      batchWriter?.flush();
    }
    catch(def e)
    {

    }
    batchWriter?.close()
  }
//  private void merge(BufferedImage src, BufferedImage dest)
//  {
//    if(src&&dest)
//    {
//      Graphics g = dest.getGraphics()
//      g.drawImage(src, 0, 0, null)
//      g.dispose()
//    }
//  }
  void writeTile(String table, byte[] tile, ImageTileKey key)//String hashId, String columnFamily, String columnQualifier)
  {
    def bwc = new BatchWriterConfig()
    def row    = new Text(key.rowId)
    Mutation m = new Mutation(row);
    def imgResult = tile//.image
    m.put(key.family?.bytes, key.qualifier?.bytes, imgResult);
    def batchWriter = createBatchWriter(table);
    try{
      batchWriter?.addMutation(m);
      batchWriter?.flush()
    }
    catch(def e)
    {

    }
    batchWriter?.close()

  }
  void writeTile(String table, TileCacheImageTile tile)//Tile tile, String columnFamily, String columnQualifier)
  {
    writeTile(table, tile.data, tile.key)
  }
  void writeTiles(String table, TileCacheImageTile[] tileList)throws Exception//, String columnFamily, String columnQualifier)
  {
    def batchWriter
    try {
      batchWriter = createBatchWriter(table);
      tileList.each { tile ->
        def image = tile.data
        def row = new Text(tile.key.rowId)
        Mutation m = new Mutation(row);
        m.put(tile.key.family.bytes, tile.key.qualifier.bytes, tile.data);
        batchWriter?.addMutation(m);
      }
      batchWriter?.flush()
    }
    catch(def e)
    {
      batchWriter?.close()
      throw e
    }
  }

  TileCacheImageTile getTile(String table, ImageTileKey key)
  {
    BatchScanner scanner =createBatchScanner(table);
    TileCacheImageTile result
    try{
      def range = Range.exact(key.rowId, key.family, key.qualifier)

      scanner?.setRanges([range] as Collection)
      for (Map.Entry<Key,Value> entry : scanner)
      {
        key.visibility = entry.key.columnVisibility
        key.timestamp = entry.key.timestamp
        result = new TileCacheImageTile(entry.getValue().get(), key)
      }
    }
    catch(def e)
    {
      e.printStackTrace()
    }

    scanner?.close()
    result
  }

  def getTiles(String table, ImageTileKey[] keyList)
  {
    def result = [:]
    BatchScanner scanner =createBatchScanner(table);
    try{
      // this will get all tiles with the row ID
      //def range = new Range(hashString);
      def ranges = []
      keyList.each{key->
        ranges << Range.exact(key.rowId, key.family, key.qualifier)
      }

      scanner?.setRanges(ranges as Collection)

      for (Map.Entry<Key,Value> entry : scanner)
      {
        TileCacheImageTile tile = new TileCacheImageTile(0,0,0)
        tile.data = entry.getValue().get() as byte[]
        tile.key = new ImageTileKey(rowId:entry.getKey().row,
                family:entry.key.columnFamily,
                qualifier:entry.key.columnQualifier,
                visibility:entry.key.columnVisibility,
                timestamp:entry.key.timestamp)
        if(tile.data)
        {
          result."${tile.hashId}"  = tile
        }
      }
    }
    catch(def e)
    {
      e.printStackTrace()
    }

    scanner?.close()

    result
  }

}
