class TilestoreWfsGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.5 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Tilestore Wfs Plugin" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/tilestore-wfs"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
        csvResultFormat( tilestore.wfs.formats.CsvResultFormat ) {
          grailsApplication = ref( 'grailsApplication' )
        }

        gml2ResultFormat( tilestore.wfs.formats.Gml2ResultFormat ) {
          grailsApplication = ref( 'grailsApplication' )
          grailsLinkGenerator = ref( 'grailsLinkGenerator' )
        }

        shpResultFormat( tilestore.wfs.formats.ShpResultFormat )

        kmlResultFormat( tilestore.wfs.formats.KmlResultFormat ) {
          grailsApplication = ref( 'grailsApplication' )
          grailsLinkGenerator = ref( 'grailsLinkGenerator' )
        }

        kmlqueryResultFormat( tilestore.wfs.formats.KmlQueryResultFormat ) {
          grailsApplication = ref( 'grailsApplication' )
          grailsLinkGenerator = ref( 'grailsLinkGenerator' )
        }

        geojsonResultFormat( tilestore.wfs.formats.GeoJsonResultFormat )

        jsonResultFormat( tilestore.wfs.formats.GeoJsonResultFormat ) {
          name = "JSON"
        }

    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
