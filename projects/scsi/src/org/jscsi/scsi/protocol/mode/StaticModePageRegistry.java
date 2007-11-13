package org.jscsi.scsi.protocol.mode;

@SuppressWarnings("unchecked")
public class StaticModePageRegistry extends ModePageRegistry
{
   
   public StaticModePageRegistry()
   {
      super();
   }

   @Override
   protected void populateModePages()
   {
      this.populateCaching();
      this.populateControl();
      this.populateInformationalExceptionsControl();
      this.populateReadWriteErrorRecovery();
   }
   
   protected void populateCaching()
   {
      Caching page = new Caching();
      
      page.setIC(false);   // Device using server-specific caching algorithm
      page.setABPF(true);  // Aborts pre-fetch on receipt of new command (DRA = 0)
      page.setCAP(false); // Caching analysis not permitted
      page.setDISC(true); // Pre-fetch accross time discontinuities permitted
      page.setSIZE(false); // The NUMBER OF CACHE SEGMENTS field would be used 
      page.setWCE(false); // Writeback cache is disabled
      page.setMF(false); // MINIMUM PRE-FETCH and MAXIMUM PRE-FETCH are number of logical blocks
      page.setRCD(true); // Read cache is disabled
      page.setDemandReadRetentionPriority(0x00); // no special read retention policy
      page.setWriteRetentionPriority(0x00); // no special write retention policy
      page.setDisablePrefetchTransferLength(0); // anticipatory pre-fetching disabled
      page.setMinimumPrefetch(0);
      page.setMaximumPrefetch(0);
      page.setMaximumPrefetchCeiling(0);
      page.setFSW(false); // writes to medium may be non-sequential
      page.setLBCSS(false); // CACHE SEGMENT SIZE field is in bytes
      page.setDRA(false); // read-ahead caching disabled
      page.setNV_DIS(false); // non-volatile cache not enabled
      page.setNumberOfCacheSegments(0);
      page.setCacheSegmentSize(0);
      
      this.setCaching(caching);
   }
   
   
   protected void populateControl()
   {
      Control page = new Control();
      
      page.setTST(0x0); // LU maintains one task set for all I_T nexuses
      page.setTMF_ONLY(false); // Arbitrary value; ACA not supported
      page.setD_SENSE(false); // Fixed format sense data is returned with autosense
      page.setGLTSD(true); // Log parameters not implicitly saved
      page.setRLEC(false); // Log exception conditions are not reported
      page.setQUEUE_ALGORIHTM_MODIFIER(0x01); // Unrestricted reordering allowed
      page.setQERR(0x00); // All tasks are processed normally after a task returns CHECK CONDITION
      page.setRAC(false); // BUSY status may be returned regardless of BUSY TIMEOUT PERIOD
      page.setUA_INTLCK_CTRL(0x00); // UA not established for BUSY, TASK SET FULL, or RESERVATION CONFLICT
      page.setSWP(false); // Write protect is disabled
      page.setATO(false); // LOGICAL BLOCK APPLICATION TAG field will not be modified by server
      page.setTAS(false); // Tasks are aborted silently
      page.setAUTOLOAD_MODE(0x00); // Field is reserved
      page.setBUSY_TIMEOUT_PERIOD(0xFFFF); // Client shall allow BUSY status for unlimited period
      page.setEXTENDED_SELF_TEST_COMPLETION_TIME(587); // Arbitrary value; self test not supported
      
      this.setControl(page);
   }
   
   
   protected void populateInformationalExceptionsControl()
   {
      InformationalExceptionsControl page = new InformationalExceptionsControl();
      
      page.setPERF(true); // Informational exception operations will not cause delays
      page.setEBF(true); // Background functions are enabled
      page.setEWASC(false); // Warnings are not enabled
      page.setDEXCPT(false); // Failure prediction threshold reporting disabled
      page.setTEST(false); // Test device failure notifications are disabled
      page.setLOGERR(false); // Informational exception conditions are not logged.
      page.setMRIE(0x00); // No reporting of informational exception condition
      page.setIntervalTimer(0);
      page.setReportCount(0);
      
      
      this.setInformationalExceptionsControl(page);
   }
   
   
   protected void populateReadWriteErrorRecovery()
   {
      ReadWriteErrorRecovery page = new ReadWriteErrorRecovery();
      
      page.setAWRE(true); // Automatic write reallocation enabled
      page.setARRE(false); // Automatic read reallocation disabled
      page.setTB(false); // Partially recovered blocks not transfered before CHECK CONDITION
      page.setRC(false); // Data not transfered before error recovery attempt
      page.setEER(false); // Most complete error recovery used
      page.setPER(false); // Device will not report recovered errors
      page.setDTE(false); // Data transfer will not terminate on detection of a recovered error
      page.setDCR(false); // Device does not use ECC for error recovery
      page.setReadRetryCount(0);
      page.setWriteRetryCount(0);
      page.setRecoveryTimeLimit(0);
      
      this.setReadWriteErrorRecovery(page);
   }
   
}
