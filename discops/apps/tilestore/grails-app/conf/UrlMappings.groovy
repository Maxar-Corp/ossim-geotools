class UrlMappings
{

  static mappings = {

    group "/wms", {
      "/" { controller = "wms"; action = "index" }
      "/getCapabilities" { controller = "wms"; action = "getCapabilities" }
      "/getMap" { controller = "wms"; action = "getMap" }
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
