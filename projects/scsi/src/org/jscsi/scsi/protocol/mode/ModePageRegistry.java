package org.jscsi.scsi.protocol.mode;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jscsi.scsi.protocol.Serializer;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public abstract class ModePageRegistry implements Serializer
{
   // Long to ModePage map
   private Map<Byte, Map<Integer,ModePage>> pages = null;


   // Factory registration methods
   private void register(byte pageCode, ModePage page)
   {
      register(pageCode, 0, page);
   }

   private void register(byte pageCode, int subPageCode, ModePage page)
   {
      if ( this.pages == null )
         this.pages = new HashMap<Byte, Map<Integer,ModePage>>();
      if ( ! this.pages.containsKey(pageCode) )
         this.pages.put(pageCode, new HashMap<Integer,ModePage>());
      this.pages.get(pageCode).put(subPageCode, page);
   }
   
   public boolean contains(byte pageCode)
   {
      return this.pages.containsKey(pageCode);
   }
   
   public boolean contains(byte pageCode, int subPageCode)
   {
      if ( ! this.pages.containsKey(pageCode) )
         return false;
      else
         return this.pages.get(pageCode).containsKey(subPageCode);
   }
   
   
   /**
    * Returns all mode pages.
    * 
    * @param subPages Returns all pages, including subpages, if <code>true</code>; returns only
    *    page_0 pages if <code>false</code>.
    */
   public Collection<ModePage> get(boolean subPages)
   {
      List<ModePage> value = new LinkedList<ModePage>();
      for ( Map<Integer,ModePage> pagelist : pages.values() )
      {
         for ( ModePage page : pagelist.values() )
         {
            if ( page.getSubPageCode() == 0x00 )
            {
               value.add(page);
            }
            else if ( subPages )
            {
               value.add(page);
            }
         }
      }
      return value;
   }
   
   
   /**
    * Returns all mode pages with the given page code.
    */
   public Collection<ModePage> get(byte pageCode)
   {
      if ( this.contains(pageCode) )
         return this.pages.get(pageCode).values();
      else
         return null;
   }
   
   public ModePage get(byte pageCode, int subPageCode)
   {
      if (this.contains(pageCode, subPageCode))
         return this.pages.get(pageCode).get(subPageCode);
      else
         return null;
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
   
   protected boolean WP = false; // WP field for DEVICE-SPECIFIC PARAMETER field in mode page header
   protected boolean DPOFUA = false; // DPOFUA field for DEVICE-SPECIFIC PARAMETER field
   
   

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
      //register(BackgroundControl.PAGE_CODE, BackgroundControl.SUBPAGE_CODE, backgroundControl);
      register(Caching.PAGE_CODE, caching);
      register(Control.PAGE_CODE, control);
      //register(ControlExtension.PAGE_CODE, ControlExtension.SUBPAGE_CODE, controlExtension);
      //register(DisconnectReconnect.PAGE_CODE, disconnectReconnect);
      register(InformationalExceptionsControl.PAGE_CODE, informationalExceptionsControl);
      //register(PowerCondition.PAGE_CODE, powerCondition);
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

         subPageCode = 0;
      }

      ModePage page = get(pageCode, subPageCode);
      
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
   
   
   

   public void setBackgroundControl(BackgroundControl backgroundControl)
   {
      this.backgroundControl = backgroundControl;
   }

   public void setCaching(Caching caching)
   {
      this.caching = caching;
   }

   public void setControl(Control control)
   {
      this.control = control;
   }

   public void setControlExtension(ControlExtension controlExtension)
   {
      this.controlExtension = controlExtension;
   }

   public void setDisconnectReconnect(DisconnectReconnect disconnectReconnect)
   {
      this.disconnectReconnect = disconnectReconnect;
   }

   public void setInformationalExceptionsControl(
         InformationalExceptionsControl informationalExceptionsControl)
   {
      this.informationalExceptionsControl = informationalExceptionsControl;
   }

   public void setPowerCondition(PowerCondition powerCondition)
   {
      this.powerCondition = powerCondition;
   }

   public void setReadWriteErrorRecovery(ReadWriteErrorRecovery readWriteErrorRecovery)
   {
      this.readWriteErrorRecovery = readWriteErrorRecovery;
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
   

   public boolean isWP()
   {
      return WP;
   }

   public void setWP(boolean wp)
   {
      WP = wp;
   }

   public boolean isDPOFUA()
   {
      return DPOFUA;
   }

   public void setDPOFUA(boolean dpofua)
   {
      DPOFUA = dpofua;
   }



}
