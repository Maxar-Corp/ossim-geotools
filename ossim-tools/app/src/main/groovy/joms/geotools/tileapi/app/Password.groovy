package joms.geotools.tileapi.app

import joms.geotools.tileapi.TwoWayPasswordEncoder


/**
 * Created by gpotts on 3/31/15.
 */
class Password
{
   TwoWayPasswordEncoder encr = new TwoWayPasswordEncoder()

   static void main(String[] args)
   {
      Password passwordApp = new Password()

      passwordApp.run(args)
   }
   def getArgumentParser()
   {
      def cli = new CliBuilder(usage: 'TileCacheApp [options]')
      // Create the list of options.
      cli.with {
         h longOpt: 'help', argName:"help", 'Show usage information'
         _ longOpt: 'encr', args: 1, argName: 'encr', 'Encrypted password.  Make sure you use the prefix returned'
         _ longOpt: 'decr', args: 1, argName: 'decr', 'Decrypt password'
      }

      cli
   }
   void run(String[] args)
   {
      TwoWayPasswordEncoder encoder = new TwoWayPasswordEncoder()
      def options = this.argumentParser.parse(args)

      if(options.encr)
      {
         println encoder.encode(options.encr)
      }
      else if(options.decr)
      {
         println encoder.decode(options.decr)
      }
   }
}
