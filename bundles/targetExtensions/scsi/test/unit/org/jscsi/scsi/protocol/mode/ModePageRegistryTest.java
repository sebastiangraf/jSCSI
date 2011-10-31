/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.BufferUnderflowException;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModePageRegistryTest
{

   private static class TestModePage extends ModePage
   {

      public TestModePage(byte pageCode, int subPageCode, int pageLength)
      {
         super(pageCode, subPageCode, pageLength);
      }

      public TestModePage(byte pageCode, int pageLength)
      {
         super(pageCode, pageLength);
      }

      @Override
      protected void decodeModeParameters(int dataLength, DataInputStream inputStream)
            throws BufferUnderflowException, IllegalArgumentException
      {
         // does nothing
      }

      @Override
      protected void encodeModeParameters(DataOutputStream output)
      {
         // does nothing
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         final TestModePage other = (TestModePage) obj;
         if (this.getPageCode() != other.getPageCode())
            return false;
         if (this.getSubPageCode() != other.getSubPageCode())
            return false;
         return true;
      }

      @Override
      public String toString()
      {
         return "<TestModePage (" + this.getPageCode() + "," + this.getSubPageCode() + ")>";
      }

   }

   @SuppressWarnings("unchecked")
   private static class TestModePageRegistry extends ModePageRegistry
   {
      @Override
      protected void populateModePages()
      {
         // does nothing
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

   private static void register(ModePageRegistry registry, byte pageCode, int subPageCode)
   {
      registry.register(pageCode, subPageCode, new TestModePage(pageCode, subPageCode, 0));
   }

   private static void check(Collection<ModePage> pages, byte pageCode, int subPageCode)
   {
      if (!pages.contains(new TestModePage(pageCode, subPageCode, 0)))
      {
         fail("Returned mode page list did not contain page: (" + pageCode + "," + subPageCode
               + ")");
      }

   }

   private static ModePageRegistry getFixedRegistry()
   {
      /*
       * Contents
       * ----------------
       * 
       * 0x00, 0x00
       * 0x01, 0x00
       * 0x01, 0x01
       * 0x02, 0x02
       * 0x03, 0x00
       * 0x04, 0x00
       * 0x04, 0x01
       * 0x05, 0x00
       * 
       */
      ModePageRegistry registry = new TestModePageRegistry();

      register(registry, (byte) 0x00, 0x00);
      register(registry, (byte) 0x01, 0x00);
      register(registry, (byte) 0x01, 0x01);
      register(registry, (byte) 0x02, 0x02);
      register(registry, (byte) 0x03, 0x00);
      register(registry, (byte) 0x04, 0x00);
      register(registry, (byte) 0x04, 0x01);
      register(registry, (byte) 0x05, 0x00);

      return registry;
   }

   @Test
   public void testContainsByte()
   {
      /*
       * Test with fixed test registry.
       * 
       * Input             Output
       * ----------------  --------------------
       * 0x00              true
       * 0x01              true
       * 0x02              true
       * 0x03              true
       * 0x04              true
       * 0x05              true
       * 0x06              false
       * 0x07              false
       * 
       * 
       */
      ModePageRegistry registry = getFixedRegistry();

      assertEquals("Content query failed", true, registry.contains((byte) 0x00));
      assertEquals("Content query failed", true, registry.contains((byte) 0x01));
      assertEquals("Content query failed", true, registry.contains((byte) 0x02));
      assertEquals("Content query failed", true, registry.contains((byte) 0x03));
      assertEquals("Content query failed", true, registry.contains((byte) 0x04));
      assertEquals("Content query failed", true, registry.contains((byte) 0x05));
      assertEquals("Content query failed", false, registry.contains((byte) 0x06));
      assertEquals("Content query failed", false, registry.contains((byte) 0x07));
   }

   @Test
   public void testContainsByteInt()
   {
      /*
       * Test with fixed test registry.
       * 
       * Input             Output
       * ----------------  --------------------
       * 0x00, 0x00        true
       * 0x00, 0x01        false
       * 0x01, 0x00        true
       * 0x01, 0x01        true
       * 0x01, 0x02        false
       * 0x02, 0x00        false
       * 0x02, 0x02        true
       * 0x03, 0x00        true
       * 0x04, 0x00        true
       * 0x04, 0x01        true
       * 0x05, 0x00        true
       * 0x06, 0x00        false
       * 0x06, 0x01        false
       */
      ModePageRegistry registry = getFixedRegistry();

      assertEquals("Content query failed", true, registry.contains((byte) 0x00, 0x00));
      assertEquals("Content query failed", false, registry.contains((byte) 0x00, 0x01));
      assertEquals("Content query failed", true, registry.contains((byte) 0x01, 0x00));
      assertEquals("Content query failed", true, registry.contains((byte) 0x01, 0x01));
      assertEquals("Content query failed", false, registry.contains((byte) 0x01, 0x02));
      assertEquals("Content query failed", false, registry.contains((byte) 0x02, 0x00));
      assertEquals("Content query failed", true, registry.contains((byte) 0x02, 0x02));
      assertEquals("Content query failed", true, registry.contains((byte) 0x03, 0x00));
      assertEquals("Content query failed", true, registry.contains((byte) 0x04, 0x00));
      assertEquals("Content query failed", true, registry.contains((byte) 0x04, 0x01));
      assertEquals("Content query failed", true, registry.contains((byte) 0x05, 0x00));
      assertEquals("Content query failed", false, registry.contains((byte) 0x06, 0x00));
      assertEquals("Content query failed", false, registry.contains((byte) 0x06, 0x01));

   }

   @Test
   public void testGetBoolean()
   {
      /*
       * Test with fixed test registry.
       * 
       * Input             Output
       * ----------------  --------------------
       * true              [(0,0), (1,0), (1,1), (2,2), (3,0), (4,0), (4,1), (5,0)]
       * false             [(0,0), (1,0), (3,0), (4,0), (5,0)]
       */
      ModePageRegistry registry = getFixedRegistry();

      Collection<ModePage> pages = registry.get(true);
      System.out.println(pages);
      assertEquals("Too many returned pages", 8, pages.size());
      check(pages, (byte) 0, 0);
      check(pages, (byte) 1, 0);
      check(pages, (byte) 1, 1);
      check(pages, (byte) 2, 2);
      check(pages, (byte) 3, 0);
      check(pages, (byte) 4, 0);
      check(pages, (byte) 4, 1);
      check(pages, (byte) 5, 0);

      pages = registry.get(false);
      assertEquals("Too many returned pages", 5, pages.size());
      check(pages, (byte) 0, 0);
      check(pages, (byte) 1, 0);
      check(pages, (byte) 3, 0);
      check(pages, (byte) 4, 0);
      check(pages, (byte) 5, 0);
   }

   @Test
   public void testGetByte()
   {
      /*
       * Test with fixed test registry.
       * 
       * Input             Output
       * ----------------  --------------------
       * 0x00              [(0,0)]
       * 0x01              [(1,0), (1,1)]
       * 0x02              [(2,2)]
       * 0x03              [(3,0)]
       * 0x04              [(4,0), (4,1)]
       * 0x05              [(5,0)]
       * 0x06              []
       */
      ModePageRegistry registry = getFixedRegistry();

      Collection<ModePage> pages = registry.get((byte) 0x00);
      assertEquals("Too many returned pages", 1, pages.size());
      check(pages, (byte) 0, 0);

      pages = registry.get((byte) 0x01);
      assertEquals("Too many returned pages", 2, pages.size());
      check(pages, (byte) 1, 0);
      check(pages, (byte) 1, 1);

      pages = registry.get((byte) 0x02);
      assertEquals("Too many returned pages", 1, pages.size());
      check(pages, (byte) 2, 2);

      pages = registry.get((byte) 0x03);
      assertEquals("Too many returned pages", 1, pages.size());
      check(pages, (byte) 3, 0);

      pages = registry.get((byte) 0x04);
      assertEquals("Too many returned pages", 2, pages.size());
      check(pages, (byte) 4, 0);
      check(pages, (byte) 4, 1);

      pages = registry.get((byte) 0x01);
      assertEquals("Too many returned pages", 2, pages.size());
      check(pages, (byte) 1, 0);
      check(pages, (byte) 1, 1);

      pages = registry.get((byte) 0x05);
      assertEquals("Too many returned pages", 1, pages.size());
      check(pages, (byte) 5, 0);

      pages = registry.get((byte) 0x06);
      assertEquals("Too many returned pages", null, pages);
   }

   @Test
   public void testGetByteInt()
   {
      /*
       * Test with fixed test registry.
       * 
       * Input             Output
       * ----------------  --------------------
       * 0x00, 0x00        <obj>
       * 0x00, 0x01        null
       * 0x01, 0x00        <obj>
       * 0x01, 0x01        <obj>
       * 0x01, 0x02        null
       * 0x02, 0x00        null
       * 0x02, 0x02        <obj>
       * 0x03, 0x00        <obj>
       * 0x04, 0x00        <obj>
       * 0x04, 0x01        <obj>
       * 0x05, 0x00        <obj>
       * 0x06, 0x00        null
       * 0x06, 0x01        null
       */
      ModePageRegistry registry = getFixedRegistry();

      assertTrue("Returned invalid mode page", registry.get((byte) 0x00, 0x00) != null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x00, 0x01) == null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x01, 0x00) != null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x01, 0x01) != null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x01, 0x02) == null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x02, 0x00) == null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x02, 0x02) != null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x03, 0x00) != null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x04, 0x00) != null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x04, 0x01) != null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x05, 0x00) != null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x06, 0x00) == null);
      assertTrue("Returned invalid mode page", registry.get((byte) 0x06, 0x01) == null);
   }

}
