package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.tapi.TerminologyException;

public class EIdentifierVersionLong extends EIdentifierVersion {

	private long denotation;
	
	public EIdentifierVersionLong(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super(in);
		denotation = in.readLong();
	}

	public EIdentifierVersionLong(I_IdPart idp) throws TerminologyException, IOException {
		super();
		denotation = (Long) idp.getDenotation();
		authorityUuid = nidToUuid(idp.getAuthorityNid());
		pathUuid = nidToUuid(idp.getPathId());
		statusUuid = nidToUuid(idp.getStatusId());
		time = idp.getTime();
	}

	@Override
	public void writeDenotation(ObjectOutput out) throws IOException {
		out.writeLong(denotation);
	}

	@Override
	public Long getDenotation() {
		return denotation;
	}
}
