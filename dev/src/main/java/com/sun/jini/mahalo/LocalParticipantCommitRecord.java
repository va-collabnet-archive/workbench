package com.sun.jini.mahalo;



/**
 * A <code>LogRecord</code> which encapsulates a participant
 * being instructed to roll-forward.
 *
 * @author Sun Microsystems, Inc.
 *
 */

class LocalParticipantCommitRecord extends LocalParticipantModRecord {
    static final long serialVersionUID = -881052193077840308L;

    LocalParticipantCommitRecord(LocalParticipantHandle part) {
	super(part,COMMITTED);
    }
}
