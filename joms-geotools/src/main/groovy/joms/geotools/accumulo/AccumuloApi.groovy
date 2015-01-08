package joms.geotools.accumulo

import joms.geotools.tileapi.Tile
import org.apache.accumulo.core.Constants
import org.apache.accumulo.core.client.BatchScanner
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
  private instance
  private connector
  private batchWriter

  private def encodeToByteBuffer(def bufferedImage, String type="tiff")
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
      if(connector.tableOperations().exists(table))
      {
        connector.tableOperations().delete(table)
      }

      connector.tableOperations().create(table);
      def bwc = new BatchWriterConfig()
      batchWriter = connector.createBatchWriter(table, bwc);
   }



    this
  }

  def deleteTable()
  {
    if(table)
    {
      if(connector.tableOperations().exists(table))
      {
        connector.tableOperations().delete(table)
      }
    }
  }
  def close()
  {
    batchWriter?.close()
  }
  void writeTile(Tile tile, String columnFamily, String columnQualifier)
  {
    // Need to add merging support
    //
    def row =  new Text(tile.hashId)
    Mutation m = new Mutation(row);

    m.put(columnFamily.bytes, columnQualifier.bytes, encodeToByteBuffer(tile.image));

    batchWriter.addMutation(m);
    batchWriter.flush()
  }
  void writeTiles(def tileList, String columnFamily, String columnQualifier)
  {
      if(connector)
      {
        tileList.each{ tile->
          // need to add merging support
          //
          def row =  new Text(tile.hashId)
          Mutation m = new Mutation(row);
          // output the
          m.put(columnFamily.bytes, columnQualifier.bytes, encodeToByteBuffer(tile.image));

          batchWriter.addMutation(m);
        }
        batchWriter.flush()

      }
  }

  Tile getTile(String hashId, String columnFamily, String columnQualifier)
  {
    BatchScanner scanner = connector.createBatchScanner(table, Constants.NO_AUTHS, 4);

    // this will get all tiles with the row ID
    //def range = new Range(hashString);

    // this will get exact tile
    def range = Range.exact(hashId, columnFamily, columnQualifier)

    scanner.setRanges([range] as Collection)
    //scanner.setRange(range)
    // lets get the exact ID
  //  def imgVerify
    Tile result
    for (Map.Entry<Key,Value> entry : scanner)
    {
      imgVerify = ImageIO.read(new java.io.ByteArrayInputStream(entry.getValue().get()))

      result.image = imgVerify
      result.hashId = hashId
     // println "${imgVerify}";

    }

    scanner.close()

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
