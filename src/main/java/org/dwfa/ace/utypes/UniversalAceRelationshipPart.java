package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceRelationshipPart implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Collection<UUID> pathId;
	private long time;
	private Collection<UUID> statusId;
	private Collection<UUID> relTypeId;
	private Collection<UUID> characteristicId;
	private Collection<UUID> refinabilityId;
	private int group;
	@Override
	public String toString() {
		return this.getClass().getSimpleName() +
		" relTypeId: " + relTypeId +
		" characteristicId: " + characteristicId +
		" refinabilityId: " + refinabilityId +
		" group: " + group +
		" status: " + statusId + " path: " + pathId + " time: " + time;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getPathId()
	 */
	public Collection<UUID> getPathId() {
		return pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setPathId(int)
	 */
	public void setPathId(Collection<UUID> pathId) {
		this.pathId = pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getCharacteristicId()
	 */
	public Collection<UUID> getCharacteristicId() {
		return characteristicId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setCharacteristicId(int)
	 */
	public void setCharacteristicId(Collection<UUID> characteristicId) {
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
	public Collection<UUID> getRefinabilityId() {
		return refinabilityId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setRefinabilityId(int)
	 */
	public void setRefinabilityId(Collection<UUID> refinabilityId) {
		this.refinabilityId = refinabilityId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getRelTypeId()
	 */
	public Collection<UUID> getRelTypeId() {
		return relTypeId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setRelTypeId(int)
	 */
	public void setRelTypeId(Collection<UUID> relTypeId) {
		this.relTypeId = relTypeId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getVersion()
	 */
	public long getTime() {
		return time;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setVersion(int)
	 */
	public void setTime(long version) {
		this.time = version;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#getStatusId()
	 */
	public Collection<UUID> getStatusId() {
		return statusId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelPart#setStatusId(int)
	 */
	public void setStatusId(Collection<UUID> statusId) {
		this.statusId = statusId;
	}
	
}
