package joms.geotools.tileapi

import java.text.SimpleDateFormat

/**
 * Created by gpotts on 1/22/15.
 */
class DateUtil
{
  static String formatTimezone(Date date)
  {
    String DATEFORMAT = "yyyy-MM-dd HH:mm:ssX"
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
    def utcTime = sdf.format(date);
  }
  static String formatUtc(Date date)
  {
    String DATEFORMAT = "yyyy-MM-dd HH:mm:ssX"
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    def utcTime = sdf.format(date);
  }
}
