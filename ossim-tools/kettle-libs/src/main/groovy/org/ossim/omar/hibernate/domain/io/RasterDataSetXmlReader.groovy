package org.ossim.omar.hibernate.domain.io

import org.ossim.omar.hibernate.domain.RasterDataSet


class RasterDataSetXmlReader
{
  static def initRasterDataSet(def node, def dataSet, XmlIoHints hints)
  {
    def allocatedDataset = false
    def rasterDataSet  = dataSet
    // if a dataSet is passed in let's make sure that the children are
    // loaded and ready for testing.  Do a simple for each to force a load
    dataSet?.rasterEntries?.each{rasterEntry->rasterEntry.rasterEntryFiles.each{}}
    dataSet?.rasterFiles?.each{}
    if(!rasterDataSet)
    {
      rasterDataSet = new RasterDataSet();
      allocatedDataset = true
    } 

    def mainFile
    for ( def rasterFileNode in node.fileObjects.RasterFile )
    {
      def rasterFile = RasterFileXmlReader.initRasterFile( rasterFileNode, hints);
      if(rasterFile.type.toLowerCase() == "main") mainFile = rasterFile.name
      def foundFile = rasterDataSet.rasterFiles.find{file->file.name==rasterFile.name}
      if(!foundFile)
      {
        rasterFile.rasterDataSet = rasterDataSet
        rasterDataSet.rasterFiles << rasterFile
      }
      else
      {
        foundFile.name   = rasterFile.name
        foundFile.type   = rasterFile.type
        foundFile.format = rasterFile.format
      }
    }
    for (def rasterEntryNode in node.rasterEntries.RasterEntry )
    {
      def searchKey   = rasterEntryNode.entryId
      def foundEntry  = rasterDataSet.rasterEntries.find{it.entryId == searchKey}
      def rasterEntry = RasterEntryXmlReader.initRasterEntry(rasterEntryNode,foundEntry, hints)

      if(mainFile)
      {
        rasterEntry.filename = mainFile
      }
      if(rasterEntry&&rasterEntry?.groundGeom)
      {			
        if(!foundEntry)
        {
          println rasterEntry
          rasterEntry.rasterDataSet   = rasterDataSet
          rasterDataSet.rasterEntries << rasterEntry
        }
      }
    }
    if(allocatedDataset)
    {
      if(!rasterDataSet.rasterEntries)
      {
        rasterDataSet = null
      }

    }
    rasterDataSet
  }
}
