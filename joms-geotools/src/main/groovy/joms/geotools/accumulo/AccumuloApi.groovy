package joms.geotools.accumulo

import joms.geotools.tileapi.Tile
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
import java.awt.Graphics
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
  boolean automergeTiles = true
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
  Scanner getRow(String rowId)
  {
    // Create a scanner
    Scanner scanner = connector.createScanner(table, Constants.NO_AUTHS);
    scanner.setRange(new Range(new Text(rowId)));
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

  void deleteRow(String rowId)
  {
    deleteRow(getRow(rowId))
    //def id = new Text(rowId)
    //connector?.tableOperations().deleteRows(table, id, null)
  }

  void deleteRow(String table, Scanner scanner)
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
  private void merge(BufferedImage src, BufferedImage dest)
  {
    if(src&&dest)
    {
      Graphics g = dest.getGraphics()
      g.drawImage(src, 0, 0, null)
      g.dispose()
    }
  }
  void writeTile(String table, byte[] tile, String hashId, String columnFamily, String columnQualifier)
  {
    def bwc = new BatchWriterConfig()
    def tileToMerge
    if(automergeTiles)
    {
      tileToMerge = getTile(table, hashId, columnFamily, columnQualifier)
    }

    // Need to add merging support
    //
    def row    = new Text(hashId)
    Mutation m = new Mutation(row);
    def imgResult = tile//.image
    if(tileToMerge?.image)
    {
//      def buf = tileToMerge.getAsBufferedImage()

//      merge(tile.image, tileToMerge.image)
 //     imgResult = tileToMerge.image
    }
   // m.put(columnFamily.bytes, columnQualifier.bytes, encodeToByteBuffer(imgResult));
    m.put(columnFamily.bytes, columnQualifier.bytes, imgResult);
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
  void writeTile(String table, Tile tile, String columnFamily, String columnQualifier)
  {
    def bwc = new BatchWriterConfig()
    def tileToMerge
    if(automergeTiles)
    {
      tileToMerge = getTile(table, tile.hashId, columnFamily, columnQualifier)
    }

    // Need to add merging support
    //
    def row    = new Text(tile.hashId)
    Mutation m = new Mutation(row);
    def imgResult = tile.image
    if(tileToMerge?.image)
    {
  //    merge(tile.image, tileToMerge.image)
  //    imgResult = tileToMerge.image
    }
    m.put(columnFamily.bytes, columnQualifier.bytes, imgResult);
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
  void writeTiles(String table, def tileList, String columnFamily, String columnQualifier)
  {
    def tilesToMergeList
    if(automergeTiles)
    {
      def hashIdList = []
      tileList.each{it->
        hashIdList<<it.hashId
      }
      if(hashIdList)
      {
        tilesToMergeList = getTiles(table, hashIdList, columnFamily, columnQualifier)
      }
    }
    else
    {
     // tilesToMergeList = tileList
    }
    def batchWriter
    try {
      batchWriter = createBatchWriter(table);
      tileList.each { tile ->
        // need to add merging support
        //
        def tileToMerge = tilesToMergeList?."${tile.hashId}"
        def image = tile.image
        def row = new Text(tile.hashId)
        Mutation m = new Mutation(row);
        if (tileToMerge) {
       //   merge(tile.image, tileToMerge.image)
       //   image = tileToMerge.image
        }
        // output the
        m.put(columnFamily.bytes, columnQualifier.bytes, image);

        batchWriter?.addMutation(m);
      }
      batchWriter?.flush()
    }
    catch(def e)
    {
      batchWriter?.close()
    }
  }

  Tile getTile(String table, String hashId, String columnFamily, String columnQualifier)
  {
    BatchScanner scanner =createBatchScanner(table);
    Tile result
    try{
      // this will get all tiles with the row ID
      //def range = new Range(hashString);

      // this will get exact tile
      def range = Range.exact(hashId, columnFamily, columnQualifier)

      scanner?.setRanges([range] as Collection)
      //scanner.setRange(range)
      // lets get the exact ID
      //  def imgVerify
      for (Map.Entry<Key,Value> entry : scanner)
      {
        result = new Tile(image:entry.getValue().get(),
                hashId:hashId)
      }
    }
    catch(def e)
    {
      e.printStackTrace()
    }

    scanner?.close()

    result
  }

  def getTiles(String table, def hashIdList, String columnFamily, String columnQualifier)
  {
    def result = [:]
    BatchScanner scanner =createBatchScanner(table);
    try{
      // this will get all tiles with the row ID
      //def range = new Range(hashString);
      def ranges = []
      hashIdList.each{hashId->
        ranges << Range.exact(hashId, columnFamily, columnQualifier)
      }

      scanner?.setRanges(ranges as Collection)

      for (Map.Entry<Key,Value> entry : scanner)
      {
        Tile tile = new Tile()
        tile.image = entry.getValue().get() as byte[]
        //tile.image = ImageIO.read(new java.io.ByteArrayInputStream(entry.getValue().get()))
        tile.hashId = entry.getKey().row
        if(tile.image)
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
