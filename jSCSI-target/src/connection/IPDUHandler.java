package connection;

import org.jscsi.parser.ProtocolDataUnit;

/**
 * 
 * @author Marcus Specht
 *
 */
public interface IPDUHandler {
	
	/**
	 * Assign the PDU to another entity.
	 * @param pdu
	 * @return true if entity is able to process PDU, false else.
	 */
	public boolean assignPDU(ProtocolDataUnit pdu);

}
