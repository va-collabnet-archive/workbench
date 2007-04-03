package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;

public class ThinRelTuple implements I_RelTuple {
	I_RelVersioned fixedPart;
	I_RelPart variablePart;
	transient Integer hash;
	public ThinRelTuple(I_RelVersioned fixedPart, I_RelPart variablePart) {
		super();
		this.fixedPart = fixedPart;
		this.variablePart = variablePart;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getC1Id()
	 */
	public int getC1Id() {
		return fixedPart.getC1Id();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getC2Id()
	 */
	public int getC2Id() {
		return fixedPart.getC2Id();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getRelId()
	 */
	public int getRelId() {
		return fixedPart.getRelId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getPathId()
	 */
	public int getPathId() {
		return variablePart.getPathId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getCharacteristicId()
	 */
	public int getCharacteristicId() {
		return variablePart.getCharacteristicId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getGroup()
	 */
	public int getGroup() {
		return variablePart.getGroup();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getRefinabilityId()
	 */
	public int getRefinabilityId() {
		return variablePart.getRefinabilityId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getRelTypeId()
	 */
	public int getRelTypeId() {
		return variablePart.getRelTypeId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getStatusId()
	 */
	public int getStatusId() {
		return variablePart.getStatusId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getVersion()
	 */
	public int getVersion() {
		return variablePart.getVersion();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setRelTypeId(java.lang.Integer)
	 */
	public void setRelTypeId(Integer typeId) {
		variablePart.setRelTypeId(typeId);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setStatusId(java.lang.Integer)
	 */
	public void setStatusId(Integer statusId) {
		variablePart.setStatusId(statusId);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setCharacteristicId(java.lang.Integer)
	 */
	public void setCharacteristicId(Integer characteristicId) {
		variablePart.setCharacteristicId(characteristicId);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setRefinabilityId(java.lang.Integer)
	 */
	public void setRefinabilityId(Integer refinabilityId) {
		variablePart.setRefinabilityId(refinabilityId);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setGroup(java.lang.Integer)
	 */
	public void setGroup(Integer group) {
		variablePart.setGroup(group);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#duplicatePart()
	 */
	public I_RelPart duplicatePart() {
		return variablePart.duplicate();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getRelVersioned()
	 */
	public I_RelVersioned getRelVersioned() {
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
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getFixedPart()
	 */
	public I_RelVersioned getFixedPart() {
		return fixedPart;
	}

}
