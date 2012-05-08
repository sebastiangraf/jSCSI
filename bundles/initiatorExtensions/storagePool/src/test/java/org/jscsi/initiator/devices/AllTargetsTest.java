/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
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
package org.jscsi.initiator.devices;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests, if all targets are available.
 * 
 * @author Bastian Lemke
 */
@Ignore("Lack of testembed, removing")
public class AllTargetsTest {

    private static final String[] TARGET_NAMES = { "testing-xen2-disk1",
            "testing-xen2-disk2"/*
                                 * , "testing-xen1-disk3" ,
                                 * "testing-xen1-disk1", "testing-xen1-disk2"
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
