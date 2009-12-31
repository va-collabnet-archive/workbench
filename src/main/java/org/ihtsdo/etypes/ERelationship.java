package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.tapi.TerminologyException;

public class ERelationship extends EComponent {

	private UUID c1Uuid;
	private UUID c2Uuid;
	private UUID characteristicUuid;
	private UUID refinabilityUuid;
	private int relGroup; 
	
	private List<ERelationshipVersion> versions;

	public ERelationship(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERelationship(I_RelVersioned rel) throws TerminologyException, IOException {
		convert(nidToIdentifier(rel.getNid()));
		int partCount = rel.getMutableParts().size();
		I_RelPart part = rel.getMutableParts().get(0);
		c1Uuid = nidToUuid(rel.getC1Id());
		c2Uuid = nidToUuid(rel.getC2Id());
		characteristicUuid = nidToUuid(part.getCharacteristicId());
		refinabilityUuid = nidToUuid(part.getRefinabilityId());
		relGroup = part.getGroup();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			versions = new ArrayList<ERelationshipVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				versions.add(new ERelationshipVersion(rel.getMutableParts().get(i)));
			}
		} 
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		c2Uuid = new UUID(in.readLong(), in.readLong());
		characteristicUuid = new UUID(in.readLong(), in.readLong());
		refinabilityUuid = new UUID(in.readLong(), in.readLong());
		relGroup = in.readInt();
		int versionSize = in.readInt();
		if (versionSize > 0) {
			versions = new ArrayList<ERelationshipVersion>(versionSize);
			for (int i = 0; i < versionSize; i++) {
				versions.add(new ERelationshipVersion(in));
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
		out.writeLong(characteristicUuid.getMostSignificantBits());
		out.writeLong(characteristicUuid.getLeastSignificantBits());
		out.writeLong(refinabilityUuid.getMostSignificantBits());
		out.writeLong(refinabilityUuid.getLeastSignificantBits());
		out.writeInt(relGroup);
		if (versions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(versions.size());
			for (ERelationshipVersion erv: versions) {
				erv.writeExternal(out);
			}
		}
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

	public int getRelGroup() {
		return relGroup;
	}

	public void setRelGroup(int relGroup) {
		this.relGroup = relGroup;
	}

}
