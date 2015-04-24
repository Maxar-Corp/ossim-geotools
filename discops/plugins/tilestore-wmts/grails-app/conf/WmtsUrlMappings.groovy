
class WmtsUrlMappings
{

  static mappings = {
    group "/wmts", {
      "/" { controller = "wmts"; action = "index" }
      "/getCapabilities" { controller = "wmts"; action = "getCapabilities" }
      "/getTile" { controller = "wmts"; action = "getTile" }
      "/tileParamGrid" { controller = "wmts"; action = "tileParamGrid" }
      "/parseWMTS" { controller = "wmts"; action = "parseWMTS" }
    }
  }
}
