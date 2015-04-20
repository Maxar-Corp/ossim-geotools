package org.ossim.common

trait CaseInsensitiveBind
{
   def fixParamNames(def params)
   {
      def names = ( getMetaClass()?.properties*.name ).sort() - ['class', 'constraints', 'errors']

      def newParams = params.inject( [:] ) { a, b ->
         def propName = names.find { it.equalsIgnoreCase( b.key ) && b.value != null }
         if ( propName )
         {
            //println "${propName}=${b.value}"
            a[propName] = b.value
         }
         else
         {
            a[b.key] = b.value
         }
         a
      }

      params.clear()
      params.putAll( newParams )
      params
   }
}
