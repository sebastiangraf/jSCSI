package org.jscsi.target;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This osgi bundle activator
 * is used to start a jscsi target within an
 * osgi environment.
 * 
 * @author Andreas Rain, University of Konstanz
 * 
 */
public class Activator implements BundleActivator {

    private ExecutorService runner;
    private TargetServer target;
    
    @Override
    public void start(BundleContext context) throws Exception {
        // The osgi container or system properties have to supply
        // a property 'jscsi_target-published_ip', to determine
        // which ip address should be used.
        System.out.println("Starting up jscsi bundle");
        String pIp = context.getProperty("jscsi_target-published_ip");
        InetAddress addr = InetAddress.getByName(pIp);

        if (addr == null)
            throw new IllegalArgumentException(
                "Either the address provided with 'jscsi_target-published_ip' is illegal or not contained within the available network interfaces.");
      
        File schemaFile = new File("jscsi-target.xsd");
        
        if(schemaFile.exists() == false){
            Files.copy(context.getBundle().getResource("/jscsi-target.xsd").openStream(), schemaFile.toPath());
        }
        
        System.out.println("Schemafile: " + ((schemaFile.exists())? "exists" : "does not exist"));
        
        File configFile = new File("jscsi-target.xml");
        if(configFile.exists() == false){
            Files.copy(context.getBundle().getResource("/jscsi-target.xml").openStream(), configFile.toPath());
        }
        
        System.out.println("Configfile: " + ((configFile.exists())? "exists" : "does not exist"));

        target = new TargetServer(Configuration.create(schemaFile, configFile, addr.getHostAddress()));

        runner = Executors.newSingleThreadExecutor();
        runner.submit(target);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Need to provide a shutdown method within the jscsi target
        runner.shutdown();
        System.exit(-1);
    }

}
