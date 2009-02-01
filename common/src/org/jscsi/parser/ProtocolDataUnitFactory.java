/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * ProtocolDataUnitFactory.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.parser;

import org.jscsi.parser.digest.DigestFactory;

/**
 * <h1>ProtocolDataUnitFactory</h1> <p/> A factory to create all supported the
 * <code>ProtocolDataUnit</code> instances.
 * 
 * @author Volker Wildi
 */
public final class ProtocolDataUnitFactory {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The factory to create the supported digests. */
  private final DigestFactory digestFactory = new DigestFactory();

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default constructor to create a new, empty
   * <code>ProtocolDataUnitFactory</code> instance.
   */
  public ProtocolDataUnitFactory() {

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method creates a <code>ProtocolDataUnit</code> instance, which
   * initializes only the digests to use, and returns it.
   * 
   * @param headerDigest
   *          The name of the digest to use for the protection of the Basic
   *          Header Segment.
   * @param dataDigest
   *          The name of the digest to use for the protection of the Data
   *          Segment.
   * @return A new <code>ProtocolDataUnit</code> instance.
   */
  public final ProtocolDataUnit create(final String headerDigest,
      final String dataDigest) {

    return new ProtocolDataUnit(digestFactory.create(headerDigest),
        digestFactory.create(dataDigest));
  }

  /**
   * This method creates a <code>ProtocolDataUnit</code> instance, which is
   * initialized with the given settings, and returns it.
   * 
   * @param immediateFlag
   *          Should this PDU send immediately?
   * @param operationCode
   *          The Operation Code of this PDU.
   * @param finalFlag
   *          Is this PDU the last one in a sequence?
   * @param headerDigest
   *          The name of the digest to use for the protection of the Basic
   *          Header Segment.
   * @param dataDigest
   *          The name of the digest to use for the protection of the Data
   *          Segment.
   * @return A new <code>ProtocolDataUnit</code> instance.
   */
  public final ProtocolDataUnit create(final boolean immediateFlag,
      final boolean finalFlag, final OperationCode operationCode,
      final String headerDigest, final String dataDigest) {

    final ProtocolDataUnit protocolDataUnit = new ProtocolDataUnit(
        digestFactory.create(headerDigest), digestFactory.create(dataDigest));

    protocolDataUnit.getBasicHeaderSegment().setImmediate(immediateFlag);
    protocolDataUnit.getBasicHeaderSegment().setFinal(finalFlag);
    protocolDataUnit.getBasicHeaderSegment().setOperationCode(protocolDataUnit,
        operationCode);

    return protocolDataUnit;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
