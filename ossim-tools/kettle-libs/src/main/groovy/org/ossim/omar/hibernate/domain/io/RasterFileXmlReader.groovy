package org.ossim.omar.hibernate.domain.io

import java.io.File;
import org.ossim.omar.hibernate.domain.RasterFile

class RasterFileXmlReader
{
	static def initRasterFile(def node, XmlIoHints hints)
	{
		def rasterFile;

		rasterFile = new RasterFile();

		rasterFile.name   = new File( node?.name?.text() ).absolutePath
		rasterFile.format = node?.@format?.text()
		rasterFile.type   = node?.@type?.text()

		rasterFile
	}
}