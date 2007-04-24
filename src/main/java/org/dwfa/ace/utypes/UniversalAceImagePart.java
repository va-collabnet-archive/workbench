package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceImagePart implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Collection<UUID> pathId;
	private long time;
	private Collection<UUID> statusId;
	private String textDescription;
	private Collection<UUID> typeId;
	@Override
	public String toString() {
		return this.getClass().getSimpleName() +
		" textDescription: " + textDescription +
		" typeId: " + typeId +
		" status: " + statusId + " path: " + pathId + " time: " + time;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getPathId()
	 */
	public Collection<UUID> getPathId() {
		return pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getStatusId()
	 */
	public Collection<UUID> getStatusId() {
		return statusId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getVersion()
	 */
	public long getTime() {
		return time;
	}
	public UniversalAceImagePart(Collection<UUID> pathId, long version, Collection<UUID> status, String textDescription,
			Collection<UUID> type) {
		super();
		this.pathId = pathId;
		this.time = version;
		this.statusId = status;
		this.textDescription = textDescription;
		this.typeId = type;
	}
	public UniversalAceImagePart() {
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setPathId(int)
	 */
	public void setPathId(Collection<UUID> pathId) {
		this.pathId = pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setStatusId(int)
	 */
	public void setStatusId(Collection<UUID> status) {
		this.statusId = status;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setVersion(int)
	 */
	public void setTime(long version) {
		this.time = version;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getTextDescription()
	 */
	public String getTextDescription() {
		return textDescription;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setTextDescription(java.lang.String)
	 */
	public void setTextDescription(String name) {
		this.textDescription = name;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getTypeId()
	 */
	public Collection<UUID> getTypeId() {
		return typeId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
	 */
	public void setTypeId(Collection<UUID> type) {
		this.typeId = type;
	}
	
}
