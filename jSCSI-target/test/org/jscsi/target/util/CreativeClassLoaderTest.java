package org.jscsi.target.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jscsi.target.Target;
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
					"/test/org/jscsi/target/util/tempClassLoaderTestFiles/").toURI()
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
		for(URL url : loader.getSystemClassPaths()){
			System.out.println(url);
		}
		System.out.println(location.isDirectory());
		loader.loadAllClasses(loadedClasses, location, true);

	}

}
