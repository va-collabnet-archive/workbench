package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetCidStrMember extends ERefset {

	public static final long serialVersionUID = 1;

	private UUID c1Uuid;
	private String strValue;
	
	protected List<ERefsetCidStrVersion> extraVersions;
	
	public ERefsetCidStrMember(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}


	public ERefsetCidStrMember(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
		convert(nidToIdentifier(m.getMemberId()));
		int partCount = m.getMutableParts().size();
		refsetUuid = nidToUuid(m.getRefsetId());
		componentUuid = nidToUuid(m.getComponentId());
		
		I_ThinExtByRefPartConceptString part = (I_ThinExtByRefPartConceptString) m.getMutableParts().get(0);
		c1Uuid = nidToUuid(part.getC1id());
		strValue = part.getStr();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			extraVersions = new ArrayList<ERefsetCidStrVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				extraVersions.add(new ERefsetCidStrVersion((I_ThinExtByRefPartConceptString) m.getMutableParts().get(i)));
			}
		} 
	}


	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		strValue = (String) in.readObject();
		int versionSize = in.readInt();
		if (versionSize > 0) {
			extraVersions = new ArrayList<ERefsetCidStrVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				extraVersions.add(new ERefsetCidStrVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeObject(strValue);
		if (extraVersions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(extraVersions.size());
			for (ERefsetCidStrVersion rmv: extraVersions) {
				rmv.writeExternal(out);
			}
		}
	}

	@Override
	public REFSET_TYPES getType() {
		return REFSET_TYPES.CID_STR;
	}

	public List<ERefsetCidStrVersion> getExtraVersionsList() {
		return extraVersions;
	}


	public UUID getC1Uuid() {
		return c1Uuid;
	}


	public void setC1Uuid(UUID c1Uuid) {
		this.c1Uuid = c1Uuid;
	}


	public String getStrValue() {
		return strValue;
	}


	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}


	public List<ERefsetCidStrVersion> getExtraVersions() {
		return extraVersions;
	}

}
