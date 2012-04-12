/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.perfidix.Benchmark;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

/**
 * Simple test, trying to provoke the initiator bug (probably located in
 * connection.SenderWorker). Try running this test more than once and
 * (hopefully) you will get an error that the "Target has no more resources to
 * accept more input". This Bug only apears on Gbit Lines.
 * 
 * @author Bastian Lemke
 */
@Ignore("Lack of testembed, removing")
public class InitiatorBug {

    @Test
    @Bench
    public void test() {

        try {
            int numBlocks = 50;
            int address = 12345;
            String target = "testing-xen2-disk1";
            ByteBuffer writeData = ByteBuffer.allocate(512 * numBlocks);
            ByteBuffer readData = ByteBuffer.allocate(512 * numBlocks);
            Random random = new Random(System.currentTimeMillis());

            // for (int i = 0; i < 15; i++) {

            random.nextBytes(writeData.array());

            Initiator initiator = new Initiator(Configuration.create());
            initiator.createSession(target);

            initiator.write(this, target, writeData, address,
                    writeData.capacity());
            initiator
                    .read(this, target, readData, address, readData.capacity());
            initiator.closeSession(target);
            while (!initiator.noSessions()) {
                Thread.sleep(7);
            }
            // System.out.println("Finished." + i);
            // }
            if(!Arrays.equals(writeData.array(),readData.array())){
                throw new IllegalStateException("Data read must be equal to the data written");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    public static void main(String args[]) {

        try {

            final Benchmark bench = new Benchmark();

            bench.add(InitiatorBug.class);
            final BenchmarkResult r = bench.run();
            final TabularSummaryOutput tab = new TabularSummaryOutput();
            tab.visitBenchmark(r);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}