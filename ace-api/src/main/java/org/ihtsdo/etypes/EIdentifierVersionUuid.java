package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.TerminologyException;

public class EIdentifierVersionUuid extends EIdentifierVersion {

	private static UUID primordialAuthority;
	private UUID denotation;
	
	public EIdentifierVersionUuid(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super(in);
		denotation = new UUID(in.readLong(), in.readLong());
	}

	public EIdentifierVersionUuid(I_IdPart idp) throws TerminologyException, IOException {
		denotation = (UUID) idp.getDenotation();
		authorityUuid = nidToUuid(idp.getAuthorityNid());
		pathUuid = nidToUuid(idp.getPathId());
		statusUuid = nidToUuid(idp.getStatusId());
		time = idp.getTime();
	}

	public EIdentifierVersionUuid(EComponent eComponent) {
		denotation = eComponent.primordialComponentUuid;
		if (primordialAuthority == null) {
			primordialAuthority = PrimordialId.PRIMORDIAL_UUID.getUids().iterator().next();
		}
		authorityUuid = primordialAuthority;
		pathUuid = eComponent.pathUuid;
		statusUuid = eComponent.statusUuid;
		time = eComponent.time;
	}

	@Override
	public void writeDenotation(ObjectOutput out) throws IOException {
		out.writeLong(denotation.getMostSignificantBits());
		out.writeLong(denotation.getLeastSignificantBits());
	}

	@Override
	public UUID getDenotation() {
		return denotation;
	}
}
