package tilecache

/**
 * Created by gpotts on 2/17/15.
 */
class ProductExportCommand implements CaseInsensitiveBind{
  def layers = [] as String[]
  String aoi
  String srs
  String format
  def outputProperties = [:]
}
