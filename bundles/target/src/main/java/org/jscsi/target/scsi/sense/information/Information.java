package org.jscsi.target.scsi.sense.information;

import org.jscsi.target.scsi.ISerializable;

/**
 * The contents of the INFORMATION field are device-type or command specific and
 * are defined in a command standard.
 * <p>
 * All INFORMATION fields used by the jSCSI Target do not contain any information.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class Information implements ISerializable {

}
