package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetMember extends ERefset {

	protected List<ERefsetMemberVersion> versions;

	public ERefsetMember() {
		super();
	}

	public ERefsetMember(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
		convert(nidToIdentifier(m.getMemberId()));
		int partCount = m.getMutableParts().size();
		refsetUuid = nidToUuid(m.getRefsetId());
		componentUuid = nidToUuid(m.getComponentId());
		
		I_ThinExtByRefPart part = (I_ThinExtByRefPart) m.getMutableParts().get(0);
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			versions = new ArrayList<ERefsetMemberVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				versions.add(new ERefsetMemberVersion((I_ThinExtByRefPart) m.getMutableParts().get(i)));
			}
		} 
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		int versionSize = in.readInt();
		if (versionSize > 0) {
			versions = new ArrayList<ERefsetMemberVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				versions.add(new ERefsetMemberVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		if (versions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(versions.size());
			for (ERefsetMemberVersion rmv: versions) {
				rmv.writeExternal(out);
			}
		}
	}

	@Override
	public REFSET_TYPES getType() {
		return REFSET_TYPES.MEMBER;
	}
}
