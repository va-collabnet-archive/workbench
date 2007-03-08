package org.dwfa.vodb.types;

public class ThinConTuple {
	ThinConVersioned core;
	ThinConPart part;
	public ThinConTuple(ThinConVersioned core, ThinConPart part) {
		super();
		this.core = core;
		this.part = part;
	}
	public int getConId() {
		return core.getConId();
	}
	public int getConceptStatus() {
		return part.getConceptStatus();
	}
	public int getPathId() {
		return part.getPathId();
	}
	public int getVersion() {
		return part.getVersion();
	}
	public boolean hasNewData(ThinConPart another) {
		return part.hasNewData(another);
	}
	public boolean isDefined() {
		return part.isDefined();
	}
	public void setStatusId(Integer statusId) {
		part.setConceptStatus(statusId);
		
	}
	public void setDefined(boolean defined) {
		part.setDefined(defined);
		
	}
	public ThinConVersioned getConVersioned() {
		return core;
	}
	public ThinConPart duplicatePart() {
		return part.duplicate();
	}
}
