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
                     message:"",
                     directory:""
      ]

      DiskCache.withTransaction {
         def row

         // DiskCache.count()%
         try
         {
            Integer count = DiskCache.count()
            if(count)
            {
               row = DiskCache.withCriteria {
                  maxResults(1)
                  order("id", "asc")
                  firstResult(roundRobinIndex)
                  setReadOnly(true)
               }.get(0)

               ++roundRobinIndex
               roundRobinIndex = roundRobinIndex%count
            }
            else
            {
               roundRobinIndex = 0
            }

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
                    message:"",
                    data:[:]];
      try{
         if(!cmd.directory)
         {
            result.status = HttpStatus.NOT_FOUND
            result.message = "No directory given.  Please provide a directory to be used as a cache."
         }
         else
         {
            File directory = cmd.directory as File
            Boolean directoryExists=directory.exists()
            if(!directoryExists)
            {
               if(cmd.autoCreateDirectory)
               {
                  if(directory.mkdirs())
                  {
                     directoryExists = true
                  }
               }
            }

            if(directoryExists)
            {
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
            else
            {
               result.status = HttpStatus.NOT_FOUND
               result.message = "Directory '${cmd.directory}' does not exist."
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
                    message:"",
                    data:[:]];
      try{
         DiskCache.withTransaction {
            DiskCache row
            if(cmd?.id!=null) row = DiskCache.findById(cmd?.id);
            else if(cmd?.directory) row = DiskCache.findByDirectory(cmd.directory);
            // cmd.remove("id")
            if(row)
            {
               File directory = cmd.directory as File
               Boolean directoryExists=directory.exists()
               row.directory=cmd.directory?:row.directory
               row.maxSize=cmd.maxSize!=null?cmd.maxSize:row.maxSize
               row.currentSize=cmd.currentSize!=null?cmd.currentSize:row.currentSize
               row.expirePeriod=cmd.expirePeriod!=null?cmd.expirePeriod:row.expirePeriod
               if(!directoryExists)
               {
                  if(cmd.autoCreateDirectory)
                  {
                     if(directory.mkdirs())
                     {
                        directoryExists = true
                     }
                  }
               }
               if(directoryExists)
               {
                  if(!row.validate())
                  {
                     def fieldErrors = []
                     row.errors.each { error ->
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
                     row.save(flush:true)
                  }
               }
               else
               {
                  result.status = HttpStatus.NOT_FOUND
                  result.message = "Directory '${cmd.directory}' does not exist."
               }
            }
            else
            {
               result.status = HttpStatus.BAD_REQUEST
               result.message = "Unable to find directory = ${cmd?.directory} for updating";
            }
         }

      }
      catch(e)
      {
         //println e
         result.status = HttpStatus.BAD_REQUEST
         result.mesage = e.toString();
      }
      result;
   }
   def remove(RemoveCommand cmd){
      def result = [status:HttpStatus.OK,
                    message:""];

      try{
         DiskCache.withTransaction {
            def row

            if(cmd?.id) row = DiskCache.findById(cmd?.id?.toInteger());
            else if(cmd?.directory) row = DiskCache.findByDirectory(cmd.directory);

            if(row)
            {
               row.delete()
               result.status = HttpStatus.OK;
            }
         }
      }
      catch(e)
      {
         result.status = HttpStatus.NOT_FOUND
         result.message = e.toString()
      }
      roundRobinIndex = 0
      result;
   }
   def list(FetchDataCommand cmd){
      def result = [data:[:],
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
            result.data = [total: rows?.size()?:0, rows: rows?:[:]]

         }
      }
      catch(e)
      {
         e.printStackTrace()
         result.status = HttpStatus.NOT_FOUND
         result.message = e.toString()
      }

      return result
   }
}
