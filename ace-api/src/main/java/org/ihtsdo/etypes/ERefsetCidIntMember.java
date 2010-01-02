package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetCidIntMember extends ERefset {

	public static final long serialVersionUID = 1;

	protected UUID c1Uuid;
	protected int intValue;
	
	protected List<ERefsetCidIntVersion> extraVersions;
	
	public ERefsetCidIntMember(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}


	public ERefsetCidIntMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
		convert(nidToIdentifier(m.getMemberId()));
		int partCount = m.getMutableParts().size();
		refsetUuid = nidToUuid(m.getRefsetId());
		componentUuid = nidToUuid(m.getComponentId());
		
		I_ThinExtByRefPartConceptInt part = (I_ThinExtByRefPartConceptInt) m.getMutableParts().get(0);
		c1Uuid = nidToUuid(part.getC1id());
		intValue = part.getIntValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			extraVersions = new ArrayList<ERefsetCidIntVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				extraVersions.add(new ERefsetCidIntVersion((I_ThinExtByRefPartConceptInt) m.getMutableParts().get(i)));
			}
		} 
	}


	public ERefsetCidIntMember() {
	}


	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		intValue = in.readInt();
		int versionSize = in.readInt();
		if (versionSize > 0) {
			extraVersions = new ArrayList<ERefsetCidIntVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				extraVersions.add(new ERefsetCidIntVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeInt(intValue);
		if (extraVersions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(extraVersions.size());
			for (ERefsetCidIntVersion rmv: extraVersions) {
				rmv.writeExternal(out);
			}
		}
	}


	@Override
	public REFSET_TYPES getType() {
		return REFSET_TYPES.CID_INT;
	}

	public List<ERefsetCidIntVersion> getExtraVersionsList() {
		return extraVersions;
	}


	public UUID getC1Uuid() {
		return c1Uuid;
	}


	public void setC1Uuid(UUID c1Uuid) {
		this.c1Uuid = c1Uuid;
	}


	public int getIntValue() {
		return intValue;
	}


	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

}
