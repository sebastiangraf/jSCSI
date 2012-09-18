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
import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * <h1>LoginResponseParser</h1>
 * <p>
 * This is a parser for the Login Response Message of the iSCSI Protocol (RFC3720). So it parses all the field
 * of this login message and offers it fields with its getter methods.
 * <p>
 * The Login Response indicates the progress and/or end of the Login Phase.
 * <p>
 * <h4>T (Transit) bit</h4> The <code>T</code> bit is set to <code>1</code> as an indicator of the end of the
 * stage. If the <code>T</code> bit is set to <code>1</code> and NSG is FullFeaturePhase, then this is also
 * the Final Login Response (see Chapter 5). A <code>T</code> bit of <code>0</code> indicates a "partial"
 * response, which means "more negotiation needed".
 * <p>
 * A Login Response with a <code>T</code> bit set to <code>1</code> MUST NOT contain key=value pairs that may
 * require additional answers from the initiator within the same stage.
 * <p>
 * A Login Response with a <code>T</code> bit set to <code>1</code> MUST NOT contain key=value pairs that may
 * require additional answers from the initiator within the same stage.
 * <p>
 * <h4>StatSN</h4> For the first Login Response (the response to the first Login Request), this is the
 * starting status Sequence Number for the connection. The next response of any kind, including the next Login
 * Response, if any, in the same Login Phase, will carry this <code>number + 1</code>. This field is only
 * valid if the Status-Class is <code>0</code>.
 * <p>
 * <h4>Login Parameters</h4> The target MUST provide some basic parameters in order to enable the initiator to
 * determine if it is connected to the correct port and the initial text parameters for the security exchange.
 * <p>
 * All the rules specified in Section 10.11.6 Text Response Data for text responses also hold for Login
 * Responses. Keys and their explanations are listed in Chapter 11 (security negotiation keys) and Chapter 12
 * (operational parameter negotiation keys). All keys in Chapter 12, except for the X extension formats, MUST
 * be supported by iSCSI initiators and targets. Keys in Chapter 11, only need to be supported when the
 * function to which they refer is mandatory to implement.
 * 
 * @author Volker Wildi
 */
public final class LoginResponseParser extends TargetMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Continue Flag. */
    private boolean continueFlag;

    /**
     * The Status returned in a Login Response indicates the execution status of
     * the Login Phase. The status includes:
     * <ul>
     * <li>Status-Class</li>
     * <li>Status-Detail</li>
     * </ul>
     * <p>
     * <code>0</code> Status-Class indicates success.
     * <p>
     * A non-zero Status-Class indicates an exception. In this case, Status-Class is sufficient for a simple
     * initiator to use when handling exceptions, without having to look at the Status-Detail. The
     * Status-Detail allows finer-grained exception handling for more sophisticated initiators and for better
     * information for logging.
     * <p>
     * If the Status Class is not <code>0</code>, the initiator and target MUST close the TCP connection.
     * <p>
     * If the target wishes to reject the Login Request for more than one reason, it should return the primary
     * reason for the rejection.
     * 
     * @see LoginStatus
     */
    private LoginStatus status;

    /** Current stage. */
    private LoginStage currentStageNumber;

    /** Next stage. */
    private LoginStage nextStageNumber;

    /** The maximum version. */
    private int maxVersion;

    /** The active version. */
    private int activeVersion;

    /** Initiator Session ID (ISID). */
    private ISID initiatorSessionID;

    /** Target Session Identifying Handle (TSIH). */
    private short targetSessionIdentifyingHandle;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>LoginResponseParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>LoginResponseParser</code> subclass
     *            object.
     */
    public LoginResponseParser(final ProtocolDataUnit initProtocolDataUnit) {

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
        Utils.printField(sb, "activeVersion", activeVersion, 1);
        Utils.printField(sb, "maxVersion", maxVersion, 1);
        initiatorSessionID.toString();
        Utils.printField(sb, "TSIH", targetSessionIdentifyingHandle, 1);
        sb.append(super.toString());
        Utils.printField(sb, "Status", status.value(), 1);

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final void clear() {

        super.clear();

        continueFlag = false;

        currentStageNumber = LoginStage.SECURITY_NEGOTIATION;
        nextStageNumber = LoginStage.SECURITY_NEGOTIATION;

        maxVersion = 0;
        activeVersion = 0;

        initiatorSessionID.clear();

        targetSessionIdentifyingHandle = 0;
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

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The Status returned in a Login Response indicates the execution status of
     * the Login Phase. The status includes:
     * <ul>
     * <li>Status-Class</li>
     * <li>Status-Detail</li>
     * </ul>
     * <p>
     * <code>0</code> Status-Class indicates success.
     * <p>
     * A non-zero Status-Class indicates an exception. In this case, Status-Class is sufficient for a simple
     * initiator to use when handling exceptions, without having to look at the Status-Detail. The
     * Status-Detail allows finer-grained exception handling for more sophisticated initiators and for better
     * information for logging.
     * 
     * @return The status of this LoginResponseParser object.
     */
    public final LoginStatus getStatus() {

        return status;
    }

    /**
     * When set to <code>1</code>, indicates that the text (set of key=value
     * pairs) in this Login Response is not complete (it will be continued on
     * subsequent Login Responses); otherwise, it indicates that this Login
     * Response ends a set of key=value pairs. A Login Response with the <code>C</code> bit set to
     * <code>1</code> MUST have the <code>T</code> bit
     * set to <code>0</code>.
     * 
     * @return The status of the Continue Flag of this <code>LoginResponseParser</code> object.
     */
    public final boolean isContinueFlag() {

        return continueFlag;
    }

    /**
     * Returns the <em>Current Stage Number</em> of this Login Response Message.
     * 
     * @return Number of the Current Stage.
     * @see org.jscsi.parser.login.LoginStage
     */
    public final LoginStage getCurrentStageNumber() {

        return currentStageNumber;
    }

    /**
     * Returns the Initiator Session ID (ISID) of this LoginResponseParser
     * object.
     * 
     * @return Returns the Initiator Session ID (ISID) of this
     *         LoginResponseParser object.
     * @see ISID
     */
    public final ISID getInitiatorSessionID() {

        return initiatorSessionID;
    }

    /**
     * This is the highest version number supported by the target.
     * <p>
     * All Login Responses within the Login Phase MUST carry the same Version-max.
     * <p>
     * The initiator MUST use the value presented as a response to the first Login Request.
     * 
     * @return The maximum version of this login request message.
     */
    public final int getMaxVersion() {

        return maxVersion;
    }

    /**
     * Indicates the highest version supported by the target and initiator.
     * <p>
     * If the target does not support a version within the range specified by the initiator, the target
     * rejects the login and this field indicates the lowest version supported by the target.
     * <p>
     * All Login Responses within the Login Phase MUST carry the same Version-active.
     * <p>
     * The initiator MUST use the value presented as a response to the first Login Request.
     * 
     * @return The active version of this <code>LoginResponseParser</code> object.
     */
    public final int getActiveVersion() {

        return activeVersion;
    }

    /**
     * Returns the <em> Next Stage Number</em> of this Login Response Message.
     * 
     * @return The Number of the Next Stage.
     * @see org.jscsi.parser.login.LoginStage
     */
    public final LoginStage getNextStageNumber() {

        return nextStageNumber;
    }

    /**
     * The TSIH is the target assigned session identifying handle. Its internal
     * format and content are not defined by this protocol except for the value <code>0</code> that is
     * reserved. With the exception of the Login
     * Final-Response in a new session, this field should be set to the TSIH
     * provided by the initiator in the Login Request. For a new session, the
     * target MUST generate a non-zero TSIH and ONLY return it in the Login
     * Final-Response (see Section 5.3 Login Phase).
     * 
     * @return Returns the Target Session Identifying Handle of this <code>LoginResponseParser</code> object.
     */
    public final short getTargetSessionIdentifyingHandle() {

        return targetSessionIdentifyingHandle;
    }

    public void setStatus(LoginStatus status) {
        this.status = status;
    }

    public void setContinueFlag(boolean continueFlag) {
        this.continueFlag = continueFlag;
    }

    public void setCurrentStageNumber(LoginStage currentStage) {
        this.currentStageNumber = currentStage;
    }

    public void setNextStageNumber(LoginStage nextStage) {
        this.nextStageNumber = nextStage;
    }

    public void setInitiatorSessionID(ISID initiatorSessionID) {
        this.initiatorSessionID = initiatorSessionID;
    }

    public void setTargetSessionIdentifyingHandle(short targetSessionIdentifyingHandle) {
        this.targetSessionIdentifyingHandle = targetSessionIdentifyingHandle;
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

        maxVersion = (line & Constants.THIRD_BYTE_MASK) >> Constants.ONE_BYTE_SHIFT;
        activeVersion = line & Constants.FOURTH_BYTE_MASK;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes12to15(final int line) throws InternetSCSIException {

        // use the logicalUnitNumber variable as temporary storage
        logicalUnitNumber |= Utils.getUnsignedLong(line);
        initiatorSessionID.deserialize(logicalUnitNumber);

        targetSessionIdentifyingHandle = (short)(line & Constants.LAST_TWO_BYTES_MASK);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes36to39(final int line) throws InternetSCSIException {

        status =
            LoginStatus
                .valueOf((short)((line & Constants.FIRST_TWO_BYTES_MASK) >>> Constants.TWO_BYTES_SHIFT));
        Utils.isReserved(line & Constants.LAST_TWO_BYTES_MASK);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        String exceptionMessage;
        do {
            if (status != LoginStatus.SUCCESS && statusSequenceNumber != 0) {
                exceptionMessage =
                    "While no successful login is preformed, the StatusSequenceNumber must be 0.";
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

        int line = activeVersion;
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
    protected final int serializeBytes36to39() {

        int line = 0;
        line |= status.value() << Constants.TWO_BYTES_SHIFT;

        return line;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
