package org.jscsi.target;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TargetTest {
	
	Target target;
	
	@Before
	public void setUp() {
		target = new Target();
	}

	@After
	public void tearDown() {

	}
	
	@Test
	public void initializeTarget(){
		try {
			target.initialize();
			System.out.println("Target Initialization finished");
		} catch (TargetException e) {
			System.out.println("Error occured initializing target: " + e.getMessage());
		}
	}
	
}
