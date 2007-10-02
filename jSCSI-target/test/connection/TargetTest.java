package connection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import utilities.Singleton;

public class TargetTest {
	
	private Target TARGET;
	
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
		TARGET.startListeningOnPort(3250);
		TARGET.startListeningOnPort(3240);
		TARGET.startListeningOnPort(3230);
		TARGET.awaitShutdown(5);
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
