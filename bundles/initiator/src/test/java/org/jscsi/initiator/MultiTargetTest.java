/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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

package org.jscsi.initiator;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;
import org.perfidix.Benchmark;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

/**
 * Simple test, trying multiple Sessions to different Targets.
 * 
 * @author Patrice
 */
@Ignore("Lack of testembed, removing")
public class MultiTargetTest {

    @Test
    @Bench
    public void testMultiThreaded() throws Exception {

        int numBlocks = 50;
        int address = 12345;
        String target1 = "testing-xen2-disk1";
        String target2 = "testing-xen2-disk2";
        final ByteBuffer writeData = ByteBuffer.allocate(512 * numBlocks);
        final ByteBuffer readData = ByteBuffer.allocate(512 * numBlocks);
        final ByteBuffer writeData2 = ByteBuffer.allocate(512 * numBlocks);
        final ByteBuffer readData2 = ByteBuffer.allocate(512 * numBlocks);
        Random random = new Random(System.currentTimeMillis());
        random.nextBytes(writeData.array());
        random.nextBytes(writeData2.array());

        Initiator initiator = new Initiator(Configuration.create());
        initiator.createSession(target1);
        initiator.createSession(target2);

        System.out.println("Buffer Size Test write1 t1: " + writeData.remaining());
        final Future<Void> write1 =
            initiator.multiThreadedWrite(target1, writeData, address, writeData.capacity());
        System.out.println("Buffer Size Test read1 t1: " + readData.remaining());
        final Future<Void> read1 =
            initiator.multiThreadedRead(target1, readData, address, readData.capacity());
        System.out.println("Buffer Size test write1 t2: " + writeData2.remaining());
        final Future<Void> write2 =
            initiator.multiThreadedWrite(target2, writeData2, address, writeData2.capacity());
        System.out.println("Buffer Size Test read1 t2: " + readData2.remaining());
        final Future<Void> read2 =
            initiator.multiThreadedRead(target2, readData2, address, readData2.capacity());

        write1.get();
        read1.get();
        write2.get();
        read2.get();

        assertEquals(writeData, readData);
        assertEquals(writeData2, readData2);

        readData.clear();
        writeData2.clear();

        final Future<Void> write3 =
            initiator.multiThreadedWrite(target2, writeData2, address, writeData2.capacity());
        final Future<Void> read3 =
            initiator.multiThreadedRead(target1, readData, address, readData.capacity());
        write3.get();
        read3.get();
        initiator.closeSession(target1);
        initiator.closeSession(target2);

    }

    @Test
    @Bench
    public void testSingleThreaded() throws Exception {

        int numBlocks = 500;
        int address = 12345;
        String target1 = "testing-xen2-disk1";
        String target2 = "testing-xen2-disk2";
        final ByteBuffer writeData = ByteBuffer.allocate(512 * numBlocks);
        final ByteBuffer readData = ByteBuffer.allocate(512 * numBlocks);
        final ByteBuffer writeData2 = ByteBuffer.allocate(512 * numBlocks);
        final ByteBuffer readData2 = ByteBuffer.allocate(512 * numBlocks);
        Random random = new Random(System.currentTimeMillis());
        random.nextBytes(writeData.array());
        random.nextBytes(writeData2.array());

        Initiator initiator = new Initiator(Configuration.create());
        initiator.createSession(target1);
        initiator.createSession(target2);

        System.out.println("Buffer Size Test write1 t1: " + writeData.remaining());
        initiator.write(target1, writeData, address, writeData.capacity());
        System.out.println("Buffer Size Test read1 t1: " + readData.remaining());
        initiator.read(target1, readData, address, readData.capacity());
        System.out.println("Buffer Size test write1 t2: " + writeData2.remaining());
        initiator.write(target2, writeData2, address, writeData2.capacity());
        System.out.println("Buffer Size Test read1 t2: " + readData2.remaining());
        initiator.read(target2, readData2, address, readData2.capacity());

        writeData.clear();
        readData.clear();
        writeData2.clear();
        readData2.clear();

        System.out.println("Buffer Size Test write2 t2: " + writeData2.remaining());
        initiator.write(target2, writeData2, address, writeData2.capacity());
        System.out.println("Buffer Size Test read2 t1: " + readData.remaining());
        initiator.read(target1, readData, address, readData.capacity());
        initiator.closeSession(target1);
        initiator.closeSession(target2);
    }

    public static void main(String args[]) {

        try {

            final Benchmark bench = new Benchmark();

            bench.add(InitiatorBug.class);
            final BenchmarkResult r = bench.run();
            final TabularSummaryOutput table = new TabularSummaryOutput();
            table.visitBenchmark(r);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
