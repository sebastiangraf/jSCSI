/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
package org.jscsi.parser.login;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.Constants;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * <h1>LoginRequestParser</h1>
 * <p>
 * This class parses a Login Request message defined in the iSCSI Standard (RFC3720).
 * <p>
 * After establishing a TCP connection between an initiator and a target, the initiator MUST start a Login
 * Phase to gain further access to the target’s resources.
 * <p>
 * The Login Phase (see Chapter 5) consists of a sequence of Login Requests and Responses that carry the same
 * Initiator Task Tag.
 * <p>
 * <b>Login Requests are always considered as immediate.</b>
 * <p/>
 * The version number of the current draft is <code>0x00</code>. As such, all devices MUST carry version
 * <code>0x00</code> for both Version-min and Version-max.
 * <p>
 * Here the final flag has the following meaning: If set to <code>1</code>, indicates that the initiator is
 * ready to transit to the next stage.
 * <p>
 * If the <code>T</code> bit is set to <code>1</code> and <code>NSG</code> is <code>FullFeaturePhase</code>,
 * then this also indicates that the initiator is ready for the Final Login Response (see Chapter 5).
 * <p>
 * <h4>T (Transit) Bit</h4> If set to <code>1</code>, indicates that the initiator is ready to transit to the
 * next stage.
 * <p>
 * If the T bit is set to <code>1</code> and NSG is FullFeaturePhase, then this also indicates that the
 * initiator is ready for the Final Login Response (see Chapter 5).
 * <p>
 * <h4>CmdSN</h4> CmdSN is either the initial command sequence number of a session (for the first Login
 * Request of a session - the "leading" login), or the command sequence number in the command stream if the
 * login is for a new connection in an existing session.<br/>
 * Examples:<br/>
 * <ul>
 * <li>Login on a leading connection - if the leading login carries the CmdSN <code>123</code>, all other
 * Login Requests in the same Login Phase carry the CmdSN <code>123</code> and the first non-immediate command
 * in FullFeaturePhase also carries the CmdSN <code>123</code>.</li>
 * <li>Login on other than a leading connection - if the current CmdSN at the time the first login on the
 * connection is issued is <code>500</code>, then that PDU carries <code>CmdSN=500</code>. Subsequent Login
 * Requests that are needed to complete this Login Phase may carry a CmdSN higher than <code>500</code> if
 * non-immediate requests that were issued on other connections in the same session advance CmdSN.</li>
 * </ul>
 * If the Login Request is a leading Login Request, the target MUST use the value presented in CmdSN as the
 * target value for ExpCmdSN.
 * <p>
 * <h4>ExpStatSN</h4> For the first Login Request on a connection this is ExpStatSN for the old connection and
 * this field is only valid if the Login Request restarts a connection (see Section 5.3.4 Connection
 * Reinstatement). For subsequent Login Requests it is used to acknowledge the Login Responses with their
 * increasing StatSN values.
 * <p>
 * <h4>Login Parameters (Data Segment)</h4> The initiator MUST provide some basic parameters in order to
 * enable the target to determine if the initiator may use the target’s resources and the initial text
 * parameters for the security exchange. All the rules specified in Section 10.10.5 Text for text requests
 * also hold for Login Requests. Keys and their explanations are listed in Chapter 11 (security negotiation
 * keys) and Chapter 12 (operational parameter negotiation keys). All keys in Chapter 12, except for the X
 * extension formats, MUST be supported by iSCSI initiators and targets. Keys in Chapter 11 only need to be
 * supported when the function to which they refer is mandatory to implement.
 * <p>
 * 
 * @author Volker Wildi
 */
public final class LoginRequestParser extends InitiatorMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Continue Flag. */
    private boolean continueFlag;

    /** The Current stage in the session. */
    private LoginStage currentStageNumber;

    /** The next stage in the session. */
    private LoginStage nextStageNumber;

    /** The maximum version number to support. */
    private byte maxVersion;

    /** The minimum version number to support. */
    private byte minVersion;

    /** The Initiator Session ID (ISID). */
    private ISID initiatorSessionID;

    /** The Target Session Identifying Handle (TSIH). */
    private short targetSessionIdentifyingHandle;

    /** The Connection ID. */
    private int connectionID;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>LoginRequestParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>LoginRequestParser</code> subclass object.
     */
    public LoginRequestParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
        initiatorSessionID = new ISID();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "Continue Flag", continueFlag, 1);
        Utils.printField(sb, "CSG", currentStageNumber.value(), 1);
        Utils.printField(sb, "NSG", nextStageNumber.value(), 1);
        Utils.printField(sb, "minVersion", minVersion, 1);
        Utils.printField(sb, "maxVersion", maxVersion, 1);
        sb.append(initiatorSessionID.toString());
        Utils.printField(sb, "TSIH", targetSessionIdentifyingHandle, 1);
        Utils.printField(sb, "CID", connectionID, 1);
        sb.append(super.toString());

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final DataSegmentFormat getDataSegmentFormat() {

        return DataSegmentFormat.TEXT;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean canHaveDigests() {

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final void clear() {

        super.clear();

        continueFlag = false;

        currentStageNumber = LoginStage.SECURITY_NEGOTIATION;
        nextStageNumber = LoginStage.SECURITY_NEGOTIATION;

        maxVersion = 0x00;
        minVersion = 0x00;

        initiatorSessionID.clear();

        targetSessionIdentifyingHandle = 0x0000;
        connectionID = 0x00000000;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * A unique ID for this connection within the session.
     * <p>
     * All Login Requests within the Login Phase MUST carry the same <code>CID</code>.
     * <p>
     * The target MUST use the value presented with the first Login Request.
     * <p>
     * A Login Request with a non-zero <code>TSIH</code> and a <code>CID</code> equal to that of an existing
     * connection implies a logout of the connection followed by a Login (see Section 5.3.4 Connection
     * Reinstatement). For the details of the implicit Logout Request, see Section 10.14 Logout Request.
     * 
     * @return The Connection ID of this LoginRequestParser object.
     */
    public final int getConnectionID() {

        return connectionID;
    }

    /**
     * When set to <code>1</code>, indicates that the text (set of key=value
     * pairs) in this Login Request is not complete (it will be continued on
     * subsequent Login Requests); otherwise, it indicates that this Login
     * Request ends a set of key=value pairs. A Login Request with the <code>C</code> bit set to
     * <code>1</code> MUST have the <code>T</code> bit
     * set to <code>0</code>.
     * 
     * @return Returns <code>true</code>, if the Continue Bit is set. Else <code>false</code>.
     */
    public final boolean isContinueFlag() {

        return continueFlag;
    }

    /**
     * Returns the <em>Current Stage Number</em> of this Login Request Message.
     * <p>
     * Through these fields, Current Stage (CSG) and Next Stage (NSG), the Login negotiation requests and
     * responses are associated with a specific stage in the session (SecurityNegotiation,
     * LoginOperationalNegotiation, FullFeaturePhase) and may indicate the next stage to which they want to
     * move (see Chapter 5). The next stage value is only valid when the T bit is 1; otherwise, it is
     * reserved.
     * <p>
     * The stage codes are:
     * <ul>
     * <li><code>0</code> - SecurityNegotiation</li>
     * <li><code>1</code> - LoginOperationalNegotiation</li>
     * <li><code>3</code> - FullFeaturePhase</li>
     * </ul>
     * <p>
     * All other codes are reserved.
     * 
     * @return Number of the Current Stage.
     * @see org.jscsi.parser.login.LoginStage
     */
    public final LoginStage getCurrentStageNumber() {

        return currentStageNumber;
    }

    /**
     * Returns the <em>Initiator Session ID (ISID)</em> of this
     * LoginRequestParser object.
     * 
     * @return Returns the <em>Initiator Session ID (ISID)</em> of this <code>LoginRequestParser</code>
     *         object.
     * @see ISID
     */
    public final ISID getInitiatorSessionID() {

        return initiatorSessionID;
    }

    /**
     * Maximum Version number supported.
     * <p>
     * All Login Requests within the Login Phase MUST carry the same Version-max.
     * <p>
     * The target MUST use the value presented with the first Login Request.
     * 
     * @return The maximum version of this login request message.
     */
    public final byte getMaxVersion() {

        return maxVersion;
    }

    /**
     * All Login Requests within the Login Phase MUST carry the same
     * Version-min. The target MUST use the value presented with the first Login
     * Request.
     * 
     * @return The minimum version of this login request message.
     */
    public final byte getMinVersion() {

        return minVersion;
    }

    /**
     * Returns the <em>Next Stage Number</em> of this Login Request Message.
     * <p>
     * Through these fields, Current Stage (CSG) and Next Stage (NSG), the Login negotiation requests and
     * responses are associated with a specific stage in the session (SecurityNegotiation,
     * LoginOperationalNegotiation, FullFeaturePhase) and may indicate the next stage to which they want to
     * move (see Chapter 5). The next stage value is only valid when the T bit is 1; otherwise, it is
     * reserved.
     * <p>
     * The stage codes are:
     * <ul>
     * <li><code>0</code> - SecurityNegotiation</li>
     * <li><code>1</code> - LoginOperationalNegotiation</li>
     * <li><code>3</code> - FullFeaturePhase</li>
     * </ul>
     * <p>
     * All other codes are reserved.
     * 
     * @return The Number of the Next Stage.
     * @see org.jscsi.parser.login.LoginStage
     */
    public final LoginStage getNextStageNumber() {

        return nextStageNumber;
    }

    /**
     * <em>Target Session Identifying Handle (TSIH)</em> must be set in the
     * first Login Request. The reserved value <code>0</code> MUST be used on
     * the first connection for a new session. Otherwise, the <em>TSIH</em> sent
     * by the target at the conclusion of the successful login of the first
     * connection for this session MUST be used. The <em>TSIH</em> identifies to
     * the target the associated existing session for this new connection.
     * <p>
     * All Login Requests within a Login Phase MUST carry the same <em>TSIH</em>.
     * <p>
     * The target MUST check the value presented with the first Login Request and act as specified in Section
     * 5.3.1 Login Phase Start.
     * 
     * @return Returns the Target Session Identifying Handle of this
     *         LoginRequestParser object.
     */
    public final short getTargetSessionIdentifyingHandle() {

        return targetSessionIdentifyingHandle;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Sets the new Connection ID of this LoginRequestParser object.
     * 
     * @param initCID
     *            The new Connection ID.
     * @see #getConnectionID()
     */
    public final void setConnectionID(final int initCID) {

        connectionID = initCID;
    }

    /**
     * Sets the new state of the <em>Continue Flag</em> of this <code>LoginRequestParser</code> obejct.
     * 
     * @param initContinueFlag
     *            The new state of the Continue Flag.
     * @see #isContinueFlag()
     */
    public final void setContinueFlag(final boolean initContinueFlag) {

        continueFlag = initContinueFlag;
    }

    /**
     * Sets the new <em>Current Stage Number</em> of this <code>LoginRequestParser</code> object.
     * 
     * @param initCSG
     *            The new Current Stage Number.
     * @see #getCurrentStageNumber()
     */
    public final void setCurrentStageNumber(final LoginStage initCSG) {

        currentStageNumber = initCSG;
    }

    /**
     * Sets the new <em>Initiator Session ID (ISID)</em> of this <code>LoginRequestParser</code> object.
     * 
     * @param initISID
     *            The new Initiator Session ID (ISID).
     */
    public final void setInitiatorSessionID(final ISID initISID) {

        initiatorSessionID = initISID;
    }

    /**
     * Sets the new <em>Maximum Version number</em> of this <code>LoginRequestParser</code> object.
     * 
     * @param initMaxVersion
     *            The new Maximum Version.
     * @see #getMaxVersion
     */
    public final void setMaxVersion(final byte initMaxVersion) {

        maxVersion = initMaxVersion;
    }

    /**
     * Sets the new <em>Minimum Version number</em> of this <code>LoginRequestParser</code> object.
     * 
     * @param initMinVersion
     *            The new Minimum Version.
     * @see #getMinVersion
     */
    public final void setMinVersion(final byte initMinVersion) {

        minVersion = initMinVersion;
    }

    /**
     * Sets the new <em>Next Stage Number</em> of this <code>LoginRequestParser</code> object.
     * 
     * @param initNSG
     *            The new Next Stage Number.
     * @see #getNextStageNumber()
     */
    public final void setNextStageNumber(final LoginStage initNSG) {

        nextStageNumber = initNSG;
    }

    /**
     * Sets the new <em>Target Session Identifying Handle</em> of this <code>LoginRequestParser</code> object.
     * 
     * @param initTSIH
     *            The new Target Session Identifying Handle.
     */
    public final void setTargetSessionIdentifyingHandle(final short initTSIH) {

        targetSessionIdentifyingHandle = initTSIH;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes1to3(final int line) throws InternetSCSIException {

        continueFlag = Utils.isBitSet(line & Constants.CONTINUE_FLAG_MASK);

        Utils.isReserved(line & LoginConstants.BIT_11_AND_12_FLAG_MASK);

        currentStageNumber =
            LoginStage
                .valueOf((byte)((line & LoginConstants.CSG_FLAG_MASK) >>> LoginConstants.CSG_BIT_SHIFT));
        nextStageNumber =
            LoginStage.valueOf((byte)((line & LoginConstants.NSG_FLAG_MASK) >>> Constants.TWO_BYTES_SHIFT));

        maxVersion = (byte)((line & Constants.THIRD_BYTE_MASK) >> Constants.ONE_BYTE_SHIFT);
        minVersion = (byte)(line & Constants.FOURTH_BYTE_MASK);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes12to15(final int line) throws InternetSCSIException {

        // use the logicalUnitNumber variable as temporary storage
        final long l = Utils.getUnsignedLong(line);

        logicalUnitNumber |= l;
        initiatorSessionID.deserialize(logicalUnitNumber);

        targetSessionIdentifyingHandle = (short)(line & Constants.LAST_TWO_BYTES_MASK);
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes20to23(final int line) throws InternetSCSIException {

        connectionID = (line & Constants.FIRST_TWO_BYTES_MASK) >>> Constants.TWO_BYTES_SHIFT;
        Utils.isReserved(line & Constants.LAST_TWO_BYTES_MASK);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        String exceptionMessage;

        do {
            final BasicHeaderSegment bhs = protocolDataUnit.getBasicHeaderSegment();
            if (bhs.isFinalFlag() && continueFlag) {
                exceptionMessage = "Transit and Continue Flag cannot be set at the same time.";
                break;
            }

            if (!bhs.isFinalFlag() && nextStageNumber != LoginStage.SECURITY_NEGOTIATION) {
                exceptionMessage = "NextStageNumber is reserved, when the TransitFlag is not set.";
                break;
            }

            if (bhs.isFinalFlag()) {
                if (currentStageNumber == LoginStage.SECURITY_NEGOTIATION) {
                    if (nextStageNumber == LoginStage.SECURITY_NEGOTIATION) {
                        exceptionMessage = "This transition (SNP -> SNP) is not allowed.";
                        break;
                    }
                } else if (currentStageNumber == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION) {
                    if (nextStageNumber == LoginStage.SECURITY_NEGOTIATION) {
                        exceptionMessage = "This transition (LONP -> SNP) is not allowed.";
                        break;
                    } else if (nextStageNumber == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION) {
                        exceptionMessage = "This transition (LONP -> LONP) is not allowed.";
                        break;
                    }
                }
            }

            if (minVersion != 0x00) {
                exceptionMessage = "MinVersion is not in a valid range.";
                break;
            }

            if (maxVersion != 0x00) {
                exceptionMessage = "MaxVersion is not in a valid range.";
                break;
            }

            if (minVersion != maxVersion) {
                exceptionMessage = "MinVersion and MaxVersion have not the same value.";
                break;
            }

            // message is checked correctly
            return;
        } while (false);

        throw new InternetSCSIException(exceptionMessage);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes1to3() {

        int line = minVersion;
        line |= maxVersion << Constants.ONE_BYTE_SHIFT;

        line |= nextStageNumber.value() << Constants.TWO_BYTES_SHIFT;
        line |= currentStageNumber.value() << LoginConstants.CSG_BIT_SHIFT;

        if (continueFlag) {
            line |= Constants.CONTINUE_FLAG_MASK;
        }

        return line;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes8to11() throws InternetSCSIException {

        logicalUnitNumber = initiatorSessionID.serialize();
        return (int)(logicalUnitNumber >>> Constants.FOUR_BYTES_SHIFT);
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes12to15() {

        int line = (int)(logicalUnitNumber & Constants.LAST_FOUR_BYTES_MASK);
        line |= targetSessionIdentifyingHandle;

        return line;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes20to23() {

        return connectionID << Constants.TWO_BYTES_SHIFT;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
