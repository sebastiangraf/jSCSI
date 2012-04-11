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
package org.jscsi.parser.scsi;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.parser.Constants;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.utils.Utils;

/**
 * <h1>SCSICommandParser</h1>
 * <p>
 * This class parses a SCSI Command message defined in the iSCSI Standard
 * (RFC3720).
 * <p>
 * <h4>CmdSN - Command Sequence Number</h4> Enables ordered delivery across
 * multiple connections in a single session.
 * <p>
 * <h4>ExpStatSN</h4> Command responses up to
 * <code>ExpStatSN - 1 (mod 2**32)</code> have been received (acknowledges
 * status) on the connection.
 * <p>
 * <h4>Data Segment - Command Data</h4> Some SCSI commands require additional
 * parameter data to accompany the SCSI command. This data may be placed beyond
 * the boundary of the iSCSI header in a data segment. Alternatively, user data
 * (e.g., from a WRITE operation) can be placed in the data segment (both cases
 * are referred to as immediate data). These data are governed by the rules for
 * solicited vs. unsolicited data outlined in Section 3.2.4.2 Data Transfer
 * Overview.
 * 
 * @author Volker Wildi
 */
public class SCSICommandParser extends InitiatorMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This enumeration defines all valid types of additional header segments,
     * which are defined by the iSCSI standard (RFC3720).
     * <p>
     * <table border="1">
     * <tr>
     * <th>Value</th>
     * <th>Meaning</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>Reserved</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>Extended CDB</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Expected Bidirectional Read Data Length</td>
     * </tr>
     * <tr>
     * <td>3 - 63</td>
     * <td>Reserved</td>
     * </tr>
     * </table>
     */
    public enum TaskAttributes {

        /**
         * Untagged queuing allows a target to accept a command from an
         * initiator for a logical unit or target routine while I/O processes
         * from other initiators are being executed. Only one command for each
         * I_T_x nexus shall be accepted at a time.
         */
        UNTAGGED((byte) 0),
        /**
         * If only SIMPLE QUEUE TAG messages are used, the target may execute
         * the commands in any order that is deemed desirable within the
         * constraints of the queue management algorithm specified in the
         * control mode page (see [SAM2, 8.3.3.1]).
         */
        SIMPLE((byte) 1),
        /**
         * If ORDERED QUEUE TAG messages are used, the target shall execute the
         * commands in the order received with respect to other commands
         * received with ORDERED QUEUE TAG messages. All commands received with
         * a SIMPLE QUEUE TAG message prior to a command received with an
         * ORDERED QUEUE TAG message, regardless of initiator, shall be executed
         * before that command with the ORDERED QUEUE TAG message. All commands
         * received with a SIMPLE QUEUE TAG message after a command received
         * with an ORDERED QUEUE TAG message, regardless of initiator, shall be
         * executed after that command with the ORDERED QUEUE TAG message.
         */
        ORDERED((byte) 2),
        /**
         * A command received with a HEAD OF QUEUE TAG message is placed first
         * in the queue, to be executed next. A command received with a HEAD OF
         * QUEUE TAG message shall be executed prior to any queued I/O process.
         * Consecutive commands received with HEAD OF QUEUE TAG messages are
         * executed in a last-in-first-out order.
         */
        HEAD_OF_QUEUE((byte) 3),
        /** ACA is Auto Contingent Allegiance. */
        ACA((byte) 4);

        private final byte value;

        private static Map<Byte, TaskAttributes> mapping;

        static {
            TaskAttributes.mapping = new HashMap<Byte, TaskAttributes>();
            for (TaskAttributes s : values()) {
                TaskAttributes.mapping.put(s.value, s);
            }
        }

        private TaskAttributes(final byte newValue) {

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
        public static final TaskAttributes valueOf(final byte value) {

            return TaskAttributes.mapping.get(value);
        }

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Bit mask to indicate data input (read) is expected. */
    private static final int READ_EXPECTED_FLAG_MASK = 0x00400000;

    /** Bit mask to indicate data output (write) is expected. */
    private static final int WRITE_EXPECTED_FLAG_MASK = 0x00200000;

    /** Bit mask to extract the task attributes. */
    private static final int TASK_ATTRIBUTES_FLAG_MASK = 0x00070000;

    /** The size (in bytes) of a normal CDB <code>ByteBuffer</code>. */
    private static final int CDB_SIZE = 16;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The expected data transfer length. */
    private int expectedDataTransferLength;

    /** SCSI Command Descriptor Block. */
    private ByteBuffer commandDescriptorBlock;

    /** The flag 'R' (Input data expected). */
    private boolean readExpectedFlag;

    /** The flag 'W' (Output data expected). */
    private boolean writeExpectedFlag;

    /** The task attributes. */
    private TaskAttributes taskAttributes;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>SCSICommandParser</code>
     * object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>SCSICommandParser</code> subclass object.
     */
    public SCSICommandParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
        commandDescriptorBlock = ByteBuffer.allocate(CDB_SIZE);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "LUN", logicalUnitNumber, 1);
        Utils.printField(sb, "Expected Data Transfer Length",
                expectedDataTransferLength, 1);
        sb.append(super.toString());

        Utils.printField(sb, "SCSI CDB", commandDescriptorBlock, 1);

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final DataSegmentFormat getDataSegmentFormat() {

        return DataSegmentFormat.BINARY;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean canContainAdditionalHeaderSegments() {

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final void clear() {

        super.clear();

        readExpectedFlag = false;
        writeExpectedFlag = false;
        taskAttributes = null;

        expectedDataTransferLength = 0x00000000;

        commandDescriptorBlock.clear();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * There are <code>16</code> bytes in the CDB field to accommodate the
     * commonly used CDBs. Whenever the CDB is larger than <code>16</code>
     * bytes, an Extended CDB AHS MUST be used to contain the CDB spillover.
     * 
     * @return A <code>ByteBuffer</code> with the content of the Command
     *         Descriptor Blocks contained in this
     *         <code>SCSICommandParser</code> object.
     */
    public final ByteBuffer getCDB() {

        return (ByteBuffer) commandDescriptorBlock.rewind();
    }

    /**
     * For unidirectional operations, the Expected Data Transfer Length field
     * contains the number of bytes of data involved in this SCSI operation. For
     * a unidirectional write operation (W flag set to <code>1</code> and R flag
     * set to <code>0</code>), the initiator uses this field to specify the
     * number of bytes of data it expects to transfer for this operation. For a
     * unidirectional read operation (W flag set to <code>0</code> and R flag
     * set to <code>1</code>), the initiator uses this field to specify the
     * number of bytes of data it expects the target to transfer to the
     * initiator. It corresponds to the SAM2 byte count.
     * <p>
     * For bidirectional operations (both R and W flags are set to
     * <code>1</code> ), this field contains the number of data bytes involved
     * in the write transfer. For bidirectional operations, an additional header
     * segment MUST be present in the header sequence that indicates the
     * Bidirectional Read Expected Data Transfer Length. The Expected Data
     * Transfer Length field and the Bidirectional Read Expected Data Transfer
     * Length field correspond to the SAM2 byte count.
     * <p>
     * If the Expected Data Transfer Length for a write and the length of the
     * immediate data part that follows the command (if any) are the same, then
     * no more data PDUs are expected to follow. In this case, the F bit MUST be
     * set to <code>1</code>.
     * <p>
     * If the Expected Data Transfer Length is higher than the FirstBurstLength
     * (the negotiated maximum amount of unsolicited data the target will
     * accept), the initiator MUST send the maximum amount of unsolicited data
     * OR ONLY the immediate data, if any.
     * <p>
     * Upon completion of a data transfer, the target informs the initiator
     * (through residual counts) of how many bytes were actually processed (sent
     * and/or received) by the target.
     * 
     * @return The Expected Data Transfer Length of this SCSICommandParser
     *         object.
     */
    public final int getExpectedDataTransferLength() {

        return expectedDataTransferLength;
    }

    /**
     * The command expects input data (read).
     * 
     * @return <code>true</code>,if a read command is expected. Else
     *         <code>false</code>.
     */
    public final boolean isReadExpectedFlag() {

        return readExpectedFlag;
    }

    /**
     * @param newReadExpectedFlag
     *            The readExpectedFlag to set.
     */
    public final void setReadExpectedFlag(final boolean newReadExpectedFlag) {

        readExpectedFlag = newReadExpectedFlag;
    }

    /**
     * @return Returns the taskAttributes.
     */
    public final TaskAttributes getTaskAttributes() {

        return taskAttributes;
    }

    /**
     * @param newTaskAttributes
     *            The taskAttributes to set.
     */
    public final void setTaskAttributes(final TaskAttributes newTaskAttributes) {

        taskAttributes = newTaskAttributes;
    }

    /**
     * The command expects output data (write).
     * 
     * @return <code>true</code>,if a write command is expected. Else
     *         <code>false</code>.
     */
    public final boolean isWriteExpectedFlag() {

        return writeExpectedFlag;
    }

    /**
     * @param newWriteExpectedFlag
     *            The writeExpectedFlag to set.
     */
    public final void setWriteExpectedFlag(final boolean newWriteExpectedFlag) {

        writeExpectedFlag = newWriteExpectedFlag;
    }

    /**
     * Sets the new Command Descriptor Block.
     * 
     * @param newCDB
     *            The new Command Descriptor Block.
     */
    public final void setCommandDescriptorBlock(final ByteBuffer newCDB) {

        if (newCDB.limit() - newCDB.position() > CDB_SIZE) {
            throw new IllegalArgumentException(
                    "Buffer cannot be longer than 16 bytes, because AHS-support is not implemented.");
        }
        commandDescriptorBlock = newCDB;
    }

    /**
     * @param newExpectedDataTransferLength
     *            The expectedDataTransferLength to set.
     */
    public final void setExpectedDataTransferLength(
            final int newExpectedDataTransferLength) {

        expectedDataTransferLength = newExpectedDataTransferLength;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes1to3(final int line)
            throws InternetSCSIException {

        readExpectedFlag = Utils.isBitSet(line & READ_EXPECTED_FLAG_MASK);
        writeExpectedFlag = Utils.isBitSet(line & WRITE_EXPECTED_FLAG_MASK);
        taskAttributes = TaskAttributes
                .valueOf((byte) ((line & TASK_ATTRIBUTES_FLAG_MASK) >> Constants.TWO_BYTES_SHIFT));

        Utils.isReserved(line & Constants.LAST_TWO_BYTES_MASK);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes20to23(final int line)
            throws InternetSCSIException {

        expectedDataTransferLength = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes32to35(final int line)
            throws InternetSCSIException {

        commandDescriptorBlock.rewind();
        commandDescriptorBlock.putInt(line);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes36to39(final int line)
            throws InternetSCSIException {

        commandDescriptorBlock.putInt(line);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes40to43(final int line)
            throws InternetSCSIException {

        commandDescriptorBlock.putInt(line);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes44to47(final int line)
            throws InternetSCSIException {

        commandDescriptorBlock.putInt(line);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        String exceptionMessage;
        do {
            if (!writeExpectedFlag
                    && !protocolDataUnit.getBasicHeaderSegment().isFinalFlag()) {
                exceptionMessage = "W and F flag cannot both be 0.";
                break;
            }

            if (expectedDataTransferLength != 0
                    && !(readExpectedFlag || writeExpectedFlag)) {
                exceptionMessage = "The ExpectedDataTransferLength is greater than 0, so Read or/and Write Flag has to be set.";
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

        int line = 0;

        line |= taskAttributes.value() << Constants.TWO_BYTES_SHIFT;

        if (writeExpectedFlag) {
            line |= WRITE_EXPECTED_FLAG_MASK;
        }

        if (readExpectedFlag) {
            line |= READ_EXPECTED_FLAG_MASK;
        }

        return line;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes20to23() {

        return expectedDataTransferLength;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes32to35() {

        commandDescriptorBlock.rewind();
        return commandDescriptorBlock.getInt();
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes36to39() {

        return commandDescriptorBlock.getInt();
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes40to43() {

        return commandDescriptorBlock.getInt();
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes44to47() {

        return commandDescriptorBlock.getInt();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
