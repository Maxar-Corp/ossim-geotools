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

   def createJob(CreateJobCommand cmd)
   {
      def result = [status : HttpStatus.OK,
                    message: "",
                    data   : []
      ]
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
               } else
               {
                  result.status = HttpStatus.BAD_REQUEST
                  result.message = "Unable to save job"
               }
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
              status: null,
              statusMessage: "",
              message: (ingestCommand as JSON).toString(),
              jobCallback: null,
              percentComplete: 0.0,
      )

      result = createJob(jobCommand)
      // create rabbit message and post
      // we can make this configurable and allow for quartz as well
      // so if rabbit is not supported then add Quartz job.  Will think about it
      //

      result
   }
}
