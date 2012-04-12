package org.jscsi.target.settings;

/**
 * This class contains all keys and some common variables and other static,
 * final {@link String} objects used during text parameter negotiation.
 * 
 * @author Andreas Ergenzinger
 */
public final class TextKeyword {

    // keys
    public static final String AUTH_METHOD = "AuthMethod";
    public static final String DATA_DIGEST = "DataDigest";
    public static final String DATA_PDU_IN_ORDER = "DataPDUInOrder";
    public static final String DATA_SEQUENCE_IN_ORDER = "DataSequenceInOrder";
    public static final String DEFAULT_TIME_2_RETAIN = "DefaultTime2Retain";
    public static final String DEFAULT_TIME_2_WAIT = "DefaultTime2Wait";
    public static final String ERROR_RECOVERY_LEVEL = "ErrorRecoveryLevel";
    public static final String FIRST_BURST_LENGTH = "FirstBurstLength";
    public static final String HEADER_DIGEST = "HeaderDigest";
    public static final String IF_MARKER = "IFMarker";
    public static final String IF_MARK_INT = "IFMarkInt";
    public static final String IMMEDIATE_DATA = "ImmediateData";
    public static final String INITIAL_R_2_T = "InitialR2T";
    public static final String INITIATOR_ALIAS = "InitiatorAlias";
    public static final String INITIATOR_NAME = "InitiatorName";
    public static final String MAX_BURST_LENGTH = "MaxBurstLength";
    public static final String MAX_CONNECTIONS = "MaxConnections";
    public static final String MAX_OUTSTANDING_R_2_T = "MaxOutstandingR2T";
    public static final String MAX_RECV_DATA_SEGMENT_LENGTH = "MaxRecvDataSegmentLength";
    public static final String OF_MARKER = "OFMarker";
    public static final String OF_MARK_INT = "OFMarkInt";
    public static final String SEND_TARGETS = "SendTargets";
    public static final String SESSION_TYPE = "SessionType";
    public static final String TARGET_ADDRESS = "TargetAddress";
    public static final String TARGET_ALIAS = "TargetAlias";
    public static final String TARGET_NAME = "TargetName";
    public static final String TARGET_PORTAL_GROUP_TAG = "TargetPortalGroupTag";
    public static final String TIME_2_RETAIN = "Time2Retain";
    public static final String TIME_2_WAIT = "Time2Wait";

    // reserved value strings
    public static final String REJECT = "Reject";
    public static final String NOT_UNDERSTOOD = "NotUnderstood";
    public static final String IRRELEVANT = "Irrelevant";

    // boolean value strings
    public static final String YES = "Yes";
    public static final String NO = "No";

    // additional string values
    public static final String NONE = "None";
    public static final String CRC_32C = "CRC-32C";
    public static final String NORMAL = "Normal";
    public static final String DISCOVERY = "Discovery";
    public static final String ALL = "All";

    // last but not least
    public static final String EQUALS = "=";
    public static final String COMMA = ",";
    public static final String NULL_CHAR = Character.valueOf((char)0).toString();
    public static final String COLON = ":";

}
