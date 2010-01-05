package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetStrMember extends ERefset<ERefsetStrVersion> {

	public static final long serialVersionUID = 1;

	protected String strValue;
		
	public ERefsetStrMember(DataInput in) throws IOException,
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
		strValue = part.getStringValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			extraVersions = new ArrayList<ERefsetStrVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				extraVersions.add(new ERefsetStrVersion((I_ThinExtByRefPartString) m.getMutableParts().get(i)));
			}
		} 
	}


	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		strValue = in.readUTF();
		int versionSize = in.readInt();
		if (versionSize > 0) {
			extraVersions = new ArrayList<ERefsetStrVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				extraVersions.add(new ERefsetStrVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(strValue);
		if (extraVersions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(extraVersions.size());
			for (ERefsetStrVersion rmv: extraVersions) {
				rmv.writeExternal(out);
			}
		}
	}

	@Override
	public REFSET_TYPES getType() {
		return REFSET_TYPES.STR;
	}

	public List<ERefsetStrVersion> getExtraVersionsList() {
		return extraVersions;
	}


	public String getStrValue() {
		return strValue;
	}


	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

}
