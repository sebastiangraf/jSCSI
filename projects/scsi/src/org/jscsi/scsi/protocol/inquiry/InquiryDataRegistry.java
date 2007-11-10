package org.jscsi.scsi.protocol.inquiry;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.scsi.protocol.Serializer;
import org.jscsi.scsi.protocol.mode.BackgroundControl;
import org.jscsi.scsi.protocol.mode.Caching;
import org.jscsi.scsi.protocol.mode.Control;
import org.jscsi.scsi.protocol.mode.ControlExtension;
import org.jscsi.scsi.protocol.mode.DisconnectReconnect;
import org.jscsi.scsi.protocol.mode.InformationalExceptionsControl;
import org.jscsi.scsi.protocol.mode.ModePage;
import org.jscsi.scsi.protocol.mode.PowerCondition;
import org.jscsi.scsi.protocol.mode.ReadWriteErrorRecovery;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

/**
 * 
 */
public abstract class InquiryDataRegistry implements Serializer
{
   // Long to ModePage map
   private Map<Long, ModePage> pages = new HashMap<Long, ModePage>();

   private long getParserID(byte pageCode, int subPageCode)
   {
      return (pageCode << 32) | subPageCode;
   }

   // Factory registration methods
   private void register(byte pageCode, ModePage page)
   {
      register(pageCode, -1, page);
   }

   private void register(byte pageCode, int subPageCode, ModePage page)
   {
      pages.put(getParserID(pageCode, subPageCode), page);
   }

   public ModePage getModePage(byte pageCode, int subPageCode)
   {
      return pages.get(getParserID(pageCode, subPageCode));
   }

   // Inquiry pages

   protected StandardInquiryData standardInquiryData = null;
   
   
   
   
   public InquiryDataRegistry()
   {
      standardInquiryData = new StandardInquiryData();

      // initialize the pages
      registerObjects();
      populateInquiryPages();
   }

   protected abstract void populateInquiryPages();

   // Registration
   private void registerObjects()
   {/*
      register(StandardInquiryData.PAGE_CODE);
      register(BackgroundControl.PAGE_CODE, BackgroundControl.SUBPAGE_CODE, backgroundControl);
      register(Caching.PAGE_CODE, caching);
      register(Control.PAGE_CODE, control);
      register(ControlExtension.PAGE_CODE, ControlExtension.SUBPAGE_CODE, controlExtension);
      register(DisconnectReconnect.PAGE_CODE, disconnectReconnect);
      register(InformationalExceptionsControl.PAGE_CODE, informationalExceptionsControl);
      register(PowerCondition.PAGE_CODE, powerCondition);
      register(ReadWriteErrorRecovery.PAGE_CODE, readWriteErrorRecovery);
      */
   }

   @SuppressWarnings("unchecked")
   public ModePage decode(ByteBuffer buffer) throws IOException
   {
      DataInputStream dataIn = new DataInputStream(new ByteBufferInputStream(buffer.duplicate()));

      boolean subPageFormat;
      byte[] header;

      dataIn.mark(0);
      int b0 = dataIn.readUnsignedByte();
      dataIn.reset();

      subPageFormat = ((b0 >>> 6) & 0x01) == 1;

      byte pageCode = (byte) (b0 & 0x3F);
      int subPageCode;

      if (subPageFormat)
      {
         header = new byte[4];
         dataIn.read(header);

         subPageCode = header[1];
      }
      else
      {
         header = new byte[2];
         dataIn.read(header);

         subPageCode = -1;
      }

      ModePage page = getModePage(pageCode, subPageCode);
      
      if(page != null)
      {
         page.decode(header, buffer);
         return page;
      }
      else
      {
         throw new RuntimeException("Invalid pageCode/subPageCode - (" + pageCode + "/"
               + subPageCode + ") no corresponding ModePage found");

      }
   }

   public void saveModePages(ByteBuffer pages) throws BufferUnderflowException, IOException
   {
      while (pages.position() < pages.limit())
      {
         decode(pages);
      }
   }

   public StandardInquiryData getStandardInquiryData()
   {
      return this.standardInquiryData;
   }
}
