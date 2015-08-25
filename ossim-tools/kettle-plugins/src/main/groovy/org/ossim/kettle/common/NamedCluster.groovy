package org.ossim.kettle.common

/**
 * Created by gpotts on 8/20/15.
 */
class NamedCluster
{
   static def getNamedCluster()
   {
      def c

      c = Class.forName("org.pentaho.di.core.namedcluster.model.NamedCluster")
      if(!c)
      {
         c = Class.forName("org.pentaho.big.data.api.cluster.NamedCluster")
      }

      c?.newInstance()
   }

   static def getNamedClusterClass()
   {
      def c

      c = Class.forName("org.pentaho.di.core.namedcluster.model.NamedCluster")
      if(!c)
      {
         c = Class.forName("org.pentaho.big.data.api.cluster.NamedCluster")
      }

      c
   }

   static def getNamedClusterWidget()
   {
      def c

      c = Class.forName("org.pentaho.di.ui.core.namedcluster.NamedClusterWidget")
      if(!c)
      {
         c = Class.forName("org.pentaho.big.data.plugins.common.ui.NamedClusterWidget")
      }

      c?.newInstance();
   }

   static def getNamedClusterWidgetClass()
   {
      def c

      c = Class.forName("org.pentaho.di.ui.core.namedcluster.NamedClusterWidget")
      if(!c)
      {
         c = Class.forName("org.pentaho.big.data.plugins.common.ui.NamedClusterWidget")
      }

      c
   }

   static def getNamedClusterManagerClass()
   {
      def c

      c = Class.forName("org.pentaho.di.core.namedcluster.NamedClusterManager")
      if(!c)
      {
         c = Class.forName("org.pentaho.big.data.api.cluster.NamedClusterService")
      }

      c
   }
   static def getNamedClusterManager()
   {
      def c

      c = Class.forName("org.pentaho.di.core.namedcluster.NamedClusterManager")
      if(!c)
      {
         c = Class.forName("org.pentaho.big.data.api.cluster.NamedClusterService")
      }

      c?.newInstance()
   }

}
