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
