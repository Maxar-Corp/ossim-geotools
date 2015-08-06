package tilestore.job

import grails.converters.JSON
import grails.transaction.Transactional
import joms.geotools.web.HttpStatus
import org.apache.commons.collections.map.CaseInsensitiveMap
import org.apache.commons.io.FilenameUtils
import org.ossim.common.FetchDataCommand
import org.ossim.common.Utility
import org.springframework.beans.factory.InitializingBean

@Transactional
class JobService
{
   def rabbitProducer
   def grailsApplication
   def columnNames = [
           'id', 'jobId', 'jobDir', 'type', 'name', 'description', 'username', 'status', 'statusMessage', 'percentComplete', 'submitDate', 'startDate', 'endDate'
   ]


   def createTableModel()
   {
      def clazz = Job.class
      def domain = grailsApplication.getDomainClass( clazz.name )

      def tempColumnNames = columnNames.clone()
      tempColumnNames.remove("jobDir")
      def columns = tempColumnNames?.collect {column->
         def property = ( column == 'id' ) ? domain?.identifier : domain?.getPersistentProperty( column )
         def sortable = !(property?.name in ["type", "description"])
         [field: property?.name, type: property?.type, title: property?.naturalName, sortable: sortable]
      }
      columns.remove("jobDir")
      def tableModel = [
              columns: [columns]
      ]
      //  println tableModel
      return tableModel
   }

   def cancel(GetJobCommand cmd)
   {
      def result = [status:HttpStatus.OK, message:""]

      if(cmd.jobId)
      {
         def jobId = cmd.jobId
         def jobCallback
         def jobStatus
         Job.withTransaction{
            def job     = Job.findByJobId(jobId)

            if(!job)
            {
               result.httpStatus = HttpStatus.NOT_FOUND
               result.message = "Job ${cmd.jobId} not found.  Unable to cancel job."

               return result
            }
            jobCallback = job?.jobCallback
            jobStatus   = job?.status
         }
         if(jobCallback)
         {
            if(jobStatus==JobStatus.RUNNING||jobStatus==JobStatus.CANCELED)
            {
               try{
            //      def messageBuilder = new RabbitMessageBuilder()
            //      messageBuilder.send(jobCallback, new AbortMessage(jobId:jobId).toJsonString())
               }
               catch(e)
               {
            //      println "CAUGHT EXCEPTION ---------------- ${e}"
            //      result.httpStatus = HttpStatus.BAD_REQUEST
            //      result.message=e.toString()
               }
            }
            else
            {
               result.httpStatus = HttpStatus.BAD_REQUEST
               result.message="Job ${jobId} not running.  Can only cancel 'running' or 'ready' jobs."
            }
         }
         else
         {
            result.httpStatus = HttpStatus.BAD_REQUEST
            result.message="Job ${jobId} currently has no callback to allow for canceling"
         }
      }
      else
      {
         result.httpStatus = HttpStatus.BAD_REQUEST
         result.message="Can't cancel. No job id given."
      }

      result
   }

   def listJobs(FetchDataCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : []
      ]
      def total = 0
      def rows = [:]
      try{
         Job.withTransaction {
            total = Job.createCriteria().count {
               if (cmd.filter)
               {
                  sqlRestriction cmd.filter
               }
            }

            def tempRows = Job.withCriteria {
               if (cmd.filter)
               {
                  sqlRestriction cmd.filter
               }
               //      projections {
               //        columnNames.each {
               //          property(it)
               //        }
               //      }
               maxResults(cmd.rows)
               order(cmd.sortBy, cmd.order)
               firstResult((cmd.page - 1) * cmd.rows)
            }
            rows = tempRows.collect { row ->
               columnNames.inject([:]) { a, b -> a[b] = row[b].toString(); a }
            }
         }
      }
      catch(e)
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = "Exception: ${e.toString}"
      }
      result.data = [total: total?:0, rows: rows?:[:]]


      result
   }
   def remove(RemoveJobCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : []
      ]
      def jobArchive
      def jobDir
      def row

      try{
         def session = grailsApplication.mainContext.sessionFactory.currentSession

         Job.withTransaction {
            if(cmd?.id != null) row = Job.findById(cmd.id);
            else if(cmd?.jobId) row = Job.findByJobId(cmd.jobId);

          //  println row
            if(row)
            {
               jobArchive = row.getArchive()
               jobDir = row.jobDir as File
               if (!jobArchive?.exists()) {
                  jobArchive = jobDir
               }
               row.delete(flush:true)
               result.success = true;
            }
         }
      }
      catch(e)
      {
         result.status = HttpStatus.BAD_REQUEST;
         result.message = "Exception: ${e.toString()}"
      }
      if(result.status == HttpStatus.OK)
      {
         try {
            if (jobArchive?.exists()) {
               if (jobArchive?.isDirectory()) {
                  jobArchive?.deleteDir()
               } else {
                  jobArchive?.delete()
               }
            }
            if(jobDir?.exists())
            {
               if (jobDir?.isDirectory()) {
                  jobDir?.deleteDir()
               } else {
                  jobDir?.delete()
               }
            }
         }
         catch(e)
         {
            result.status = HttpStatus.BAD_REQUEST;
            result.message = "Exception: ${e.toString()}"
         }
      }
      result
   }
   def updateJob(CreateJobCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : []
      ]

      Job.withTransaction{
         Job job = Job.findByJobId(cmd.jobId)

         if(job)
         {
            // println "WILL UPDATE ${jsonObj.jobId} WITH NEW STATUS === ${jsonObj.status}"

            def status = "${cmd.status?.toUpperCase()}"

            if(cmd.statusMessage != null) job.statusMessage = cmd.statusMessage

            if(cmd.status != null)
            {
               job.status  = JobStatus."${status}"
               switch(job.status)
               {
                  case JobStatus.READY:
                     job.submitDate = new Date()
                     break
                  case JobStatus.CANCELED:
                  case JobStatus.FINISHED:
                  case JobStatus.FAILED:
                     job.endDate = new Date()
                     break
                  case JobStatus.RUNNING:
                     job.startDate = new Date()
                     break
               }
            }
            if(cmd.percentComplete != null) job.percentComplete = cmd.percentComplete
            if(cmd.jobCallback != null)     job.jobCallback     = cmd.jobCallback

            if(!job.save(flush:true))
            {
               result.status = HttpStatus.BAD_REQUEST
               result.message = "Unable to update job ${cmd.jobId}."
            }
         }
         else
         {
            result.status = HttpStatus.BAD_REQUEST
            result.message = "Job with id ${cmd.jobId} not found.  Unable to update job."
         }
      }
      result
   }

   def create(CreateJobCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : [:]
      ]
      if(!cmd.submitDate)
      {
         cmd.submitDate = new Date()
      }
      try{
         Job.withTransaction {
            def job = new Job(cmd.toMap())

            if (!job.validate())
            {
               def fieldErrors = []
               job.errors.each { error ->
                  error.getFieldErrors().each { fieldError ->
                     //println field.field
                     fieldErrors << fieldError.field
                  }
               }

               result.status = HttpStatus.BAD_REQUEST
               result.message = "Bad field value for fields: ${fieldErrors}"
            }
            else
            {
               if (job.save())
               {
                  if(grailsApplication.config.rabbitmq.enabled)
                  {
                     def type = cmd.type?.toLowerCase()
                     if(type.contains("export"))
                     {
                        rabbitProducer.sendProductMessage(job.message)
                     }
                     else
                     {
                        rabbitProducer.sendIngestMessage(job.message)
                     }
                  }

                  result.data = job.toMap()
               }
               else
               {
                  result.status = HttpStatus.BAD_REQUEST
                  result.message = "Unable to save job"
               }
            }

         }
      }
      catch(e)
      {
         //println "ERROR!!!!!!!!!!!!!!!! ${e}"
         result.status = HttpStatus.BAD_REQUEST;
         result.message = "Exception: ${e.toString()}"
      }

      result

   }


   def show(GetJobCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : []
      ]
      def rows
      if(cmd.id != null)
      {
         def ids = [cmd.id]
         def tempRows = Job.withCriteria {
            sqlRestriction "(id in (${ids.collect{"${it}" }.join(',')}))"
         }
         rows = tempRows.collect { row ->
            columnNames.inject( [:] ) { a, b -> a[b] = row[b].toString(); a }
         }

      }
      else if(cmd.jobId != null)
      {
         def jobIds = [cmd.jobId]
         def tempRows = Job.withCriteria {
            sqlRestriction "(job_id in (${jobIds.collect{"'${it}'" }.join(',')}))"
         }
         rows = tempRows.collect { row ->
            columnNames.inject( [:] ) { a, b -> a[b] = row[b].toString(); a }
         }
      }
      else if(cmd.ids != null)
      {
         def ids = cmd.ids.split(",")

         def tempRows = Job.withCriteria {
            sqlRestriction "(id in (${ids.collect{"${it}" }.join(',')}))"
         }
         rows = tempRows.collect { row ->
            columnNames.inject( [:] ) { a, b -> a[b] = row[b].toString(); a }
         }

      }
      else if(cmd.jobIds != null)
      {
         def jobIds = cmd.jobIds.split(",")

         def tempRows = Job.withCriteria {
            sqlRestriction "(job_id in (${jobIds.collect{"'${it}'" }.join(',')}))"
         }
         rows = tempRows.collect { row ->
            columnNames.inject( [:] ) { a, b -> a[b] = row[b].toString(); a }
         }
      }
      result.data = [total: rows?.size()?:0, rows: rows?:[:]]


      result
   }

   def download(DowloadJobCommand cmd, def response)
   {
      //println params
      def httpStatus = HttpStatus.OK
      def errorMessage = ""
      def archive
      def jobStatus
      def jobFound
      def contentType = "text/plain"
      if(cmd.jobId)
      {
         def job
         Job.withTransaction {
            job = Job.findByJobId(cmd.jobId)
            archive = job?.getArchive()
            jobStatus = job?.status
            jobFound = job != null
         }
         if(jobFound) {
            if (jobStatus == JobStatus.FINISHED) {
               archive = job?.getArchive()
               // println "ARCHIVE ====== ${archive}"
               if (archive)
               {
                  def ext = FilenameUtils.getExtension(archive.toString()).toLowerCase()

                  // println "EXT === ${ext}"
                  switch (ext) {
                     case "zip":
                        contentType = "application/octet-stream"
                        break
                     case "tgz":
                        contentType = "application/x-compressed"
                        break
                  }
               }
               else
               {
                  File jobDir = job.jobDir as File
                  if(jobDir.exists())
                  {
                     archive = jobDir
                  }
                  else
                  {
                     httpStatus = HttpStatus.NOT_FOUND
                     errorMessage = "ERROR: Archive for Job ${cmd.jobId} is no longer present"
                  }
               }
            }
            else
            {
               httpStatus = HttpStatus.NOT_FOUND
               errorMessage = "ERROR: Can only download finished jobs.  The current status is ${job?.status.toString()}"
            }
         }
         else
         {
            httpStatus = HttpStatus.NOT_FOUND
            errorMessage = "ERROR: Job ${cmd.jobId} not found"
         }


         if(errorMessage)
         {
            response.status = httpStatus.value
            response.contentType = contentType
            response.sendError(response.status, errorMessage)
            //response.outputStream.write(errorMessage.bytes)
         }
         else
         {
            try
            {
               response.status = httpStatus.value

               if(archive.isFile())
               {
                  response.setHeader("Accept-Ranges", "bytes");
                  def tempFile = archive as File
                  response.contentType = contentType
                  response.setContentLength((int)tempFile.length());
                  response.setHeader( "Content-disposition", "attachment; filename=${archive.name}" )
                  Utility.writeFileToOutputStream(archive, response.outputStream)
               }
               else if(archive.isDirectory())
               {
                  response.contentType = "application/octet-stream"
                  response.setHeader( "Content-disposition", "attachment; filename=${archive.name}.zip" )
                  Utility.zipDirToStream(archive.toString(), response.outputStream, cmd.jobId)
               }
            }
            catch(e)
            {

               println "ERROR ============ ${e}"
               response.status = HttpStatus.BAD_REQUEST.value
               errorMessage = "ERROR: ${e}"
               response.contentType = "text/plain"
               response.outputStream.write(errorMessage.bytes)
            }
         }

      }
   }

}
