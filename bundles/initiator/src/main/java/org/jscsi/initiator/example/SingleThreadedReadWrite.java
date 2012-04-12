/**
 * 
 */
package org.jscsi.initiator.example;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import org.jscsi.exception.ConfigurationException;
import org.jscsi.exception.NoSuchSessionException;
import org.jscsi.exception.TaskExecutionException;
import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.Initiator;

/**
 * Example 2, Reading and Writing data single-threaded to one target.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class SingleThreadedReadWrite {

    public static void main(final String[] args) throws NoSuchSessionException, TaskExecutionException,
        ConfigurationException {
        // init of test structures
        int numBlocks = 50;
        int address = 12345;
        ByteBuffer writeData = ByteBuffer.allocate(512 * numBlocks);
        ByteBuffer readData = ByteBuffer.allocate(512 * numBlocks);
        Random random = new Random(System.currentTimeMillis());
        random.nextBytes(writeData.array());

        // init of initiator and the session
        String target = "testing-xen2-disk1";
        Initiator initiator = new Initiator(Configuration.create());
        initiator.createSession(target);

        // writing the data single threaded
        initiator.write(target, writeData, address, writeData.capacity());

        // reading the data single threaded
        initiator.read(target, readData, address, readData.capacity());

        // closing the session
        initiator.closeSession(target);

        // correctness check
        if (!Arrays.equals(writeData.array(), readData.array())) {
            throw new IllegalStateException("Data read must be equal to the data written");
        }
    }

}
