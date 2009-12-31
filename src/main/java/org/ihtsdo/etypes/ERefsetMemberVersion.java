package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public class ERefsetMemberVersion extends EVersion {

	public ERefsetMemberVersion(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetMemberVersion(I_ThinExtByRefPart part) throws TerminologyException, IOException {
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
	}

}
