package tilestore

import grails.converters.JSON
import grails.transaction.Transactional
import joms.geotools.tileapi.hibernate.domain.TileCacheLayerInfo
import joms.geotools.tileapi.job.TileCacheMessage
import joms.geotools.web.HttpStatus
import org.codehaus.groovy.grails.web.binding.bindingsource.JsonDataBindingSourceCreator
import tilestore.job.CreateJobCommand
import tilestore.job.JobStatus
import tilestore.security.SecUser

@Transactional
class ProductService {

   def jobService
   def springSecurityService
   def diskCacheService
   def layerManagerService

   def export(ProductExportCommand cmd)
   {
      def result = [status:HttpStatus.OK,
                    message:"",
                    data:[:]
      ]
      println cmd.layer
      if(!cmd.layer)
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = "A layer must be defined for the export."
      }

      if(result.status == HttpStatus.OK)
      {
         String jobId = UUID.randomUUID().toString()
         HashMap jobSpec = [
                 name:cmd.jobName?:"GeopackageExport",
                 type:"GeopackageExport",
                 status:JobStatus.READY,
                 jobId:jobId,
                 username:"anonymous",
                 jobDir:"",
                 submitDate:new Date()
         ]
         String jobDir = diskCacheService.nextLocation.directory

         File jobDirFile = new File(jobDir as File,jobId)
         jobSpec.jobDir = jobDirFile.toString()
         if(!jobDirFile.mkdirs())
         {
            result.status = HttpStatus.BAD_REQUEST
            result.message = "Unable to create output directory ${jobDirFile}"
         }
         else
         {


            SecUser user= springSecurityService.currentUser
            if(user)
            {
               jobSpec.username = user.username
            }

            TileCacheLayerInfo layerInfo = layerManagerService.daoTileCacheService.getLayerInfoByName(cmd.layer)
            if(!cmd.properties)
            {
               cmd.properties = [:]
            }

            if(!cmd.properties.filename) cmd.properties.filename = "image"
            if(layerInfo)
            {
               jobSpec.message = [type:"GeopackageExport",
                                  jobId:jobId,
                                  jobDir:jobSpec.jobDir,
                                  layer:cmd.layer,
                                  aoi:cmd.aoi,
                                  aoiEpsg:cmd.aoiEpsg?:layerInfo.epsgCode, // default to the layers EPSG code if none given
                                  properties:cmd.properties,
                                  outputEpsg:cmd.outputEpsg?:layerInfo.epsgCode,
                                  minLevel:(cmd.minLevel!=null)?cmd.minLevel:layerInfo.minLevel,
                                  maxLevel:(cmd.maxLevel!=null)?cmd.maxLevel:layerInfo.maxLevel,
                                  archive:[
                                          type:"zip",
                                          inputFile:jobDir,
                                          deleteInputAfterArchiving:true
                                  ]
                                 ]as JSON

               result = jobService.create(new CreateJobCommand(jobSpec))
            }
         }
      }
      result
   }
}
