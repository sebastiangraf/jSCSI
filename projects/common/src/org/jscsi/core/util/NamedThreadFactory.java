package org.jscsi.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class NamedThreadFactory implements ThreadFactory
{
   private static Logger _logger = Logger.getLogger(NamedThreadFactory.class);

   protected String name;
   protected int priority;
   protected AtomicInteger numThreads;

   
   /////////////////////////////////////////////////////////////////////////////
   // constructors
   
   public NamedThreadFactory(String name)
   {
      this(name, Thread.NORM_PRIORITY);
   }
   
   public NamedThreadFactory(String name, int priority)
   {
      this.name = name;
      this.priority = priority;

      this.numThreads = new AtomicInteger();
   }
   
   
   /////////////////////////////////////////////////////////////////////////////
   // operations
   
   @Override
   public Thread newThread(Runnable target)
   {
      String tname = this.name + ":" + this.numThreads.incrementAndGet();
      Thread t = new Thread(target, tname);
      t.setPriority(this.priority);
      return t;
   }
}
