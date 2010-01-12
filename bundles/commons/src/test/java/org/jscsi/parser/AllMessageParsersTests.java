/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * AllMessageParsersTests.java 2500 2007-03-05 13:29:08Z lemke $
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
