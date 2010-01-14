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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jini.mahalo.log.CannotRecoverException;

/**
 * A <code>CommitRecord</code> represents the logged state of
 * a <code>Transaction</code> which has moved to the COMMITTED
 * state.
 *
 * @author Sun Microsystems, Inc.
 *
 */
class LocalCommitRecord implements LocalTxnLogRecord {
    static final long serialVersionUID = 5706802011705258126L;

    static final Logger logger = TxnManagerImpl.participantLogger;

    /**
     * @serial
     */
    LocalParticipantHandle[] parts; //Note: Use an array of ParticipantHandles;

    //      We want a list of things.  By using
    //      an array, we can use the type system
    //      to guarantee that each thing is a
    //      LocalParticipantHandle rather than checking
    //      explicitly.	

    /**
     * Constructs an <code>CommitRecord</code> which  represents a
     * <code>Transaction</code> which has moved to the COMMITTED state.
     *
     * @param parts The array of participants joined in the transaction
     *
     * @see net.jini.core.transaction.Transaction
     * @see net.jini.core.transaction.server.TransactionParticipant
     * @see net.jini.core.transaction.server.TransactionConstants
     */
    LocalCommitRecord(LocalParticipantHandle parts[]) {
        //Note: the state is implied in the
        //      class name

        if (parts == null)
            throw new IllegalArgumentException("CommitRecord: must specify "
                + "a non-null parts array");

        this.parts = parts;
    }

    /**
     * Retrieves the set of <code>TransactionParticipant</code>s associated 
     * with the recovered <code>Transaction</code>.
     *
     */
    LocalParticipantHandle[] getParts() {
        return parts;
    }

    /**
     * Recovers the state encapsulated the <code>CommitRecord</code> to
     * the caller.
     *
     * @param tmt  The <code>TxnManagerTransaction</code> to which
     *             state is recovered.
     *
     * @see com.sun.jini.mahalo.TxnManagerTransaction
     */
    public void recover(LocalTxnManagerTransaction tmt)
            throws CannotRecoverException {
        try {
            for (int i = 0; i < parts.length; i++) {
                tmt.add(parts[i]);
            }
            tmt.modifyTxnState(VOTING);
        } catch (InternalManagerException ime) {
            throw new CannotRecoverException("CommitRecord: recover: "
                + ime.getMessage());
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "CommitRecord:recover recovered");
        }
    }

}
