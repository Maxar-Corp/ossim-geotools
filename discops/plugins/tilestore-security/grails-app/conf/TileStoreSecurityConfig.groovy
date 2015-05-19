// configuration for plugin testing - will not be included in the plugin zip

log4j = {
  // Example of changing the log pattern for the default console
  // appender:
  //
  //appenders {
  //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
  //}

  error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
      'org.codehaus.groovy.grails.web.pages', //  GSP
      'org.codehaus.groovy.grails.web.sitemesh', //  layouts
      'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
      'org.codehaus.groovy.grails.web.mapping', // URL mapping
      'org.codehaus.groovy.grails.commons', // core / classloading
      'org.codehaus.groovy.grails.plugins', // plugins
      'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
      'org.springframework',
      'org.hibernate',
      'net.sf.ehcache.hibernate'
}

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'tilestore.security.SecUser'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'tilestore.security.SecUserSecRole'
grails.plugin.springsecurity.authority.className = 'tilestore.security.SecRole'
grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
    '/': ['ROLE_USER', 'ROLE_ADMIN'],
    '/index': ['ROLE_USER', 'ROLE_ADMIN'],
    '/index.gsp': ['ROLE_USER', 'ROLE_ADMIN'],

    '/assets/**': ['permitAll'],
    '/**/js/**': ['permitAll'],
    '/**/css/**': ['permitAll'],
    '/**/*.js': ['permitAll'],
    '/**/*.css': ['permitAll'],
    '/**/images/**': ['permitAll'],
    '/**/favicon.ico': ['permitAll'],

    '/register/**': ['permitAll'],
    '/securityInfo/**': ['ROLE_ADMIN'],
    '/user/**': ['ROLE_ADMIN'],
    '/role/**': ['ROLE_ADMIN'],
    '/registrationCode/**': ['ROLE_ADMIN']
]

grails.plugin.springsecurity.verifyByEmail = false
grails.plugin.springsecurity.createAccountLocked = false

