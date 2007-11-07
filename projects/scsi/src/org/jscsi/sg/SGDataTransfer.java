package org.jscsi.sg;

public interface SGDataTransfer
{
   /**
    * Called by the SG transport implementation; actually implemented from within the user space netlink end-point
    * 
    * @param initiatorPort
    * @param targetPort
    * @param lun
    * @param input
    * @return
    */
   boolean sendDataIn(String initiatorPort, String targetPort, long lun, byte[] input);  

   /**
    * Called by the SG transport implementation; actually implemented from within the user space netlink end-point
    * 
    * @param initiatorPort
    * @param targetPort
    * @param lun
    * @return
    */
   byte[] receiveDataOut(String initiatorPort, String targetPort, long lun);
}
