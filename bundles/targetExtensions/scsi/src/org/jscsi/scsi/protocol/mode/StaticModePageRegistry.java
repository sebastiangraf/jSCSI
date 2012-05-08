/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
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
//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information: 
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

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
      this.populateControlExtension();
      this.populateInformationalExceptionsControl();
      this.populateReadWriteErrorRecovery();

      //register(BackgroundControl.PAGE_CODE, BackgroundControl.SUBPAGE_CODE, backgroundControl);
      register(Caching.PAGE_CODE, caching);
      register(Control.PAGE_CODE, control);
      register(ControlExtension.PAGE_CODE, ControlExtension.SUBPAGE_CODE, controlExtension);
      //register(DisconnectReconnect.PAGE_CODE, disconnectReconnect);
      register(InformationalExceptionsControl.PAGE_CODE, informationalExceptionsControl);
      //register(PowerCondition.PAGE_CODE, powerCondition);
      register(ReadWriteErrorRecovery.PAGE_CODE, readWriteErrorRecovery);
   }

   protected void populateCaching()
   {
      Caching page = new Caching();

      page.setIC(false); // Device using server-specific caching algorithm
      page.setABPF(true); // Aborts pre-fetch on receipt of new command (DRA = 0)
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
      page.setQueueAlgorithmModifier(0x01); // Unrestricted reordering allowed
      page.setQERR(0x00); // All tasks are processed normally after a task returns CHECK CONDITION
      page.setRAC(false); // BUSY status may be returned regardless of BUSY TIMEOUT PERIOD
      page.setUA_INTLCK_CTRL(0x00); // UA not established for BUSY, TASK SET FULL, or RESERVATION CONFLICT
      page.setSWP(false); // Write protect is disabled
      page.setATO(false); // LOGICAL BLOCK APPLICATION TAG field will not be modified by server
      page.setTAS(false); // Tasks are aborted silently
      page.setAutoloadMode(0x00); // Field is reserved
      page.setBusyTimeoutPeriod(0xFFFF); // Client shall allow BUSY status for unlimited period
      page.setExtendedSelfTestCompletionTime(587); // Arbitrary value; self test not supported

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

   protected void populateControlExtension()
   {
      ControlExtension page = new ControlExtension();

      page.setTCMOS(false);
      page.setSCSIP(false);
      page.setIALUAE(false);
      page.setInitialPriority(0);

      this.setControlExtension(page);
   }

}
