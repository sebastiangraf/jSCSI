package org.jscsi.scsi.protocol.mode;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.scsi.protocol.Serializer;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public abstract class ModePageRegistry implements Serializer
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

   // Mode pages
   protected BackgroundControl backgroundControl = null;
   protected Caching caching = null;
   protected Control control = null;
   protected ControlExtension controlExtension = null;
   protected DisconnectReconnect disconnectReconnect = null;
   protected InformationalExceptionsControl informationalExceptionsControl = null;
   protected PowerCondition powerCondition = null;
   protected ReadWriteErrorRecovery readWriteErrorRecovery = null;

   public ModePageRegistry()
   {
      backgroundControl = new BackgroundControl();
      caching = new Caching();
      control = new Control();
      controlExtension = new ControlExtension();
      disconnectReconnect = new DisconnectReconnect();
      informationalExceptionsControl = new InformationalExceptionsControl();
      powerCondition = new PowerCondition();
      readWriteErrorRecovery = new ReadWriteErrorRecovery();
      registerObjects();
      populateModePages();
   }

   protected abstract void populateModePages();

   // Registration
   private void registerObjects()
   {
      register(BackgroundControl.PAGE_CODE, BackgroundControl.SUBPAGE_CODE, backgroundControl);
      register(Caching.PAGE_CODE, caching);
      register(Control.PAGE_CODE, control);
      register(ControlExtension.PAGE_CODE, ControlExtension.SUBPAGE_CODE, controlExtension);
      register(DisconnectReconnect.PAGE_CODE, disconnectReconnect);
      register(InformationalExceptionsControl.PAGE_CODE, informationalExceptionsControl);
      register(PowerCondition.PAGE_CODE, powerCondition);
      register(ReadWriteErrorRecovery.PAGE_CODE, readWriteErrorRecovery);
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

   public BackgroundControl getBackgroundControl()
   {
      return this.backgroundControl;
   }

   public Caching getCaching()
   {
      return this.caching;
   }

   public Control getControl()
   {
      return this.control;
   }

   public ControlExtension getControlExtension()
   {
      return this.controlExtension;
   }

   public DisconnectReconnect getDisconnectReconnect()
   {
      return this.disconnectReconnect;
   }

   public InformationalExceptionsControl getInformationalExceptionsControl()
   {
      return informationalExceptionsControl;
   }

   public PowerCondition getPowerCondition()
   {
      return powerCondition;
   }

   public ReadWriteErrorRecovery getReadWriteErrorRecovery()
   {
      return readWriteErrorRecovery;
   }
}
