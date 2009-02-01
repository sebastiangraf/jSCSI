
package org.jscsi.initiator;

import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;
import org.perfidix.Benchmark;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

/**
 * Simple test, trying to provoke the initiator bug (probably located in
 * connection.SenderWorker). Try running this test more than once and
 * (hopefully) you will get an error that the "Target has no more resources to
 * accept more input".
 * 
 * @author Patrice
 */

public class MultiConnectionTest {

  @Test
  @Bench
  public void test() {

    try {
      // for (int i = 0; i < 15; i++) {

      int numBlocks = 50;
      int address = 12345;
      String target = "testing-xen2-disk2";
      ByteBuffer writeData = ByteBuffer.allocate(512 * numBlocks);
      ByteBuffer readData = ByteBuffer.allocate(512 * numBlocks);
      Random random = new Random(System.currentTimeMillis());
      random.nextBytes(writeData.array());

      Initiator initiator = new Initiator(Configuration.create());
      initiator.createSession(target);

      initiator.write(this, target, writeData, address, writeData.capacity());
      // Thread.sleep(1000);
      initiator.read(this, target, readData, address, writeData.capacity());
      initiator.closeSession(target);
      // System.out.println("Finished." + i);
      // }
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
