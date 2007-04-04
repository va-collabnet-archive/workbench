package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_MapNativeToNative;


public class ThinConPart implements I_ConceptAttributePart {

	private int pathId;
	private int version;
	private int conceptStatus;
	private boolean defined;
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#getPathId()
	 */
	public int getPathId() {
		return pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#setPathId(int)
	 */
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#getConceptStatus()
	 */
	public int getConceptStatus() {
		return conceptStatus;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#setConceptStatus(int)
	 */
	public void setConceptStatus(int conceptStatus) {
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
	public int getVersion() {
		return version;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#setVersion(int)
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#hasNewData(org.dwfa.vodb.types.ThinConPart)
	 */
	public boolean hasNewData(I_ConceptAttributePart another) {
		return ((this.defined != another.isDefined()) ||
				(this.pathId != another.getPathId()) ||
				(this.conceptStatus != another.getConceptStatus()));
	}
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		pathId = jarToDbNativeMap.get(pathId);
		conceptStatus = jarToDbNativeMap.get(conceptStatus);
	}
	@Override
	public boolean equals(Object obj) {
		ThinConPart another = (ThinConPart) obj;
		return ((pathId == another.pathId) &&
				(version == another.version) && 
				(conceptStatus == another.conceptStatus) && 
				(defined == another.defined));
	}
	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] {pathId, version, conceptStatus } );
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ConceptAttributePart#duplicate()
	 */
	public I_ConceptAttributePart duplicate() {
		ThinConPart newPart = new ThinConPart();
		newPart.setConceptStatus(conceptStatus);
		newPart.setDefined(defined);
		newPart.setPathId(pathId);
		newPart.setVersion(version);
		return newPart;
	}
}
