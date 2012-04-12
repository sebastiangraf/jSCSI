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
package org.jscsi.parser.asynchronous;

import java.util.HashMap;
import java.util.Map;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * <h1>AsynchronousMessageParser</h1>
 * <p>
 * Parser for the Asynchronous Message of the iSCSI Protocol (RFC3720).
 * 
 * @author Volker Wildi
 */
public final class AsynchronousMessageParser extends TargetMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Enumeration of all valid asynchronous message event codes.
     * <table * border="1">
     * <tr>
     * <th>Event Code</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>A SCSI Asynchronous Event is reported in the sense data. Sense Data
     * that accompanies the report, in the data segment, identifies the
     * condition. The sending of a SCSI Event (Asynchronous Event Reporting in
     * SCSI terminology) is dependent on the target support for SCSI
     * asynchronous event reporting (see [SAM2]) as indicated in the standard
     * INQUIRY data (see [SPC3]). Its use may be enabled by parameters in the
     * SCSI Control mode page (see [SPC3]).</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>Target requests Logout. This Async Message MUST be sent on the same
     * connection as the one requesting to be logged out. The initiator MUST
     * honor this request by issuing a Logout as early as possible, but no later
     * than Parameter3 seconds. Initiator MUST send a Logout with a reason code
     * of "Close the connection" OR "Close the session" to close all the
     * connections. Once this message is received, the initiator SHOULD NOT
     * issue new iSCSI commands on the connection to be logged out. The target
     * MAY reject any new I/O requests that it receives after this Message with
     * the reason code "Waiting for Logout". If the initiator does not Logout in
     * Parameter3 seconds, the target should send an Async PDU with iSCSI event
     * code "Dropped the connection" if possible, or simply terminate the
     * transport connection. Parameter1 and Parameter2 are reserved.</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Target indicates it will drop the connection. The Parameter1 field
     * indicates the CID of the connection that is going to be dropped. The
     * Parameter2 field (Time2Wait) indicates, in seconds, the minimum time to
     * wait before attempting to reconnect or reassign. The Parameter3 field
     * (Time2Retain) indicates the maximum time allowed to reassign commands
     * after the initial wait (in Parameter2). If the initiator does not attempt
     * to reconnect and/or reassign the outstanding commands within the time
     * specified by Parameter3, or if Parameter3 is 0, the target will terminate
     * all outstanding commands on this connection. In this case, no other
     * responses should be expected from the target for the outstanding commands
     * on this connection. A value of 0 for Parameter2 indicates that reconnect
     * can be attempted immediately.</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>Target indicates it will drop all the connections of this session.
     * Parameter1 field is reserved. The Parameter2 field (Time2Wait) indicates,
     * in seconds, the minimum time to wait before attempting to reconnect. The
     * Parameter3 field (Time2Retain) indicates the maximum time allowed to
     * reassign commands after the initial wait (in Parameter2). If the
     * initiator does not attempt to reconnect and/or reassign the outstanding
     * commands within the time specified by Parameter3, or if Parameter3 is 0,
     * the session is terminated. In this case, the target will terminate all
     * outstanding commands in this session; no other responses should be
     * expected from the target for the outstanding commands in this session. A
     * value of 0 for Parameter2 indicates that reconnect can be attempted
     * immediately.</td>
     * </tr>
     * <tr>
     * <td>4</td>
     * <td>Target requests parameter negotiation on this connection. The
     * initiator MUST honor this request by issuing a Text Request (that can be
     * empty) on the same connection as early as possible, but no later than
     * Parameter3 seconds, unless a Text Request is already pending on the
     * connection, or by issuing a Logout Request. If the initiator does not
     * issue a Text Request the target may reissue the Asynchronous Message
     * requesting parameter negotiation.</td>
     * </tr>
     * <tr>
     * <td>255</td>
     * <td>Vendor specific iSCSI Event. The AsyncVCode details the vendor code,
     * and data MAY accompany the report.</td>
     * </tr>
     * </table>
     * <br/>
     * All other event codes are reserved.
     */
    public static enum AsyncEventCodes {
        /** A SCSI Asynchronous Event is reported in the sense data. */
        SCSI_ASYNCHRONOUS_EVENT((byte) 0),

        /** Target requests Logout. */
        LOGOUT((byte) 1),

        /** Target indicates it will drop the connection. */
        DROP_CONNECTION((byte) 2),

        /** Target indicates it will drop all the connections of this session. */
        DROP_ALL_CONNECTIONS((byte) 3),

        /** Target requests parameter negotiation on this connection. */
        PARAMETER_NEGOTIATION((byte) 4),

        /** Vendor specific iSCSI Event. */
        VENDOR_SPECIFIC_EVENT((byte) 255);

        private final byte value;

        private static Map<Byte, AsyncEventCodes> mapping;

        static {
            AsyncEventCodes.mapping = new HashMap<Byte, AsyncEventCodes>();
            for (AsyncEventCodes s : values()) {
                AsyncEventCodes.mapping.put(s.value, s);
            }
        }

        private AsyncEventCodes(final byte newValue) {

            value = newValue;
        }

        /**
         * Returns the value of this enumeration.
         * 
         * @return The value of this enumeration.
         */
        public final byte value() {

            return value;
        }

        /**
         * Returns the constant defined for the given <code>value</code>.
         * 
         * @param value
         *            The value to search for.
         * @return The constant defined for the given <code>value</code>. Or
         *         <code>null</code>, if this value is not defined by this
         *         enumeration.
         */
        public static final AsyncEventCodes valueOf(final byte value) {

            return AsyncEventCodes.mapping.get(value);
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The event code of this asynchronous message. */
    private AsyncEventCodes asyncEvent;

    /** The asynchronous vendor specific detail code. */
    private byte asyncVCode;

    /** The first parameter. */
    private short parameter1;

    /** The second parameter. */
    private short parameter2;

    /** The third parameter. */
    private short parameter3;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty
     * <code>AsynchronousMessageParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>AsynchronousMessageParser</code> subclass
     *            object.
     */
    public AsynchronousMessageParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);
        Utils.printField(sb, "LUN", logicalUnitNumber, 1);
        sb.append(super.toString());
        Utils.printField(sb, "AsyncEvent", asyncEvent.value(), 1);
        Utils.printField(sb, "AsyncVCode", asyncVCode, 1);
        Utils.printField(sb, "Parameter 1", parameter1, 1);
        Utils.printField(sb, "Parameter 2", parameter2, 1);
        Utils.printField(sb, "Parameter 3", parameter3, 1);
        sb.append(super.toString());

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final DataSegmentFormat getDataSegmentFormat() {

        return DataSegmentFormat.BINARY;
    }

    /** {@inheritDoc} */
    @Override
    public final void clear() {

        super.clear();

        asyncEvent = null;
        asyncVCode = 0x00;

        parameter1 = 0x0000;
        parameter2 = 0x0000;
        parameter3 = 0x0000;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the code of the iSCSI Asynchronous message.
     * <p>
     * <code>AsyncVCode</code> is a vendor specific detail code that is only
     * valid if the <code>AsyncEvent</code> field indicates a vendor specific
     * event. Otherwise, it is reserved.
     * 
     * @return The Asynchronous vendor code.
     * @see AsyncEventCodes
     */
    public final byte getAsyncVCode() {

        return asyncVCode;
    }

    /**
     * Returns the asynchronous event code of this
     * <code>AsynchronousMessageParser</code> object.
     * 
     * @return The asynchronous event.
     */
    public final AsyncEventCodes getAsyncEvent() {

        return asyncEvent;
    }

    /**
     * Returns the first parameter of this asynchronous message.
     * 
     * @return The first parameter.
     */
    public final short getParameter1() {

        return parameter1;
    }

    /**
     * Returns the second parameter of this asynchronous message.
     * 
     * @return The second parameter.
     */
    public final short getParameter2() {

        return parameter2;
    }

    /**
     * Returns the third parameter of this asynchronous message.
     * 
     * @return The third parameter.
     */
    public final short getParameter3() {

        return parameter3;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes36to39(final int line)
            throws InternetSCSIException {

        asyncEvent = AsyncEventCodes
                .valueOf((byte) (line & Constants.FIRST_BYTE_MASK));
        asyncVCode = (byte) (line & Constants.SECOND_BYTE_MASK);
        parameter1 = (short) (line & Constants.LAST_TWO_BYTES_MASK);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes40to43(final int line)
            throws InternetSCSIException {

        parameter2 = (short) (line & Constants.FIRST_TWO_BYTES_MASK);
        parameter3 = (short) (line & Constants.LAST_TWO_BYTES_MASK);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        String exceptionMessage;
        do {
            if (asyncVCode != 0
                    && asyncEvent != AsyncEventCodes.VENDOR_SPECIFIC_EVENT) {
                exceptionMessage = "A vendor specific code is only valid, if also a vendor specific event occured.";
                break;
            }

            if (asyncEvent == AsyncEventCodes.SCSI_ASYNCHRONOUS_EVENT
                    && logicalUnitNumber == 0) {
                exceptionMessage = "A valid LogicalUnitNumber must be given, if an asynchronous event occured.";
                break;
            } else {
                Utils.isReserved(logicalUnitNumber);
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
    protected final int serializeBytes36to39() {

        int line = parameter1;
        line |= asyncVCode << Constants.TWO_BYTES_SHIFT;
        line |= asyncEvent.value() << Constants.THREE_BYTES_SHIFT;

        return line;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes40to43() {

        int line = parameter3;
        line |= parameter2 << Constants.TWO_BYTES_SHIFT;

        return line;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
