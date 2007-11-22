package org.jscsi.target.connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.jscsi.target.connection.TSIHFactory;
import org.jscsi.target.util.Singleton;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



public class TSIHFactoryTest {
	
	/**
	 * 
	 */
	private final int runningSecs = 5 * 1000;

	private final int CONSUMERS = 10;

	private Set<Short> allUsedTSIHs;

	@Before
	public void setUp() {
		try {
			Singleton.getInstance(TSIHFactory.class);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		allUsedTSIHs = new HashSet<Short>();
	}

	@After
	public void tearDown() {

	}

	@SuppressWarnings("static-access")
	@Test
	public void testAll() {
		Set<HardcoreTSIHConsumer> consumers = new HashSet<HardcoreTSIHConsumer>();

		for (int i = 0; i < CONSUMERS; i++) {
			HardcoreTSIHConsumer newOne = new HardcoreTSIHConsumer(50);
			newOne.setName("Consumer " + i);
			consumers.add(newOne);
		}

		Iterator<HardcoreTSIHConsumer> iterator = consumers.iterator();
		while (iterator.hasNext()) {
			iterator.next().start();
		}
		try {
			Thread.currentThread().sleep(runningSecs);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addUsedTSIH(short tsih, String consumerName) {
		synchronized (this) {
			Assert.assertFalse(allUsedTSIHs.contains(tsih));
			System.out.println(consumerName + " used TSIH: " + tsih);
			allUsedTSIHs.add(tsih);
		}
	}

	private void removeUsedTSIH(short tsih, String consumerName)  {
		synchronized (this) {
			Assert.assertTrue(allUsedTSIHs.contains(tsih));
			System.out.println(consumerName + " removed TSIH: " + tsih);
			allUsedTSIHs.remove(tsih);
		}

	}

	public class HardcoreTSIHConsumer extends Thread {

		private TSIHFactory myTSIHFactory;
		private Set<Short> myUsedTSIHs;

		private int sleepingSECS;

		private Random random = new Random();

		public HardcoreTSIHConsumer(int sleepingSecs) {
			try {
				myTSIHFactory = Singleton.getInstance(TSIHFactory.class);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myUsedTSIHs = new HashSet<Short>();
			sleepingSECS = sleepingSecs;
		}

		@Override
		public void run() {

			while (!interrupted()) {
				try {
					sleep(sleepingSECS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				boolean test = (random.nextInt(2) == 1);
				if (test) {
					addUsedTSIH(receiveTSIH(), getName());
				} else {
					if (myUsedTSIHs.size() != 0) {
						int counter = random.nextInt(myUsedTSIHs.size());
						Iterator<Short> iterator = myUsedTSIHs.iterator();
						for(int i = 0; i < counter; i++){
							iterator.next();
						}
						short tsih = iterator.next();
						removeTSIH(tsih);
						removeUsedTSIH(tsih, getName());
					}
				}
			}
		}

		private short receiveTSIH() {
			short newOne = 0;
			try {
				newOne = myTSIHFactory.getNewTSIH();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myUsedTSIHs.add(newOne);
			return newOne;
		}

		private void removeTSIH(short tsih) {
			myUsedTSIHs.remove(tsih);
			myTSIHFactory.removeTSIH(tsih);
		}

	}

}
