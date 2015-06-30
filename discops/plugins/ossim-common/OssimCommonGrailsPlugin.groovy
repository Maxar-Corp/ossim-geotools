import java.util.zip.ZipInputStream

class OssimCommonGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.5 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Ossim Common Plugin" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/ossim-common"

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
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
        File.metaClass.unzip = { String dest ->
            //in metaclass added methods, 'delegate' is the object on which
            //the method is called. Here it's the file to unzip
            def result = new ZipInputStream(new FileInputStream(delegate))
            def destFile = new File(dest)
            if(dest)
            {
                if(!destFile.exists())
                {
                    destFile.mkdirs();
                }
            }
            result.withStream{
                def entry
                while(entry = result.nextEntry)
                {
                    if (!entry.isDirectory())
                    {
                        if(dest)
                        {
                            new File(dest + File.separator + entry.name).parentFile?.mkdirs()
                        }
                        else
                        {
                            new File(entry.name).parentFile?.mkdirs()
                        }
                        def output = new FileOutputStream(dest + File.separator
                                + entry.name)
                        output.withStream{
                            int len = 0;
                            byte[] buffer = new byte[4096]
                            while ((len = result.read(buffer)) > 0){
                                output.write(buffer, 0, len);
                            }
                        }
                    }
                    else
                    {
                        if(dest)
                        {
                            new File(dest + File.separator + entry.name).mkdir()
                        }
                        else
                        {
                            new File(entry.name).mkdir()
                        }
                    }
                }
            }
        }
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
