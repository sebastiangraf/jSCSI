package org.jscsi.target.scsi;

/**
 * In addition to dictating the serialization methods inherited from {@link ISerializable}, this interface
 * serves as a marker interface for all
 * classes that can be sent as SCSI Response data, i.e. as information sent in
 * response to a successful SCSI request.
 * <p>
 * {@link IResponseData} can either be a part of {@link ScsiResponseDataSegment} objects or it can be used
 * individually as Data-In PDU payload (followed by a SCSI Response PDU with status but with data segment
 * length zero.
 * 
 * @author Andreas Ergenzinger
 */
public interface IResponseData extends ISerializable {
    // nothing here
}
