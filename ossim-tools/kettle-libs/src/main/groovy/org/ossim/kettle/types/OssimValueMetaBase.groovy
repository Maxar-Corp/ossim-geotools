package org.ossim.kettle.types
import org.pentaho.di.core.row.value.ValueMetaBase

import javax.media.jai.RenderedOp
import java.awt.Image
import java.awt.image.RenderedImage
import java.awt.image.DataBufferByte
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.WritableRaster
import org.pentaho.di.core.exception.KettleValueException
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.io.UnsupportedEncodingException;
import java.io.EOFException;
import java.io.IOException;
import java.io.DataOutputStream
import java.io.DataInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageReadParam
import javax.imageio.stream.ImageInputStream
import javax.imageio.ImageIO
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTWriter
import com.vividsolutions.jts.io.WKBWriter
import com.vividsolutions.jts.io.WKBReader
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.GeometryFactory
import javax.media.jai.JAI
import javax.media.jai.PlanarImage
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import javax.media.jai.remote.SerializableRenderedImage
import javax.swing.ImageIcon

public class OssimValueMetaBase extends ValueMetaBase
{
	static final int TYPE_IMAGE          = 1000
	static final int TYPE_CLONABLE_IMAGE = 1001
	static final int TYPE_GEOMETRY_2D    = 1002

	OssimValueMetaBase()
	{
		super( );
	}

	OssimValueMetaBase( String name )
	{
		super( name );
	}

	OssimValueMetaBase( String name, int type )
	{
		super( name, type);
	}

	OssimValueMetaBase( String name, int type, int storageType )
	{
		super(name, type, storageType)
	}

	OssimValueMetaBase( String name, int type, int length, int precision )
	{
		super(name, type, length, precision)
	}
	OssimValueMetaBase( Node node ) throws KettleException
	{
		this()
	}
	OssimValueMetaBase( DataInputStream inputStream ) throws KettleFileException, KettleEOFException {
		this()
		try {
			type = inputStream.readInt();
		} catch ( EOFException e ) {
			throw new KettleEOFException( e );
		} catch ( IOException e ) {
			throw new KettleFileException( toString() + " : Unable to read value metadata from input stream", e );
		}
		readMetaData( inputStream );
	}

	Object cloneValueData( Object object ) throws KettleValueException
	{
//		println "CLONE VALUE DATA"
		def result = object
		if ( storageType == STORAGE_TYPE_NORMAL )
		{
			switch ( getType() ) {
				case TYPE_IMAGE:
					result = object
					break
				case TYPE_CLONABLE_IMAGE:
					result = readImageFromByteArray(writeImageToByteArray(object))
					break
				case TYPE_GEOMETRY_2D:
					Geometry geometry = (Geometry) object
					result = geometry?.clone()
					break
				default:
					result = super.cloneValueData(object)
			}
		}
		result
	}
	String getString( Object object ) throws KettleValueException
	{
		//println "${this.class}.getString: entered......"
		def result
		switch ( getType() )
		{
			case TYPE_IMAGE:
			case TYPE_CLONABLE_IMAGE:
				result = convertImageToString(object)
				break
			case TYPE_GEOMETRY_2D:
				result = object?.toString()
				break
			default:
				result = super.getString(object)
		}
		//println "${this.class}.getString: leaving......${result?.bytes?.length}"

		result
	}

	def getImage(Object object) throws KettleValueException
	{
		//println "${this}.getImage: ...........entered"
		def result
		if ( object == null )
		{
			return null;
		}
		switch ( type )
		{
			case TYPE_IMAGE:
			case TYPE_CLONABLE_IMAGE:
				switch ( storageType ) {
					case STORAGE_TYPE_NORMAL:
						if(object instanceof BufferedImage)
						{
							def planarImage = PlanarImage.wrapRenderedImage(object as RenderedImage)
							result = JAI.create("NULL", planarImage)
							result.data
						}
						else if(object instanceof PlanarImage)
						{
							result = (PlanarImage)object
						}
						else if(object instanceof String)
						{
							result = readImageFromByteArray(object.decodeBase64())
						}
						else if(object instanceof byte[])
						{
							result = readImageFromByteArray(object)
						}
						else
						{
							throw new KettleValueException( toString() + " : Unsupported Image instance " + object.class + " specified." );
						}
						break
					case STORAGE_TYPE_BINARY_STRING:
					case STORAGE_TYPE_INDEXED:
						throw new KettleValueException( toString() + " : Unsupported storage type " + storageType + " specified." );
						break
					default:
						throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
				}
				break
			default:
				throw new KettleValueException( toString() + " : I don't know how to convert type " +  type);

		}
		//println "${this}.getImage: ........... leaving"

		result
	}

	def getGeometry(Object object) throws KettleValueException
	{
		def result = object
		if ( object == null )
		{
			return null;
		}
		switch ( type )
		{
			case TYPE_GEOMETRY_2D:
				switch ( storageType ) {
					case STORAGE_TYPE_NORMAL:
						if(object instanceof Geometry)
						{
							result = (Geometry) object;

						}
						else if(object instanceof String)
						{
							result = readGeometryFromBuffer(object)
						}
						break
					case STORAGE_TYPE_BINARY_STRING:
					case STORAGE_TYPE_INDEXED:
						throw new KettleValueException( toString() + " : Unsupported storage type " + storageType + " specified." );
						break
					default:
						throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
						break
				}
				break
			default:
				throw new KettleValueException( toString() + " : I don't know how to convert type " +  type);
		}
		result
	}
	boolean isBinary()
	{
		def result = false
		switch(getType())
		{
			case TYPE_IMAGE:
			case TYPE_CLONABLE_IMAGE:
				result = true
				break
			case TYPE_GEOMETRY_2D:
				result = false
				break
			default:
				result = super.isBinary()
				break
		}

		result
	}
	public Object convertData( ValueMetaInterface meta2, Object data2 ) throws KettleValueException {
		def result
		switch ( getType() ) {
			case TYPE_IMAGE:
			case TYPE_CLONABLE_IMAGE:
				if(meta2.isBinary())
				{
					if((data2 instanceof RenderedOp)||
							  (data2 instanceof BufferedImage))
					{
						result = data2
					}
					else
					{
						def bytes = meta2.getBinary(data2)
						result = convertBinaryToImage(bytes)
					}
				}
				else
				{
					result = getImage(meta2.getString(data2))
				}
				break
			case TYPE_GEOMETRY_2D:
				if(data2 instanceof Geometry)
				{
					result = data2
				}
				else
				{
					result = getGeometry(meta2.getString(data2))
				}
				break
			default:
				super.convertData(meta2, data2)
				break
		}

		result
	}
	public byte[] getBinary( Object object ) throws KettleValueException {
		def result
		switch(getType())
		{
			case TYPE_IMAGE:
			case TYPE_CLONABLE_IMAGE:
				result = writeImageToByteArray(object)
				break
			case TYPE_GEOMETRY_2D:
				def writer = new com.vividsolutions.jts.io.WKTWriter(2)
				def tempResult= writer.write((Geometry)object)
				result = tempResult.bytes
				break;
			default:
				result = super.getBinary(object)
				break
		}

		result
	}
	private def convertBinaryToImage(def binaryImage)
	{
		readImageFromByteArray(binaryImage)
	}
	private String convertImageToString(def image)
	{
		def result
		//println "${this.class}.convertImageToString: .... entered"
		if(image)
		{
			if(image instanceof String)
			{
				result = image
			}
			else
			{
				def bytes = writeImageToByteArray(image)
				if(bytes)
				{
					result    = bytes?.encodeBase64()?.toString()
				}
			}
		}
		if(result == null) result = new String("")
		//println "${this.class}.convertImageToString: .... leaving"
		result
	}
	private def readGeometryFromBuffer(def geomString)
	{
		def result
		if(geomString)
		{
			//	def reader = new com.vividsolutions.jts.io.WKTReader(new GeometryFactory())
			//	result = reader.read(geomString)
			result = (new WKTReader()).read(geomString)
		}

		result
	}
	private def readImageFromByteArray(def bytes)
	{
		def result

		if(bytes instanceof RenderedOp)
		{
			result = bytes
			result?.data
		}
		else if(bytes instanceof BufferedImage)
		{
			result = bytes
		}
		try{
			if(bytes instanceof byte[])
			{
				def byteArrayInputStream = new ByteArrayInputStream(bytes)
				//def image = ImageIO.read(byteArrayInputStream)
				def objIStream = new ObjectInputStream(byteArrayInputStream)
				def planarImage = objIStream.readObject() //as PlanarImage

				if(planarImage instanceof RenderedImage)
				{
					planarImage = PlanarImage.wrapRenderedImage(planarImage as RenderedImage)
					result = JAI.create("NULL", planarImage)
					result.data
				}
			}
		}
		catch(e)
		{
			result = null
		}
		if(!result)
		{
			// see if it's a raw image
			try{
				def img = ImageIO.read(new ByteArrayInputStream(bytes))
				if(img)
				{
					img = PlanarImage.wrapRenderedImage(img as RenderedImage)
					result = JAI.create("NULL", img)
					result?.data
				}
			}
			catch(e)
			{
				result = null
			}
		}

		if(!result)
		{
			throw new KettleException("Unable to convert byte array to an image of type ${bytes.class.name}")
		}//println result

		result
	}
	private def writeImageToByteArray(def image)
	{
		//println "${this}.writeImageToByteArray: ... entered"
		def result
		def byteOutputStream  = new ByteArrayOutputStream()
		def objOStream        = new ObjectOutputStream(byteOutputStream)
		def planarImage
		//println "${this.class}.writeImageToByteArray .... entered"

		try{
			if(image)
			{
				if(image instanceof byte[])
				{
					result = image
				}
				else if(image instanceof RenderedOp)
				{
					image?.data
					def serializable = new SerializableRenderedImage(image.createSnapshot(), true)
					objOStream.writeObject(serializable)
					objOStream.close()
					result = byteOutputStream.toByteArray()
				}
				else if(image instanceof PlanarImage)
				{
					planarImage = (PlanarImage)image
					planarImage?.data // make sure the tile is loaded before serialization
					//serialize deep copy
					def serializable = new SerializableRenderedImage(planarImage.createSnapshot(), true)
					objOStream.writeObject(serializable)
					objOStream.close()
					result = byteOutputStream.toByteArray()
				}
				else if(image instanceof RenderedImage)
				{
					planarImage = PlanarImage.wrapRenderedImage(image as RenderedImage)
					//println "TRYING image instanceof image instanceof RenderedImage"
					// convert to a serializable planar image 
					//
					planarImage = JAI.create("NULL", planarImage)
					planarImage.data // load data
					//serialize deep copy
					def serializable = new SerializableRenderedImage(planarImage.createSnapshot(), true)
					objOStream.writeObject(serializable)
					objOStream.close()
					result = byteOutputStream.toByteArray()
				}
				else
				{
					result = null
				}
			}
		}
		catch(e)
		{
			e.printStackTrace()
			throw e
			//println byteOutputStream.toByteArray()
			result = null
		}
		//println "${this.class}.writeImageToByteArray .... leaving"
		result
	}
	private void writeImage(def outputStream, def image)
	{
		def bytes = writeImageToByteArray(image)
		if(bytes)
		{
			outputStream.writeInt( bytes.length );
			outputStream.write(bytes)
		}

		/*
      def imageString = convertImageToString(image)
      if(imageString)
      {
      //	println imageString
         writeString(outputStream, imageString)
        }
        */
	}
	private def readImage(def inputStream)
	{
		def result
		int size = inputStream.readInt();
		if(size)
		{
			byte[] buffer = new byte[size];
			inputStream.readFully( buffer );
			def byteArrayInputStream = new ByteArrayInputStream(buffer)
			def objIStream = new ObjectInputStream(byteArrayInputStream)
			def object = objIStream.readObject()

			if (object instanceof RenderedImage)
			{
				def planarImage = PlanarImage.wrapRenderedImage(object as RenderedImage)
				result = JAI.create("NULL", planarImage)
			}
			else if(object instanceof PlanarImage)
			{
				result = object as PlanarImage
			}
			else
			{
				throw new Exception("Can't convert stream object to Planar.  Please add support for class conversion: ${object.class.name}")
			}
			result?.data
			objIStream.close()
		}
		result
		/*
      def result
      def imageString = readString(inputStream)

      if(imageString)
      {
         //println imageString
         buffer = imageString?.decodeBase64()
         def byteArrayInputStream = new ByteArrayInputStream(buffer)
         def objIStream = new ObjectInputStream(byteArrayInputStream)
         result = objIStream.readObject() as PlanarImage
         result?.data
         objIStream.close()
      }
      result
      */
	}
	public void writeData( DataOutputStream outputStream,
								  Object object ) throws KettleFileException
	{
		try{
			outputStream.writeBoolean( object == null );
			switch ( storageType )
			{
				case STORAGE_TYPE_NORMAL:
					switch(getType())
					{
						case TYPE_IMAGE:
						case TYPE_CLONABLE_IMAGE:
							try{
								def im
								if(object instanceof RenderedImage)
								{
									im = object as RenderedImage
								}
								else if(object instanceof PlanarImage)
								{
									im = object as PlanarImage
								}
								else if(object instanceof String)
								{
									im = object as String
								}
								else
								{
									throw new KettleValueException( toString() + " Unable to write image to stream" );
								}

								writeImage(outputStream, im)
							}
							catch(e)
							{
								//println e.printStackTrace()
								throw new KettleValueException( toString() + " Unable to write image to stream" );
							}
							break
						case TYPE_GEOMETRY_2D:
							try{
								if(object)
								{
									def writer = new com.vividsolutions.jts.io.WKTWriter(2)
									def tempString = writer.write((Geometry)object)
									writeString(outputStream, tempString)
								}
								//outputStream.writeInt( bytes.length );
								//outputStream.write(bytes)
							}
							catch(e)
							{
								throw new KettleValueException( toString() + " Unable to write geometry to stream" );
							}
							break
						default:
							super.writeData(outputStream, object)
							break
					}
					break
				case OssimValueMetaBase.TYPE_GEOMETRY_2D:

				default:
					super.writeData(outputStream, object)
					break
			}
		}
		catch(e)
		{
			throw new KettleValueException( toString() + " Unable to write object to stream" );
		}

	}
	Object readData( DataInputStream inputStream ) throws KettleFileException,
			  KettleEOFException, SocketTimeoutException
	{
		def result
		try {
			// Is the value NULL?
			if ( inputStream.readBoolean() )
			{
				return null; // done
			}
			switch ( storageType )
			{
				case STORAGE_TYPE_NORMAL:
					// Handle Content -- only when not NULL
					switch ( getType() ) {
						case TYPE_IMAGE:
						case TYPE_CLONABLE_IMAGE:
							result = readImage(inputStream)
							break
						case TYPE_GEOMETRY_2D:
							try {
								def value = readString(inputStream)

								//int size = inputStream.readInt();
								//byte[] buffer = new byte[size];
								//inputStream.readFully( buffer );

								result = readGeometryFromBuffer(value)
								//def byteArrayInputStream = new ByteArrayInputStream(buffer)

								//def reader = new com.vividsolutions.jts.io.WKTReader(new GeometryFactory())
								//result = reader.read(new com.vividsolutions.jts.io.InputStreamInStream(byteArrayInputStream))
							}
							catch(e) {
								throw new KettleValueException( toString() + " Unable to read geometry from stream" );
							}
							break
						default:
							result = super.readData(inputStream)
							break
					}
					break
			//case STORAGE_TYPE_BINARY_STRING:

			//case STORAGE_TYPE_INDEXED:
			//  return readSmallInteger( inputStream ); // just an index: 4-bytes should
			// be enough.

				default:
					switch(getType())
					{
						case TYPE_IMAGE:
						case TYPE_CLONABLE_IMAGE:
						case TYPE_GEOMETRY_2D:
							throw new KettleException("TYPE_IMAGE, TYPE_CLONABLE_IMAGE, and TYPE_GEOMETRY_2D not implemented for storage type")
							break
						default:
							result = super.readData(inputStream)
							break

					}
					break
			}
		} catch ( EOFException e ) {
			throw new KettleEOFException( e );
		} catch ( SocketTimeoutException e ) {
			throw e;
		} catch ( IOException e ) {
			throw new KettleFileException( toString() + " : Unable to read value data from input stream", e );
		}
		result
	}
	void readMetaData( DataInputStream inputStream ) throws 	KettleFileException,
			  KettleEOFException
	{
		super.readMetaData(inputStream)
	}
	@Override
	public String getMetaXML() throws IOException
	{

		switch(type)
		{
			case TYPE_IMAGE:
			case TYPE_CLONABLE_IMAGE:
				break
			default:
				return super.getMetaXML()
		}

		StringBuffer xml = new StringBuffer();

		xml.append( XMLHandler.openTag( XML_META_TAG ) );

		xml.append( XMLHandler.addTagValue( "type", getTypeDesc() ) );
		xml.append( XMLHandler.addTagValue( "storagetype", getStorageTypeCode( getStorageType() ) ) );

		switch ( storageType ) {
			case STORAGE_TYPE_INDEXED:
				xml.append( XMLHandler.openTag( "index" ) );

				// Save the indexed strings...
				//
				if ( index != null ) {
					for ( int i = 0; i < index.length; i++ ) {
						try {
							xml.append( XMLHandler.addTagValue( "value", convertImageToString(index[i])))
						} catch ( ClassCastException e )
						{
							throw new RuntimeException( toString() + " : There was a data type error: the data type of "
									  + index[i].getClass().getName() + " object [" + index[i] + "] does not correspond to value meta ["
									  + toStringMeta() + "]" );
						}
					}
				}
				xml.append( XMLHandler.closeTag( "index" ) );
				break;

			case STORAGE_TYPE_BINARY_STRING:
				// Save the storage meta data...
				//
				if ( storageMetadata != null ) {
					xml.append( XMLHandler.openTag( "storage-meta" ) );
					xml.append( storageMetadata.getMetaXML() );
					xml.append( XMLHandler.closeTag( "storage-meta" ) );
				}
				break;

			default:
				break;
		}
		xml.append( XMLHandler.addTagValue( "name", getName()?:"" ) );
		xml.append( XMLHandler.addTagValue( "length", getLength() ) );
		xml.append( XMLHandler.addTagValue( "precision", getPrecision() ) );
		xml.append( XMLHandler.addTagValue( "origin", getOrigin()?:"" ) );
		xml.append( XMLHandler.addTagValue( "comments", getComments()?:"" ) );
		xml.append( XMLHandler.addTagValue( "conversion_Mask", getConversionMask()?:"" ) );
		xml.append( XMLHandler.addTagValue( "decimal_symbol", getDecimalSymbol()?:"" ) );
		xml.append( XMLHandler.addTagValue( "grouping_symbol", getGroupingSymbol()?:"" ) );
		xml.append( XMLHandler.addTagValue( "currency_symbol", getCurrencySymbol()?:"" ) );
		xml.append( XMLHandler.addTagValue( "trim_type", getTrimTypeCode( trimType ) ) );
		xml.append( XMLHandler.addTagValue( "case_insensitive", isCaseInsensitive() ) );
		xml.append( XMLHandler.addTagValue( "sort_descending", isSortedDescending() ) );
		xml.append( XMLHandler.addTagValue( "output_padding", isOutputPaddingEnabled() ) );
		xml.append( XMLHandler.addTagValue( "date_format_lenient", isDateFormatLenient() ) );
		xml.append( XMLHandler.addTagValue( "date_format_locale", getDateFormatLocale()?.toString()?:"" ) );
//		xml.append( XMLHandler.addTagValue( "date_format_timezone", getDateFormatTimeZone() != null ? getDateFormatTimeZone().getID()
//		  : null ) );
		xml.append( XMLHandler.addTagValue( "lenient_string_to_number", isLenientStringToNumber() ) );

		xml.append( XMLHandler.closeTag( XML_META_TAG ) );

		return xml.toString();
	}
	String getDataXML( Object object ) throws IOException
	{
		//println "${this.class}.getDataXML!!!!!!!!!!!"
		super.getDataXML(object)
	}
	Object getValue( Node node ) throws KettleException
	{
		//println "${this.class}.getValue!!!!!!!!!!!"
		super.getValue(node)
	}
	int compare( Object data1, Object data2 ) throws KettleValueException
	{
		//println "${this.class}.compare!!!!!!!!!!!"
		super.compare(data1, data2)
	}

}
