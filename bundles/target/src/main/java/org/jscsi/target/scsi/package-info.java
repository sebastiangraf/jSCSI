/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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

/**
 * This package and its sub-packages contain all classes which represent SCSI byte
 * structures, elements consisting of one or more fields, which are used by SCSI
 * devices to exchange information.
 * <p/>
 * The class and member variable names in the sub-packages usually match those of the represented SCSI
 * elements and fields, respectively. This means that additional information about the purpose and correct
 * usage of these classes can be found in either the <code>SCSI Architecture Model (SAM)</code> or one of the
 * relevant command standards (<code>SCSI PRIMARY COMMANDS (SPC)</code> and <code>SCSI BLOCK
COMMANDS (SBC)</code>), using the
 * class name as a starting point.
 * <p/>
 * The following standards versions served as the basis for all classes from this package (some optional
 * functionality may have been omitted):
 * <ul>
 * <li><code>SAM-5 Revision 5</code></li>
 * <li><code>SPC-3 Revision 23</code></li>
 * <li><code>SBC-3 Revision 25</code></li>
 * </ul>
 * 
 * @author Andreas Ergenzinger, University of Konstanz
 */
package org.jscsi.target.scsi;

