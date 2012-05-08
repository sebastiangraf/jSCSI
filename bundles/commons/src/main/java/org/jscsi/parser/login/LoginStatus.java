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
package org.jscsi.parser.login;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>LoginStatus</h1>
 * <p>
 * This enumeration contains only all defined Login Stati, which can send in a
 * login response message in the iSCSI Protocol (RFC3720).
 * <p>
 * <table border="1">
 * <tr>
 * <td>0</td>
 * <td>Success - indicates that the iSCSI target successfully received,
 * understood, and accepted the request. The numbering fields (
 * <code>StatSN</code>, <code>ExpCmdSN</code>, <code>MaxCmdSN</code>) are only
 * valid if Status-Class is <code>0</code>.</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>Redirection - indicates that the initiator must take further action to
 * complete the request. This is usually due to the target moving to a different
 * address. All of the redirection status class responses MUST return one or
 * more text key parameters of the type &quot;TargetAddress&quot;, which
 * indicates the target's new address. A redirection response MAY be issued by a
 * target prior or after completing a security negotiation if a security
 * negotiation is required. A redirection SHOULD be accepted by an initiator
 * even without having the target complete a security negotiation if any
 * security negotiation is required, and MUST be accepted by the initiator after
 * the completion of the security negotiation if any security negotiation is
 * required.</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>Initiator Error (not a format error) - indicates that the initiator most
 * likely caused the error. This MAY be due to a request for a resource for
 * which the initiator does not have permission. The request should not be tried
 * again.</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>Target Error - indicates that the target sees no errors in the
 * initiator’s Login Request, but is currently incapable of fulfilling the
 * request. The initiator may re-try the same Login Request later. The table
 * below shows all of the currently allocated status codes. The codes are in
 * hexadecimal; the first byte is the status class and the second byte is the
 * status detail.</td>
 * </tr>
 * </table>
 * 
 * @author Volker Wildi
 */
public enum LoginStatus {

    /**
     * Login is proceeding OK.
     * <p>
     * If the response <code>T</code> bit is <code>1</code> in both the request
     * and the matching response, and the <code>NSG</code> is
     * <code>FullFeaturePhase</code> in both the request and the matching
     * response, the Login Phase is finished and the initiator may proceed to
     * issue SCSI commands.
     */
    SUCCESS((short) 0x0000),

    /**
     * The requested iSCSI Target Name (ITN) has temporarily moved to the
     * address provided.
     */
    TARGET_MOVED_TEMPORARILY((short) 0x0101),

    /**
     * The requested ITN has permanently moved permanently to the address
     * provided.
     */
    TARGET_MOVED((short) 0x0102),

    /**
     * Miscellaneous iSCSI initiator errors.
     */
    INITIATOR_ERROR((short) 0x0200),

    /**
     * The initiator could not be successfully authenticated or target
     * authentication is not supported.
     */
    AUTHENTICATION_FAILURE((short) 0x0201),

    /**
     * The initiator is not allowed access to the given target.
     */
    AUTHORIZATION_FAILURE((short) 0x0202),

    /**
     * The requested ITN does not exist at this address.
     */
    NOT_FOUND((short) 0x0203),

    /**
     * The requested ITN has been removed and no forwarding address is provided.
     */
    TARGET_REMOVED((short) 0x0204),

    /**
     * The requested iSCSI version range is not supported by the target.
     */
    UNSUPPORTED_VERSION((short) 0x0205),

    /**
     * Too many connections on this SSID.
     */
    TOO_MANY_CONNECTIONS((short) 0x0206),

    /**
     * Missing parameters (e.g., iSCSI Initiator and/or Target Name).
     */
    MISSING_PARAMETER((short) 0x0207),

    /**
     * Target does not support session spanning to this connection (address).
     */
    CANNOT_INCLUDE_IN_SESSION((short) 0x0208),

    /**
     * Target does not support this type of of session or not from this
     * Initiator.
     */
    SESSION_TYPE_NOT_SUPPORTED((short) 0x0209),

    /**
     * Attempt to add a connection to a non-existent session.
     */
    SESSION_DOSE_NOT_EXIST((short) 0x020A),

    /**
     * Invalid Request type during Login.
     */
    INVALID_DURING_LOGIN((short) 0x020B),

    /**
     * Target hardware or software error.
     */
    TARGET_ERROR((short) 0x0300),

    /**
     * The iSCSI service or target is not currently operational.
     */
    SERVICE_UNAVAILABLE((short) 0x0301),

    /**
     * The target has insufficient session, connection, or other resources.
     */
    OUT_OF_RESOURCES((short) 0x0302);

    private final short value;

    private static Map<Short, LoginStatus> mapping;

    static {
        LoginStatus.mapping = new HashMap<Short, LoginStatus>();
        for (LoginStatus s : values()) {
            LoginStatus.mapping.put(s.value, s);
        }
    }

    private LoginStatus(final short newValue) {

        value = newValue;
    }

    /**
     * Returns the value of this enumeration.
     * 
     * @return The value of this enumeration.
     */
    public final short value() {

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
    public static final LoginStatus valueOf(final short value) {

        return LoginStatus.mapping.get(value);
    }
}
