package org.ossim.omar.utilities

import java.util.Date;
import java.text.SimpleDateFormat
import java.math.MathContext

class DateUtil
{
	static parseDate(def dateString)
	{
    TimeZone utc = null
    SimpleDateFormat sdf = null
    Date date = null
    switch ( dateString )
    {
    case ~/[0-9]{4}[0-1][0-9][0-3][0-9]/:
      sdf = new SimpleDateFormat("yyyyMMdd");
//        println "one: ${dateString}"
      break
    case ~/[0-9]{4}-[0-1][0-9]-[0-3][0-9]/:
      sdf = new SimpleDateFormat("yyyy-MM-dd");
      //println "one: ${dateString}"
      break
    case ~/[0-9]{4}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9]Z/:
      utc = TimeZone.getTimeZone("UTC");
      sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      //println "two: ${dateString}"
      break
    case ~/[0-9]{4}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]{3}Z/:
      utc = TimeZone.getTimeZone("UTC");
      sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      //println "three: ${dateString}"
      break
    case ~/[0-9]{4}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5]?[0-9].[0-9]{1,}Z/:
      def x = dateString.split('T')
      def y = x[0].split('-')
      def z = (x[1] - 'Z').split(':')
      def r = new BigDecimal(z[2]).round(new MathContext(5)) as String

      if ( r.size() == 1 )
      r = "00.000"

      z[2] = r
      dateString = "${y[0]}-${y[1]}-${y[2]}T${z[0]}:${z[1]}:${z[2]}Z"
      utc = TimeZone.getTimeZone("UTC");
      sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      //println "four: ${dateString}"
      break
    }
    if ( sdf )
    {
      if ( utc )
      {
        sdf.setTimeZone(utc);
      }

      date = sdf.parse(dateString)
    }

    date
	}
  public static String getUtcDateTimeAsString(def date, def format="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    String utcTime = sdf.format(new Date());

    return utcTime;
  }

}