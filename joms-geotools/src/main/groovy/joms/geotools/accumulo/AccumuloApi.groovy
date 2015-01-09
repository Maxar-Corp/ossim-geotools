package joms.geotools.accumulo

import joms.geotools.tileapi.Tile
import org.apache.accumulo.core.Constants
import org.apache.accumulo.core.client.BatchScanner
import org.apache.accumulo.core.client.Scanner
import org.apache.accumulo.core.client.BatchWriterConfig
import org.apache.accumulo.core.client.Instance
import org.apache.accumulo.core.client.ZooKeeperInstance
import org.apache.accumulo.core.client.security.tokens.PasswordToken
import org.apache.accumulo.core.data.Key
import org.apache.accumulo.core.data.Mutation
import org.apache.accumulo.core.data.Range
import org.apache.accumulo.core.data.Value
import org.apache.hadoop.io.Text

import javax.imageio.ImageIO
import java.awt.Graphics
import java.awt.image.BufferedImage

/**
 * Created by gpotts on 1/8/15.
 */
class AccumuloApi
{
  String username
  String password
  String instanceName
  String zooServers
  String table
  boolean automergeTiles = true
  private instance
  private connector
  private batchWriter

  private def encodeToByteBuffer(BufferedImage bufferedImage, String type="tiff")
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, type, out)
    out.toByteArray()
  }

  def initialize()
  {
    //String instanceName = "accumulo";
    //String zooServers = "accumulo.radiantblue.local"
    instance = new ZooKeeperInstance(instanceName, zooServers);
    //Connector conn
    connector = instance.getConnector("root", new PasswordToken("root"));
    //conn = inst.getConnector("root", new PasswordToken("hadoop"));
    if(table)
    {
      if(!connector.tableOperations().exists(table))
      {
        connector.tableOperations().create(table);
      }

      def bwc = new BatchWriterConfig()
      batchWriter = connector.createBatchWriter(table, bwc);
   }



    this
  }

  Scanner getRow(String rowId)
  {
    // Create a scanner
    Scanner scanner = connector.createScanner(table, Constants.NO_AUTHS);
    scanner.setRange(new Range(new Text(rowId)));
    scanner
  }
  void deleteTable()
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

  void deleteRow(Scanner scanner)
  {
    Mutation deleter = null;
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
  void close()
  {
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
  void writeTile(Tile tile, String columnFamily, String columnQualifier)
  {
    def tileToMerge
    if(automergeTiles)
    {
      tileToMerge = getTile(tile.hashId, columnFamily, columnQualifier)
    }

    // Need to add merging support
    //
    def row    = new Text(tile.hashId)
    Mutation m = new Mutation(row);
    def imgResult = tile.image
    if(tileToMerge?.image)
    {
      merge(tile.image, tileToMerge.image)
      imgResult = tileToMerge.image
    }
    m.put(columnFamily.bytes, columnQualifier.bytes, encodeToByteBuffer(imgResult));
    batchWriter?.addMutation(m);
    batchWriter?.flush()
  }
  void writeTiles(def tileList, String columnFamily, String columnQualifier)
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
        tilesToMergeList = getTiles(hashIdList, columnFamily, columnQualifier)
      }
    }
    else
    {
      tilesToWriteList = tileList
    }
    tileList.each{ tile->
      // need to add merging support
      //
      def tileToMerge = tilesToMergeList?."${tile.hashId}"
      def image       = tile.image
      def row         =  new Text(tile.hashId)
      Mutation m      = new Mutation(row);
      if(tileToMerge)
      {
        merge(tile.image, tileToMerge.image)
        image = tileToMerge.image
      }
      // output the
      m.put(columnFamily.bytes, columnQualifier.bytes, encodeToByteBuffer(image));

      batchWriter?.addMutation(m);
    }
    batchWriter?.flush()
  }

  Tile getTile(String hashId, String columnFamily, String columnQualifier)
  {
    BatchScanner scanner = connector?.createBatchScanner(table, Constants.NO_AUTHS, 4);

    // this will get all tiles with the row ID
    //def range = new Range(hashString);

    // this will get exact tile
    def range = Range.exact(hashId, columnFamily, columnQualifier)

    scanner?.setRanges([range] as Collection)
    //scanner.setRange(range)
    // lets get the exact ID
  //  def imgVerify
    Tile result
    for (Map.Entry<Key,Value> entry : scanner)
    {
      result = new Tile(image:ImageIO.read(new java.io.ByteArrayInputStream(entry.getValue().get())),
                        hashId:hashId)


     // println "${imgVerify}";

    }

    scanner?.close()

    result
   // imgVerify
  }

  def getTiles(def hashIdList, String columnFamily, String columnQualifier)
  {
    BatchScanner scanner = connector.createBatchScanner(table, Constants.NO_AUTHS, 4);

    // this will get all tiles with the row ID
    //def range = new Range(hashString);
    def ranges = []
    hashIdList.each{hashId->
      ranges << Range.exact(hashId, columnFamily, columnQualifier)
    }

    scanner.setRanges(ranges as Collection)

    def result = [:]
    for (Map.Entry<Key,Value> entry : scanner)
    {
      Tile tile = new Tile()
      tile.image = ImageIO.read(new java.io.ByteArrayInputStream(entry.getValue().get()))
      tile.hashId = entry.getKey().row
      if(tile.image)
      {
        result."${tile.hashId}"  = tile
      }

    }

    scanner.close()

    result
  }
}
