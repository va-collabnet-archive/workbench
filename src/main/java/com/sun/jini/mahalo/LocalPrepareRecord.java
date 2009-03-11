package com.sun.jini.mahalo;


class LocalPrepareRecord extends LocalParticipantModRecord {
    static final long serialVersionUID = 7771643811455951474L;

    LocalPrepareRecord(LocalParticipantHandle part, int result) {
	super(part, result);
    }
}
