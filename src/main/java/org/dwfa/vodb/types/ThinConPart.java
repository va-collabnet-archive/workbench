package org.dwfa.vodb.types;

import org.dwfa.vodb.jar.I_MapNativeToNative;


public class ThinConPart {

	private int pathId;
	private int version;
	private int conceptStatus;
	private boolean defined;
	
	public int getPathId() {
		return pathId;
	}
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	public int getConceptStatus() {
		return conceptStatus;
	}
	public void setConceptStatus(int conceptStatus) {
		this.conceptStatus = conceptStatus;
	}
	public boolean isDefined() {
		return defined;
	}
	public void setDefined(boolean defined) {
		this.defined = defined;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public boolean hasNewData(ThinConPart another) {
		return ((this.defined != another.defined) ||
				(this.pathId != another.pathId) ||
				(this.conceptStatus != another.conceptStatus));
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
	public ThinConPart duplicate() {
		ThinConPart newPart = new ThinConPart();
		newPart.setConceptStatus(conceptStatus);
		newPart.setDefined(defined);
		newPart.setPathId(pathId);
		newPart.setVersion(version);
		return newPart;
	}
}
