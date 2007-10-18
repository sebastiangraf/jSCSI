
package org.jscsi.scsi.protocol.sense.exceptions;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.SenseKey;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

/**
 * Base exception for exceptions based on sense data. 
 */
public abstract class SenseException extends Exception
{
   
   public static enum ResponseCode
   {
      CURRENT_FIXED((byte)0x70),
      
      DEFERRED_FIXED((byte)0x71),
      
      CURRENT_DESCRIPTOR((byte)0x72),
      
      DEFERRED_DESCRIPTOR((byte)0x73);
      
      private final byte code;
      
      private static Map<Byte, ResponseCode> mapping = new HashMap<Byte, ResponseCode>();
      
      private ResponseCode(final byte code)
      {
         ResponseCode.mapping.put(code, this);
         this.code = code;
      }
      
      public final byte code()
      {
         return code;
      }
      
      public static final ResponseCode valueOf( byte code ) throws IOException
      {
         // FIXME: Through an error if code not found in map
         return ResponseCode.mapping.get(code);
      }
      
      public static final ResponseCode valueOf( boolean current, boolean descriptor )
      {
         if ( current )
         {
            if ( descriptor )
            {
               return CURRENT_DESCRIPTOR;
            }
            else
            {
               return CURRENT_FIXED;
            }
         }
         else
         {
            if ( descriptor )
            {
               return DEFERRED_DESCRIPTOR;
            }
            else
            {
               return DEFERRED_FIXED;
            }
         }
      }
   }

   
   private static Map<KCQ,Class<? extends SenseException>> _exceptions =
      new HashMap<KCQ,Class<? extends SenseException>>();
   
   protected static void register( KCQ kcq, Class<? extends SenseException> exception )
   {
      _exceptions.put(kcq, exception);
   }
   
   private boolean current; // whether a current error or deferred error
   private KCQ kcq;
   
   public SenseException( KCQ kcq, boolean current )
   {
      this.kcq = kcq;
      this.current = current;
   }
   
   public KCQ getKCQ()
   {
      return this.kcq;
   }
   
   public SenseKey getSenseKey()
   {
      return this.kcq.key();
   }
   
   public boolean isCurrent()
   {
      return this.current;
   }
   
   public boolean isDeferred()
   {
      return ! this.current;
   }
   
   /**
    * Returns encoded information field, or <code>null</code> if information field is not valid
    * (VALID bit set to false).
    */
   protected abstract byte[] getInformation();
   
   /**
    * Returns encoded command specific information, or <code>null</code> if command specific
    * information field is unused or invalid.
    */
   protected abstract byte[] getCommandSpecificInformation();
   
   /**
    * Returns sense key specific field class or <code>null</code> if unused or invalid (SKSV
    * bit set to false on fixed format sense data, sense key specific data descriptor omitted
    * for descriptor format sense data).
    */
   protected abstract SenseKeySpecificField getSenseKeySpecific();
   
   public abstract void encode( ByteBuffer senseData ) throws BufferOverflowException, IOException;
   
   
   
   

   
   
   
   
   
}


