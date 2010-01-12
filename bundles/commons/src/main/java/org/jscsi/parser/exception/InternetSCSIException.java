/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * InternetSCSIException.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser.exception;

/**
 * This is a base class for the exception handling at the parsing process of the
 * iSCSI protocol.
 * 
 * @author Volker Wildi
 */
public class InternetSCSIException extends Exception {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The Serial Version ID. */
  private static final long serialVersionUID = 1L;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructs a new exception with null as its detail message.
   */
  public InternetSCSIException() {

    super();
  }

  /**
   * Constructs a new exception with the specified detail message.
   * 
   * @param msg
   *          the detail message. The detail message is saved for later
   *          retrieval by the Throwable.getMessage() method.
   */
  public InternetSCSIException(final String msg) {

    super(msg);
  }

  /**
   * Constructs a new exception as a wrapper for a given exception.
   * 
   * @param e
   *          the exception to be wrapped
   */
  public InternetSCSIException(final Exception e) {

    super(e);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
