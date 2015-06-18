package ossim.diskcache

import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import joms.geotools.web.HttpStatus

//import grails.plugins.springsecurity.Secured
import org.apache.commons.collections.map.CaseInsensitiveMap
import org.ossim.common.FetchDataCommand

class DiskCacheController
{

  def diskCacheService
  static allowedMethods = [create:['POST'],
                           remove:['POST'],
                           update:['POST'],
                           list:['GET','POST'],
                           index:['GET', 'POST']
  ]

  @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
  def index()
  {
    [
            initParams:[
                    tableModel: diskCacheService.createTableModel()
            ] as JSON
    ]
  }

  @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
  def getNextLocation()
  {
    def data = diskCacheService.getNextLocation()
    response.status = data.status.value
    data.remove( "status" )
    response.withFormat {
      json {
        render contentType: 'application/json', text: data as JSON
      }
      xml {
        render contentType: 'application/xml', text: data as XML
      }
    }
  }

  @Secured( ['ROLE_USER', 'ROLE_ADMIN'] )
  def getBestLocation()
  {
    def data = diskCacheService.getBestLocation()
    response.status = data.status.value
    data.remove( "status" )
    response.withFormat {
      json {
        render contentType: 'application/json', text: data as JSON
      }
      xml {
        render contentType: 'application/xml', text: data as XML
      }
    }
  }
   @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
  def list(FetchDataCommand cmd)
  {
    def result = diskCacheService.list( cmd )

    response.status = result.status.value
    response.withFormat {
      json {
        if(result.status != HttpStatus.OK)
        {
          render contentType: 'application/json', text: [message:result.message] as JSON
        }
        else
        {
          render contentType: 'application/json', text: result.data as JSON
        }
      }
      xml {
        if(result.status != HttpStatus.OK)
        {
          render contentType: 'application/json', text: [message:result.message] as XML
        }
        else
        {
          render contentType: 'application/xml', text: result.data as XML
        }
      }
    }

  }

  @Secured( ['ROLE_ADMIN', "ROLE_USER"] )
  def create(CreateCommand cmd)
  {
    def result = diskCacheService.create( cmd )
    response.status = result.status.value
    response.withFormat {
      json {
        if(result.status != HttpStatus.OK)
        {
          render contentType: 'application/json', text: [message:result.message] as JSON
        }
        else
        {
          render contentType: 'application/json', text: result.data as JSON
        }
      }
      xml {
        if(result.status != HttpStatus.OK)
        {
          render contentType: 'application/json', text: [message:result.message] as XML
        }
        else
        {
          render contentType: 'application/xml', text: result.data as XML
        }
      }
    }
  }

  @Secured(['ROLE_ADMIN'])
  def update(UpdateCommand cmd)
  {
    def result = diskCacheService.update( cmd )//new CaseInsensitiveMap(params));
    response.status = result.status.value
    response.withFormat {
      json {
        if(result.status != HttpStatus.OK)
        {
          render contentType: 'application/json', text: [message:result.message] as JSON
        }
        else
        {
          render contentType: 'application/json', text: result.data as JSON
        }
      }
      xml {
        if(result.status != HttpStatus.OK)
        {
          render contentType: 'application/json', text: [message:result.message] as XML
        }
        else
        {
          render contentType: 'application/xml', text: result.data as XML
        }
      }
    }
  }

  @Secured( ['ROLE_ADMIN'] )
  def remove(RemoveCommand cmd)
  {
    def data = diskCacheService.remove( cmd );
    data.remove( "status" )
    data.remove( "message" )
    response.withFormat {
      json {
        render contentType: 'application/json', text: data as JSON
      }
      xml {
        render contentType: 'application/xml', text: data as XML
      }
    }
  }
}
