class UrlMappings
{

  static mappings = {
    // remove the format overide for the getTile action
    "/wmts/getTile/$id?"( action: "getTile", controller: "accumulo" ) {
    }
    "/accumuloProxy/getTiles/$id?"( action: "getTiles", controller: "accumulo" ) {
    }
    "/wms/index/$id?"( action: "index", controller: "wms" ) {
    }
    "/wmts/index/$id?"( action: "index", controller: "wmts" ) {
    }
    "/wmts/tileParamGrid"( action: "tileParamGrid", controller: "wmts" ) {
    }
    
    "/$controller/$action?/$id?(.$format)?" {
      constraints {
        // apply constraints here
      }
    }

    "/"( controller: "app" )
    "500"( view: '/error' )
  }
}
