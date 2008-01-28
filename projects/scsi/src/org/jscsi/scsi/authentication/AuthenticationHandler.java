//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information:
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

package org.jscsi.scsi.authentication;

import java.util.Map;

/**
 * A pluggable interface for handling iSCSI login phase authentication. Implementations perform
 * a key-value map authentication sequence specified in RFC 3720 or elsewhere. The authentication
 * back-end is implementation specific.
 * 
 * 
 */
public interface AuthenticationHandler
{

   /**
    * The authentication protocol implemented by this handler. Used by the iSCSI layer.
    */
   String protocol();

   /**
    * Performs one step in an authenticaton method specific exchange.
    * 
    * @return A map to be sent to the initiator, advancing to the next method-specific step;
    *    <code>null</code> if authentication is successful and the "SecurityNegotiation" phase
    *    should be ended.
    * @throws Exception If authentication failed. A Login reject with "Authentication Failure"
    *    status should be returned to the initiator.
    */
   Map<String, String> exchange(Map<String, String> properties) throws Exception;

   /**
    * Reset the authentication session. The next authentication exchange must start a new
    * authentication attempt.
    */
   void reset();

   /**
    * Indicates if the authentication is successful and the iSCSI transport can proceed to the
    * next stage when the initiator so requests. Returns false after {@link #reset()} until
    * proper exchanges have occured.
    */
   boolean proceed();

}
