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

package org.jscsi.scsi.protocol.mode;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Serializer;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public abstract class ModePageRegistry implements Serializer
{
   private static Logger _logger = Logger.getLogger(ModePageRegistry.class);

   // Long to ModePage map
   private SortedMap<Byte, SortedMap<Integer, ModePage>> pages = null;

   // Factory registration methods
   protected void register(byte pageCode, ModePage page)
   {
      register(pageCode, 0, page);
   }

   protected void register(byte pageCode, int subPageCode, ModePage page)
   {
      if (this.pages == null)
         this.pages = new TreeMap<Byte, SortedMap<Integer, ModePage>>();
      if (!this.pages.containsKey(pageCode))
         this.pages.put(pageCode, new TreeMap<Integer, ModePage>());
      this.pages.get(pageCode).put(subPageCode, page);
   }

   public boolean contains(byte pageCode)
   {
      return this.pages.containsKey(pageCode);
   }

   public boolean contains(byte pageCode, int subPageCode)
   {
      if (!this.pages.containsKey(pageCode))
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
      for (Map<Integer, ModePage> pagelist : pages.values())
      {
         for (ModePage page : pagelist.values())
         {
            if (page.getSubPageCode() == 0x00)
            {
               value.add(page);
            }
            else if (subPages)
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
      if (this.contains(pageCode))
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
      this.populateModePages();
   }

   @SuppressWarnings("unchecked")
   public ModePage decode(ByteBuffer buffer) throws IOException
   {
      _logger.trace("Decoding mode page at buffer position: " + buffer.position());

      DataInputStream dataIn = new DataInputStream(new ByteBufferInputStream(buffer));

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

      if (page != null)
      {
         _logger.trace("Decoding mode page: " + page);
         page.decode(header, buffer);
         _logger.trace("Mode page decoded up to buffer position: " + buffer.position());
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
