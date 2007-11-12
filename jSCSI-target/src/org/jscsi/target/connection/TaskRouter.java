package org.jscsi.target.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.task.TaskAbstracts.AbstractTask;

public class TaskRouter {
	
	private final Map<Integer, Connection> signaledPDUs;
	
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
	
	private class ConnectionTaskRouter extends Thread{
		
		private final int signaledPDUs;
		
		private final Map<Integer, ? extends AbstractTask> activeTasks;
		
		private final Connection refConnection;
		
		public ConnectionTaskRouter(Connection refConnection){
			activeTasks = new ConcurrentHashMap<Integer, AbstractTask>();
			this.refConnection = refConnection;
			//no active Tasks at Startup
			signaledPDUs = 0;
		}
		
		public void createTask(ProtocolDataUnit initialPDU){
			
		}
		
		public void addTask(Class<? extends AbstractTask> newTask){
		
		}
		
		public void getTask(int initiatorTaskTag){
			
		}
		
		public void signalPDU(){
			
		}
		
		public void signalImmediatePDU(){
			
		}
		
		public void run(){
			
		}
		
		
		
	}
	
	
}
