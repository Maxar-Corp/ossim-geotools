package tilecache

import grails.converters.JSON

class ProductController {

  def index() {}

  def export()
  {
    response.setHeader( "Access-Control-Allow-Origin", "*" );
    response.setHeader( "Access-Control-Allow-Origin", "*" );
    response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
   // response.setHeader( "Access-Control-Max-Age", "3600" );
    response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
   // if ( params.callback )
   // {
     // result = "${params.callback}(${result});";
    //}
    println request.JSON
    // allow cross domain
    // println output
    render contentType: "text/plain", "DOING CUT!!!"//([message:"Heck YEA!! "] as JSON).toString()

  }
  def estimate()
  {

  }
}
