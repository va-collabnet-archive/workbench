package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EComponent.IDENTIFIER_PART_TYPES;

public class EIdentifierVersionLong extends EIdentifierVersion {

	public static final long serialVersionUID = 1;

	protected long denotation;
	
	public EIdentifierVersionLong(DataInput in) throws IOException,
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
	public void writeDenotation(DataOutput out) throws IOException {
		out.writeLong(denotation);
	}

	@Override
	public Long getDenotation() {
		return denotation;
	}

	@Override
	public IDENTIFIER_PART_TYPES getIdType() {
		return IDENTIFIER_PART_TYPES.LONG;
	}
}
