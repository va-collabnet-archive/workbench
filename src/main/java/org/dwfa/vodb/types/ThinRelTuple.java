package org.dwfa.vodb.types;

public class ThinRelTuple {
	ThinRelVersioned fixedPart;
	ThinRelPart variablePart;
	transient Integer hash;
	public ThinRelTuple(ThinRelVersioned fixedPart, ThinRelPart variablePart) {
		super();
		this.fixedPart = fixedPart;
		this.variablePart = variablePart;
	}
	public int getC1Id() {
		return fixedPart.getC1Id();
	}
	public int getC2Id() {
		return fixedPart.getC2Id();
	}
	public int getRelId() {
		return fixedPart.getRelId();
	}
	public int getPathId() {
		return variablePart.getPathId();
	}
	public int getCharacteristicId() {
		return variablePart.getCharacteristicId();
	}
	public int getGroup() {
		return variablePart.getGroup();
	}
	public int getRefinabilityId() {
		return variablePart.getRefinabilityId();
	}
	public int getRelTypeId() {
		return variablePart.getRelTypeId();
	}
	public int getStatusId() {
		return variablePart.getStatusId();
	}
	public int getVersion() {
		return variablePart.getVersion();
	}
	public void setRelTypeId(Integer typeId) {
		variablePart.setRelTypeId(typeId);
		
	}
	public void setStatusId(Integer statusId) {
		variablePart.setStatusId(statusId);
		
	}
	public void setCharacteristicId(Integer characteristicId) {
		variablePart.setCharacteristicId(characteristicId);
		
	}
	public void setRefinabilityId(Integer refinabilityId) {
		variablePart.setRefinabilityId(refinabilityId);
		
	}
	public void setGroup(Integer group) {
		variablePart.setGroup(group);
		
	}
	public ThinRelPart duplicatePart() {
		return variablePart.duplicate();
	}
	public ThinRelVersioned getRelVersioned() {
		return fixedPart;
	}
	@Override
	public boolean equals(Object obj) {
		ThinRelTuple another = (ThinRelTuple) obj;
		return fixedPart.equals(another.fixedPart) &&
			variablePart.equals(another.variablePart);
	}
	@Override
	public int hashCode() {
		if (hash == null) {
			hash = HashFunction.hashCode(new int[] {fixedPart.hashCode(), 
					variablePart.hashCode()});
		}
		return hash;
	}

}
