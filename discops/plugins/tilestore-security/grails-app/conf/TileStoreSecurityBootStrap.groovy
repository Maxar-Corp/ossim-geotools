import tilestore.security.SecRole
import tilestore.security.SecUser
import tilestore.security.SecUserSecRole

class TileStoreSecurityBootStrap
{
  def init = { servletContext ->

    if ( SecUser.count() == 0 )
    {
      def adminRole          = new SecRole( authority: 'ROLE_ADMIN' ).save( flush: true )
      def userRole           = new SecRole( authority: 'ROLE_USER' ).save( flush: true )
      def tilestoreAdminRole = new SecRole( authority: 'ROLE_LAYER_ADMIN' ).save( flush: true )

      def testUser = new SecUser( username: 'user', password: 'user' )
      testUser.save( flush: true )

      def adminUser = new SecUser( username: 'admin', password: 'admin' )
      adminUser.save( flush: true )


      SecUserSecRole.create testUser, userRole, true
      SecUserSecRole.create adminUser, userRole, true
      SecUserSecRole.create adminUser, adminRole, true
      SecUserSecRole.create adminUser, tilestoreAdminRole, true

      assert SecUser.count() == 2
      assert SecRole.count() == 3
      assert SecUserSecRole.count() == 4
    }
  }

  def destroy = {
  }
}
