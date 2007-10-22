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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
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

   private ModePage getModePage(byte pageCode, int subPageCode)
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

   public void save(byte[] input) throws BufferUnderflowException, IOException
   {
      boolean parametersSavable;
      int dataLength;
      boolean subPageFormat;
      byte pageCode;
      int subPageCode;

      int b0 = input[0];
      parametersSavable = ((b0 >>> 7) & 0x01) == 1;
      subPageFormat = ((b0 >>> 6) & 0x01) == 1;
      pageCode = (byte) (b0 & 0x3F);

      ByteBuffer inputBuffer = ByteBuffer.wrap(input);
      if (subPageFormat)
      {
         subPageCode = input[1];
         dataLength = ((input[2] << 8) | input[3]) - 4;
         inputBuffer.position(4);
      }
      else
      {
         subPageCode = -1;
         dataLength = input[1] - 2;
         inputBuffer.position(2);
      }

      ModePage page = getModePage(pageCode, subPageCode);

      if (page != null)
      {
         page.setParametersSavable(parametersSavable);
         page.decodeModeParameters(dataLength, inputBuffer);
      }
      else
      {
         throw new RuntimeException("Invalid pageCode/subPageCode - (" + pageCode + "/"
               + subPageCode + ") no corresponding ModePage class found");
      }
   }

   public byte[] getEncodedModePage(byte pageCode)
   {
      return getEncodedModePage(pageCode, -1);
   }

   public byte[] getEncodedModePage(byte pageCode, int subPageCode) throws BufferOverflowException
   {
      ModePage modePage = getModePage(pageCode, subPageCode);

      // Below, header is 2 bytes for page format, 4 bytes for subpage format.
      ByteArrayOutputStream header = new ByteArrayOutputStream(modePage.getSubPageFormat() ? 4 : 2);
      DataOutputStream out = new DataOutputStream(header);

      try
      {
         int b0 = 0;

         if (modePage.getParametersSavable())
         {
            b0 |= 0x80;
         }
         if (modePage.getSubPageFormat())
         {
            b0 |= 0x40;
         }

         b0 |= (modePage.getPageCode() & 0x3F);

         out.writeByte(b0);

         if (modePage.getSubPageFormat())
         {
            out.writeByte(modePage.getSubPageCode());
            out.writeShort(modePage.getPageLength());
         }
         else
         {
            out.writeByte(modePage.getPageLength());
         }

         // Allocate page length
         ByteBuffer outputBuffer = ByteBuffer.allocate(modePage.getPageLength());

         // Write header
         outputBuffer.put(header.toByteArray());

         // Write mode parameters
         modePage.encodeModeParameters(outputBuffer);

         return outputBuffer.array();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode mode page.");
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
