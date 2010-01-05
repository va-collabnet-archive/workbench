package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartCidLong;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetCidLongMember extends ERefset<ERefsetCidLongVersion> {

	public static final long serialVersionUID = 1;

	protected UUID c1Uuid;
	protected long longValue;
	
	protected List<ERefsetCidLongVersion> extraVersions;
	
	public ERefsetCidLongMember(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}


	public ERefsetCidLongMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
		convert(nidToIdentifier(m.getMemberId()));
		int partCount = m.getMutableParts().size();
		refsetUuid = nidToUuid(m.getRefsetId());
		componentUuid = nidToUuid(m.getComponentId());
		
		I_ThinExtByRefPartCidLong part = (I_ThinExtByRefPartCidLong) m.getMutableParts().get(0);
		c1Uuid = nidToUuid(part.getC1id());
		longValue = part.getLongValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			extraVersions = new ArrayList<ERefsetCidLongVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				extraVersions.add(new ERefsetCidLongVersion((I_ThinExtByRefPartCidLong) m.getMutableParts().get(i)));
			}
		} 
	}


	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		longValue = in.readLong();
		int versionSize = in.readInt();
		if (versionSize > 0) {
			extraVersions = new ArrayList<ERefsetCidLongVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				extraVersions.add(new ERefsetCidLongVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeLong(longValue);
		if (extraVersions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(extraVersions.size());
			for (ERefsetCidLongVersion rmv: extraVersions) {
				rmv.writeExternal(out);
			}
		}
	}

	@Override
	public REFSET_TYPES getType() {
		return REFSET_TYPES.CID_LONG;
	}

	public List<ERefsetCidLongVersion> getExtraVersionsList() {
		return extraVersions;
	}


	public UUID getC1Uuid() {
		return c1Uuid;
	}


	public void setC1Uuid(UUID c1Uuid) {
		this.c1Uuid = c1Uuid;
	}


	public long getLongValue() {
		return longValue;
	}


	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}


	public List<ERefsetCidLongVersion> getExtraVersions() {
		return extraVersions;
	}

}
