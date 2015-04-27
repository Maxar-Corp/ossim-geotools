package tilestore

/**
 * Created by gpotts on 4/8/15.
 */
class TileCacheTagLib
{
   static namespace = "tilestore"

   def securityClassificationBanner = {attrs ->
      def level = grailsApplication.config.security.level
      def levelMap = grailsApplication.config.security."${level}"
//    def fontSize    = attrs.fontSize?:"1.5em"
      def fontSize = attrs.fontSize ?: "20px"
      def fontWeight = attrs.fontWeight ?: "bold"
      def id = attrs.id ? "id=\"${attrs.id}\"" : ''
      def style = attrs.style?:""
      def classString = "class=\"${attrs.class?:''.toString()}\""
      if ( !style ) style = "style=\"background: ${levelMap.color}; color: black; font-size:${fontSize}; font-weight:${fontWeight}\""
     // println style
      out << """<div ${id} ${style} ${classString}>
       ${levelMap.description}
     </div>"""
   }
  // def logout = {attrs->
  //    out<< """<div class=\"logout\"><a href="${createLink( controller: 'logout' )}">Logout</a></div>"""
  // }
}
