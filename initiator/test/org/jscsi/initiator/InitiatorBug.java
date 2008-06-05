package org.jscsi.initiator;


import java.nio.ByteBuffer;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Simple test, trying to provoke the initiator bug (probably located in
 * connection.SenderWorker). Try running this test more than once and
 * (hopefully) you will get an error that the "Target has no more resources to
 * accept more input".
 * 
 * @author Bastian Lemke
 * 
 */
public class InitiatorBug {

  private void run() throws Exception {
    Logger logger = Logger.getLogger("org.jscsi.connection");
    logger.addAppender(new ConsoleAppender(new PatternLayout(
        "%r [%t] %-5p %c %x - %m\n")));
    logger.setLevel(org.apache.log4j.Level.INFO);

    int numBlocks = 50000;
    int address = 12345;
    String target = "xen1-disk1";
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
    System.out.println("Finished.");
  }

  public static void main(String args[]) {
    try {
      new InitiatorBug().run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}