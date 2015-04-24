class WfsUrlMappings
{
  static mappings = {
    group "/wfs", {
      "/" { controller = "wfs"; action = "index" }
      "/getCapabilities" { controller = "wfs"; action = "getCapabilities" }
      "/describeFeatureType" { controller = "wfs"; action = "describeFeatureType" }
      "/getFeature" { controller = "wfs"; action = "getFeature" }
    }
  }
}
