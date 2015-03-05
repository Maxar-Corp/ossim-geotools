class UrlMappings
{

  static mappings = {
    // remove the format overide for the getTile action
    "/accumuloProxy/getTile/$id?"( action: "getTile", controller: "accumuloProxy" ) {
    }
    "/accumuloProxy/getTiles/$id?"( action: "getTiles", controller: "accumuloProxy" ) {
    }
    "/accumuloProxy/wms/$id?"( action: "wms", controller: "accumuloProxy" ) {
    }
    "/accumuloProxy/wmts/$id?"( action: "wmts", controller: "accumuloProxy" ) {
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
