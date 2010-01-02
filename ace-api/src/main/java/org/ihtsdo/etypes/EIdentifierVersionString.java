package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.tapi.TerminologyException;

public class EIdentifierVersionString extends EIdentifierVersion {

	public static final long serialVersionUID = 1;

	protected String denotation;
	
	public EIdentifierVersionString(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super(in);
		denotation = (String) in.readObject();
	}

	public EIdentifierVersionString(I_IdPart idp) throws TerminologyException, IOException {
		denotation = (String) idp.getDenotation();
		authorityUuid = nidToUuid(idp.getAuthorityNid());
		pathUuid = nidToUuid(idp.getPathId());
		statusUuid = nidToUuid(idp.getStatusId());
		time = idp.getTime();
	}

	@Override
	public void writeDenotation(ObjectOutput out) throws IOException {
		out.writeObject(denotation);
	}

	@Override
	public String getDenotation() {
		return denotation;
	}
}
