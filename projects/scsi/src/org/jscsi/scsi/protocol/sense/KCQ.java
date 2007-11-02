package org.jscsi.scsi.protocol.sense;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public enum KCQ
{
   NO_ERROR( 0, 0, 0 ),
   
   PERIPHERAL_DEVICE_WRITE_FAULT_RECOVERED( 1, 0x03, 0x00 ),
   INTERNAL_TARGET_FAILURE_RECOVERED( 1, 0x44, 0x00 ),
   
   LOGICAL_UNIT_IS_IN_PROCESS_OF_BECOMING_READY( 2, 0x04, 0x01 ),
   LOGICAL_UNIT_FAILED_SELF_CONFIGURATION( 2, 0x4C, 0x00 ),
   
   WRITE_ERROR( 3, 0x00, 0x00 ),
   PERIPHERAL_DEVICE_WRITE_FAULT_ON_MEDIUM( 3, 0x03, 0x00 ),
   UNRECOVERED_READ_ERROR( 3, 0x11, 0x00 ),
   CANNOT_READ_MEDIUM_UNKNOWN_FORMAT( 3, 0x30, 0x01 ),
   CANNOT_READ_MEDIUM_INCOMPATIBLE_FORMAT( 3, 0x30, 0x02 ),
   CANNOT_WRITE_MEDIUM_UNKNOWN_FORMAT( 3, 0x30, 0x04 ),
   CANNOT_WRITE_MEDIUM_INCOMPATIBLE_FORMAT( 3, 0x30, 0x05 ),
   
   PERIPHERAL_DEVICE_WRITE_FAULT_ON_HARDWARE( 4, 0x03, 0x00 ),
   INTERNAL_TARGET_FAILURE_ON_HARDWARE( 4, 0x44, 0x00 ),
   
   INVALID_COMMAND_OPERATION_CODE( 5, 0x20, 0x00 ),
   LOGICAL_BLOCK_ADDRESS_OUT_OF_RANGE( 5, 0x21, 0x00 ),
   INVALID_FIELD_IN_CDB( 5, 0x24, 0x00 ),
   LOGICAL_UNIT_NOT_SUPPORTED( 5, 0x25, 0x00 ),
   INVALID_FIELD_IN_PARAMETER_LIST( 5, 0x26, 0x00 ),
   PARAMETER_NOT_SUPPORTED( 5, 0x26, 0x01 ),
   PARAMETER_VALUE_INVALID( 5, 0x26, 0x02 ),
   COMMAND_SEQUENCE_ERROR( 5, 0x2C, 0x00 ),
   INVALID_MESSAGE_ERROR( 5, 0x49, 0x00 ),
   
   PARAMETERS_CHANGED( 6, 0x2A, 0x00 ),
   MODE_PARAMETERS_CHANGED( 6, 0x2A, 0x01 ),
   CAPACITY_DATA_HAS_CHANGED( 6, 0x2A, 0x09 ),
   INQUIRY_DATA_HAS_CHANGED( 6, 0x3F, 0x03 ),
   REPORTED_LUNS_DATA_HAS_CHANGED( 6, 0x3F, 0x0E ),
   
   WRITE_PROTECTED( 7, 0x27, 0x00 ),
   
   ABORTED_COMMAND_NO_ADDITIONAL_INFORMATION( 0xB, 0x00, 0x00 ),
   SYNCHRONOUS_DATA_TRANSFER_ERROR( 0xB, 0x1B, 0x00 ),
   LOGICAL_UNIT_NOT_SUPPORTED_ABORTED( 0xB, 0x25, 0x00 ),
   INTERNAL_TARGET_FAILURE_ABORTED( 0xB, 0x44, 0x00 ),
   INVALID_MESSAGE_ERROR_ABORTED( 0xB, 0x49, 0x00 ),
   
   ;
   
   
   private final SenseKey key;
   private final int code;  // UBYTE_MAX
   private final int qualifier; // UBYTE_MAX
   
   
   private static Map<Long, KCQ> mapping;
   
   private KCQ(int key, int code, int qualifier)
   {
      if ( KCQ.mapping == null )
      {
         KCQ.mapping = new HashMap<Long,KCQ>();
      }
      try
      {
         this.key = SenseKey.valueOf(key);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Invalid sense key value specified");
      }
      this.code = code;
      this.qualifier = qualifier;
      
      KCQ.mapping.put( get20b(key, code, qualifier), this );
   }
   
   // Returns a long containing the 20-bit KCQ.
   private static long get20b(int key, int code, int qualifier)
   {
      long kcq = 0;
      kcq |= ((key & 0x0F) << 16);
      kcq |= ((code & 0xFF) << 8);
      kcq |= (qualifier & 0xFF);
      return kcq;
   }
   
   public SenseKey key()
   {
      return this.key;
   }
   
   public int code()
   {
      return this.code;
   }
   
   public int qualifier()
   {
      return this.qualifier;
   }
   
   public static final KCQ valueOf( SenseKey key, int code, int qualifier ) throws IOException
   {
      return valueOf(key.value(), code, qualifier);
   }
   
   public static final KCQ valueOf( int key, int code, int qualifier ) throws IOException
   {
      KCQ kcq = KCQ.mapping.get( get20b(key, code, qualifier) );
      if ( kcq == null )
      {
         throw new IOException("Invalid KCQ values: " + key + ", " + code + ", " + qualifier);
      }
      else
      {
         return kcq;
      }
   }
}
