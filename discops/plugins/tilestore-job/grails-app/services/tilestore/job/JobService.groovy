package tilestore.job

import grails.transaction.Transactional
import joms.geotools.web.HttpStatus
import org.ossim.common.FetchDataCommand

@Transactional
class JobService {
   def rabbitProducer

   def columnNames = [
           'id','jobId', 'jobDir', 'type', 'name', 'username', 'status', 'statusMessage', 'percentComplete', 'submitDate', 'startDate', 'endDate'
   ]

   def listJobs(FetchDataCommand cmd)
   {
      def total = 0
      def rows  = [:]
      Job.withTransaction{
         total = Job.createCriteria().count {
            if ( cmd.filter )
            {
               sqlRestriction cmd.filter
            }
         }

         def tempRows = Job.withCriteria {
            if ( cmd.filter )
            {
               sqlRestriction cmd.filter
            }
            //      projections {
            //        columnNames.each {
            //          property(it)
            //        }
            //      }
            maxResults( cmd.rows )
            order( cmd.sortBy, cmd.order )
            firstResult( ( cmd.page - 1 ) * cmd.rows )
         }
         rows = tempRows.collect { row ->
            columnNames.inject( [:] ) { a, b -> a[b] = row[b].toString(); a }
         }
      }

      return [total: total, rows: rows]
   }
   def ingest(IngestCommand cmd)
   {
      def result = [status:HttpStatus.OK,
                    message:"",
                    data:[]
         ]

      // create rabbit message and post
      // we can make this configurable and allow for quartz as well
      // so if rabbit is not supported then add Quartz job.  Will think about it
      //

      result
   }
}
