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

import java.util.HashMap;
import java.util.Map;

import org.jscsi.core.scsi.Status;
import org.jscsi.core.utils.Utils;
import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>SCSIResponseParser</h1>
 * <p>
 * This class parses a SCSI Response message defined in the iSCSI Standard
 * (RFC3720).
 * <p>
 * <h4>StatSN - Status Sequence Number</h4> StatSN is a Sequence Number that the
 * target iSCSI layer generates per connection and that in turn, enables the
 * initiator to acknowledge status reception. StatSN is incremented by
 * <code>1</code> for every response/status sent on a connection except for
 * responses sent as a result of a retry or SNACK. In the case of responses sent
 * due to a retransmission request, the StatSN MUST be the same as the first
 * time the PDU was sent unless the connection has since been restarted.
 * <p>
 * <h4>ExpCmdSN - Next Expected CmdSN from this Initiator</h4> ExpCmdSN is a
 * Sequence Number that the target iSCSI returns to the initiator to acknowledge
 * command reception. It is used to update a local variable with the same name.
 * An ExpCmdSN equal to <code>MaxCmdSN + 1</code> indicates that the target
 * cannot accept new commands.
 * <p>
 * <h4>MaxCmdSN - Maximum CmdSN from this Initiator</h4> MaxCmdSN is a Sequence
 * Number that the target iSCSI returns to the initiator to indicate the maximum
 * CmdSN the initiator can send. It is used to update a local variable with the
 * same name. If MaxCmdSN is equal to <code>ExpCmdSN - 1</code>, this indicates
 * to the initiator that the target cannot receive any additional commands. When
 * MaxCmdSN changes at the target while the target has no pending PDUs to convey
 * this information to the initiator, it MUST generate a NOP-IN to carry the new
 * MaxCmdSN. <p/> iSCSI targets MUST support and enable autosense. If Status is
 * CHECK CONDITION (<code>0x02</code>), then the Data Segment MUST contain sense
 * data for the failed command. <br/> For some iSCSI responses, the response
 * data segment MAY contain some response related information, (e.g., for a
 * target failure, it may contain a vendor specific detailed description of the
 * failure).
 * 
 * @author Volker Wildi
 */
public class SCSIResponseParser extends TargetMessageParser {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This enumerations defines all valid service responses, which are defined in
   * the iSCSI Standard (RFC 3720).
   * <p>
   * 0x80-0xff - Vendor specific
   */
  public static enum ServiceResponse {
    /** Command completed at target. */
    COMMAND_COMPLETED_AT_TARGET((byte) 0x00),
    /** Target Failure. */
    TARGET_FAILURE((byte) 0x01);

    private final byte value;

    private static Map<Byte, ServiceResponse> mapping;

    static {
      ServiceResponse.mapping = new HashMap<Byte, ServiceResponse>();
      for (ServiceResponse s : values()) {
        ServiceResponse.mapping.put(s.value, s);
      }
    }

    private ServiceResponse(final byte newValue) {

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
     *          The value to search for.
     * @return The constant defined for the given <code>value</code>. Or
     *         <code>null</code>, if this value is not defined by this
     *         enumeration.
     */
    public static final ServiceResponse valueOf(final byte value) {

      return ServiceResponse.mapping.get(value);
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Bit mask for the bits <code>1,2, and 7</code>. These bits are reserved. */
  private static final int RESERVED_FLAGS_MASK = 0x610000;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The o-bit. */
  private boolean bidirectionalReadResidualOverflow;

  /** The u-bit. */
  private boolean bidirectionalReadResidualUnderflow;

  /** The O-bit. */
  private boolean residualOverflow;

  /** The U-bit. */
  private boolean residualUnderflow;

  private ServiceResponse response;

  /** The Status code. */
  private SCSIStatus status;

  /** The SNACK Tag. */
  private int snackTag;

  /** The Expected Data Sequence Number. */
  private int expectedDataSequenceNumber;

  private int bidirectionalReadResidualCount;

  private int residualCount;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default constructor, creates a new, empty <code>SCSIResponseParser</code>
   * object.
   * 
   * @param initProtocolDataUnit
   *          The reference <code>ProtocolDataUnit</code> instance, which
   *          contains this <code>SCSIResponseParser</code> subclass object.
   */
  public SCSIResponseParser(final ProtocolDataUnit initProtocolDataUnit) {

    super(initProtocolDataUnit);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final String toString() {

    final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

    Utils.printField(sb, "Response", response.value(), 1);
    Utils.printField(sb, "SNACK TAG", snackTag, 1);
    sb.append(super.toString());
    Utils.printField(sb, "ExpDataSN", expectedDataSequenceNumber, 1);
    Utils.printField(sb, "Bidirectional Read Residual Count",
        bidirectionalReadResidualCount, 1);

    return sb.toString();
  }

  /** {@inheritDoc} */
  @Override
  public final DataSegmentFormat getDataSegmentFormat() {

    return DataSegmentFormat.SCSI_RESPONSE;
  }

  /** {@inheritDoc} */
  @Override
  public final void clear() {

    super.clear();

    bidirectionalReadResidualOverflow = false;
    bidirectionalReadResidualUnderflow = false;
    residualOverflow = false;
    residualUnderflow = false;

    response = null;
    status = null;

    snackTag = 0x00000000;

    bidirectionalReadResidualCount = 0x00000000;
    residualCount = 0x00000000;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * The Bidirectional Read Residual Count field MUST be valid in the case where
   * either the u bit or the o bit is set. If neither bit is set, the
   * Bidirectional Read Residual Count field is reserved. Targets may set the
   * Bidirectional Read Residual Count and initiators may use it when the
   * response code is "completed at target". If the o bit is set, the
   * Bidirectional Read Residual Count indicates the number of bytes that were
   * not transferred to the initiator because the initiator’s Expected
   * Bidirectional Read Transfer Length was not sufficient. If the u bit is set,
   * the Bidirectional Read Residual Count indicates the number of bytes that
   * were not transferred to the initiator out of the number of bytes expected
   * to be transferred.
   * 
   * @return The bidirectional read residual count of this
   *         <code>SCSIResponseParser</code> object.
   */
  public final int getBidirectionalReadResidualCount() {

    return bidirectionalReadResidualCount;
  }

  /**
   * The number of R2T and Data-In (read) PDUs the target has sent for the
   * command.
   * <p>
   * This field MUST be <code>0</code> if the response code is not Command
   * Completed at Target or the target sent no Data-In PDUs for the command.
   * 
   * @return The expected data sequence number of this
   *         <code>SCSIResponseParser</code> object.
   */
  public final int getExpectedDataSequenceNumber() {

    return expectedDataSequenceNumber;
  }

  /**
   * Returns the status of the Bidirectional Read Residual Overflow flag. In
   * this case, the Bidirectional Read Residual Count indicates the number of
   * bytes that were not transferred to the initiator because the initiator’s
   * Expected Bidirectional Read Data Transfer Length was not sufficient.
   * 
   * @return <code>true</code>, if it is set; else <code>false</code>.
   */
  public final boolean isBidirectionalReadResidualOverflow() {

    return bidirectionalReadResidualOverflow;
  }

  /**
   * Returns the status of the Bidirectional Read Residual Underflow flag. In
   * this case, the Bidirectional Read Residual Count indicates the number of
   * bytes that were not transferred to the initiator out of the number of bytes
   * expected to be transferred.
   * 
   * @return <code>true</code>, if it is set; else <code>false</code>.
   */
  public final boolean isBidirectionalReadResidualUnderflow() {

    return bidirectionalReadResidualUnderflow;
  }

  /**
   * The Residual Count field MUST be valid in the case where either the U bit
   * or the O bit is set. If neither bit is set, the Residual Count field is
   * reserved. Targets may set the residual count and initiators may use it when
   * the response code is "completed at target" (even if the status returned is
   * not GOOD). If the O bit is set, the Residual Count indicates the number of
   * bytes that were not transferred because the initiator’s Expected Data
   * Transfer Length was not sufficient. If the U bit is set, the Residual Count
   * indicates the number of bytes that were not transferred out of the number
   * of bytes expected to be transferred.
   * 
   * @return The residual count of this <code>SCSIResponseParser</code> object.
   */
  public final int getResidualCount() {

    return residualCount;
  }

  /**
   * Returns the status of the Residual Overflow flag. In this case, the
   * Residual Count indicates the number of bytes that were not transferred
   * because the initiator’s Expected Data Transfer Length was not sufficient.
   * For a bidirectional operation, the Residual Count contains the residual for
   * the write operation.
   * 
   * @return <code>true</code>, if it is set; else <code>false</code>.
   */
  public final boolean isResidualOverflow() {

    return residualOverflow;
  }

  /**
   * Returns the status of the Residual Underflow flag. In this case, the
   * Residual Count indicates the number of bytes that were not transferred out
   * of the number of bytes that were expected to be transferred. For a
   * bidirectional operation, the Residual Count contains the residual for the
   * write operation.
   * 
   * @return <code>true</code>, if it is set; else <code>false</code>.
   */
  public final boolean isResidualUnderflow() {

    return residualUnderflow;
  }

  /**
   * This field contains the iSCSI service response.
   * <p>
   * All other response codes are reserved.
   * <p>
   * The Response is used to report a Service Response. The mapping of the
   * response code into a SCSI service response code value, if needed, is
   * outside the scope of this document. However, in symbolic terms response
   * value 0x00 maps to the SCSI service response (see [SAM2] and [SPC3]) of
   * TASK COMPLETE or LINKED COMMAND COMPLETE. All other Response values map to
   * the SCSI service response of SERVICE DELIVERY OR TARGET FAILURE.
   * <p>
   * If a PDU that includes SCSI status (Response PDU or Data-In PDU including
   * status) does not arrive before the session is terminated, the SCSI service
   * response is SERVICE DELIVERY OR TARGET FAILURE. A non-zero Response field
   * indicates a failure to execute the command in which case the Status and
   * Flag fields are undefined.
   * 
   * @return The service response of this <code>SCSIResponseParser</code>
   *         object.
   * @see ServiceResponse
   */
  public final ServiceResponse getResponse() {

    return response;
  }

  /**
   * This field contains a copy of the SNACK Tag of the last SNACK Tag accepted
   * by the target on the same connection and for the command for which the
   * response is issued. Otherwise it is reserved and should be set to
   * <code>0</code>.
   * <p>
   * After issuing a R-Data SNACK the initiator must discard any SCSI status
   * unless contained in an SCSI Response PDU carrying the same SNACK Tag as the
   * last issued R-Data SNACK for the SCSI command on the current connection.
   * <p>
   * For a detailed discussion on R-Data SNACK see Section 10.16 SNACK Request.
   * 
   * @return The SNACK Tag of this <code>SCSIResponseParser</code> object.
   */
  public final int getSNACKTag() {

    return snackTag;
  }

  /**
   * The Status field is used to report the SCSI status of the command (as
   * specified in [SAM2]) and is only valid if the Response Code is Command
   * Completed at target.
   * 
   * @return The status field of this <code>SCSIResponseParser</code> object.
   * @see Status
   */
  public final SCSIStatus getStatus() {

    return status;
  }
  
  public final void setBidirectionalReadResidualCount(int bidirectionalReadResidualCount) {
	  this.bidirectionalReadResidualCount = bidirectionalReadResidualCount;
  }
  
  public final void setBidirectionalReadResidualOverflow(boolean bidirectionalReadResidualOverflow) {
	  this.bidirectionalReadResidualOverflow = bidirectionalReadResidualOverflow;
  }
  
  public final void setBidirectionalReadResidualUnderflow(boolean bidirectionalReadResidualUnderflow) {
	  this.bidirectionalReadResidualUnderflow = bidirectionalReadResidualUnderflow;
  }
  
  public final void setExpectedDataSequenceNumber(int expectedDataSequenceNumber) {
	  this.expectedDataSequenceNumber = expectedDataSequenceNumber;
  }
  
  public final void setResidualCount(int residualCount) {
	  this.residualCount = residualCount;
  }
  
  public final void setResidualOverflow(boolean residualOverflow) {
	  this.residualOverflow = residualOverflow;
  }
  
  public final void setResidualUnderflow(boolean residualUnderflow) {
	  this.residualUnderflow = residualUnderflow;
  }
  
  public void setResponse(SCSIResponseParser.ServiceResponse response) {
	  this.response = response;
  }
  
  public final void setSNACKTag(int snackTag) {
	  this.snackTag = snackTag;
  }
  
  public final void setStatus(SCSIStatus status) {
	  this.status = status;
  }
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes1to3(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line & RESERVED_FLAGS_MASK);

    bidirectionalReadResidualOverflow = Utils.isBitSet(line
        & Constants.READ_RESIDUAL_OVERFLOW_FLAG_MASK);
    bidirectionalReadResidualUnderflow = Utils.isBitSet(line
        & Constants.READ_RESIDUAL_UNDERFLOW_FLAG_MASK);
    residualOverflow = Utils.isBitSet(line
        & Constants.RESIDUAL_OVERFLOW_FLAG_MASK);
    residualUnderflow = Utils.isBitSet(line
        & Constants.RESIDUAL_UNDERFLOW_FLAG_MASK);
    response = ServiceResponse
        .valueOf((byte) ((line & Constants.THIRD_BYTE_MASK) >>> Constants.ONE_BYTE_SHIFT));
    status = SCSIStatus.valueOf((byte) (line & Constants.FOURTH_BYTE_MASK));
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes20to23(final int line)
      throws InternetSCSIException {

    snackTag = line;
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes36to39(final int line)
      throws InternetSCSIException {

    expectedDataSequenceNumber = line;
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes40to43(final int line)
      throws InternetSCSIException {

    bidirectionalReadResidualCount = line;
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes44to47(final int line)
      throws InternetSCSIException {

    residualCount = line;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected final void checkIntegrity() throws InternetSCSIException {

    String exceptionMessage;
    do {
      Utils.isReserved(logicalUnitNumber);

      if (response != ServiceResponse.COMMAND_COMPLETED_AT_TARGET) {
        if (bidirectionalReadResidualOverflow
            || bidirectionalReadResidualUnderflow || residualOverflow
            || residualUnderflow) {
          exceptionMessage = "Theses bits must to be 0, because the command is not completed at the target.";
          break;
        }
        
        if (status != SCSIStatus.GOOD) {
          exceptionMessage = "Status Code is only valid, because the command is not completed at the target.";
          break;
        }
      }

      if (bidirectionalReadResidualOverflow
          && bidirectionalReadResidualUnderflow) {
        exceptionMessage = "The 'o' and 'u' bits must be set mutal exclusion.";
        break;
      }

      if (residualOverflow && residualUnderflow) {
        exceptionMessage = "The 'O' and 'U' bits must be set mutal exclusion.";
        break;
      }

      if ((!residualOverflow && !residualUnderflow) && residualCount != 0) {
        exceptionMessage = "ResidualCount is only valid either the ResidualOverflow or ResidualUnderflow-Flag is set.";
        break;
      }

      if ((!bidirectionalReadResidualOverflow && !bidirectionalReadResidualUnderflow)
          && bidirectionalReadResidualCount != 0) {
        exceptionMessage = "BidirectionalResidualCount is only valid either the "
            + "BidirectionalResidualOverflow or BidirectionalResidualUnderflow-Flag is set.";
        break;
      }

      if (response != ServiceResponse.COMMAND_COMPLETED_AT_TARGET
          && expectedDataSequenceNumber != 0) {
        exceptionMessage = "The ExpectedDataSequenceNumber is not valid, because the command is not "
            + "completed at the target.";
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

    int line = status.value();
    line |= response.value() << Constants.ONE_BYTE_SHIFT;

    if (residualUnderflow) {
      line |= Constants.RESIDUAL_UNDERFLOW_FLAG_MASK;
    }

    if (residualOverflow) {
      line |= Constants.RESIDUAL_OVERFLOW_FLAG_MASK;
    }

    if (bidirectionalReadResidualUnderflow) {
      line |= Constants.READ_RESIDUAL_UNDERFLOW_FLAG_MASK;
    }

    if (bidirectionalReadResidualOverflow) {
      line |= Constants.READ_RESIDUAL_OVERFLOW_FLAG_MASK;
    }

    return line;
  }

  /** {@inheritDoc} */
  @Override
  protected final int serializeBytes20to23() {

    return snackTag;
  }

  /** {@inheritDoc} */
  @Override
  protected final int serializeBytes36to39() {

    return expectedDataSequenceNumber;
  }

  /** {@inheritDoc} */
  @Override
  protected final int serializeBytes40to43() {

    return bidirectionalReadResidualCount;
  }

  /** {@inheritDoc} */
  @Override
  protected final int serializeBytes44to47() {

    return residualCount;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
