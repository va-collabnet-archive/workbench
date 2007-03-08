package org.dwfa.vodb.types;

import java.util.Arrays;

import org.dwfa.vodb.jar.I_MapNativeToNative;


public class ThinRelPart {
	
	private int pathId;
	private int version;
	private int statusId;
	private int relTypeId;
	private int characteristicId;
	private int refinabilityId;
	private int group;
	
	public boolean hasNewData(ThinRelPart another) {
		return ((this.pathId != another.pathId) ||
				(this.statusId != another.statusId) ||
				(this.relTypeId != another.relTypeId) ||
				(this.characteristicId != another.characteristicId) ||
				(this.refinabilityId != another.refinabilityId) ||
				(this.group != another.group));
	}

	public int getPathId() {
		return pathId;
	}
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	public int getCharacteristicId() {
		return characteristicId;
	}
	public void setCharacteristicId(int characteristicId) {
		this.characteristicId = characteristicId;
	}
	public int getGroup() {
		return group;
	}
	public void setGroup(int group) {
		this.group = group;
	}
	public int getRefinabilityId() {
		return refinabilityId;
	}
	public void setRefinabilityId(int refinabilityId) {
		this.refinabilityId = refinabilityId;
	}
	public int getRelTypeId() {
		return relTypeId;
	}
	public void setRelTypeId(int relTypeId) {
		this.relTypeId = relTypeId;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getStatusId() {
		return statusId;
	}
	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("ThinRelPart pathId: ");
		buff.append(pathId);
		buff.append(" version: ");
		buff.append(version);
		buff.append(" statusId: ");
		buff.append(statusId);
		buff.append(" relTypeId: ");
		buff.append(relTypeId);
		buff.append(" characteristicId: ");
		buff.append(characteristicId);
		buff.append(" refinabilityId: ");
		buff.append(refinabilityId);
		buff.append(" group: ");
		buff.append(group);
		
		return buff.toString();
	}

	@Override
	public boolean equals(Object obj) {
		ThinRelPart another = (ThinRelPart) obj;
		return Arrays.equals(getAsArray(), another.getAsArray());
	}
	
	private int[] getAsArray() {
		return new int[] { pathId, version, statusId,
				relTypeId, characteristicId, refinabilityId, group};
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(getAsArray());
	}

	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		pathId = jarToDbNativeMap.get(pathId);
		statusId = jarToDbNativeMap.get(statusId);
		relTypeId = jarToDbNativeMap.get(relTypeId);
		characteristicId = jarToDbNativeMap.get(characteristicId);
		refinabilityId = jarToDbNativeMap.get(refinabilityId);
	}

	public ThinRelPart duplicate() {
		ThinRelPart part = new ThinRelPart();
		part.setCharacteristicId(characteristicId);
		part.setGroup(group);
		part.setPathId(pathId);
		part.setRefinabilityId(refinabilityId);
		part.setRelTypeId(relTypeId);
		part.setStatusId(statusId);
		part.setVersion(version);
		return part;
	}
	
	
}
