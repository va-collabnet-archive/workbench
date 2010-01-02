package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetCidCidStrMember extends ERefset {

	public static final long serialVersionUID = 1;

	protected UUID c1Uuid;
	protected UUID c2Uuid;
	protected String strValue;
	
	protected List<ERefsetCidCidStrVersion> extraVersions;
	
	public ERefsetCidCidStrMember(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}


	public ERefsetCidCidStrMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
		convert(nidToIdentifier(m.getMemberId()));
		int partCount = m.getMutableParts().size();
		refsetUuid = nidToUuid(m.getRefsetId());
		componentUuid = nidToUuid(m.getComponentId());
		
		I_ThinExtByRefPartConceptConceptString part = (I_ThinExtByRefPartConceptConceptString) m.getMutableParts().get(0);
		c1Uuid = nidToUuid(part.getC1id());
		c2Uuid = nidToUuid(part.getC2id());
		strValue = part.getStringValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			extraVersions = new ArrayList<ERefsetCidCidStrVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				extraVersions.add(new ERefsetCidCidStrVersion((I_ThinExtByRefPartConceptConceptString) m.getMutableParts().get(i)));
			}
		} 
	}


	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		c2Uuid = new UUID(in.readLong(), in.readLong());
		strValue = (String) in.readObject();
		int versionSize = in.readInt();
		if (versionSize > 0) {
			extraVersions = new ArrayList<ERefsetCidCidStrVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				extraVersions.add(new ERefsetCidCidStrVersion(in));
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
		out.writeObject(strValue);
		if (extraVersions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(extraVersions.size());
			for (ERefsetCidCidStrVersion rmv: extraVersions) {
				rmv.writeExternal(out);
			}
		}
	}


	@Override
	public REFSET_TYPES getType() {
		return REFSET_TYPES.CID_CID_STR;
	}

	public List<ERefsetCidCidStrVersion> getExtraVersionsList() {
		return extraVersions;
	}


	public UUID getC1Uuid() {
		return c1Uuid;
	}


	public void setC1Uuid(UUID c1Uuid) {
		this.c1Uuid = c1Uuid;
	}


	public UUID getC2Uuid() {
		return c2Uuid;
	}


	public void setC2Uuid(UUID c2Uuid) {
		this.c2Uuid = c2Uuid;
	}


	public String getStrValue() {
		return strValue;
	}


	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

}
