package tilestore.wfs

import tilestore.wfs.DescribeFeatureTypeCommand
import tilestore.wfs.GetCapabilitiesCommand
import tilestore.wfs.GetFeatureCommand
import tilestore.wfs.WfsCommand

class WfsFilters
{

  def filters = {
    wfs( uri: '/wfs' ) {
      before = {
        //println "before: ${params}"
        new WfsCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wfsGetCapabilities( uri: '/wfs/getCapabilities' ) {
      before = {
        //println "before: ${params}"
        new GetCapabilitiesCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wfsDescribeFeatureType( uri: '/wfs/describeFeatureType' ) {
      before = {
        //println "before: ${params}"
        new DescribeFeatureTypeCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }

    wfsGetFeature( uri: '/wfs/getFeature' ) {
      before = {
        //println "before: ${params}"
        new GetFeatureCommand().fixParamNames( params )
        //println "after: ${params}"
      }
      after = { Map model ->

      }
      afterView = { Exception e ->

      }
    }
  }

//  getFeature( uri: '/wfs/index' ) {
//  before = {
//    response.setHeader( "Access-Control-Allow-Origin", "*" );
//    //response.setHeader( "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE" );
//    response.setHeader( "Access-Control-Allow-Methods", "GET" );
//    response.setHeader( "Access-Control-Max-Age", "3600" );
//    response.setHeader( "Access-Control-Allow-Headers", "x-requested-with" );
//
//    //println "before: ${params}"
//    new WfsCommand().fixParamNames( params )
//    //println "after: ${params}"
//  }
//  after = { Map model ->
//
//  }
//  afterView = { Exception e ->
//
//  }
//}
}
