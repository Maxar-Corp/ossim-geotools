package tilestore.security

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.authentication.dao.NullSaltSource
import grails.plugin.springsecurity.ui.RegistrationCode

import grails.validation.Validateable

class RegisterController extends grails.plugin.springsecurity.ui.RegisterController
{
  def springSecurityService

  def register(RegisterCommand command)
  {

    if ( command.hasErrors() )
    {
      render view: 'index', model: [command: command]
      return
    }
    def conf = SpringSecurityUtils.securityConfig

    if ( conf.verifyByEmail )
    {
      String salt = saltSource instanceof NullSaltSource ? null : command.username
      def user = lookupUserClass().newInstance( email: command.email, username: command.username,
          accountLocked: true, enabled: true )

      RegistrationCode registrationCode = springSecurityUiService.register( user, command.password, salt )
      if ( registrationCode == null || registrationCode.hasErrors() )
      {
        // null means problem creating the user
        flash.error = message( code: 'spring.security.ui.register.miscError' )
        flash.chainedParams = params
        redirect action: 'index'
        return
      }

      String url = generateLink( 'verifyRegistration', [t: registrationCode.token] )

      def body = conf.ui.register.emailBody
      if ( body.contains( '$' ) )
      {
        body = evaluate( body, [user: user, url: url] )
      }
      mailService.sendMail {
        to command.email
        from conf.ui.register.emailFrom
        subject conf.ui.register.emailSubject
        html body.toString()
      }

      render view: 'index', model: [emailSent: true]
    }
    else
    {
      def User = lookupUserClass()

      def user = User.newInstance(
          email: command.email, username: command.username, password: command.password,
          accountExpired: false, accountLocked: conf.createAccountLocked, passwordExpired: false,
          enabled: true )

      User.withTransaction {
        if ( user.hasErrors() || !user.save( flush: true ) )
        {
          user.errors.allErrors.each { println messageSource.getMessage( it, null ) }
          flash.error = message( code: 'spring.security.ui.register.miscError' )
          flash.chainedParams = params
          redirect action: 'index'
        }

        def UserRole = lookupUserRoleClass()
        def Role = lookupRoleClass()

        for ( roleName in conf.ui.register.defaultRoleNames )
        {
          UserRole.create user, Role.findByAuthority( roleName )
        }
      }

      if ( !( user.accountLocked || user.passwordExpired || user.accountExpired ) && user.enabled )
      {
        springSecurityService.reauthenticate user.username
      }

      String defaultTargetUrl = conf.successHandler.defaultTargetUrl

      flash.message = message( code: 'spring.security.ui.register.complete' )
      redirect uri: conf.ui.register.postRegisterUrl ?: defaultTargetUrl
    }
  }
}

@Validateable
class RegisterCommand
{

  String username
  String email
  String password
  String password2

  def grailsApplication

  static constraints = {
    username blank: false, validator: { value, command ->
      if ( value )
      {
        def User = command.grailsApplication.getDomainClass(
            SpringSecurityUtils.securityConfig.userLookup.userDomainClassName ).clazz
        if ( User.findByUsername( value ) )
        {
          return 'registerCommand.username.unique'
        }
      }
    }
    email blank: false, email: true
    password blank: false, validator: RegisterController.passwordValidator
    password2 validator: RegisterController.password2Validator
  }
}