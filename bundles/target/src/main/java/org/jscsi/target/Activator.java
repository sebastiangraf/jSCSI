package org.jscsi.target;

import java.net.InetAddress;

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

    private TargetServer target;
    
    @Override
    public void start(BundleContext context) throws Exception {
        // The osgi container or system properties have to supply
        // a property 'jscsi_target-published_ip', to determine
        // which ip address should be used.
        String pIp = context.getProperty("jscsi_target-published_ip");
        InetAddress addr = InetAddress.getByName(pIp);

        if (addr == null)
            throw new IllegalArgumentException(
                "Either the address provided with 'jscsi_target-published_ip' is illegal or not contained within the available network interfaces.");

        target = new TargetServer(Configuration.create(addr.getHostAddress()));

        target.call();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Need to provide a shutdown method within the jscsi target
        System.exit(-1);
    }

}
