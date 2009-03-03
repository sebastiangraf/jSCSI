/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * OperationalTextKeyException.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser.exception;

/**
 * <h1>OperationalTextKeyException</h1>
 * <p/>
 * This exception is thrown when an error with an
 * <code>OperationalTextKey</code> occurs.
 * 
 * @author Volker Wildi
 */
public class OperationalTextKeyException extends InternetSCSIException {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The Serial Version ID. */
  private static final long serialVersionUID = 1L;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructs a new exception with null as its detail message.
   */
  public OperationalTextKeyException() {

    super();
  }

  /**
   * Constructs a new exception with the specified detail message.
   * 
   * @param msg
   *          the detail message. The detail message is saved for later
   *          retrieval by the Throwable.getMessage() method.
   */
  public OperationalTextKeyException(final String msg) {

    super(msg);
  }

  /**
   * Constructs a new exception with the specified exception.
   * 
   * @param e
   *          the detail exception.
   */
  public OperationalTextKeyException(final Exception e) {

    super(e);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
}
