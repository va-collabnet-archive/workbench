package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.tapi.TerminologyException;

public class ERelationshipVersion extends EVersion {

	public static final long serialVersionUID = 1;

	protected UUID characteristicUuid;
	protected UUID refinabilityUuid;
	protected int group; 
	protected UUID typeUuid;

	public ERelationshipVersion(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERelationshipVersion(I_RelPart part) throws TerminologyException, IOException {
		characteristicUuid = nidToUuid(part.getCharacteristicId());
		refinabilityUuid = nidToUuid(part.getRefinabilityId());
		group = part.getGroup();
		typeUuid = nidToUuid(part.getTypeId());
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	public ERelationshipVersion() {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		characteristicUuid = new UUID(in.readLong(), in.readLong());
		refinabilityUuid = new UUID(in.readLong(), in.readLong());
		group = in.readInt();
		typeUuid = new UUID(in.readLong(), in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(characteristicUuid.getMostSignificantBits());
		out.writeLong(characteristicUuid.getLeastSignificantBits());
		out.writeLong(refinabilityUuid.getMostSignificantBits());
		out.writeLong(refinabilityUuid.getLeastSignificantBits());
		out.writeInt(group);
		out.writeLong(typeUuid.getMostSignificantBits());
		out.writeLong(typeUuid.getLeastSignificantBits());
	}

	public UUID getCharacteristicUuid() {
		return characteristicUuid;
	}

	public void setCharacteristicUuid(UUID characteristicUuid) {
		this.characteristicUuid = characteristicUuid;
	}

	public UUID getRefinabilityUuid() {
		return refinabilityUuid;
	}

	public void setRefinabilityUuid(UUID refinabilityUuid) {
		this.refinabilityUuid = refinabilityUuid;
	}

	public int getGroup() {
		return group;
	}

	public void setRelGroup(int relGroup) {
		this.group = relGroup;
	}

	public UUID getTypeUuid() {
		return typeUuid;
	}

	public void setTypeUuid(UUID typeUuid) {
		this.typeUuid = typeUuid;
	}

}
