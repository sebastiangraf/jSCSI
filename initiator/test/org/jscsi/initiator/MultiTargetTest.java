
package org.jscsi.initiator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.Future;

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

public class MultiTargetTest {

  @Test
  @Bench
  public void testMultiThreaded() {

    try {

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

      System.out
          .println("Buffer Size Test write1 t1: " + writeData.remaining());
      final Future<Void> write1 = initiator.multiThreadedWrite(this, target1,
          writeData, address, writeData.capacity());
      System.out.println("Buffer Size Test read1 t1: " + readData.remaining());
      final Future<Void> read1 = initiator.multiThreadedRead(this, target1,
          readData, address, readData.capacity());
      System.out.println("Buffer Size test write1 t2: "
          + writeData2.remaining());
      final Future<Void> write2 = initiator.multiThreadedWrite(this, target2,
          writeData2, address, writeData2.capacity());
      System.out.println("Buffer Size Test read1 t2: " + readData2.remaining());
      final Future<Void> read2 = initiator.multiThreadedRead(this, target2,
          readData2, address, readData2.capacity());

      write1.get();
      read1.get();
      write2.get();
      read2.get();

      assertEquals(writeData, readData);
      assertEquals(writeData2, readData2);

      readData.clear();
      writeData2.clear();

      final Future<Void> write3 = initiator.multiThreadedWrite(this, target2,
          writeData2, address, writeData2.capacity());
      final Future<Void> read3 = initiator.multiThreadedRead(this, target1,
          readData, address, readData.capacity());
      write3.get();
      read3.get();
      initiator.closeSession(target1);
      initiator.closeSession(target2);

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }

  }

  @Test
  @Bench
  public void testSingleThreaded() {

    try {

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

      System.out
          .println("Buffer Size Test write1 t1: " + writeData.remaining());
      initiator.write(this, target1, writeData, address, writeData.capacity());
      System.out.println("Buffer Size Test read1 t1: " + readData.remaining());
      initiator.read(this, target1, readData, address, readData.capacity());
      System.out.println("Buffer Size test write1 t2: "
          + writeData2.remaining());
      initiator
          .write(this, target2, writeData2, address, writeData2.capacity());
      System.out.println("Buffer Size Test read1 t2: " + readData2.remaining());
      initiator.read(this, target2, readData2, address, readData2.capacity());

      writeData.clear();
      readData.clear();
      writeData2.clear();
      readData2.clear();

      System.out.println("Buffer Size Test write2 t2: "
          + writeData2.remaining());
      initiator
          .write(this, target2, writeData2, address, writeData2.capacity());
      System.out.println("Buffer Size Test read2 t1: " + readData.remaining());
      initiator.read(this, target1, readData, address, readData.capacity());
      initiator.closeSession(target1);
      initiator.closeSession(target2);
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
      final TabularSummaryOutput table = new TabularSummaryOutput();
      table.visitBenchmark(r);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
