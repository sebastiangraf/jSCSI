package org.jscsi.scsi.protocol.inquiry.vpd;

import java.util.HashMap;
import java.util.Map;

public enum IdentifierType
{
   VENDOR_SPECIFIC                  (0x00),
   T10_VENDOR_ID_BASED              (0x01),
   EUI_64_BASED                     (0x02),
   NAA                              (0x03),
   RELATIVE_TARGET_PORT_IDENTIFIER  (0x04),
   TARGET_PORT_GROUP                (0x05),
   LOGICAL_UNIT_GROUP               (0x06),
   MD5_LOGICAL_UNIT_IDENTIFIER      (0x07),
   SCSI_NAME_STRING                 (0x08);

   private final int value;

   private static Map<Integer, IdentifierType> mapping;

   private IdentifierType(final int newValue)
   {

      if (IdentifierType.mapping == null)
      {
         IdentifierType.mapping = new HashMap<Integer, IdentifierType>();
      }

      IdentifierType.mapping.put(newValue, this);
      value = newValue;
   }

   public final int value()
   {
      return value;
   }

   public static final IdentifierType valueOf(final int value)
   {
      return IdentifierType.mapping.get(value);
   }

   @Override
   public String toString()
   {
      String output = "<status:";

      switch (IdentifierType.valueOf(value))
      {
         case VENDOR_SPECIFIC :
            output += " vendor specific";
            break;
         case T10_VENDOR_ID_BASED :
            output += " T10 vendor id-based";
            break;
         case EUI_64_BASED :
            output += " EUI 64-based";
            break;
         case NAA :
            output += " NAA";
            break;
         case RELATIVE_TARGET_PORT_IDENTIFIER :
            output += " relative target port identifier";
            break;
         case TARGET_PORT_GROUP :
            output += " target port group";
            break;
         case LOGICAL_UNIT_GROUP :
            output += " logical unit group";
            break;
         case MD5_LOGICAL_UNIT_IDENTIFIER :
            output += " MD5 logical unit identifier";
            break;
         case SCSI_NAME_STRING :
            output += " SCSI name string";
            break;
      }
      return output + ">";
   }
}
