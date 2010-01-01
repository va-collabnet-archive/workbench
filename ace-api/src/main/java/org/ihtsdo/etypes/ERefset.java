package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public abstract class ERefset extends EComponent {

	protected UUID refsetUuid;
	protected UUID componentUuid;

	public ERefset() {
		super();
	}

	public ERefset(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		refsetUuid = new UUID(in.readLong(), in.readLong());
		componentUuid = new UUID(in.readLong(), in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(refsetUuid.getMostSignificantBits());
		out.writeLong(refsetUuid.getLeastSignificantBits());
		out.writeLong(componentUuid.getMostSignificantBits());
		out.writeLong(componentUuid.getLeastSignificantBits());
	}

	public UUID getRefsetUuid() {
		return refsetUuid;
	}

	public void setRefsetUuid(UUID refsetUuid) {
		this.refsetUuid = refsetUuid;
	}

	public UUID getComponentUuid() {
		return componentUuid;
	}

	public void setComponentUuid(UUID componentUuid) {
		this.componentUuid = componentUuid;
	}
	
	public abstract REFSET_TYPES getType();

	public static ERefset convert(I_ThinExtByRefVersioned m) throws TerminologyException, IOException {
		REFSET_TYPES type = REFSET_TYPES.nidToType(m.getTypeId());
		if (type != null) {
			switch (type) {
			case CID:
				return new ERefsetCidMember(m);
			case CID_CID:
				return new ERefsetCidCidMember(m);
			case CID_CID_CID:
				return new ERefsetCidCidCidMember(m);
			case CID_CID_STR:
				return new ERefsetCidCidStrMember(m);
			case INT:
				return new ERefsetIntMember(m);
			case MEMBER:
				return new ERefsetMember(m);
			case STR:
				return new ERefsetStrMember(m);
			case CID_INT:
				return new ERefsetCidIntMember(m);
			default:
				throw new UnsupportedOperationException("Cannot handle: " + type);
			}
		} else {
			AceLog.getAppLog().severe("Can't handle refset type: " + m);
		}
		return null;
	}
}