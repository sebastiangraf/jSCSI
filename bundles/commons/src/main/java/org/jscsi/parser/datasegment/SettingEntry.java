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

/**
 * <h1>SettingsMap</h1>
 * <p>
 * This class encapsulates all informations of one setting of the configuration file.
 * 
 * @author Volker Wildi
 */

public final class SettingEntry {
    /** Scope identifier. */
    private String scope;

    /** Result function. */
    private IResultFunction result;

    /** Value of the parameter. */
    private String value;

    /**
     * Default constructor to create a new, empty <code>SettingEntry</code>.
     */
    public SettingEntry() {

    }

    /**
     * Sets the result function of this object to the given one.
     * 
     * @param newResult
     *            The new result function.
     */
    public final void setResult(final IResultFunction newResult) {

        result = newResult;
    }

    /**
     * Sets the scope of this object to the given one.
     * 
     * @param newScope
     *            The new scope.
     */
    public void setScope(final String newScope) {

        scope = newScope;
    }

    /**
     * Sets the value of this object to the given one.
     * 
     * @param newValue
     *            The new value.
     */
    public void setValue(final String newValue) {

        value = newValue;
    }

    /**
     * Returns the scope of this object.
     * 
     * @return The scope of this object.
     */
    public String getScope() {

        return scope;
    }

    /**
     * Returns the result function of this object.
     * 
     * @return The result function of this object.
     */
    public IResultFunction getResult() {

        return result;
    }

    /**
     * Returns the value of this object.
     * 
     * @return The value of this object.
     */
    public String getValue() {

        return value;
    }
}
