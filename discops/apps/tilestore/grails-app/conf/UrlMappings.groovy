class UrlMappings
{

  static mappings = {
    "/wms/$action" {
      controller = 'wms'
      constraints {
        // apply constraints here
      }
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
