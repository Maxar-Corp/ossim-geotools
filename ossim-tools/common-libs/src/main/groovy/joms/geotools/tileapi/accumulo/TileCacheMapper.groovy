package joms.geotools.tileapi.accumulo

import groovy.json.JsonSlurper
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
/**
 * Created by gpotts on 2/25/15.
 */
class TileCacheMapper extends MapReduceBase implements Mapper<Object, Text, Text, IntWritable> {
  private static final IntWritable ONE = new IntWritable(1);

  @Override
  public void map(Object key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter)
          throws IOException {

    //If more than one word is present, split using white space.
    String[] words = value.toString();
    def slurper   = new JsonSlurper()
    def jsonObj   = slurper.parseText(value.toString())

    if(jsonObj.jobId)
    {
      println "EXECUTING JOBID: ${jsonObj.jobId}"
      output.collect(new Text(jsonObj.jobId),ONE)
    }
//    if(jsonObj.type != type) throw new Exception("Message not of type TileCacheMessage")
    //Only the first word is the candidate name
  }

}
