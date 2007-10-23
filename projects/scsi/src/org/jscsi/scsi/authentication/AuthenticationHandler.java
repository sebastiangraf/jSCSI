
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
   Map<String,String> exchange( Map<String,String> properties ) throws Exception;
   
   
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


