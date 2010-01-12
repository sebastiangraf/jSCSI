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
 * accept more input". This Bug only apears on Gbit Lines.
 * 
 * @author Bastian Lemke
 */
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

      initiator.write(this, target, writeData, address, writeData.capacity());
      initiator.read(this, target, readData, address, readData.capacity());
      initiator.closeSession(target);
      while (!initiator.noSessions()) {
        Thread.sleep(7);
      }
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