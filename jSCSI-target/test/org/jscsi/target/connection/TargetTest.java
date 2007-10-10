package org.jscsi.target.connection;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import org.jscsi.Configuration;
import org.jscsi.Initiator;
import org.jscsi.target.connection.Target;
import org.jscsi.target.util.Singleton;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;


public class TargetTest {
	
	private static final String TARGET_DRIVE_NAME = "disk6";

	private Target TARGET;
	
	private Initiator initiator;
	
	@Before
	public void setUp(){
		
		
		try {
			TARGET = Singleton.getInstance(Target.class);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@After
	public void tearDown(){
		
	}
	
	@Test
	public void startTarget(){
		TARGET.startListeningOnPort(3260);
		try {
			initiator = new Initiator(Configuration.create());
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	    try {
			initiator.createSession(TARGET_DRIVE_NAME);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		TARGET.awaitShutdown();
		int[] listeningPorts = TARGET.getListeningPorts();
		Assert.assertEquals(4, listeningPorts.length);
		TARGET.stopListeningOnPort(3260);
		TARGET.stopListeningOnPort(3250);
		TARGET.stopListeningOnPort(3240);
		TARGET.stopListeningOnPort(3230);
		listeningPorts = TARGET.getListeningPorts();
		Assert.assertEquals(0, listeningPorts.length);
		
	}
	
}
