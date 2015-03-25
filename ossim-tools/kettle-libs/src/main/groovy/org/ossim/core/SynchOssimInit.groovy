package org.ossim.core
import groovy.transform.Synchronized
import joms.oms.Init

class SynchOssimInit
{
	static private def contextLock = new Object()
	static private def initialized = false

   @Synchronized("contextLock")
	static void initialize()
	{
		if(!initialized){
      	Init.instance().initialize();
		//	def newArgs = ["","-T",".*"] as String[];
    //     Init.instance().initialize( newArgs.length, newArgs);

		} 
		initialized = true
	}
}
