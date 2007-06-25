package com.sun.jini.mahalo;

/**
 * A <code>LogRecord</code> which encapsulates a participant
 * being instructed to perform the prepareAndCommit optimization.
 *
 * @author Sun Microsystems, Inc.
 *
 */
class LocalPrepareAndCommitRecord extends LocalParticipantModRecord {
    static final long serialVersionUID = -4355088085028784921L;

    LocalPrepareAndCommitRecord(LocalParticipantHandle part, int result)
    {
	super(part, result);
    }
}

