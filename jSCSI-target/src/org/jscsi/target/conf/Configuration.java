package org.jscsi.target.conf;

import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.Session;

public class Configuration {

	/**
	 * The relative path (to the project) of the main directory of all
	 * configuration files.
	 */
	public static final String CONFIG_DIR = "conf/";

	/**
	 * The file name of the XML Schema configuration file for the global
	 * settings.
	 */
	public static final String CONFIGURATION_SCHEMA_FILE = CONFIG_DIR
			+ "iscsi.xsd";

	/** The file name, which contains all global settings. */
	public static final String CONFIGURATION_CONFIG_FILE = CONFIG_DIR
			+ "iscsi.xml";

	public static final String TARGET_PORTAL_GROUP_TAG = "TargetPortalGroupTag";
	
	public static final String TARGET_PORT = "TargetPort";
	
	public static final String TARGET_NAME = "TargetName";
	
	public static final String TARGET_ADRESS = "TargetAdress";
	

	public static final Configuration create() {
		// load configuration file from standard folder
		return null;
	}

	public static final Configuration create(final String configSchemaFileName,
			final String configFileName) throws Exception {
		// load folder from specified directory
		return null;
	}

	public final void addNewConnectionSettings(Connection connection) {

	}

	public final void addNewSessionSettings(Session session) {

	}

	public final void getSessionSettings(Session session) {

	}

	public final void getConnectionSettings(Connection connection) {

	}

	public final void getGlobalSettings() {

	}

	public final void removeConnectionSettings(Connection connection) {

	}

	public final void removeSessionSettings(Session session) {

	}

	private void parseGlobalSettings() {

	}

}
