package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetCidCidMember extends ERefset {

	private UUID c1Uuid;
	private UUID c2Uuid;
	
	protected List<ERefsetCidCidMemberVersion> extraVersions;
	
	public ERefsetCidCidMember(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}


	public ERefsetCidCidMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
		convert(nidToIdentifier(m.getMemberId()));
		int partCount = m.getMutableParts().size();
		refsetUuid = nidToUuid(m.getRefsetId());
		componentUuid = nidToUuid(m.getComponentId());
		
		I_ThinExtByRefPartConceptConcept part = (I_ThinExtByRefPartConceptConcept) m.getMutableParts().get(0);
		c1Uuid = nidToUuid(part.getC1id());
		c2Uuid = nidToUuid(part.getC2id());
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			extraVersions = new ArrayList<ERefsetCidCidMemberVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				extraVersions.add(new ERefsetCidCidMemberVersion((I_ThinExtByRefPartConceptConcept) m.getMutableParts().get(i)));
			}
		} 
	}


	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		c2Uuid = new UUID(in.readLong(), in.readLong());
		int versionSize = in.readInt();
		if (versionSize > 0) {
			extraVersions = new ArrayList<ERefsetCidCidMemberVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				extraVersions.add(new ERefsetCidCidMemberVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeLong(c2Uuid.getMostSignificantBits());
		out.writeLong(c2Uuid.getLeastSignificantBits());
		if (extraVersions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(extraVersions.size());
			for (ERefsetCidCidMemberVersion rmv: extraVersions) {
				rmv.writeExternal(out);
			}
		}
	}


	@Override
	public REFSET_TYPES getType() {
		return REFSET_TYPES.CID_CID;
	}

	public List<ERefsetCidCidMemberVersion> getExtraVersionsList() {
		return extraVersions;
	}

}
