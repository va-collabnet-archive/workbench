package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetStrMember extends ERefset {

	private String stringValue;
	
	protected List<ERefsetStrMemberVersion> versions;
	
	public ERefsetStrMember(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}


	public ERefsetStrMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
		convert(nidToIdentifier(m.getMemberId()));
		int partCount = m.getMutableParts().size();
		refsetUuid = nidToUuid(m.getRefsetId());
		componentUuid = nidToUuid(m.getComponentId());
		
		I_ThinExtByRefPartString part = (I_ThinExtByRefPartString) m.getMutableParts().get(0);
		stringValue = part.getStringValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			versions = new ArrayList<ERefsetStrMemberVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				versions.add(new ERefsetStrMemberVersion((I_ThinExtByRefPartString) m.getMutableParts().get(i)));
			}
		} 
	}


	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		stringValue = (String) in.readObject();
		int versionSize = in.readInt();
		if (versionSize > 0) {
			versions = new ArrayList<ERefsetStrMemberVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				versions.add(new ERefsetStrMemberVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(stringValue);
		if (versions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(versions.size());
			for (ERefsetStrMemberVersion rmv: versions) {
				rmv.writeExternal(out);
			}
		}
	}

	@Override
	public REFSET_TYPES getType() {
		return REFSET_TYPES.STR;
	}

}
