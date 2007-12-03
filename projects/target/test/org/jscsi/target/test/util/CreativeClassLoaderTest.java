package org.jscsi.target.test.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jscsi.target.util.CreativeClassLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreativeClassLoaderTest {

	private static CreativeClassLoader loader;

	private static URL additionalClassPath;

	private static Set<Class<?>> loadedClasses;

	@Before
	public void setUp() {
		loader = CreativeClassLoader.getInstance();
		try {
			additionalClassPath = new File(
					"misc/testTaskClassFiles/").toURI()
					.toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		loadedClasses = new HashSet<Class<?>>();
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testConversionMethods() {
		File location = null;
		try {
			location = new File(additionalClassPath.toURI());
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(URL url : CreativeClassLoader.getSystemClassPaths()){
			System.out.println(url);
		}
		System.out.println(location.isDirectory());
		System.out.println(location.toString());
		loader.loadAllClasses(loadedClasses, location, true);
		for(Class<?> loadedClass : loadedClasses){
			System.out.println("loaded" + loadedClass.getName());
			try {
				loadedClass.newInstance();
			} catch (InstantiationException e) {
				System.out.println(e.getMessage());
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
