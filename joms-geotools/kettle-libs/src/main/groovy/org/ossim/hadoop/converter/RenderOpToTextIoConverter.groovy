package org.ossim.hadoop.converter

import org.pentaho.hadoop.mapreduce.converter.spi.ITypeConverter
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hadoop.mapreduce.converter.TypeConversionException;
import org.ossim.kettle.types.ValueMetaImage
import org.apache.hadoop.io.Text

class RenderOpToTextIoConverter implements ITypeConverter<javax.media.jai.RenderedOp, org.apache.hadoop.io.Text> 
{

	boolean canConvert(Class from, Class to)
	{
		//println "${this}.class: ......................... entered"
		def result

		//println "******************** ${from} --->  ${to} ******************"
		if(to == org.apache.hadoop.io.Text.class)
		{
			if(from == javax.media.jai.RenderedOp.class)
			{
				result = true
			}	
			else if(from == org.apache.hadoop.io.Text.class)
			{
				result = true
			}		
		}
		//println "${this}.class: ......................... leaving with result = ${result}"

		result
	}
	Text convert(ValueMetaInterface meta, Object obj) throws TypeConversionException
	{
		def result

		try{
			def objString = ""
			def text = new Text()

			//println "*****************CONVERTING THE OBJECT FROM CLASS TYPE ====== ${obj.class}"
			if(obj)
			{
				objString = meta.getString(obj)
			}
			if(objString)
			{
				text.set(objString)
			}
			//println "************** RESULT == ${objString}"
			result = text
		}
		catch(e)
		{
			//println "************** ERROR!!!!!!!!!! ${e}"
			throw new TypeConversionException(e.message)
		}

		result
	}

}