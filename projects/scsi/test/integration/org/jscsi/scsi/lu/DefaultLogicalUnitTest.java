package org.jscsi.scsi.lu;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import org.junit.After;
import org.junit.Before;

public class DefaultLogicalUnitTest
{
   private static Logger _logger = Logger.getLogger(DefaultLogicalUnitTest.class);
   
   @Before
   public void setUp() throws Exception
   {
      DOMConfigurator.configure(System.getProperty("log4j.configuration"));
   }

   @After
   public void tearDown() throws Exception
   {
   }
   
   
   
}
