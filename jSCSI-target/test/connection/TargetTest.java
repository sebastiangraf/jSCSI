package connection;

import org.junit.After;
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
		
	}
	
}
