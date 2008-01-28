//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information: 
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

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

   public Thread newThread(Runnable target)
   {
      String tname = this.name + ":" + this.numThreads.incrementAndGet();
      _logger.debug("spawning new thread: " + tname);
      Thread t = new Thread(target, tname);
      t.setPriority(this.priority);
      return t;
   }
}
