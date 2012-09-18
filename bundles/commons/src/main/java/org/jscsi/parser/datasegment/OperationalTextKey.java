/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
package org.jscsi.parser.datasegment;

import java.util.HashMap;
import java.util.Map;

/**
 * This enumeration defines all valid operational text keys, which are defined
 * in the iSCSI STandard (RFC 3720).
 */
public enum OperationalTextKey {
    /**
     * Use: During Login - Security Negotiation Senders: Initiator and Target
     * Scope: connection AuthMethod = &lt;list-of-values&gt; The main item of
     * security negotiation is the authentication method (AuthMethod). The
     * authentication methods that can be used (appear in the list-of-values)
     * are either those listed in the following table or are vendor-unique
     * methods:
     * <p/>
     * <table border="1">
     * <tr>
     * <th>Name</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>KRB5</td>
     * <td>Kerberos V5 - defined in [RFC1510]</td>
     * </tr>
     * <tr>
     * <td>SPKM1</td>
     * <td>Simple Public-Key GSS-API Mechanism defined in [RFC2025]</td>
     * </tr>
     * <tr>
     * <td>SPKM2</td>
     * <td>Simple Public-Key GSS-API Mechanism defined in [RFC2025]</td>
     * </tr>
     * <tr>
     * <td>SRP</td>
     * <td>Secure Remote Password defined in [RFC2945]</td>
     * </tr>
     * <tr>
     * <td>CHAP</td>
     * <td>Challenge Handshake Authentication Protocol defined in [RFC1994]</td>
     * </tr>
     * <tr>
     * <td>None</td>
     * <td>No authentication</td>
     * </tr>
     * </table>
     * <br/>
     * The AuthMethod selection is followed by an "authentication exchange" specific to the authentication
     * method selected. The authentication method proposal may be made by either the initiator or the target.
     * However the initiator MUST make the first step specific to the selected authentication method as soon
     * as it is selected. It follows that if the target makes the authentication method proposal the initiator
     * sends the first keys(s) of the exchange together with its authentication method selection. The
     * authentication exchange authenticates the initiator to the target, and optionally, the target to the
     * initiator. Authentication is OPTIONAL to use but MUST be supported by the target and initiator. The
     * initiator and target MUST implement CHAP. All other authentication methods are OPTIONAL. Private or
     * public extension algorithms MAY also be negotiated for authentication methods. Whenever a private or
     * public extension algorithm is part of the default offer (the offer made in absence of explicit
     * administrative action) the implementer MUST ensure that CHAP is listed as an alternative in the default
     * offer and "None" is not part of the default offer. Extension authentication methods MUST be named using
     * one of the following two formats:
     * <ol>
     * <li>Z-reversed.vendor.dns_name.do_something=</li>
     * <li>Z&lt;#&gt;&lt;IANA-registered-string&gt;=</li>
     * </ol>
     * Authentication methods named using the Z- format are used as private extensions. Authentication methods
     * named using the Z# format are used as public extensions that must be registered with IANA and MUST be
     * described by an informational RFC. For all of the public or private extension authentication methods,
     * the method specific keys MUST conform to the format specified in Section 5.1 Text Format for
     * standard-label. To identify the vendor for private extension authentication methods, we suggest you use
     * the reversed DNS-name as a prefix to the proper digest names. The part of digest-name following Z- and
     * Z# MUST conform to the format for standard-label specified in Section 5.1 Text Format. Support for
     * public or private extension authentication methods is OPTIONAL. The following subsections define the
     * specific exchanges for each of the standardized authentication methods. As mentioned earlier the first
     * step is always done by the initiator.
     */
    AUTH_METHOD("AuthMethod"),

    /**
     * Use: IO <br/>
     * Senders: Initiator and Target<br/>
     * Scope: CO
     * <p/>
     * HeaderDigest = &lt;list-of-values&gt; <br/>
     * DataDigest = &lt;list-of-values&gt;
     * <p/>
     * Default is None for both HeaderDigest and DataDigest.
     * <p/>
     * Digests enable the checking of end-to-end, non-cryptographic data integrity beyond the integrity checks
     * provided by the link layers and the covering of the whole communication path including all elements
     * that may change the network level PDUs such as routers, switches, and proxies. <br/>
     * The following table lists cyclic integrity checksums that can be negotiated for the digests and that
     * MUST be implemented by every iSCSI initiator and target. These digest options only have error detection
     * significance.
     * <p/>
     * <table border="1">
     * <tr>
     * <th>Name</th>
     * <th>Description</th>
     * <th>Generator</th>
     * </tr>
     * <tr>
     * <td>CRC32CDigest</td>
     * <td>32 bit CRC</td>
     * <td>0x11edc6f41</td>
     * </tr>
     * <tr>
     * <td>None</td>
     * <td colspan="2">no digest</td>
     * </tr>
     * </table>
     * <br/>
     * The generator polynomial for this digest is given in hex-notation (e.g., 0x3b stands for 0011 1011 and
     * the polynomial is x**5+X**4+x**3+x+1).
     * <p/>
     * When the Initiator and Target agree on a digest, this digest MUST be used for every PDU in Full Feature
     * Phase. <br/>
     * Padding bytes, when present in a segment covered by a CRC, SHOULD be set to 0 and are included in the
     * CRC. <br/>
     * The CRC MUST be calculated by a method that produces the same results as the following process: <br/>
     * <ul>
     * <li>The PDU bits are considered as the coefficients of a polynomial M(x) of degree n-1; bit 7 of the
     * lowest numbered byte is considered the most significant bit (x^n-1), followed by bit 6 of the lowest
     * numbered byte through bit 0 of the highest numbered byte (x^0).</li>
     * <li>The most significant 32 bits are complemented.</li>
     * <li>The polynomial is multiplied by x^32 then divided by G(x). The generator polynomial produces a
     * remainder R(x) of degree <= 31.</li>
     * <li>The coefficients of R(x) are considered a 32 bit sequence.</li>
     * <li>The bit sequence is complemented and the result is the CRC.</li>
     * <li>The CRC bits are mapped into the digest word. The x^31 coefficient in bit 7 of the lowest numbered
     * byte of the digest continuing through to the byte up to the x^24 coefficient in bit 0 of the lowest
     * numbered byte, continuing with the x^23 coefficient in bit 7 of next byte through x^0 in bit 0 of the
     * highest numbered byte.</li>
     * <li>Computing the CRC over any segment (data or header) extended to include the CRC built using the
     * generator 0x11edc6f41 will always get the value 0x1c2d19ed as its final remainder (R(x)). This value is
     * given here in its polynomial form (i.e., not mapped as the digest word).</li>
     * </ul>
     * For a discussion about selection criteria for the CRC, see [RFC3385]. For a detailed analysis of the
     * iSCSI polynomial, see [Castagnoli93]. <br/>
     * Private or public extension algorithms MAY also be negotiated for digests. Whenever a private or public
     * digest extension algorithm is part of the default offer (the offer made in absence of explicit
     * administrative action) the implementer MUST ensure that CRC32CDigest is listed as an alternative in the
     * default offer and "None" is not part of the default offer. <br/>
     * Extension digest algorithms MUST be named using one of the following two formats: <br/>
     * <ol>
     * <li>Y-reversed.vendor.dns_name.do_something=</li>
     * <li>Y&lt;#&gt;&lt;IANA-registered-string&gt;=</li>
     * </ol>
     * Digests named using the Y- format are used for private purposes (unregistered). Digests named using the
     * Y# format (public extension) must be registered with IANA and MUST be described by an informational
     * RFC. <br/>
     * For private extension digests, to identify the vendor, we suggest you use the reversed DNS-name as a
     * prefix to the proper digest names. <br/>
     * The part of digest-name following Y- and Y# MUST conform to the format for standard-label specified in
     * Section 5.1 Text Format. Support for public or private extension digests is OPTIONAL.
     */
    HEADER_DIGEST("HeaderDigest"),

    /**
     * See for the details in the HEADER_DIGEST documentation above.
     */
    DATA_DIGEST("DataDigest"),

    /**
     * Use: LO <br/>
     * Senders: Initiator and Target<br/>
     * Scope: SW <br/>
     * Irrelevant when: SessionType=Discovery
     * <p/>
     * MaxConnections=&lt;numerical-value-from-1-to-65535&gt;
     * <p/>
     * Default is <code>1</code>. <br/>
     * Result function is Minimum.
     * <p/>
     * Initiator and target negotiate the maximum number of connections requested/acceptable.
     */
    MAX_CONNECTIONS("MaxConnections"),

    /**
     * Use: FFPO<br/>
     * Senders: Initiator<br/>
     * Scope: SW
     * <p/>
     * For a complete description, see Appendix D. - SendTargets Operation -.
     */
    SEND_TARGETS("SendTargets"),

    /**
     * Use: IO by initiator, FFPO by target - only as response to a SendTargets,
     * Declarative, Any-Stage <br/>
     * Senders: Initiator and Target <br/>
     * Scope: SW
     * <p/>
     * TargetName=&lt;iSCSI-name-value&gt;
     * <p/>
     * Examples: <br/>
     * TargetName=iqn.1993-11.com.disk-vendor:diskarrays.sn.45678 <br/>
     * TargetName=eui.020000023B040506
     * <p/>
     * The initiator of the TCP connection MUST provide this key to the remote endpoint in the first login
     * request if the initiator is not establishing a discovery session. The iSCSI Target Name specifies the
     * worldwide unique name of the target. <br/>
     * The TargetName key may also be returned by the "SendTargets" text request (which is its only use when
     * issued by a target). TargetName MUST not be redeclared within the login phase.
     */
    TARGET_NAME("TargetName"),

    /**
     * Use: IO, Declarative, Any-Stage <br/>
     * Senders: Initiator<br/>
     * Scope: SW
     * <p/>
     * InitiatorName=&lt;iSCSI-name-value&gt;
     * <p/>
     * Examples: <br/>
     * InitiatorName=iqn.1992-04.com.os-vendor.plan9:cdrom.12345 <br/>
     * InitiatorName=iqn.2001-02.com.ssp.users:customer235.host90
     * <p/>
     * The initiator of the TCP connection MUST provide this key to the remote endpoint at the first Login of
     * the Login Phase for every connection. The InitiatorName key enables the initiator to identify itself to
     * the remote endpoint. <br/>
     * InitiatorName MUST not be redeclared within the login phase.
     */
    INITIATOR_NAME("InitiatorName"),

    /**
     * Use: ALL, Declarative, Any-Stage <br/>
     * Senders: Target <br/>
     * Scope: SW <br/>
     * TargetAlias=&lt;iSCSI-local-name-value&gt;
     * <p/>
     * Examples: TargetAlias=Bob-s Disk <br/>
     * TargetAlias=Database Server 1 Log Disk <br/>
     * TargetAlias=Web Server 3 Disk 20
     * <p/>
     * If a target has been configured with a human-readable name or description, this name SHOULD be
     * communicated to the initiator during a Login Response PDU if SessionType=Normal (see Section 12.21
     * SessionType). This string is not used as an identifier, nor is it meant to be used for authentication
     * or authorization decisions. It can be displayed by the initiator’s user interface in a list of targets
     * to which it is connected.
     */
    TARGET_ALIAS("TargetAlias"),

    /**
     * Use: ALL, Declarative, Any-Stage <br/>
     * Senders: Initiator <br/>
     * Scope: SW
     * <p/>
     * InitiatorAlias=&lt;iSCSI-local-name-value&gt;
     * <p/>
     * Examples: <br/>
     * InitiatorAlias=Web Server 4 <br/>
     * InitiatorAlias=spyalley.nsa.gov <br/>
     * InitiatorAlias=Exchange Server
     * <p/>
     * If an initiator has been configured with a human-readable name or description, it SHOULD be
     * communicated to the target during a Login Request PDU. If not, the host name can be used instead. This
     * string is not used as an identifier, nor is meant to be used for authentication or authorization
     * decisions. It can be displayed by the target's user interface in a list of initiators to which it is
     * connected.
     */
    INITIATOR_ALIAS("InitiatorAlias"),

    /**
     * Use: ALL, Declarative, Any-Stage <br/>
     * Senders: Target <br/>
     * Scope: SW <br/>
     * TargetAddress=domainname[:port][,portal-group-tag]
     * <p/>
     * The domainname can be specified as either a DNS host name, a dotted-decimal IPv4 address, or a
     * bracketed IPv6 address as specified in [RFC2732]. <br/>
     * <br/>
     * If the TCP port is not specified, it is assumed to be the IANA-assigned default port for iSCSI (see
     * Section 13 IANA Considerations). <br/>
     * If the TargetAddress is returned as the result of a redirect status in a login response, the comma and
     * portal group tag MUST be omitted. If the TargetAddress is returned within a SendTargets response, the
     * portal group tag MUST be included.
     * <p/>
     * Examples: <br/>
     * TargetAddress=10.0.0.1:5003,1<br/>
     * TargetAddress=[1080:0:0:0:8:800:200C:417A],65<br/>
     * TargetAddress=[1080::8:800:200C:417A]:5003,1 <br/>
     * TargetAddress=computingcenter.example.com,23
     * <p/>
     * Use of the portal-group-tag is described in Appendix D. - SendTargets Operation -. The formats for the
     * port and portal-group-tag are the same as the one specified in Section 12.9 TargetPortalGroupTag.
     */
    TARGET_ADDRESS("TargetAddress"),

    /**
     * Use: IO by target, Declarative, Any-Stage <br/>
     * Senders: Target <br/>
     * Scope: SW
     * <p/>
     * TargetPortalGroupTag=&lt;16-bit-binary-value&gt;
     * <p/>
     * Examples: <br/>
     * TargetPortalGroupTag=1
     * <p/>
     * The target portal group tag is a 16-bit binary-value that uniquely identifies a portal group within an
     * iSCSI target node. This key carries the value of the tag of the portal group that is servicing the
     * Login request. The iSCSI target returns this key to the initiator in the Login Response PDU to the
     * first Login Request PDU that has the C bit set to 0 when TargetName is given by the initiator. <br/>
     * For the complete usage expectations of this key see Section 5.3 Login Phase.
     */
    TARGET_PORTAL_GROUP_TAG("TargetPortalGroupTag"),

    /**
     * Use: LO <br/>
     * Senders: Initiator and Target<br/>
     * Scope: SW <br/>
     * Irrelevant when: SessionType=Discovery
     * <p/>
     * InitialR2T=&lt;boolean-value&gt;
     * <p/>
     * Examples: <br/>
     * I-&gt;InitialR2T=No <br/>
     * T-&gt;InitialR2T=No
     * <p/>
     * Default is Yes. <br/>
     * Result function is OR.
     * <p/>
     * The InitialR2T key is used to turn off the default use of R2T for unidirectional and the output part of
     * bidirectional commands, thus allowing an initiator to start sending data to a target as if it has
     * received an initial R2T with Buffer Offset=Immediate Data Length and Desired Data Transfer
     * Length=(min(FirstBurstLength, Expected Data Transfer Length) - Received Immediate Data Length). <br/>
     * The default action is that R2T is required, unless both the initiator and the target send this key-pair
     * attribute specifying InitialR2T=No. Only the first outgoing data burst (immediate data and/or separate
     * PDUs) can be sent unsolicited (i.e., not requiring an explicit R2T).
     */
    INITIAL_R2T("InitialR2T"),

    /**
     * Use: LO <br/>
     * Senders: Initiator and Target <br/>
     * Scope: SW <br/>
     * Irrelevant when: SessionType=Discovery
     * <p/>
     * ImmediateData=&lt;boolean-value&gt;
     * <p/>
     * Default is Yes. <br/>
     * Result function is AND.
     * <p/>
     * The initiator and target negotiate support for immediate data. To turn immediate data off, the
     * initiator or target must state its desire to do so. ImmediateData can be turned on if both the
     * initiator and target have ImmediateData=Yes.<br/>
     * If ImmediateData is set to Yes and InitialR2T is set to Yes (default), then only immediate data are
     * accepted in the first burst. If ImmediateData is set to No and InitialR2T is set to Yes, then the
     * initiator MUST NOT send unsolicited data and the target MUST reject unsolicited data with the
     * corresponding response code. <br/>
     * If ImmediateData is set to No and InitialR2T is set to No, then the initiator MUST NOT send unsolicited
     * immediate data, but MAY send one unsolicited burst of Data-Out PDUs. <br/>
     * If ImmediateData is set to Yes and InitialR2T is set to No, then the initiator MAY send unsolicited
     * immediate data and/or one unsolicited burst of Data-Out PDUs.
     * <p/>
     * The following table is a summary of unsolicited data options: <br/>
     * <table * border="1">
     * <tr>
     * <th>InitialR2T</th>
     * <th>ImmediateData</th>
     * <th>Unsolicited Data Out PDUs</th>
     * <th>Immediate Data</th>
     * </tr>
     * <tr>
     * <td>No</td>
     * <td>No</td>
     * <td>Yes</td>
     * <td>No</td>
     * </tr>
     * <tr>
     * <td>No</td>
     * <td>Yes</td>
     * <td>Yes</td>
     * <td>Yes</td>
     * </tr>
     * <tr>
     * <td>Yes</td>
     * <td>No</td>
     * <td>No</td>
     * <td>No</td>
     * </tr>
     * <tr>
     * <td>Yes</td>
     * <td>Yes</td>
     * <td>No</td>
     * <td>Yes</td>
     * </tr>
     * </table>
     */
    IMMEDIATE_DATA("ImmediateData"),

    /**
     * Use: ALL, Declarative <br/>
     * Senders: Initiator and Target
     * <p/>
     * Scope: CO
     * <p/>
     * MaxRecvDataSegmentLength=&lt;numerical-value-512-to-(2**24-1)&gt; <br/>
     * <p/>
     * Default is 8192 bytes.
     * <p/>
     * The initiator or target declares the maximum data segment length in bytes it can receive in an iSCSI
     * PDU. <br/>
     * The transmitter (initiator or target) is required to send PDUs with a data segment that does not exceed
     * MaxRecvDataSegmentLength of the receiver. <br/>
     * A target receiver is additionally limited by MaxBurstLength for solicited data and FirstBurstLength for
     * unsolicited data. An initiator MUST NOT send solicited PDUs exceeding MaxBurstLength nor unsolicited
     * PDUs exceeding FirstBurstLength (or FirstBurstLength-Immediate Data Length if immediate data were
     * sent).
     */
    MAX_RECV_DATA_SEGMENT_LENGTH("MaxRecvDataSegmentLength"),

    /**
     * Use: LO <br/>
     * Senders: Initiator and Target <br/>
     * Scope: SW <br/>
     * Irrelevant when: SessionType=Discovery
     * <p/>
     * MaxBurstLength=&lt;numerical-value-512-to-(2**24-1)&gt;
     * <p/>
     * Default is <code>262144</code> (<code>256</code> Kbytes). <br/>
     * Result function is Minimum.
     * <p/>
     * The initiator and target negotiate maximum SCSI data payload in bytes in a Data-In or a solicited
     * Data-Out iSCSI sequence. A sequence consists of one or more consecutive Data-In or Data-Out PDUs that
     * end with a Data-In or Data-Out PDU with the F bit set to one.
     */
    MAX_BURST_LENGTH("MaxBurstLength"),

    /**
     * Use: LO <br/>
     * Senders: Initiator and Target <br/>
     * Scope: SW <br/>
     * Irrelevant when: SessionType=Discovery <br/>
     * Irrelevant when: ( InitialR2T=Yes and ImmediateData=No )
     * <p/>
     * FirstBurstLength=&lt;numerical-value-512-to-(2**24-1)&gt;
     * <p/>
     * Default is <code>65536</code> (<code>64</code> Kbytes).<br/>
     * Result function is Minimum.
     * <p/>
     * The initiator and target negotiate the maximum amount in bytes of unsolicited data an iSCSI initiator
     * may send to the target during the execution of a single SCSI command. This covers the immediate data
     * (if any) and the sequence of unsolicited Data-Out PDUs (if any) that follow the command.<br/>
     * FirstBurstLength MUST NOT exceed MaxBurstLength.
     */
    FIRST_BURST_LENGTH("FirstBurstLength"),

    /**
     * Use: LO <br/>
     * Senders: Initiator and Target <br/>
     * Scope: SW
     * <p/>
     * DefaultTime2Wait=&lt;numerical-value-0-to-3600&gt;
     * <p/>
     * Default is <code>2</code>. <br/>
     * Result function is Maximum.
     * <p/>
     * The initiator and target negotiate the minimum time, in seconds, to wait before attempting an
     * explicit/implicit logout or an active task reassignment after an unexpected connection termination or a
     * connection reset. <br/>
     * A value of <code>0</code> indicates that logout or active task reassignment can be attempted
     * immediately.
     */
    DEFAULT_TIME_2_WAIT("DefaultTime2Wait"),

    /**
     * Use: LO<br/>
     * Senders: Initiator and Target<br/>
     * Scope: SW
     * <p/>
     * DefaultTime2Retain=&lt;numerical-value-0-to-3600&gt;
     * <p/>
     * Default is <code>20</code>. Result function is Minimum.
     * <p/>
     * The initiator and target negotiate the maximum time, in seconds after an initial wait (Time2Wait),
     * before which an active task reassignment is still possible after an unexpected connection termination
     * or a connection reset. <br/>
     * This value is also the session state timeout if the connection in question is the last LOGGED_IN
     * connection in the session. A value of 0 indicates that connection/task state is immediately discarded
     * by the target.
     */
    DEFAULT_TIME_2_RETAIN("DefaultTime2Retain"),

    /**
     * Use: LO <br/>
     * Senders: Initiator and Target <br/>
     * Scope: SW
     * <p/>
     * MaxOutstandingR2T=&lt;numerical-value-from-1-to-65535&gt;
     * <p/>
     * Irrelevant when: SessionType=Discovery <br/>
     * Default is <code>1</code>. <br/>
     * Result function is Minimum.
     * <p/>
     * Initiator and target negotiate the maximum number of outstanding R2Ts per task, excluding any implied
     * initial R2T that might be part of that task. An R2T is considered outstanding until the last data PDU
     * (with the <code>F</code> bit set to <code>1</code>) is transferred, or a sequence reception timeout
     * (Section 6.1.4.1 Recovery Within-command) is encountered for that data sequence.
     */
    MAX_OUTSTANDING_R2T("MaxOutstandingR2T"),

    /**
     * Use: LO <br/>
     * Senders: Initiator and Target <br/>
     * Scope: SW <br/>
     * Irrelevant when: SessionType=Discovery
     * <p/>
     * DataPDUInOrder=&lt;boolean-value&gt;
     * <p/>
     * Default is Yes. <br/>
     * Result function is OR.
     * <p/>
     * No is used by iSCSI to indicate that the data PDUs within sequences can be in any order. Yes is used to
     * indicate that data PDUs within sequences have to be at continuously increasing addresses and overlays
     * are forbidden.
     */
    DATA_PDU_IN_ORDER("DataPDUInOrder"),

    /**
     * Use: LO <br/>
     * Senders: Initiator and Target <br/>
     * Scope: SW <br/>
     * Irrelevant when: SessionType=Discovery
     * <p/>
     * DataSequenceInOrder=&lt;boolean-value&gt;
     * <p/>
     * Default is Yes. <br/>
     * Result function is OR.
     * <p/>
     * A Data Sequence is a sequence of Data-In or Data-Out PDUs that end with a Data-In or Data-Out PDU with
     * the F bit set to one. A Data-Out sequence is sent either unsolicited or in response to an R2T.
     * Sequences cover an offset-range. <br/>
     * If DataSequenceInOrder is set to No, Data PDU sequences may be transferred in any order. <br/>
     * If DataSequenceInOrder is set to Yes, Data Sequences MUST be transferred using continuously
     * non-decreasing sequence offsets (R2T buffer offset for writes, or the smallest SCSI Data-In buffer
     * offset within a read data sequence). <br/>
     * If DataSequenceInOrder is set to Yes, a target may retry at most the last R2T, and an initiator may at
     * most request retransmission for the last read data sequence. For this reason, if ErrorRecoveryLevel is
     * not <code>0</code> and DataSequenceInOrder is set to Yes then MaxOustandingR2T MUST be set to
     * <code>1</code>.
     */
    DATA_SEQUENCE_IN_ORDER("DataSequenceInOrder"),

    /**
     * Use: LO
     * <p>
     * Senders: Initiator and Target
     * <p>
     * Scope: SW
     * <p>
     * <p>
     * ErrorRecoveryLevel=&lt;numerical-value-0-to-2&gt;
     * <p>
     * Default is <code>0</code>.
     * <p>
     * Result function is Minimum.
     * <p>
     * The initiator and target negotiate the recovery level supported. Recovery levels represent a
     * combination of recovery capabilities. Each recovery level includes all the capabilities of the lower
     * recovery levels and adds some new ones to them.
     * <p>
     * In the description of recovery mechanisms, certain recovery classes are specified. Section 6.1.5 Error
     * Recovery Hierarchy describes the mapping between the classes and the levels.
     */
    ERROR_RECOVERY_LEVEL("ErrorRecoveryLevel"),

    /**
     * Use: LO, Declarative, Any-Stage
     * <p>
     * Senders: Initiator
     * <p>
     * Scope: SW
     * <p>>
     * SessionType= &lt;Discovery|Normal&gt;
     * <p/>
     * Default is Normal.
     * <p>
     * The initiator indicates the type of session it wants to create. The target can either accept it or
     * reject it.
     * <p>
     * A discovery session indicates to the Target that the only purpose of this Session is discovery. The
     * only requests a target accepts in this type of session are a text request with a SendTargets key and a
     * logout request with reason "close the session".
     * <p>
     * The discovery session implies MaxConnections = <code>1</code> and overrides both the default and an
     * explicit setting.
     */
    SESSION_TYPE("SessionType"),

    /**
     * Use: LO, Declarative, Any-Stage
     * <p>
     * Senders: Initiator
     * <p>
     * Scope: SW
     * <p>>
     * SessionBalancer= &lt;RoundRobinSessionBalancer|ParallelSessionBalancer&gt;
     * <p/>
     * Default is RoundRobinSessionBalancer.
     * <p>
     * The SessionBalancer to use;
     * <p>
     */
    SESSION_BALANCER("SessionBalancer"),

    /**
     * OFMarker = <code>&lt;boolean-value&gt;</code>
     * <p>
     * IFMarker = <code>&lt;boolean-value&gt;</code>
     * <p>
     * Default is <code>No</code>. Result function is <code>AND</code>.
     * <p>
     * IFMarker is used to turn on or off the target to initiator markers on the connection.
     * <p>
     * Examples:
     * <p>
     * I-&gt;OFMarker=Yes,IFMarker=Yes
     * <p>
     * T-&gt;OFMarker=Yes,IFMarker=Yes
     * <p>
     * Results in the Marker being used in both directions while:
     * <p>
     * I->OFMarker=Yes,IFMarker=Yes
     * <p>
     * T-&gt;OFMarker=Yes,IFMarker=No
     * <p>
     * Results in Marker being used from the initiator to the target, but not from the target to initiator.
     */
    IF_MARKER("IFMarker"),

    /**
     * OFMarkInt is Irrelevant when: OFMarker = <code>No</code>
     * <p>
     * IFMarkInt is Irrelevant when: IFMarker = <code>No</code>
     * <p>
     * Offering:
     * <p>
     * OFMarkInt=<code>&lt;numeric-range-from-1-to-65535&gt;</code><br/>
     * IFMarkInt=<code>&lt;numeric-range-from-1-to-65535&gt;</code>
     * <p>
     * Responding:
     * <p>
     * OFMarkInt=<code>&lt;numeric-value-from-1-to-65535&gt|Reject</code><br/>
     * IFMarkInt=<code>&lt;numeric-value-from-1-to-65535&gt;|Reject</code>
     * <p>
     * IFMarkInt is used to set the interval for the target to initiator markers on the connection.
     * <p>
     * For the offering, the initiator or target indicates the minimum to maximum interval (in 4-byte words)
     * it wants the markers for one or both directions. In case it only wants a specific value, only a single
     * value has to be specified. The responder selects a value within the minimum and maximum offered or the
     * only value offered or indicates through the xFMarker key=value its inability to set and/or receive
     * markers. When the interval is unacceptable the responder answers with "Reject". Reject is resetting the
     * marker function in the specified direction (Output or Input) to No.
     * <p>
     * The interval is measured from the end of a marker to the beginning of the next marker. For example, a
     * value of <code>1024</code> means <code>1024</code> words (<code>4096</code> bytes of iSCSI payload
     * between markers).
     * <p>
     * The default is <code>2048</code>.
     * 
     * @see #IF_MARKER
     */
    IF_MARK_INT("IFMarkInt"),

    /**
     * OFMarker is used to turn on or off the initiator to target markers on the
     * connection.
     * <p>
     * 
     * @see #IF_MARKER
     */
    OF_MARKER("OFMarker"),

    /**
     * OFMarkInt is used to set the interval for the initiator to target markers
     * on the connection.
     * 
     * @see #OF_MARKER
     * @see #IF_MARK_INT
     */
    OF_MARK_INT("OFMarkInt");

    private final String value;

    private static Map<String, OperationalTextKey> mapping;

    static {
        OperationalTextKey.mapping = new HashMap<String, OperationalTextKey>();
        for (OperationalTextKey s : values()) {
            OperationalTextKey.mapping.put(s.value, s);
        }
    }

    private OperationalTextKey(final String newValue) {

        value = newValue;
    }

    /**
     * Returns the value of this enumeration.
     * 
     * @return The value of this enumeration.
     */
    public final String value() {

        return value;
    }

    /**
     * Returns the constant defined for the given <code>value</code>.
     * 
     * @param value
     *            The value to search for.
     * @return The constant defined for the given <code>value</code>. Or <code>null</code>, if this value is
     *         not defined by this
     *         enumeration.
     */
    public static final OperationalTextKey valueOfEx(final String value) {

        return OperationalTextKey.mapping.get(value);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
