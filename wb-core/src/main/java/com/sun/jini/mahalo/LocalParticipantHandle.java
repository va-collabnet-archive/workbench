/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.jini.mahalo;

import java.rmi.RemoteException;

import net.jini.core.transaction.server.TransactionConstants;
import net.jini.core.transaction.server.TransactionParticipant;
import net.jini.security.ProxyPreparer;

/**
 * 
 * @author Sun Microsystems, Inc.
 * 
 */

class LocalParticipantHandle implements TransactionConstants {
    static final long serialVersionUID = -1776073824495304317L;

    /**
     * Cached reference to prepared participant.
     */
    private TransactionParticipant preparedPart;

    /**
     * @serial
     */
    private long crashcount = 0;

    /**
     * @serial
     */
    private int prepstate;

    /**
     * Create a new node that is equivalent to that node
     */
    LocalParticipantHandle(TransactionParticipant preparedPart, long crashcount) throws RemoteException {
        if (preparedPart == null)
            throw new NullPointerException("TransactionParticipant argument cannot be null");
        this.preparedPart = preparedPart;
        this.crashcount = crashcount;
        this.prepstate = ACTIVE;
    }

    long getCrashCount() {
        return crashcount;
    }

    synchronized TransactionParticipant getPreParedParticipant() {
        return preparedPart;
    }

    // Only called by service initialization code
    void restoreTransientState(ProxyPreparer recoveredListenerPreparer) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    synchronized void setPrepState(int state) {
        switch (state) {
        case PREPARED:
        case NOTCHANGED:
        case COMMITTED:
        case ABORTED:
            break;
        default:
            throw new IllegalArgumentException("LocalParticipantHandle: " + "setPrepState: cannot set to "
                + com.sun.jini.constants.TxnConstants.getName(state));
        }

        this.prepstate = state;
    }

    synchronized int getPrepState() {
        return prepstate;
    }

    boolean compareTo(LocalParticipantHandle other) {
        return this.equals(other);
    }

    /**
     * Return the <code>hashCode</code> of the
     * embedded <code>TransactionParticipant</code>.
     */
    public int hashCode() {
        return preparedPart.hashCode();
    }

    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null)
            return false;
        if (that.getClass() != getClass())
            return false;

        LocalParticipantHandle h = (LocalParticipantHandle) that;
        return preparedPart.equals(h.preparedPart);
    }
}
