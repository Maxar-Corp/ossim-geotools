package joms.geotools.accumulo

import groovy.transform.ToString

/**
 * Created by gpotts on 1/23/15.
 */
@ToString
class ImageTileKey {
  String rowId=""
  String family=""
  String qualifier=""
  String visibility=""
  long timestamp


  String getHashId()
  {
    String result = rowId

    if(family) result += "/${family}"
    if(qualifier) result += "/${qualifier}"

    result
  }

}
