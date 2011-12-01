package org.jscsi.target.connection.phase;

import java.io.IOException;
import java.security.DigestException;

import javax.naming.OperationNotSupportedException;

import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.target.connection.TargetConnection;
import org.jscsi.target.connection.stage.login.LoginOperationalParameterNegotiationStage;
import org.jscsi.target.connection.stage.login.SecurityNegotiationStage;
import org.jscsi.target.connection.stage.login.TargetLoginStage;
import org.jscsi.target.settings.ConnectionSettingsNegotiator;
import org.jscsi.target.settings.SettingsException;

/**
 * Objects of this class represent the Target Login Phase of a connection.
 * @see TargetPhase
 * @author Andreas Ergenzinger
 */
public final class TargetLoginPhase extends TargetPhase {
	
	/**
	 * The current stage of this phase
	 */
	private TargetLoginStage stage;
	
	/**
	 * This variable indicates if the initiator is to be considered as
	 * authenticated, i.e. if it has given sufficient proof of its identity
	 * to proceed to the next (Target Full Feature) phase.
	 * <p>
	 * Currently the jSCSI Target does not support any authentication methods
	 * and this value is initialized to <code>true</code> for all initiators. 
	 */
	private boolean authenticated = true;//TODO false if authentication required
	
	/**
	 * This variable will be <code>true</code> until the first call of
	 * {@link #getFirstPduAndSetToFalse()} has happened.
	 * <p>
	 * This value will be <code>true</code> if the currently processed PDU
	 * is the first PDU sent by the initiator over this phase's connection.
	 * This means that it must contain all text parameters necessary for
	 * either starting a discovery session or a normal session.
	 */
	private boolean firstPdu = true;
	
	/**
	 * The constructor.
	 * @param connection {@inheritDoc}
	 */
	public TargetLoginPhase(TargetConnection connection) {
		super(connection);
	}
	
	
	/**
	 * Starts the login phase.
	 * @param pdu {@inheritDoc}
	 * @return {@inheritDoc}
	 * @throws OperationNotSupportedException {@inheritDoc}
	 * @throws IOException {@inheritDoc}
	 * @throws InterruptedException {@inheritDoc}
	 * @throws InternetSCSIException {@inheritDoc}
	 * @throws DigestException {@inheritDoc}
	 * @throws SettingsException {@inheritDoc}
	 */
	@Override
	public boolean execute(ProtocolDataUnit pdu) throws IOException,
	InterruptedException, InternetSCSIException, DigestException,
	SettingsException {
		
		//begin login negotiation
		final ConnectionSettingsNegotiator negotiator =
			connection.getConnectionSettingsNegotiator();
		while(!negotiator.beginNegotiation()) {
			//do nothing, just wait for permission to begin, method is blocking
		}
		
		boolean loginSuccessful = true;//will determine if settings are committed 
		
		try {
			//if possible, enter LOPN Stage
			BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
			LoginRequestParser parser = (LoginRequestParser) bhs.getParser();
			
			LoginStage nextStageNumber;//will store return value from the last login stage
			
			//Security Negotiation Stage (optional)
			if ( parser.getCurrentStageNumber() == LoginStage.SECURITY_NEGOTIATION) {
				//complete SNS
				stage = new SecurityNegotiationStage(this);
				stage.execute(pdu);
				nextStageNumber = stage.getNextStageNumber();
				
				if (nextStageNumber != null)
					authenticated = true;
				else {
					loginSuccessful = false;
					return false;
				}
				
				if (nextStageNumber == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION) {
					//receive first PDU from LOPNS
					pdu = connection.receivePdu();
					bhs = pdu.getBasicHeaderSegment();
					parser = (LoginRequestParser) bhs.getParser();
				} else if (nextStageNumber == LoginStage.FULL_FEATURE_PHASE){
					//we are done here
					return true;
				} else {
					//should be unreachable, since SNS may not return NSG==SNS
					loginSuccessful = false;
					return false;
				}
			}
			
			//Login Operational Parameter Negotiation Stage (also optional, but
			//either SNS or LOPNS must be passed before proceeding to FFP)
			if (authenticated && parser.getCurrentStageNumber() ==
				LoginStage.LOGIN_OPERATIONAL_NEGOTIATION) {
				stage = new LoginOperationalParameterNegotiationStage(this);
				stage.execute(pdu);
				nextStageNumber = stage.getNextStageNumber();
				if (nextStageNumber == LoginStage.FULL_FEATURE_PHASE)
					return true;
			}
			//else
			loginSuccessful = false;
			return false;
		} catch (DigestException e) {
			loginSuccessful = false;
			throw e;
		} catch (IOException e) {
			loginSuccessful = false;
			throw e;
		} catch (InterruptedException e) {
			loginSuccessful = false;
			throw e;
		} catch (InternetSCSIException e) {
			loginSuccessful = false;
			throw e;
		} catch (SettingsException e) {
			loginSuccessful = false;
			throw e;
		} finally {
			//commit or roll back changes and release exclusive negotiator lock
			negotiator.finishNegotiation(loginSuccessful);
		}
	}
	
	/**
	 * This method will return <code>true</code> if currently processed
	 * PDU is the first PDU sent by the initiator over this phase's
	 * connection. Subsequent calls will always return <code>false</code>.
	 * @return <code>true</code> if and only if this method is called for
	 * the first time
	 */
	public boolean getFirstPduAndSetToFalse() {
		if (!firstPdu)
			return false;
		firstPdu = false;
		return true;
	}
	
	public final boolean getAuthenticated() {
		return authenticated;
	}
}
