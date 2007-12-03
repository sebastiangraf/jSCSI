package org.jscsi.target.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.task.abstracts.TaskDescriptor;

public class FileSystemClassLoader extends URLClassLoader {

	/** The Log interface. */
	private static final Log LOGGER = LogFactory
			.getLog(FileSystemClassLoader.class);

	private FileSystemClassLoader(URL[] urls) {
		super(urls);
	}
	
	

	private static List<URL> getSystemClassPaths() {
		List<URL> classPaths = new ArrayList<URL>();
		for (String path : System.getProperty("java.class.path", ".")
				.split(";")) {
			try {
				classPaths.add(new File(path).toURI().toURL());
			} catch (MalformedURLException e) {
				logDebug("Couldn't create URL of String \"" + path + "\": "
						+ e.getMessage());
			}
		}
		
		return classPaths;
	}

	private boolean isSubdirectory(String directory, String subdirectory) {
		if (directory.length() > subdirectory.length()) {
			return false;
		}
		String equalTest = subdirectory.substring(0, directory.length());
		if (directory.compareTo(equalTest) == 0) {
			return true;
		}
		return false;
	}

	private String classPathToName(File classFile) {
		URL classURL = null;
		try {
			classURL = classFile.toURI().toURL();
		} catch (MalformedURLException e) {
			logDebug("Couldn't create URL of String \"" + classFile.getPath()
					+ "\": " + e.getMessage());
		}
		Iterator<URL> files = getSystemClassPaths().iterator();
		URL testedFile = null;
		String result = null;
		// if sub-directory of classPath, return package name
		while (files.hasNext()) {
			testedFile = files.next();
			if (isSubdirectory(testedFile.getPath(), classURL.getPath())) {
				result = classURL.getPath().substring(
						testedFile.getPath().length(),
						classURL.getPath().length());
				result = result.replace(".class", "");
				result = result.replace("/", ".");
				return result;
			}
		}
		// return className
		result = classURL.getPath().substring(
				classURL.getPath().lastIndexOf("/") + 1,
				classURL.getPath().length());
		result = result.substring(0, result.lastIndexOf("."));
		return result;
	}

	public Class<?> loadClass(File location) throws MalformedURLException,
			ClassNotFoundException {
		Class<?> loadedClass = null;
		// add URL to the URLClassLoader
		this.addURL(location.toURI().toURL());
		// create className that will actually work
		String className = classPathToName(location);
		loadedClass = super.loadClass(className);
		return loadedClass;
	}

	public TaskDescriptor loadTaskDescriptor(File location){

		TaskDescriptor result = null;
		try {
			result = (TaskDescriptor) loadClass(location).newInstance();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static FileSystemClassLoader newInstance() {
		Object[] objectURLs = getSystemClassPaths().toArray();
		URL[] urls = new URL[objectURLs.length];
		for (int i = 0; i < urls.length; i++) {
			urls[i] = (URL) objectURLs[i];
		}
		return new FileSystemClassLoader(urls);
	}

	public static FileSystemClassLoader newInstance(URL[] urls) {
		return new FileSystemClassLoader(urls);
	}

	/**
	 * Logs a trace Message, if trace log is enabled within the logging
	 * environment.
	 * 
	 * @param logMessage
	 */
	private static void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(" Message: " + logMessage);
		}
	}

	/**
	 * Logs a debug Message, if debug log is enabled within the logging
	 * environment.
	 * 
	 * @param logMessage
	 */
	private static void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.trace(" Message: " + logMessage);

		}
	}

}
