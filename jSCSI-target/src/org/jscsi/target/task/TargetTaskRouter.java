package org.jscsi.target.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.task.TaskAbstracts.AbstractTask;
import org.jscsi.target.util.Singleton;

public class TargetTaskRouter {
	
	
	
	/**
	 * the SendingLock is used to synchronize every request for sending PDUs
	 */
	private final Lock PDUReceivingLock = new ReentrantLock();
	
	

	/**
	 * the Condition is used to signal waiting Threads for received PDUs
	 */
	private final Condition pduReceived = PDUReceivingLock.newCondition();
	
	public final void signalPDU(int initiatorTaskTag, Connection refConnection){
		
	}
	
	public final void signalImmediatePDU(int initiatorTaskTag, Connection refConnection){
		
	}
	
	private final void awaitSignaledPDU(){
		PDUReceivingLock.lock();
		
		PDUReceivingLock.unlock();
	}
	
	
	private class TaskRouterWorker extends Thread{
		
		public void run(){
			
		}
		
		
	}
	
	
	
	
}
