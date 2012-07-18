/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * 
 */
package org.jscsi.initiator.example;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jscsi.exception.ConfigurationException;
import org.jscsi.exception.NoSuchSessionException;
import org.jscsi.exception.TaskExecutionException;
import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.Initiator;

/**
 * Example 3, Reading and Writing data multi-threaded to multiple targets.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class MultiThreadedReadWrite {
    public static void main(final String[] args) throws NoSuchSessionException, TaskExecutionException,
        ConfigurationException, InterruptedException, ExecutionException {
        // init of test structures
        int numBlocks = 50;
        int address = 12345;
        final ByteBuffer writeData1 = ByteBuffer.allocate(512 * numBlocks);
        final ByteBuffer readData1 = ByteBuffer.allocate(512 * numBlocks);
        final ByteBuffer writeData2 = ByteBuffer.allocate(512 * numBlocks);
        final ByteBuffer readData2 = ByteBuffer.allocate(512 * numBlocks);
        Random random = new Random(System.currentTimeMillis());
        random.nextBytes(writeData1.array());
        random.nextBytes(writeData2.array());

        // init of initiator and the session
        String target1 = "testing-xen2-disk1";
        String target2 = "testing-xen2-disk2";
        Initiator initiator = new Initiator(Configuration.create());
        initiator.createSession(target1);
        initiator.createSession(target2);

        // writing the first target multithreaded
        final Future<Void> write1 =
            initiator.multiThreadedWrite(target1, writeData1, address, writeData1.capacity());
        // writing the second target multithreaded
        final Future<Void> write2 =
            initiator.multiThreadedWrite(target2, writeData2, address, writeData2.capacity());

        // Blocking until writes are concluded
        write1.get();
        write2.get();

        // Getting the data from the first target multithreaded
        final Future<Void> read1 =
            initiator.multiThreadedRead(target1, readData1, address, readData1.capacity());
        // Getting the data from the second target multithreaded
        final Future<Void> read2 =
            initiator.multiThreadedRead(target2, readData2, address, readData2.capacity());

        // Blocking until reads are concluded
        read1.get();
        read2.get();

        // closing the targets
        initiator.closeSession(target1);
        initiator.closeSession(target2);

        // correctness check
        if (!Arrays.equals(writeData1.array(), readData1.array())
            || !Arrays.equals(writeData2.array(), readData2.array())) {
            throw new IllegalStateException("Data read must be equal to the data written");
        }
    }
}
