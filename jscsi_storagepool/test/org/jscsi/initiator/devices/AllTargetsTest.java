/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * JSCSIDeviceTest.java 2641 2007-04-10 09:46:28Z lemke $
 */

package org.jscsi.initiator.devices;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.junit.Test;

/**
 * Tests, if all targets are available.
 * 
 * @author Bastian Lemke
 */
public class AllTargetsTest {

  private static final String[] TARGET_NAMES = { "testing-xen2-disk1",
      "testing-xen2-disk2"/*
                           * , "testing-xen1-disk3" , "testing-xen1-disk1",
                           * "testing-xen1-disk2"
                           */};

  /**
   * Number of Blocks to write.
   * 
   * @throws Exception
   */

  @Test
  public final void test() throws Exception {

    final Hashtable<String, List<Exception>> exc = new Hashtable<String, List<Exception>>();

    for (String target : TARGET_NAMES) {
      // System.out.print(target);
      exc.put(target, new ArrayList<Exception>());
      JSCSIDevice device = null;
      try {
        device = new JSCSIDevice(target);
      } catch (Exception e) {
        exc.get(target).add(e);
      }
      try {
        device.open();
      } catch (Exception e) {
        exc.get(target).add(e);
      }
      // System.out.println(" (" + device.getName() + ") --- OK");
      try {
        device.close();
      } catch (Exception e) {
        exc.get(target).add(e);
      }
    }

    boolean failed = false;
    for (final String target : exc.keySet()) {
      if (!exc.get(target).isEmpty()) {
        exc.get(target).get(0).printStackTrace();
        failed = true;
      }
    }
    if (failed) {
      fail();
    }

  }
}
