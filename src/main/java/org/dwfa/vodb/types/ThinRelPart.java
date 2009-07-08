package org.dwfa.vodb.types;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.vodb.bind.ThinVersionHelper;


public class ThinRelPart implements I_RelPart {
	
	private int pathId;
	private int version;
	private int statusId;
	private int relTypeId;
	private int characteristicId;
	private int refinabilityId;
	private int group;

	public ArrayIntList getPartComponentNids() {
		ArrayIntList partComponentNids = new ArrayIntList(5);
		partComponentNids.add(getPathId());
		partComponentNids.add(getStatusId());
		partComponentNids.add(relTypeId);
		partComponentNids.add(characteristicId);
		partComponentNids.add(refinabilityId);
		return partComponentNids;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#hasNewData(org.dwfa.vodb.types.ThinRelPart)
	 */
	public boolean hasNewData(I_RelPart another) {
		return ((this.pathId != another.getPathId()) ||
				(this.statusId != another.getStatusId()) ||
				(this.relTypeId != another.getTypeId()) ||
				(this.characteristicId != another.getCharacteristicId()) ||
				(this.refinabilityId != another.getRefinabilityId()) ||
				(this.group != another.getGroup()));
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getPathId()
	 */
	public int getPathId() {
		return pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setPathId(int)
	 */
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getCharacteristicId()
	 */
	public int getCharacteristicId() {
		return characteristicId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setCharacteristicId(int)
	 */
	public void setCharacteristicId(int characteristicId) {
		this.characteristicId = characteristicId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getGroup()
	 */
	public int getGroup() {
		return group;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setGroup(int)
	 */
	public void setGroup(int group) {
		this.group = group;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getRefinabilityId()
	 */
	public int getRefinabilityId() {
		return refinabilityId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setRefinabilityId(int)
	 */
	public void setRefinabilityId(int refinabilityId) {
		this.refinabilityId = refinabilityId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getRelTypeId()
	 */
	@Deprecated
	public int getRelTypeId() {
		return relTypeId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setRelTypeId(int)
	 */
	@Deprecated
	public void setRelTypeId(int relTypeId) {
		this.relTypeId = relTypeId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getVersion()
	 */
	public int getVersion() {
		return version;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setVersion(int)
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getStatusId()
	 */
	public int getStatusId() {
		return statusId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setStatusId(int)
	 */
	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
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
		buff.append(" pathId: ");
		buff.append(pathId);
		buff.append(" version: ");
		buff.append(version);
		buff.append(" (");
		buff.append(new Date(ThinVersionHelper.convert(version)));
		buff.append(")");
		
		return buff.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (ThinRelPart.class.isAssignableFrom(obj.getClass())) {
			ThinRelPart another = (ThinRelPart) obj;
			return Arrays.equals(getAsArray(), another.getAsArray());
		} else if (I_RelPart.class.isAssignableFrom(obj.getClass())) {
			I_RelPart another = (I_RelPart) obj;
			return Arrays.equals(getAsArray(), getAsArray(another));
		}
		return false;
	}
	private static int[] getAsArray(I_RelPart part) {
		return new int[] { part.getPathId(), part.getVersion(), part.getStatusId(),
				part.getTypeId(), part.getCharacteristicId(), part.getRefinabilityId(), part.getGroup()};
	}
	
	private int[] getAsArray() {
		return new int[] { pathId, version, statusId,
				relTypeId, characteristicId, refinabilityId, group};
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(getAsArray());
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		pathId = jarToDbNativeMap.get(pathId);
		statusId = jarToDbNativeMap.get(statusId);
		relTypeId = jarToDbNativeMap.get(relTypeId);
		characteristicId = jarToDbNativeMap.get(characteristicId);
		refinabilityId = jarToDbNativeMap.get(refinabilityId);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#duplicate()
	 */
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

	public int getTypeId() {
		return getRelTypeId();
	}

	public void setTypeId(int type) {		
		setRelTypeId(type);
	}
	
}
