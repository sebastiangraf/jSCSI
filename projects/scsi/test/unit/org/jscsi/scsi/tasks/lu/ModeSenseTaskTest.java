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
// Author: wleggette
//
// Date: Nov 15, 2007
//---------------------

package org.jscsi.scsi.tasks.lu;
import static org.junit.Assert.fail;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.ModeSense6;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.inquiry.StaticInquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePage;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.SenseData;
import org.jscsi.scsi.protocol.sense.SenseDataFactory;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.tasks.management.AbstractTaskSetTest.TestTargetTransportPort;
import org.jscsi.scsi.transport.Nexus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO: Describe class or interface
public class ModeSenseTaskTest
{
   private static Logger _logger = Logger.getLogger(ModeSenseTaskTest.class);
   
   public static class ModeSenseException extends Exception
   {
      private static Logger _logger = Logger.getLogger(ModeSenseException.class);
      
      private Status status;
      private ByteBuffer senseData;
      
      public ModeSenseException(Status status, ByteBuffer senseData)
      {
         super("ModeSenseTask returned bad status: " + status.name());
         _logger.error("ModeSenseTask returned bad status: " + status.name());
         this.status = status;
         this.senseData = senseData;
      }

      public ModeSenseException(String message)
      {
         super(message);
         _logger.error(message);
         status = null;
         senseData = null;
      }

      public Status getStatus()
      {
         return status;
      }

      public ByteBuffer getSenseData()
      {
         return senseData;
      }
   }

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
   }

   @Before
   public void setUp() throws Exception
   {
   }

   @After
   public void tearDown() throws Exception
   {
   }
   
   private Collection<ModePage> execute6(ModePageRegistry registry, byte pageCode, int subPageCode)
         throws ModeSenseException
   {
      // We allocate 200 bytes for return data
      CDB cdb = new ModeSense6(true, 0, pageCode, subPageCode, 200);
      
      TestTargetTransportPort port = new TestTargetTransportPort(null, false);
      Command command = new Command(
            new Nexus("initiator", "target", 0, 0),
            cdb,
            TaskAttribute.SIMPLE,
            0,
            0 );
      InquiryDataRegistry inqreg = new StaticInquiryDataRegistry();
      
      _logger.debug("Creating ModeSenseTask from MODE SENSE (6) command");
      Task task = new ModeSenseTask(port, command, registry, inqreg);
      _logger.debug("Running mode sense task");
      task.run();
      
      if ( port.getLastStatus() != Status.GOOD )
      {
         throw new ModeSenseException(port.getLastStatus(), port.getSenseData());
      }
      
      ByteBuffer data = port.getTransferData();
      if ( data == null )
      {
         throw new ModeSenseException("ModeSenseTask returned null");
      }
      
      try
      {
         DataInputStream in = new DataInputStream( new ByteBufferInputStream(data) );
         
         int modeDataLength = in.readUnsignedByte();
         int mediumType = in.readUnsignedByte();
         in.readUnsignedByte();  // skip DEVICE-SPECIFIC PARAMETER
         int blockDescriptorLength = in.readUnsignedByte();
         
         if ( mediumType != 0x00 )
            throw new ModeSenseException(
                  "medium type value invalid (expected 0x00, got " + mediumType + ")");
         
         // TODO: Check device specific parameter
         
         if ( blockDescriptorLength != 0 )
            throw new ModeSenseException("block descriptor length non-zero");
         
         if ( data.limit() != modeDataLength + 1 )
            throw new ModeSenseException("improper amount of data returned, header says " +
                  modeDataLength + " + 1 bytes, buffer contains " + data.limit());
         
         List<ModePage> pages = new LinkedList<ModePage>();
         
         while ( data.position() < data.limit() )
         {
            try
            {
               pages.add( registry.decode(data) );
            }
            catch (IOException e)
            {
               throw new ModeSenseException(
                     "Could not decode one or more mode pages: " + e.getMessage());
            }
         }
         
         return pages;
         
      }
      catch (IOException e)
      {
         throw new ModeSenseException("Error decoding return data: " + e.getMessage());
      }
      
   }
   
   
   private void matches(ModePage page, byte pageCode, int subPageCode)
   {
      if (page.getPageCode() == pageCode && page.getSubPageCode() == subPageCode)
         return;
      else
         fail( String.format("Mode page does not match: expected (%X,%X), found (%X,%X)",
               pageCode, subPageCode, page.getPageCode(), page.getSubPageCode()));
   }
   
   public void checkErrorCondition(ModeSenseException exception)
   {
      if (exception.getStatus() == null || exception.getStatus() != Status.CHECK_CONDITION)
         fail("Expected task to return CHECK CONDITION; got " + 
               (exception.getStatus() != null ? exception.getStatus().name() : "null"));
      
      if ( exception.getSenseData() == null )
         fail("Task returned CHECK CONDITION but did not return autosense data");
      
      try
      {
         SenseDataFactory fact = new SenseDataFactory();
         SenseData sense = fact.decode(exception.getSenseData());
         if ( sense.getKCQ() != KCQ.INVALID_FIELD_IN_CDB )
            fail("Expected KCQ INVALID FIELD IN CDB; got " + sense.getKCQ().name());
      }
      catch (IOException e)
      {
         fail("Could not decode returned sense data: " + e.getMessage());
      }
   }
   
   
   @Test
   public void testRequestAllModePages()
   {
      try
      {
         ModePageRegistry registry = new StaticModePageRegistry();
         
         Collection<ModePage> pages = execute6(registry, (byte)0x3F, 0xFF);
         Iterator<ModePage> it = pages.iterator();

         matches(it.next(), (byte)0x01, 0x00);
         matches(it.next(), (byte)0x08, 0x00);
         matches(it.next(), (byte)0x0A, 0x00);
         matches(it.next(), (byte)0x0A, 0x01);
         matches(it.next(), (byte)0x1C, 0x00);
      }
      catch (ModeSenseException e)
      {
         fail(e.getMessage());
      }
   }
   
   @Test
   public void testRequestAllPage0ModePages()
   {
      try
      {
         ModePageRegistry registry = new StaticModePageRegistry();
         
         Collection<ModePage> pages = execute6(registry, (byte)0x3F, 0x00);
         Iterator<ModePage> it = pages.iterator();

         matches(it.next(), (byte)0x01, 0x00);
         matches(it.next(), (byte)0x08, 0x00);
         matches(it.next(), (byte)0x0A, 0x00);
         matches(it.next(), (byte)0x1C, 0x00);
      }
      catch (ModeSenseException e)
      {
         fail(e.getMessage());
      }
   }
   
   @Test
   public void testRequestInvalidModePage()
   {
      try
      {
         ModePageRegistry registry = new StaticModePageRegistry();
         
         execute6(registry, (byte)0x80, 0x00);
         fail("MODE SENSE request succeeded unexpectedly");
      }
      catch (ModeSenseException e)
      {
         checkErrorCondition(e);  
      }
   }
   
   @Test
   public void testRequestValidModePage()
   {
      try
      {
         ModePageRegistry registry = new StaticModePageRegistry();
         
         Collection<ModePage> pages = execute6(registry, (byte)0x08, 0x00);
         Iterator<ModePage> it = pages.iterator();

         matches(it.next(), (byte)0x08, 0x00);
         
         
         pages = execute6(registry, (byte)0x0A, 0x01);
         it = pages.iterator();

         matches(it.next(), (byte)0x0A, 0x01);
      }
      catch (ModeSenseException e)
      {
         fail(e.getMessage());
      }
   }
   
   
   

}


