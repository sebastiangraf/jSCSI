package org.jscsi.target.test;


import org.jscsi.target.Target;
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
			target.start();
			System.out.println("Target Initialization finished");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Waiting 2 minutes before shutdown");
		target.awaitShutdown(120);
	}
	
}
