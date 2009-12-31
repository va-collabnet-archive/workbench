package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetCidCidCidMember extends ERefset {

	private UUID c1Uuid;
	private UUID c2Uuid;
	private UUID c3Uuid;
	
	protected List<ERefsetCidCidCidMemberVersion> versions;
	
	public ERefsetCidCidCidMember(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}


	public ERefsetCidCidCidMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
		convert(nidToIdentifier(m.getMemberId()));
		int partCount = m.getMutableParts().size();
		refsetUuid = nidToUuid(m.getRefsetId());
		componentUuid = nidToUuid(m.getComponentId());
		
		I_ThinExtByRefPartConceptConceptConcept part = (I_ThinExtByRefPartConceptConceptConcept) m.getMutableParts().get(0);
		c1Uuid = nidToUuid(part.getC1id());
		c2Uuid = nidToUuid(part.getC2id());
		c3Uuid = nidToUuid(part.getC3id());
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			versions = new ArrayList<ERefsetCidCidCidMemberVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				versions.add(new ERefsetCidCidCidMemberVersion((I_ThinExtByRefPartConceptConceptConcept) m.getMutableParts().get(i)));
			}
		} 
	}


	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		c2Uuid = new UUID(in.readLong(), in.readLong());
		c3Uuid = new UUID(in.readLong(), in.readLong());
		int versionSize = in.readInt();
		if (versionSize > 0) {
			versions = new ArrayList<ERefsetCidCidCidMemberVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				versions.add(new ERefsetCidCidCidMemberVersion(in));
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
		out.writeLong(c3Uuid.getMostSignificantBits());
		out.writeLong(c3Uuid.getLeastSignificantBits());
		if (versions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(versions.size());
			for (ERefsetCidCidCidMemberVersion rmv: versions) {
				rmv.writeExternal(out);
			}
		}
	}


	@Override
	public REFSET_TYPES getType() {
		return REFSET_TYPES.CID_CID_CID;
	}

}
