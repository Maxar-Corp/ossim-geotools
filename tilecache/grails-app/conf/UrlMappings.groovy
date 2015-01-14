class UrlMappings {

  static mappings = {
    // remove the format overide for the getTile action
    "/accumuloProxy/getTile/$id?"(action:"getTile", controller:"accumuloProxy"){
    }
    "/accumuloProxy/getTiles/$id?"(action:"getTiles", controller:"accumuloProxy"){
    }
    "/$controller/$action?/$id?(.$format)?"{
      constraints {
        // apply constraints here
      }
    }

    "/"(view:"/index")
    "500"(view:'/error')
  }
}
