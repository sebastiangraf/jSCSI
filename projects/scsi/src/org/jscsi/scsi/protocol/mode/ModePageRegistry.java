//
// Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
// Cleversafe Dispersed Storage(TM) is software for secure, private and
// reliable storage of the world's data using information dispersal.
//
// Copyright (C) 2005-2007 Cleversafe, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
// USA.
//
// Contact Information: Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email licensing@cleversafe.org
//
// END-OF-HEADER
//-----------------------
// @author: mmotwani
//
// Date: Oct 22, 2007
//---------------------

package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;

public abstract class ModePageRegistry
{
   // Long to ModePage map
   private Map<Long, ModePage> _pages = new HashMap<Long, ModePage>();

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
      _pages.put(getParserID(pageCode, subPageCode), page);
   }

   public ModePage getModePage(byte pageCode, int subPageCode)
   {
      return _pages.get(getParserID(pageCode, subPageCode));
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

   public void saveModePages(byte[] input) throws BufferUnderflowException, IOException
   {
      DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(input));

      // While all pages are not saved
      while (dataIn.available() > 0)
      {
         boolean parametersSavable;
         int dataLength;
         boolean subPageFormat;
         byte pageCode;
         int subPageCode;

         int b0 = dataIn.readUnsignedByte();
         parametersSavable = ((b0 >>> 7) & 0x01) == 1;
         subPageFormat = ((b0 >>> 6) & 0x01) == 1;
         pageCode = (byte) (b0 & 0x3F);

         short pageLength;
         if (subPageFormat)
         {
            subPageCode = dataIn.readByte();
            pageLength = dataIn.readShort();
            dataLength = pageLength - 4;
         }
         else
         {
            subPageCode = -1;
            pageLength = dataIn.readByte();
            dataLength = pageLength - 2;
         }

         ModePage page = getModePage(pageCode, subPageCode);

         if (page != null)
         {
            page.setParametersSavable(parametersSavable);
            page.decodeModeParameters(dataLength, dataIn);
         }
         else
         {
            throw new RuntimeException("Invalid pageCode/subPageCode - (" + pageCode + "/"
                  + subPageCode + ") no corresponding ModePage found");
         }
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
