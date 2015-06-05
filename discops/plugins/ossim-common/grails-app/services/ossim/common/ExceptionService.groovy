package ossim.common

import groovy.xml.StreamingMarkupBuilder


class ExceptionService
{

  def createMessage(String message)
  {
    def results = new StreamingMarkupBuilder().bind() {
      mkp.xmlDeclaration()
      mkp.declareNamespace( xsi: "http://www.w3.org/2001/XMLSchema-instance" )
      ServiceExceptionReport( version: "1.2.0", xmlns: "http://www.opengis.net/ogc",
          'xsi:schemaLocation': "http://www.opengis.net/ogc http://schemas.opengis.net/wfs/1.0.0/OGC-exception.xsd" ) {
        ServiceException( code: "GeneralException", message )
      }
    }.toString()

    results
  }
}
