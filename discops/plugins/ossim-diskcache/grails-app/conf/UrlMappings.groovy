class UrlMappings {

	static mappings = {
//      group "/product",{
//         "/export" { controller = "product"; action = "export" }
//      }
      "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
       }

      "/"(view:"/index")
      "500"(view:'/error')
	}
}
