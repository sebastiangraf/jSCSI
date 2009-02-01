/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.jscsi.initiator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perfidix.Benchmark;
import org.perfidix.annotation.BenchClass;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

/**
 * <h1>InitBench</h1>
 * <p/>
 * This class is a benchmark to measure the performance of the Java implemented
 * iSCSI Initiator. It only measures the performance of opening and closing a
 * connection.
 * 
 * @author Patrice Brend'amour
 */
@BenchClass(runs = 100)
public class OpenCloseBench {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private static final Log LOGGER = LogFactory.getLog(OpenCloseBench.class);

  /** Name of the device name on the iSCSI Target. */
  private static final String TARGET_NAME = "testing-xen2-disk1";

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The <code>Initiator</code> instance to use for measurements. */
  private final Initiator initiator;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private long loopCounter;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>InitiatorBench</code> instance.
   * 
   * @throws Exception
   *           if any error occurs.
   */
  public OpenCloseBench() throws Exception {

    initiator = new Initiator(Configuration.create());
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public final void openclose() {

    try {
      loopCounter++;
      initiator.createSession(TARGET_NAME);
      LOGGER.debug("Open the " + loopCounter + "th session.");
      initiator.closeSession(TARGET_NAME);
      LOGGER.debug("Closed the " + loopCounter + "th session.");
    } catch (final Exception e) {
      e.printStackTrace();
    }

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public static void main(String[] args) throws Exception {

    try {
      final Benchmark bench = new Benchmark();

      bench.add(OpenCloseBench.class);
      final BenchmarkResult r = bench.run();
      final TabularSummaryOutput output = new TabularSummaryOutput();
      output.visitBenchmark(r);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
