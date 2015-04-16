package ossim.diskcache

import grails.transaction.Transactional
import joms.geotools.web.HttpStatus
import org.ossim.common.FetchDataCommand

@Transactional
class DiskCacheService {
   def springSecurityService
   def grailsApplication
   def messageSource
   Integer roundRobinIndex = 0

   static columnNames = [
           'id','directory', 'directoryType', 'maxSize', 'currentSize','expirePeriod'
   ]
   def createTableModel()
   {
      def clazz = DiskCache.class
      def domain = grailsApplication.getDomainClass( clazz.name )

      def columns = columnNames?.collect {column->
         def property = ( column == 'id' ) ? domain?.identifier : domain?.getPersistentProperty( column )
         def sortable = !(property?.name in ["type"])
         [field: property?.name, type: property?.type, title: property?.naturalName, sortable: sortable]
      }

      def tableModel = [
              columns: [columns]
      ]
      // println tableModel
      return tableModel
   }
   def getNextLocation()
   {
      def result = [status:HttpStatus.OK,
                     message:""
      ]

      DiskCache.withTransaction {
         def row

         // DiskCache.count()%
         try
         {
            row = DiskCache.withCriteria {
               maxResults(1)
               order("id", "asc")
               firstResult(roundRobinIndex)
               setReadOnly(true)
            }.get(0)

            ++roundRobinIndex
            roundRobinIndex = roundRobinIndex%DiskCache.count()
         }
         catch (e)
         {
            result.status = HttpStatus.BAD_REQUEST
            result.message = "${e}"
            // println e
            row = null
         }
         if(row) result.directory = row?.directory
      }

      if(!result?.directory)
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = "Unable to find a directory to write to.\nPlease register a directory cache."
      }
      result
   }
   def getBestLocation(){


      /**
       * Fetch data that basically has the smallest size
       *
       */
      // will move this out later
      // keep here for now
      FetchDataCommand cmd = new FetchDataCommand(sortBy:"currentSize",
              order:"asc",
              filter:null,
              rows:1, page:1)

      def result = [:]

      DiskCache.withTransaction {
         def row

         try{
            row = DiskCache.withCriteria {
               maxResults( cmd.rows )
               order( cmd.sortBy, cmd.order )
               firstResult( 0 )
               setReadOnly(true)
            }.get(0)
         }
         catch(e)
         {
            result.status = HttpStatus.BAD_REQUEST
            result.message = "${e}"
            row = null
         }

         if(row) result.directory = row?.directory
      }
      if(!result?.directory)
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = "Unable to find a directory to write to.\nPlease register a directory cache."
      }

      result
   }
   def create(CreateCommand cmd){
      def result = [status:HttpStatus.OK,
                    message:""];
      try{
         DiskCache.withTransaction {
            def diskCache = new DiskCache(cmd.properties);
            if(!diskCache.save(flush:true))
            {
               diskCache.errors.allErrors.each {result.message = "${result.message?'\n':''} ${messageSource.getMessage(it, null)}"  }
               result.status = HttpStatus.BAD_REQUEST
            }
            else
            {
               result.message = "";
            }
         }
      }
      catch(e)
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = e.toString();
      }
      result;
   }
   def update(UpdateCommand cmd){
      def result = [status:HttpStatus.OK,
                    message:""];
      try{
         DiskCache.withTransaction {
            def row
            println cmd
            if(cmd?.id!=null) row = DiskCache.findById(cmd?.id);
            else if(cmd?.directory) row = DiskCache.findByDirectory(cmd.directory);
            // cmd.remove("id")
            if(row)
            {
               row.directory=cmd.directory!=null?cmd.directory:row.directory
               row.maxSize=cmd.maxSize!=null?cmd.maxSize:row.maxSize
               row.currentSize=cmd.currentSize!=null?cmd.currentSize:row.currentSize
               row.expirePeriod=cmd.expirePeriod!=null?cmd.expirePeriod:row.expirePeriod

               row.save(flush:true)
            }
            else
            {
               result.status = HttpStatus.BAD_REQUEST
               result.message = "Unable to find directory = ${params?.directory} for updating";
            }
         }

      }
      catch(e)
      {
         result.status = HttpStatus.BAD_REQUEST
         result.mesage = e.toString();
      }
      result;
   }
   def remove(def params){
      def result = [status:HttpStatus.OK,
                    message:""];

      try{
         DiskCache.withTransaction {
            def row

            if(params?.id) row = DiskCache.findById(params?.id?.toInteger());
            else if(params?.directory) row = DiskCache.findByDirectory(params.directory);

            if(row)
            {
               row.delete()
               result.status = HttpStatus.OK;
            }
         }
      }
      catch(e)
      {
         result.status = HttpStatus.BAD_REQUEST
         result.message = e.toString()
      }

      result;
   }
   def list(FetchDataCommand cmd){
      println cmd
      def result = [total: 0, rows: null,
                    status:HttpStatus.OK,
                    message:""];

      def total = 0
      def rows  = [:]
      try
      {
         DiskCache.withTransaction {
            total = DiskCache.createCriteria().count {
               if (cmd.filter)
               {
                  sqlRestriction cmd.filter
               }
            }

            def tempRows = DiskCache.withCriteria {
               if (cmd.filter)
               {
                  sqlRestriction cmd.filter
               }
               //      projections {
               //        columnNames.each {
               //          property(it)
               //        }
               //      }
               if(cmd.rows) maxResults(cmd.rows)
               order(cmd.sortBy?:"id", cmd.order?:"asc")
               firstResult((cmd.page - 1) * cmd.rows)
            }
            rows = tempRows.collect { row ->
               columnNames.inject([:]) { a, b -> a[b] = row[b].toString(); a }
            }

            result.total = rows.size()
            result.rows = rows
         }
      }
      catch(e)
      {
         e.printStackTrace()
         result.status = HttpStatus.BAD_REQUEST
         result.message = e.toString()
      }

      return result
   }
}
