package org.ossim.omar.hibernate.domain.io

import org.ossim.omar.hibernate.domain.VideoFile

class VideoFileXmlReader
{
	static VideoFile initVideoFile(def videoFileNode, XmlIoHints hints)
  	{
  		def videoFile = new VideoFile()

		videoFile.name = new File( videoFileNode?.name?.text() ).absolutePath
		videoFile.format = videoFileNode?.@format?.text()
		videoFile.type = videoFileNode?.@type?.text()
		
		videoFile
	}	
}