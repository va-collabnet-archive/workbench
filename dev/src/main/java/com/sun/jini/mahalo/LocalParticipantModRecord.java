package com.sun.jini.mahalo;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jini.mahalo.log.CannotRecoverException;

/**
 * A <code>LogRecord</code> which encapsulates a generic
 * interaction with a participant.
 *
 * @author Sun Microsystems, Inc.
 *
 */
class LocalParticipantModRecord implements TxnLogRecord {
    static final long serialVersionUID = 5542043673924560855L;

    /** Logger for operations related messages */
    private static final Logger operationsLogger = 
        TxnManagerImpl.operationsLogger;

    /**
     * @serial
     */
    private LocalParticipantHandle part;

    /**
     * @serial
     */
    private int result;

    LocalParticipantModRecord(LocalParticipantHandle part, int result) {
	if (part == null)
	    throw new IllegalArgumentException("ParticipantModRecord: " +
			    "recover: non-null ParticipantHandle " +
						"recover attempted");

	this.part = part;
	this.result = result;
    }

    LocalParticipantHandle getPart() {
	return part;
    }

    int getResult() {
	return result;
    }

    public void recover(LocalTxnManagerTransaction tmt)
	throws CannotRecoverException
    {
        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.entering(ParticipantModRecord.class.getName(), 
	        "recover", tmt);
	}
	if (tmt == null)
	    throw new NullPointerException("ParticipantModRecord: recover: " +
			    "non-null transaction must be specified");

        tmt.modifyParticipant(getPart(), getResult());

	if (getResult() == ABORTED)
	    tmt.modifyTxnState(ABORTED);

        if (operationsLogger.isLoggable(Level.FINER)) {
            operationsLogger.exiting(ParticipantModRecord.class.getName(), 
	        "recover");
	}
    }

	public void recover(TxnManagerTransaction arg0) throws CannotRecoverException {
		throw new UnsupportedOperationException();
	}
}
