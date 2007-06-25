package com.sun.jini.mahalo;

/**
 * A <code>LogRecord</code> which encapsulates a participant
 * being instructed to roll-back.
 *
 * @author Sun Microsystems, Inc.
 *
 */
class LocalParticipantAbortRecord extends LocalParticipantModRecord {
    static final long serialVersionUID = -5884802000474046591L;

    LocalParticipantAbortRecord(LocalParticipantHandle part) {
	super(part, ABORTED);
    }
}
