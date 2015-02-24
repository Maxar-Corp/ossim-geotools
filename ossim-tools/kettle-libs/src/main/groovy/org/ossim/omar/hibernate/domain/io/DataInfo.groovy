package org.ossim.omar.hibernate.domain.io

class DataInfo
{
	def rasterDataSets
	def videoDataSets

	void initFromString(String dataInfoString, def hints)
	{
		if(!hints) hints = new XmlIoHints()
		// this will check all cases
		//
		rasterDataSets = []
		videoDataSets  = []
		def xml = new XmlSlurper().parseText(dataInfoString)
		if(xml.name() == "oms")
		{
			for (def rasterDataSetNode in xml?.dataSets?.RasterDataSet )
			{
				def rasterDataSet = RasterDataSetXmlReader.initRasterDataSet(rasterDataSetNode, null, hints)
				if(rasterDataSet)
				{
					rasterDataSets << rasterDataSet
				}
			}
			for (def videoDataSetNode in xml?.dataSets?.VideoDataSet )
			{
				def videoDataSet = VideoDataSetXmlReader.initVideoDataSet(videoDataSetNode, null, hints)
				if(videoDataSet)
				{
					videoDataSets << videoDataSet
				}
			}
		}
	}
	def getRasterEntries(){
		def rasterEntries = []

		rasterDataSets.each{dataSet->dataSet.rasterEntries.each{rasterEntries<<it}}

		rasterEntries
	}
	def getXml()
	{

		def result = new StringBuilder("<oms><dataSets>")

		rasterDataSets.each{rasterDataSet->result.append(rasterDataSet.xml)}
		videoDataSets.each{videoDataSet->result.append(videoDataSet.xml)}

		result.append("</dataSets></oms>")
		
		result.toString()
	}
}