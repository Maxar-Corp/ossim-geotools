package org.ossim.omar.hibernate.domain.io

import org.ossim.omar.hibernate.domain.RasterEntryFile
import java.io.File

class RasterEntryFileXmlReader
{
	static def initRasterEntryFile(def rasterEntryFileNode, XmlIoHints hints)
    {
        def rasterEntryFile = new RasterEntryFile()

        rasterEntryFile.name = new File( rasterEntryFileNode?.name?.text() ).absolutePath
        rasterEntryFile.type = rasterEntryFileNode?.@type?.text()
        
        return rasterEntryFile
    }

}