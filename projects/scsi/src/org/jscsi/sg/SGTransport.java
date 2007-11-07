package org.jscsi.sg;

public interface SGTransport
{
   /**
    * service_indication(Nexus, CDB, Task Attribute, Auto Sense Request, Command Reference Number)
    * 
    * Note:
    * 1. we are foregoing the explicit use of "Auto Sense Request"
    * 2. TaskAttribute is always simple, as SG3 supports nothing else
    * 3. TaskTag is generated from the TransportPortLayer; generally it will be the same as the cmdRef
    * 
    * @param initiatorPort Part of the Nexus
    * @param targetPort Part of the Nexus
    * @param lun Part of the Nexus
    * @param cdb
    * @param cmdRef
    */
	void serviceIndication(String initiatorPort, String targetPort, long lun, byte[] cdb, long cmdRef);	

	/**
	 * service_response(Nexus, Sense Data, Status, Service Response)
	 * 
	 * @param initiatorPort
	 * @param targetPort
	 * @param lun
	 * @param senseData
	 * @param status
	 * @return ServiceResponse
	 */
	int serviceResponse(String initiatorPort, String targetPort, long lun, byte[] senseData, int status);
}
