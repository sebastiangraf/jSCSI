/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * IResultFunction.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.parser.datasegment;

/**
 * <h1>IResultFunction</h1> <p/> This interface represents the result function
 * of an iSCSI parameter. Each such result function must implement this
 * interface.
 * 
 * @author Volker Wildi
 */
public interface IResultFunction {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method represents the result function.
   * 
   * @param a
   *          The value of the request.
   * @param b
   *          The value of the response.
   * @return The result of these both parameters.
   */
  public String result(final String a, final String b);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
