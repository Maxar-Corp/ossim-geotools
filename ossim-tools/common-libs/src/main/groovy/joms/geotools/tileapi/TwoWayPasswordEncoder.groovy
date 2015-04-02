package joms.geotools.tileapi

/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

/**
 * This code was added from pentaho data integration for encode decode of passwords
 *
 */
class TwoWayPasswordEncoder
{
   private static final int RADIX = 16;
   private static final String SEED = "0933910847463829827159347601486730416058";


   TwoWayPasswordEncoder() {
   }


   void init() throws Exception {
      // Nothing to do here.
   }


   String encode( String rawPassword ) {
      return encode( rawPassword, true );
   }


   String encode( String rawPassword, boolean includePrefix ) {
      if ( includePrefix ) {
         return encryptPasswordIfNotUsingVariables( rawPassword );
      } else {
         return encryptPassword( rawPassword );
      }
   }


   String decode( String encodedPassword ) {

      if ( encodedPassword != null && encodedPassword.startsWith( PASSWORD_ENCRYPTED_PREFIX ) ) {
         encodedPassword = encodedPassword.substring( PASSWORD_ENCRYPTED_PREFIX.length() );
      }

      decryptPassword( encodedPassword );
   }

   String decode( String encodedPassword, boolean optionallyEncrypted ) {

      if ( encodedPassword == null ) {
         return null;
      }

      if ( optionallyEncrypted ) {

         if ( encodedPassword.startsWith( PASSWORD_ENCRYPTED_PREFIX ) ) {
            encodedPassword = encodedPassword.substring( PASSWORD_ENCRYPTED_PREFIX.length() );
            return decryptPassword( encodedPassword );
         } else {
            return encodedPassword;
         }
      } else {
         return decryptPassword( encodedPassword );
      }
   }

   static final String encryptPassword( String password ) {
      if ( password == null ) {
         return "";
      }
      if ( password.length() == 0 ) {
         return "";
      }

      BigInteger bi_passwd = new BigInteger( password.getBytes() );

      BigInteger bi_r0 = new BigInteger( SEED );
      BigInteger bi_r1 = bi_r0.xor( bi_passwd );

      bi_r1.toString( RADIX );
   }

   static final String decryptPassword( String encrypted ) {
      if ( encrypted == null ) {
         return "";
      }
      if ( encrypted.length() == 0 ) {
         return "";
      }

      BigInteger bi_confuse = new BigInteger( SEED );

      try {
         BigInteger bi_r1 = new BigInteger( encrypted, RADIX );
         BigInteger bi_r0 = bi_r1.xor( bi_confuse );

         return new String( bi_r0.toByteArray() );
      } catch ( Exception e ) {
         return "";
      }
   }

   static final String PASSWORD_ENCRYPTED_PREFIX = "Encrypted ";


   String[] getPrefixes() {
      [PASSWORD_ENCRYPTED_PREFIX] as String [];
   }

   static final String encryptPasswordIfNotUsingVariables( String password ) {
      String encrPassword = "";
      encrPassword = PASSWORD_ENCRYPTED_PREFIX + TwoWayPasswordEncoder.encryptPassword( password );

      encrPassword;
   }

   static final String decryptPasswordOptionallyEncrypted( String password ) {
      if ( password?.startsWith( PASSWORD_ENCRYPTED_PREFIX ) ) {
         return TwoWayPasswordEncoder.decryptPassword( password.substring( PASSWORD_ENCRYPTED_PREFIX.length() ) );
      }
      password;
   }

}
