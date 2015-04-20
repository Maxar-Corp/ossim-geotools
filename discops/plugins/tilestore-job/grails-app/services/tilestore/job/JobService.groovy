package tilestore.job

import grails.converters.JSON
import grails.transaction.Transactional
import joms.geotools.web.HttpStatus
import org.ossim.common.FetchDataCommand

@Transactional
class JobService
{
   def rabbitProducer
   def grailsApplication
   def columnNames = [
           'id', 'jobId', 'jobDir', 'type', 'name', 'username', 'status', 'statusMessage', 'percentComplete', 'submitDate', 'startDate', 'endDate'
   ]

   def listJobs(FetchDataCommand cmd)
   {
      def total = 0
      def rows = [:]
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

      return [total: total, rows: rows]
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
         Job.withTransaction {

            if(cmd?.id != null) row = Job.findById(cmd.id?.toInteger());
            else if(cmd?.jobId) row = Job.findByJobId(cmd.jobId);
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
         result.message = e.toString()
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
            //println "ERROR!!!!!!!!!!!!!!!! ${e}"
            result.status = HttpStatus.BAD_REQUEST;
            result.message = e.toString()
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
                    data   : []
      ]
      if(!cmd.submitDate)
      {
         cmd.submitDate = new Date()
      }
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
               if (grailsApplication.config.rabbitmq.enabled)
               {
                  rabbitProducer.sendIngestMessage(job.message)
               }
            }
            else
            {
               result.status = HttpStatus.BAD_REQUEST
               result.message = "Unable to save job"
            }
         }

         result
      }
   }

   def ingest(IngestCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : []
      ]
      String jobId = UUID.randomUUID().toString()
      HashMap ingestCommand = cmd.toMap();
      ingestCommand.jobName = ingestCommand.jobName?:"Ingest"
      ingestCommand.jobId = jobId
      ingestCommand.type = "TileServerIngestMessage"
      CreateJobCommand jobCommand = new CreateJobCommand(
              jobId: jobId,
              type: "TileServerIngestMessage",
              jobDir: "",
              name: cmd.jobName,
              username: "anonymous",
              status: JobStatus.READY.toString(),
              statusMessage: "",
              message: (ingestCommand as JSON).toString(),
              jobCallback: null,
              percentComplete: 0.0,
      )

      result = create(jobCommand)
      // create rabbit message and post
      // we can make this configurable and allow for quartz as well
      // so if rabbit is not supported then add Quartz job.  Will think about it
      //

      result
   }

   def getJob(GetJobCommand cmd)
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
            columnNames.inject( [:] ) { a, b -> a[b] = row[b]; a }
         }

      }
      else if(cmd.jobId != null)
      {
         def jobIds = [cmd.jobId]
         def tempRows = Job.withCriteria {
            sqlRestriction "(job_id in (${jobIds.collect{"'${it}'" }.join(',')}))"
         }
         rows = tempRows.collect { row ->
            columnNames.inject( [:] ) { a, b -> a[b] = row[b]; a }
         }
      }
      else if(cmd.ids != null)
      {
         def ids = cmd.ids.split(",")

         def tempRows = Job.withCriteria {
            sqlRestriction "(id in (${ids.collect{"${it}" }.join(',')}))"
         }
         rows = tempRows.collect { row ->
            columnNames.inject( [:] ) { a, b -> a[b] = row[b]; a }
         }

      }
      else if(cmd.jobIds != null)
      {
         def jobIds = cmd.jobIds.split(",")

         def tempRows = Job.withCriteria {
            sqlRestriction "(job_id in (${jobIds.collect{"'${it}'" }.join(',')}))"
         }
         rows = tempRows.collect { row ->
            columnNames.inject( [:] ) { a, b -> a[b] = row[b]; a }
         }
      }

      result.data = [total: rows?.size(), rows: rows]


      result
   }
}
