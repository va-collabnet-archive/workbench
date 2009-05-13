package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;

public class ThinRelTuple implements I_RelTuple {

	I_RelVersioned fixedPart;
	I_RelPart part;
	transient Integer hash;
	public ThinRelTuple(I_RelVersioned fixedPart, I_RelPart variablePart) {
		super();
		this.fixedPart = fixedPart;
		this.part = variablePart;
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
		return part.getPathId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getCharacteristicId()
	 */
	public int getCharacteristicId() {
		return part.getCharacteristicId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getGroup()
	 */
	public int getGroup() {
		return part.getGroup();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getRefinabilityId()
	 */
	public int getRefinabilityId() {
		return part.getRefinabilityId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getRelTypeId()
	 */
	@Deprecated
	public int getRelTypeId() {
		return part.getRelTypeId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getStatusId()
	 */
	public int getStatusId() {
		return part.getStatusId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getVersion()
	 */
	public int getVersion() {
		return part.getVersion();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setRelTypeId(java.lang.Integer)
	 */
	@Deprecated
	public void setRelTypeId(Integer typeId) {
		part.setRelTypeId(typeId);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setStatusId(java.lang.Integer)
	 */
	public void setStatusId(Integer statusId) {
		part.setStatusId(statusId);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setCharacteristicId(java.lang.Integer)
	 */
	public void setCharacteristicId(Integer characteristicId) {
		part.setCharacteristicId(characteristicId);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setRefinabilityId(java.lang.Integer)
	 */
	public void setRefinabilityId(Integer refinabilityId) {
		part.setRefinabilityId(refinabilityId);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#setGroup(java.lang.Integer)
	 */
	public void setGroup(Integer group) {
		part.setGroup(group);
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#duplicatePart()
	 */
	@Deprecated
	public I_RelPart duplicatePart() {
		return part.duplicate();
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
			part.equals(another.part);
	}
	@Override
	public int hashCode() {
		if (hash == null) {
			hash = HashFunction.hashCode(new int[] {fixedPart.hashCode(), 
					part.hashCode()});
		}
		return hash;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelTuple#getFixedPart()
	 */
	public I_RelVersioned getFixedPart() {
		return fixedPart;
	}
   public I_RelPart getPart() {
      return part;
   }
   @Override
   public String toString() {
      StringBuffer buff = new StringBuffer();
            buff.append("ThinRelTuple: relId: ");
      buff.append(getRelId());
      buff.append(" c1id: ");
      buff.append(getC1Id());
      buff.append(" c2id: ");
      buff.append(getC2Id());
      buff.append(" ");
     buff.append(part.toString());
      return buff.toString();
   }
   
   public int getTypeId() {
	return part.getTypeId();
   }

	public void setTypeId(int type) {
		part.setTypeId(type);
	}
	
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		// TODO
	}
	
	public I_RelPart duplicate() {
		return duplicatePart();
	}
	
	public void setPathId(int pathId) {
		part.setPathId(pathId);
	}
	
	public void setStatusId(int statusId) {
		part.setStatusId(statusId);
	}
	
	public void setVersion(int version) {
		part.setVersion(version);
	}
   
}
