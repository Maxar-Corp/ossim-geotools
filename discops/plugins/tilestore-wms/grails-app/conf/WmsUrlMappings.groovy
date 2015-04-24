class WmsUrlMappings
{
  static mappings = {
    group "/wms", {
      "/" { controller = "wms"; action = "index" }
      "/getCapabilities" { controller = "wms"; action = "getCapabilities" }
      "/getMap" { controller = "wms"; action = "getMap" }
    }
  }
}
