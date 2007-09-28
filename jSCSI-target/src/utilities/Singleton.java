package utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class represents a Singleton pattern for all classes needed to be
 * Singleton.
 * 
 * @author Marcus Specht
 * 
 */
public final class Singleton {

	/** used for synchronization */
	private final static Lock LOCK = new ReentrantLock();
	
	/** already instanced classes */
	private final static Map<String, Object> Singletons = new HashMap<String, Object>();;

	private Singleton() {
	}

	/**
	 * Returns the single instance of a class.
	 * @param <T> 
	 * @param classInstance wanted class 
	 * @return single object instance
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> classInstance)
			throws ClassNotFoundException {
		T result = null;
		String className = classInstance.getName();
		LOCK.lock();
		if (Singletons.containsKey(className)) {
			result = (T) Singletons.get(className);
		} else {
			try {
				result = (T) Class.forName(className).newInstance();
			} catch (Exception e) {
				throw new ClassNotFoundException("Couldn't create instance: "
						+ className);
			}
			Singletons.put(className, result);
		}
		LOCK.unlock();
		return result;
	}
	
	/**
	 * Checks whether a class was already instanced or not.
	 * @param <T> Classes type parameter.
	 * @param classInstance checked class
	 * @return true if already instanced, false else
	 */
	public static <T> boolean hasInstance(Class<T> classInstance) {
		if (Singletons.containsKey(classInstance.getName())) {
			return true;
		}
		return false;

	}
}