package org.ossim.core


public class Folder {

  static long getFileSize(File folder, boolean recurse) throws IOException
  {
    def history = [] as Set
    recurseGetFileSize(folder, history, recurse)
  }
  private static long recurseGetFileSize(File folder , def history, boolean recurse) throws IOException  
  {
    long foldersize = folder?.length();

    if(folder.isDirectory())
    {
      def filelist = folder?.listFiles();
      for (file in filelist) 
      {
        foldersize += file.length();
        if (!history.contains(file.canonicalPath)&&file.isDirectory()) 
        {
          history.add( file.canonicalPath ) ;
          if(recurse)
          {
            foldersize += recurseGetFileSize(file,history, recurse);            
          }
        } 
      }
    }
    return foldersize;
  }
}