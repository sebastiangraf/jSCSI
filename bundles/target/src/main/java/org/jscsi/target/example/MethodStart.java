package org.jscsi.target.example;

import java.io.File;

import org.jscsi.target.Configuration;
import org.jscsi.target.TargetServer;

public class MethodStart {

    private static File CONFIGPATH = Configuration.CONFIGURATION_CONFIG_FILE;

    public static void main(final String[] args) throws Exception {
        // Getting the config path
        final File configFile = CONFIGPATH;
        // Creating the Configuration
        final Configuration config =
            Configuration.create(Configuration.CONFIGURATION_SCHEMA_FILE, configFile);
        // Starting the Target
        final TargetServer target = new TargetServer(config);
        target.call();
    }

}
