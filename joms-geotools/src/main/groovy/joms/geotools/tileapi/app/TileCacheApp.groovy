package joms.geotools.tileapi.app

/**
 * Created by gpotts on 1/27/15.
 */
class TileCacheApp
{
  static void main(String[] args)
  {
    def cli = new CliBuilder(usage: 'showdate.groovy -[chflms] [date] [prefix]')
    // Create the list of options.
    cli.with {
      h longOpt: 'help', 'Show usage information'
      _ longOpt: 'spring-config', args: 1, argName: 'springConfig', 'Pass in a spring configuration for the spatial and accumulo definitions'
      f longOpt: 'format-full',   'Use DateFormat#FULL format'
      l longOpt: 'format-long',   'Use DateFormat#LONG format'
      m longOpt: 'format-medium', 'Use DateFormat#MEDIUM format (default)'
      s longOpt: 'format-short',  'Use DateFormat#SHORT format'
    }

    def options = cli.parse(args)
    if (!options) {
      return
    }
    // Show usage text when -h or --help option is used.
    if (options.h) {
      cli.usage()
      // Will output:
      // usage: showdate.groovy -[chflms] [date] [prefix]
      //  -c,--format-custom <format>   Format date with custom format defined by "format"
      //  -f,--format-full              Use DateFormat#FULL format
      //  -h,--help                     Show usage information
      //  -l,--format-long              Use DateFormat#LONG format
      //  -m,--format-medium            Use DateFormat#MEDIUM format
      //  -s,--format-short             Use DateFormat#SHORT format
      return
    }
  }
}
