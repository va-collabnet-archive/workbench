package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EComponent.IDENTIFIER_PART_TYPES;

public class EIdentifierVersionUuid extends EIdentifierVersion {

	public static final long serialVersionUID = 1;

	protected static UUID primordialAuthority;
	protected UUID denotation;
	
	public EIdentifierVersionUuid(DataInput in) throws IOException,
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
			primordialAuthority = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids().iterator().next();
		}
		authorityUuid = primordialAuthority;
		pathUuid = eComponent.pathUuid;
		statusUuid = eComponent.statusUuid;
		time = eComponent.time;
	}

	@Override
	public void writeDenotation(DataOutput out) throws IOException {
		out.writeLong(denotation.getMostSignificantBits());
		out.writeLong(denotation.getLeastSignificantBits());
	}

	@Override
	public UUID getDenotation() {
		return denotation;
	}
	
	@Override
	public IDENTIFIER_PART_TYPES getIdType() {
		return IDENTIFIER_PART_TYPES.UUID;
	}

}
