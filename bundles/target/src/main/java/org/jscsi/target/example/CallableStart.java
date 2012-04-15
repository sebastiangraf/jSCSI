package org.jscsi.target.example;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.jscsi.target.Configuration;
import org.jscsi.target.TargetServer;
import org.xml.sax.SAXException;

public class CallableStart {

    public static void main(String[] args) throws SAXException, ParserConfigurationException, IOException {
        // Getting the config path
        final File configFile = Configuration.CONFIGURATION_CONFIG_FILE;
        // Creating the Configuration
        final Configuration config =
            Configuration.create(Configuration.CONFIGURATION_SCHEMA_FILE, configFile);
        // Starting the Target
        final TargetServer target = new TargetServer(config);

        // Getting an Executor
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        // Starting the target
        threadPool.submit(target);

    }

}
