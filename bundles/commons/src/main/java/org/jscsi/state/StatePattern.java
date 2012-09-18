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
package org.jscsi.state;

/**
 * This enumeration represents the order in which states can be called.
 * If a state has a following state you can use nextState(), to see
 * what that state is. The pattern is build according to
 * 
 * @author Andreas Rain
 * 
 */
public enum StatePattern {

    /** Login request state */
    LoginRequestState,
    /** Login response state */
    LoginResponseState,
    /** Capacity request state */
    CapacityRequestState,
    /** Capacity response state */
    CapacityResponseState,
    /** Read request state */
    ReadRequestState,
    /** Read request state */
    ReadResponseState,
    /** Write request state */
    WriteRequestState,
    /** Write first burst state */
    WriteFirstBurstState,
    /** Write second response state */
    WriteSecondResponseState,
    /** Write second burst state */
    WriteSecondBurstState,
    /** Logout request state */
    LogoutRequestState,
    /** Logout response state */
    LogoutResponseState,
    /** Waiting for work state */
    WaitingForWorkState,
    /** Connection closed state */
    ConnectionClosedState;

    /** This state is the initial state in the process */
    public static final StatePattern intial = LoginRequestState;

    /**
     * This method returns the following state(s)
     * Multiple states mean that there is more than one
     * state possible for the follow up.
     * 
     * @param StatePattern
     *            s
     * @return StatePattern[]
     */
    public static StatePattern[] nextState(StatePattern s) {

        switch (s) {
        case LoginRequestState:
            return new StatePattern[] {
                LoginResponseState
            };
        case LoginResponseState:
            return new StatePattern[] {
                CapacityRequestState
            };
        case CapacityRequestState:
            return new StatePattern[] {
                CapacityResponseState
            };
        case CapacityResponseState:
            return new StatePattern[] {
                WaitingForWorkState
            };
        case WaitingForWorkState:
            return new StatePattern[] {
                ReadRequestState, WriteRequestState, LogoutRequestState
            };
        case ReadRequestState:
            return new StatePattern[] {
                ReadResponseState
            };
        case ReadResponseState:
            return new StatePattern[] {
                WaitingForWorkState
            };
        case WriteRequestState:
            return new StatePattern[] {
                WriteFirstBurstState, WriteSecondResponseState
            };
        case WriteFirstBurstState:
            return new StatePattern[] {
                WriteSecondResponseState
            };
        case WriteSecondBurstState:
            return new StatePattern[] {
                WriteSecondResponseState
            };
        case WriteSecondResponseState:
            return new StatePattern[] {
                WriteSecondBurstState, WaitingForWorkState
            };
        case LogoutRequestState:
            return new StatePattern[] {
                LogoutResponseState
            };
        case LogoutResponseState:
            return new StatePattern[] {
                ConnectionClosedState
            };

        default:
            break;
        }

        return null;
    }

}
