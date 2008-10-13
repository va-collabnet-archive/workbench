package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

public class UniversalAceConceptAttributesPart implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Collection<UUID> pathId;
	private long time;
	private Collection<UUID> conceptStatus;
	private boolean defined;
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#getPathId()
	 */
	public Collection<UUID> getPathId() {
		return pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#setPathId(int)
	 */
	public void setPathId(Collection<UUID> pathId) {
		this.pathId = pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#getConceptStatus()
	 */
	public Collection<UUID> getConceptStatus() {
		return conceptStatus;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#setConceptStatus(int)
	 */
	public void setConceptStatus(Collection<UUID> conceptStatus) {
		this.conceptStatus = conceptStatus;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#isDefined()
	 */
	public boolean isDefined() {
		return defined;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#setDefined(boolean)
	 */
	public void setDefined(boolean defined) {
		this.defined = defined;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#getVersion()
	 */
	public long getTime() {
		return time;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#setVersion(int)
	 */
	public void setTime(long version) {
		this.time = version;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " defined: " + defined + 
		" status: " + conceptStatus + 
		" path: " + pathId + 
		" time: " + time + " (" + new Date(time) + ")";
	}

	
}
