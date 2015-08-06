package tilestore.security

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.authentication.dao.NullSaltSource
import grails.plugin.springsecurity.ui.RegistrationCode

import grails.validation.Validateable

class RegisterController extends grails.plugin.springsecurity.ui.RegisterController
{
  def springSecurityService
  def mailService
  def ldapUtilService

  def resetPassword = { ResetPasswordCommand command ->

    String token = params.t

    def registrationCode = token ? RegistrationCode.findByToken( token ) : null
    if ( !registrationCode )
    {
      flash.error = message( code: 'spring.security.ui.resetPassword.badCode' )
      redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
      return
    }

    if ( !request.post )
    {
      return [token: token, command: new ResetPasswordCommand()]
    }

    command.username = registrationCode.username
    command.validate()

    if ( command.hasErrors() )
    {
      return [token: token, command: command]
    }

    String salt = saltSource instanceof NullSaltSource ? null : registrationCode.username
    RegistrationCode.withTransaction { status ->
      def user = lookupUserClass().findByUsername( registrationCode.username )
      def newPassword = springSecurityService.encodePassword( command.password, salt )

      if ( user?.password == "Authenticated by LDAP" )
      {
        ldapUtilService.changePassword( [username: user.username, password: newPassword] )
      }
      else
      {
        user.password = newPassword
        user.save()
      }
      registrationCode.delete()
    }

    springSecurityService.reauthenticate registrationCode.username

    flash.message = message( code: 'spring.security.ui.resetPassword.success' )

    def conf = SpringSecurityUtils.securityConfig
    String postResetUrl = conf.ui.register.postResetUrl ?: conf.successHandler.defaultTargetUrl
    redirect uri: postResetUrl
  }


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

  static final myPasswordValidator = { String password, command ->

    if(!password)
    {
      return 'registerCommand.password.blank'
    }
    if ( command.username && command.username.equals( password ) )
    {
      return 'command.password.error.username'
    }

    if(password?.length() <8)
    {
      return 'registerCommand.password.minSize.notmet'
    }
    else if(password?.length() > 64)
    {
      return 'registerCommand.password.maxSize.exceeded'
    }
    if ( password && password.length() >= 8 && password.length() <= 64 &&
            ( !password.matches( '^.*\\p{Alpha}.*$' ) ||
                    !password.matches( '^.*\\p{Digit}.*$' ) ||
                    !password.matches( '^.*[!@#$%^&].*$' ) ) )
    {
      return 'command.password.error.strength'
    }
  }

  static final myPassword2Validator = { value, command ->
    if(!command?.password2)
    {
      return 'registerCommand.password.blank'
    }
    if ( command.password != command.password2 )
    {
      return 'command.password2.error.mismatch'
    }
  }

  static final myEmailValidator = { value, command ->
    if ( command.email != command.email2 )
    {
      return 'registerCommand.email.error.mismatch'
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
      println "VALIDATING USERNAME"
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
    password blank: false, validator: RegisterController.myPasswordValidator
    password2 validator: RegisterController.myPassword2Validator
  }
}

@Validateable
class ResetPasswordCommand
{
  String username
  String password
  String password2

  static constraints = {
    password blank: false, minSize: 8, maxSize: 64, validator: RegisterController.myPasswordValidator
    password2 validator: RegisterController.myPassword2Validator
  }
}
