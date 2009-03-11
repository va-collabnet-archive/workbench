package com.sun.jini.mahalo;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jini.mahalo.log.CannotRecoverException;

/**
 * An <code>AbortRecord</code> represents the logged state of
 * a <code>Transaction</code> which has changed to the ABORTED
 * state.
 *
 * @author Sun Microsystems, Inc.
 *
 */
class LocalAbortRecord implements TxnLogRecord  {
    /**
     * @serial
     */
    private LocalParticipantHandle[] parts;

    static final long serialVersionUID = -8121722031382234695L;

    static final Logger logger = TxnManagerImpl.participantLogger;

    /**
     * Constructs an <code>AbortRecord</code> which  represents a
     * <code>Transaction</code> which has moved to the ABORTED state.
     *
     * @param parts The array of participants joined in the transaction
     *
     * @see net.jini.core.transaction.Transaction
     * @see net.jini.core.transaction.server.TransactionParticipant
     * @see net.jini.core.transaction.server.TransactionConstants
     */
    LocalAbortRecord(LocalParticipantHandle[] parts) {
        if (parts == null)
            throw new IllegalArgumentException("AbortRecord: must specify " +
                                                "a non-null parts array");
	this.parts = parts;
    }

    /**
     * Recovers the state encapsulated the <code>AbortRecord</code> to
     * the caller.
     *
     * @param tmt  The <code>TxnManagerTransaction</code> to which
     *             state is recovered.
     *
     * @see com.sun.jini.mahalo.TxnManagerTransaction
     */
    public void recover(LocalTxnManagerTransaction tmt)
	throws CannotRecoverException
    {
        try {
            for (int i = 0; i< parts.length; i++) {
                tmt.add(parts[i]);
            }
	    tmt.modifyTxnState(ABORTED);
        } catch (InternalManagerException ime) {
            throw new CannotRecoverException("AbortRecord: recover: " +
                                                        ime.getMessage());
        }

	if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "AbortJob:recover recovered");
        }    
    }

    /**
     * Retrieves the set of <code>TransactionParticipant</code>s associated
     * with the recovered <code>Transaction</code>.
     *
     */
    LocalParticipantHandle[] getParts() {
        return parts;
    }

	public void recover(TxnManagerTransaction arg0) throws CannotRecoverException {
		throw new UnsupportedOperationException();
	}
}
