package tilecache

import grails.converters.JSON
import joms.geotools.tileapi.job.TileCacheMessage

class ProductController {

  def index() {}

  def export()
  {
    response.setHeader( "Access-Control-Allow-Origin", "*" );
    response.setHeader( "Access-Control-Allow-Origin", "*" );
    response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
    // response.setHeader( "Access-Control-Max-Age", "3600" );
    response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );

    switch(request.method.toLowerCase())
    {
      case "post":
      //  def inputString = request.JSON.toString()
        def message = TileCacheMessage.newMessage()
        println request.JSON.toString()
        def inputString = '{"aoi":"POLYGON((-76.34119444915345 36.96805897475397,-76.34119444915345 36.90159168959772,-76.2901080233722 36.90159168959772,-76.2901080233722 36.96805897475397,-76.34119444915345 36.96805897475397))","writerProperties":{"imageFormat":"png","srs":"EPSG:4326"},"layers":["highres_us"],"srs":"EPSG:4326","format":"image/gpkg","type":"TileCacheMessage"}'
        //println "MESSAGE STRING: ${request.JSON.toString()}"
        // println "INPUT: ${inputString}"
        message.fromJsonString(inputString)

        message.newJobId()

        def result = ([jobId:message.jobId] as JSON).toString()
        render contentType: "application/json", result//([message:"Heck YEA!! "] as JSON).toString()
        break
      default:
        render contentType: "text/plain", "ERROR:  Only accept posts"//([message:"Heck YEA!! "] as JSON).toString()
        break;
    }
    // if ( params.callback )
    // {
    // result = "${params.callback}(${result});";
    //}
//    println request.JSON
    // allow cross domain
    // println output

  }
  def estimate()
  {

  }
}
