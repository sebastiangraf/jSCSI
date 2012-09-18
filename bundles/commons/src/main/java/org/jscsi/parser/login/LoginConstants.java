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
package org.jscsi.parser.login;

/**
 * <h1>LoginConstants</h1>
 * <p>
 * This class defines only constants, which are used by the Login classes.
 * 
 * @author Volker Wildi
 */
final class LoginConstants {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Current Stage bit mask. */
    static final int CSG_FLAG_MASK = 0x000C0000;

    /** Number of bits to shift to the current stage. */
    static final int CSG_BIT_SHIFT = 18;

    /** Next Stage bit mask. */
    static final int NSG_FLAG_MASK = 0x00030000;

    /** Bit mask, where the 11th and 12th bit are set. */
    static final int BIT_11_AND_12_FLAG_MASK = 0x00300000;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Hidden default constructor. */
    private LoginConstants() {

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
