package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.tapi.TerminologyException;

public class EIdentifierVersionUuid extends EIdentifierVersion {

	private long mostSigBits;
	private long leastSigBits;
	
	public EIdentifierVersionUuid(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super(in);
		mostSigBits = in.readLong();
		leastSigBits = in.readLong();
	}

	public EIdentifierVersionUuid(I_IdPart idp) throws TerminologyException, IOException {
		UUID denotation = (UUID) idp.getDenotation();
		mostSigBits = denotation.getMostSignificantBits();
		leastSigBits = denotation.getLeastSignificantBits();
		authorityUuid = nidToUuid(idp.getAuthorityNid());
		pathUuid = nidToUuid(idp.getPathId());
		statusUuid = nidToUuid(idp.getStatusId());
		time = idp.getTime();
	}

	@Override
	public void writeDenotation(ObjectOutput out) throws IOException {
		out.writeLong(mostSigBits);
		out.writeLong(leastSigBits);
	}

	@Override
	public UUID getDenotation() {
		return new UUID(mostSigBits, leastSigBits);
	}
}
