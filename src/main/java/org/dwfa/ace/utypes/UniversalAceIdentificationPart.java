package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceIdentificationPart implements Serializable, I_VersionComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Collection<UUID> pathId;
	private long time;
	private Collection<UUID> idStatus;
	private Collection<UUID> source;
	private Object sourceId;
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() +
		" sourceId: " + sourceId +
		" source: " + source +
		" status: " + idStatus + " path: " + pathId + " time: " + time;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getPathId()
	 */
	public Collection<UUID> getPathId() {
		return pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setPathId(int)
	 */
	public void setPathId(Collection<UUID> pathId) {
		this.pathId = pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getIdStatus()
	 */
	public Collection<UUID> getIdStatus() {
		return idStatus;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setIdStatus(int)
	 */
	public void setIdStatus(Collection<UUID> idStatus) {
		this.idStatus = idStatus;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getSource()
	 */
	public Collection<UUID> getSource() {
		return source;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setSource(int)
	 */
	public void setSource(Collection<UUID> source) {
		this.source = source;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getSourceId()
	 */
	public Object getSourceId() {
		return sourceId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setSourceId(java.lang.Object)
	 */
	public void setSourceId(Object sourceId) {
		this.sourceId = sourceId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getVersion()
	 */
	public long getTime() {
		return time;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setVersion(int)
	 */
	public void setTime(long version) {
		this.time = version;
	}
	public Collection<UUID> getStatusId() {
		return getIdStatus();
	}
	public void setStatusId(Collection<UUID> status) {
		setStatusId(status);
	}

}
