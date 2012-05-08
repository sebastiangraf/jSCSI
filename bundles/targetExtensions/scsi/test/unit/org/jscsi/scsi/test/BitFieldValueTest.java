/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
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

package org.jscsi.scsi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//TODO: Describe class or interface
public class BitFieldValueTest
{

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

   @Test
   public void testParseValueList() throws Exception
   {
      List<List<Boolean>> values = BitFieldValue.parseValueList("0x0A;0b001;0b010", 5, 0);
      assertEquals("returned list improper length", 3, values.size());

      Object[] val = null;

      val = (Object[]) values.get(0).toArray();
      assertTrue("element parsed incorrectly", Arrays.equals(val, new Object[]{
            false, true, false, true, false
      }));

      val = (Object[]) values.get(1).toArray();
      assertTrue("element parsed incorrectly", Arrays.equals(val, new Object[]{
            false, false, false, false, true
      }));

      val = (Object[]) values.get(2).toArray();
      assertTrue("element parsed incorrectly", Arrays.equals(val, new Object[]{
            false, false, false, true, false
      }));
   }

   @Test
   public void testParseFieldLength() throws Exception
   {
      assertEquals("invalid field length parsed", 8, BitFieldValue.parseFieldLength("bits",
            "bits(8)", 0));
      assertEquals("invalid field length parsed", 16, BitFieldValue.parseFieldLength("bits",
            "bits(16)", 0));
      assertEquals("invalid field length parsed", 100, BitFieldValue.parseFieldLength("bits",
            "bits(100)", 0));
      assertEquals("invalid field length parsed", 10234, BitFieldValue.parseFieldLength("bits",
            "bits(10234)", 0));
      assertEquals("invalid field length parsed", 58, BitFieldValue.parseFieldLength("bits",
            "bits(58)", 0));
   }

   @Test
   public void testParseBinaryValue() throws Exception
   {
      Object[] val = null;

      val = (Object[]) BitFieldValue.parseBinaryValue("0110101", 7, 0).toArray();
      assertTrue("parse failure", Arrays.equals(val, new Object[]{
            false, true, true, false, true, false, true
      }));

      val = (Object[]) BitFieldValue.parseBinaryValue("0110101", 5, 0).toArray();
      assertTrue("parse failure", Arrays.equals(val, new Object[]{
            true, false, true, false, true
      }));

      val = (Object[]) BitFieldValue.parseBinaryValue("0110101", 9, 0).toArray();
      assertTrue("parse failure", Arrays.equals(val, new Object[]{
            false, false, false, true, true, false, true, false, true
      }));
   }

   @Test
   public void testParseHexValue_FillAndTruncate() throws Exception
   {
      Object[] val = null;

      val = (Object[]) BitFieldValue.parseHexValue("5", 0, 0).toArray();
      assertTrue("truncate failure", Arrays.equals(val, new Object[]{}));

      val = (Object[]) BitFieldValue.parseHexValue("5", 1, 0).toArray();
      assertTrue("truncate failure", Arrays.equals(val, new Object[]{
         true
      }));

      val = (Object[]) BitFieldValue.parseHexValue("5", 2, 0).toArray();
      assertTrue("truncate failure", Arrays.equals(val, new Object[]{
            false, true
      }));

      val = (Object[]) BitFieldValue.parseHexValue("5", 3, 0).toArray();
      assertTrue("truncate failure", Arrays.equals(val, new Object[]{
            true, false, true
      }));

      val = (Object[]) BitFieldValue.parseHexValue("5", 4, 0).toArray();
      assertTrue("truncate failure", Arrays.equals(val, new Object[]{
            false, true, false, true
      }));

      val = (Object[]) BitFieldValue.parseHexValue("5", 5, 0).toArray();
      assertTrue("fill failure", Arrays.equals(val, new Object[]{
            false, false, true, false, true
      }));

      val = (Object[]) BitFieldValue.parseHexValue("5", 6, 0).toArray();
      assertTrue("fill failure", Arrays.equals(val, new Object[]{
            false, false, false, true, false, true
      }));

      val = (Object[]) BitFieldValue.parseHexValue("5", 7, 0).toArray();
      assertTrue("fill failure", Arrays.equals(val, new Object[]{
            false, false, false, false, true, false, true
      }));

      val = (Object[]) BitFieldValue.parseHexValue("5", 8, 0).toArray();
      assertTrue("fill failure", Arrays.equals(val, new Object[]{
            false, false, false, false, false, true, false, true
      }));

   }

   @Test
   public void testParseHexValue_MultipleCharacters() throws Exception
   {
      Object[] val = null;

      val = (Object[]) BitFieldValue.parseHexValue("5", 4, 0).toArray();
      assertTrue("truncate failure", Arrays.equals(val, new Object[]{
            false, true, false, true
      }));

      val = (Object[]) BitFieldValue.parseHexValue("5A", 8, 0).toArray();
      assertTrue("truncate failure", Arrays.equals(val, new Object[]{
            false, true, false, true, true, false, true, false
      }));
   }

}
