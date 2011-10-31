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
package org.jscsi.parser;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.jscsi.parser.data.DataInParserTest;
import org.jscsi.parser.data.DataOutParserTest;
import org.jscsi.parser.login.ISIDTest;
import org.jscsi.parser.login.LoginRequestParserTest;
import org.jscsi.parser.login.LoginResponseParserTest;
import org.jscsi.parser.logout.LogoutRequestParserTest;
import org.jscsi.parser.logout.LogoutResponseParserTest;
import org.jscsi.parser.r2t.Ready2TransferParserTest;
import org.jscsi.parser.reject.RejectParserTest;
import org.jscsi.parser.scsi.SCSICommandParserTest;
import org.jscsi.parser.snack.SNACKRequestParserTest;
import org.jscsi.parser.text.TextRequestParserTest;
import org.jscsi.parser.text.TextResponseParserTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { AdditionalHeaderSegmentTest.class, DataInParserTest.class,
    DataOutParserTest.class, ISIDTest.class, LoginRequestParserTest.class,
    LoginResponseParserTest.class, LogoutRequestParserTest.class,
    LogoutResponseParserTest.class, Ready2TransferParserTest.class,
    RejectParserTest.class, SCSICommandParserTest.class,
    SNACKRequestParserTest.class, TextRequestParserTest.class,
    TextResponseParserTest.class })
public final class AllMessageParsersTests {

  public AllMessageParsersTests() {

  }

  // suite.addTestSuite(AsynchronousMessageParserTest.class);
  // suite.addTestSuite(TestSCSIDataSegment.class);
  // suite.addTestSuite(TestSCSIResponseParser.class);
  // suite.addTestSuite(TaskManagementFunctionRequestTest.class);
  // suite.addTestSuite(TaskManagementFunctionResponseTest.class);

  /**
   * @param args
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {

    return new JUnit4TestAdapter(AllMessageParsersTests.class);
  }
}
