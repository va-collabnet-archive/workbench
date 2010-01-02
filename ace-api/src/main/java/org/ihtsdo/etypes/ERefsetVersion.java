package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public class ERefsetVersion extends EVersion {

	protected static final long serialVersionUID = 1;

	public ERefsetVersion(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetVersion(I_ThinExtByRefPart part) throws TerminologyException, IOException {
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
	}

}
