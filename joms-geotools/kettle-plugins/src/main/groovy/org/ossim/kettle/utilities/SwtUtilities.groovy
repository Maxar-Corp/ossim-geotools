package org.ossim.kettle.utilities

class SwtUtilities
{
	static def previousStepFields(def transMeta, def stepname, def fieldType=null)
	{
		def result = []
		try
		{
			def stepFields = transMeta?.getPrevStepFields(stepname)
			if(!fieldType)
			{
				result = stepFields?.fieldNames
			}
			else
			{
				stepFields.valueMetaList.each{valueMeta->
					if(valueMeta.type in fieldType)
					{
						result << valueMeta.name
					}
				}
			}
		}
		catch(e)
		{
			result = []
		}
		result
	}	
}