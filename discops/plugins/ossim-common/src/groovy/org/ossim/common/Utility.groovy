package org.ossim.common

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by gpotts on 6/18/15.
 */
class Utility
{
   static Boolean zipDir( String dir2zip, String outputFile, String prefix="")
   {
      Boolean result=true

      def zos = new ZipOutputStream( new FileOutputStream( outputFile ) )
      result = Utility.zipDir( dir2zip, zos, prefix);
      zos.close();

      result
   }

   static Boolean zipDirToStream(String dir2zip, OutputStream os, String prefix="")
   {
      Boolean result=true

      def zos = new ZipOutputStream( os )
      result = Utility.zipDir( dir2zip, zos, prefix );
      zos.close();

      result
   }
   static Boolean zipDir( String dir2zip, ZipOutputStream zos, String prefix = "" )
   {
      Boolean result = true
      try
      {
         //create a new File object based on the directory we
         def zipDirTemp = new File( dir2zip);
         //get a listing of the directory content
         String[] dirList = zipDirTemp.list();

         //loop through dirList, and zip the files
         for ( int i = 0; i < dirList.length; i++ )
         {
            File f = new File( zipDirTemp, dirList[i] );
            if ( f.isDirectory() )
            {
               //if the File object is a directory, call this
               //function again to add its content recursively
               String filePath = f.getPath();
               zipDir( filePath, zos, dirList[i] );
               //loop again
               continue;
            }

            ZipEntry anEntry = new ZipEntry( prefix+"/"+f.name )//f.getPath() );
            zos.putNextEntry( anEntry );

            Utility.writeFileToOutputStream(f,zos)
         }
      }
      catch ( Exception e )
      {
         //println "ERROR: ${e}"
         //e.printStackTrace()
         result = false;
      }

      result
   }
   static void writeStreamToOutputStream( def inputStream, def output, def blockSize = 4096 )
   {
      byte[] buffer = new byte[blockSize]; // To hold file contents
      int bytes_read; // How many bytes in buffer

      while ( ( bytes_read = inputStream.read( buffer ) ) != -1 )
      {
         // Read until EOF
         output.write( buffer, 0, bytes_read ); // write
      }

   }

   static void writeFileToOutputStream( def inputFile, def output, def blockSize = 4096 )
   {
      def from = new FileInputStream( inputFile ); // Create input stream
      writeStreamToOutputStream( from, output, blockSize )
      from.close()
      from = null
   }
}
