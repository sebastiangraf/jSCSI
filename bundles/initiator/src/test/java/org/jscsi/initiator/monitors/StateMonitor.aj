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
package org.jscsi.initiator.monitors;

import junit.framework.Assert;

import org.jscsi.initiator.connection.state.CapacityRequestState;
import org.jscsi.initiator.connection.state.CapacityResponseState;
import org.jscsi.initiator.connection.state.IState;
import org.jscsi.initiator.connection.state.LoginRequestState;
import org.jscsi.initiator.connection.state.LoginResponseState;
import org.jscsi.initiator.connection.state.LogoutRequestState;
import org.jscsi.initiator.connection.state.LogoutResponseState;
import org.jscsi.initiator.connection.state.ReadRequestState;
import org.jscsi.initiator.connection.state.ReadResponseState;
import org.jscsi.initiator.connection.state.WriteFirstBurstState;
import org.jscsi.initiator.connection.state.WriteRequestState;
import org.jscsi.initiator.connection.state.WriteSecondBurstState;
import org.jscsi.initiator.connection.state.WriteSecondResponseState;
import org.jscsi.state.StatePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This aspect is used to keep
 * track of the states.
 * 
 * @author Andreas Rain
 * 
 */
public aspect StateMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger("StateMonitor");

    /** The current state int the process */
    private static StatePattern currentState = null;

    /**
     * Point cutting the execute method of the IState.
     * Looking at the state itself in the observations.
     * 
     * @param s
     */
    pointcut execute(IState s):
		call(* IState.execute()) && target(s);

    before(IState s) : execute(s){
        LOGGER.info("Executing state: " + s.toString());

        if (currentState == null) {
            Assert.assertTrue(s instanceof LoginRequestState);
            currentState = StatePattern.intial;
        } else {
            switch (currentState) {
            case LoginRequestState:
                Assert.assertTrue(s instanceof LoginResponseState);
                currentState = StatePattern.nextState(currentState)[0];
                break;
            case LoginResponseState:
                Assert.assertTrue(s instanceof CapacityRequestState);
                currentState = StatePattern.nextState(currentState)[0];
                break;
            case CapacityRequestState:
                Assert.assertTrue(s instanceof CapacityResponseState);
                currentState = StatePattern.nextState(currentState)[0];
                break;
            case CapacityResponseState:
                currentState = StatePattern.nextState(currentState)[0];
                break;
            case WaitingForWorkState:
                if (s instanceof ReadRequestState) {
                    currentState = StatePattern.nextState(currentState)[0];
                } else if (s instanceof WriteRequestState) {
                    currentState = StatePattern.nextState(currentState)[1];
                } else if (s instanceof LogoutRequestState) {
                    currentState = StatePattern.nextState(currentState)[2];
                }
                break;
            case ReadRequestState:
                Assert.assertTrue(s instanceof ReadResponseState);
                currentState = StatePattern.nextState(currentState)[0];
                break;
            case ReadResponseState:
                currentState = StatePattern.nextState(currentState)[0];
                break;
            case WriteRequestState:
                if (s instanceof WriteFirstBurstState) {
                    currentState = StatePattern.nextState(currentState)[0];
                } else if (s instanceof WriteSecondResponseState) {
                    currentState = StatePattern.nextState(currentState)[1];
                }
                break;
            case WriteFirstBurstState:
                Assert.assertTrue(s instanceof WriteSecondResponseState);
                currentState = StatePattern.nextState(currentState)[0];
                break;
            case WriteSecondBurstState:
                Assert.assertTrue(s instanceof WriteSecondResponseState);
                currentState = StatePattern.nextState(currentState)[0];
                break;
            case WriteSecondResponseState:
                if (s instanceof WriteSecondBurstState) {
                    currentState = StatePattern.nextState(currentState)[0];
                } else {
                    currentState = StatePattern.nextState(currentState)[1];
                }
                break;
            case LogoutRequestState:
                Assert.assertTrue(s instanceof LogoutResponseState);
                currentState = StatePattern.nextState(currentState)[0];
                break;

            default:
                break;
            }
        }

    }

    after(IState s) : execute(s){
        LOGGER.info("Finished state execution of " + s.toString());
    }
}
