package joms.geotools.tileapi.job

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.ossim.oms.job.Message
import org.ossim.oms.job.ArchiveParams

class TileCacheMessage implements Message
{
  String jobId
  String jobDir
  String type = "TileCacheMessage"

  TileCacheParams tileCacheParams
  ArchiveParams   archiveParams

  static TileCacheMessage newMessage(def options=[:])
  {
    def result = new TileCacheMessage(options)

    result
  }
  void setJobId(String jobId)
  {
    this.jobId = jobId
  }
  String getJobId()
  {
    jobId
  }
  def execute()
  {

  }
  void abort()
  {

  }
  String newJobId()
  {
    jobId = UUID.randomUUID()

    jobId
  }
  String toJsonString()
  {
    def tempMap = [:]

    if(archiveParams)
    {
      tempMap.archive = archiveParams.toMap()

    }
    if(tileCacheParams)
    {
      tempMap.format = tileCacheParams.format
      tempMap.aoi = tileCacheParams.aoi
      tempMap.layers = tileCacheParams.layers
      tempMap.writerProperties = tileCacheParams.writerProperties
    }

    tempMap.jobId  = jobId
    tempMap.jobDir = jobDir
    tempMap.type   = type

    def jsonBuilder = new JsonBuilder( tempMap )

    jsonBuilder.toString()
  }
  def fromJsonString(String jsonString) throws Exception
  {

    archiveParams = new ArchiveParams()
    tileCacheParams = new TileCacheParams()

    def slurper   = new JsonSlurper()
    def jsonObj   = slurper.parseText(jsonString)
    if(jsonObj.type != type) throw new Exception("Message not of type TileCacheMessage")
    jobId  = jsonObj.jobId?:null
    jobDir = jsonObj.jobDir?:null

    tileCacheParams.layers = jsonObj.layers
    tileCacheParams.format = jsonObj.format
    tileCacheParams.aoi = jsonObj.aoi
    tileCacheParams.srs = jsonObj.srs
    def writerProperties = [:]

    if(jsonObj?.writerProperties)
    {
      tileCacheParams.writerProperties = jsonObj.writerProperties as HashMap

      tileCacheParams.writerProperties.each{k,v->
        tileCacheParams.writerProperties."${k}" = v.toString();
      }
    }
    if(jsonObj?.archive)
    {
      if(jsonObj.archive.type!=null) archiveParams.type = jsonObj.archive.type.toString()
      if(jsonObj.archive.deleteInputAfterArchiving!=null){
        archiveParams.deleteInputAfterArchiving = "${jsonObj.archive.deleteInputAfterArchiving}".toBoolean()
      }

      if(jsonObj.archive.inputFile!=null) {
        archiveParams.inputFile = jsonObj.archive.inputFile.toString()
      }
      else
      {
        archiveParams.inputFile = jobDir
      }

    }
    this
  }
}