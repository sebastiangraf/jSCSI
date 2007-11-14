package org.jscsi.scsi.lu;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class DefaultLogicalUnitTest
{
   private static Logger _logger = Logger.getLogger(DefaultLogicalUnitTest.class);
   
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
      DOMConfigurator.configure(System.getProperty("log4j.configuration"));
   }

   @After
   public void tearDown() throws Exception
   {
   }
   
   
   
}
