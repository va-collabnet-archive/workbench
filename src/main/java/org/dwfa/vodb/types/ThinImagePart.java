package org.dwfa.vodb.types;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_MapNativeToNative;

public class ThinImagePart implements I_ImagePart {
	private int pathId;
	private int version;
	private int statusId;
	private String textDescription;
	private int typeId;
	
	public ArrayIntList getPartComponentNids() {
		ArrayIntList partComponentNids = new ArrayIntList(3);
		partComponentNids.add(getPathId());
		partComponentNids.add(getStatusId());
		partComponentNids.add(typeId);
		return partComponentNids;
	}

	public ThinImagePart() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getPathId()
	 */
	public int getPathId() {
		return pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getStatusId()
	 */
	public int getStatusId() {
		return statusId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getVersion()
	 */
	public int getVersion() {
		return version;
	}
	public ThinImagePart(int pathId, int version, int status, String textDescription,
			int type) {
		super();
		this.pathId = pathId;
		this.version = version;
		this.statusId = status;
		this.textDescription = textDescription;
		this.typeId = type;
	}
	public ThinImagePart(I_ImagePart another) {
		super();
		this.pathId = another.getPathId();
		this.version = another.getVersion();
		this.statusId = another.getStatusId();
		this.textDescription = another.getTextDescription();
		this.typeId = another.getTypeId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setPathId(int)
	 */
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setStatusId(int)
	 */
	public void setStatusId(int status) {
		this.statusId = status;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setVersion(int)
	 */
	public void setVersion(int version) {
		this.version = version;
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
	public int getTypeId() {
		return typeId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
	 */
	public void setTypeId(int type) {
		this.typeId = type;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#hasNewData(org.dwfa.vodb.types.ThinImagePart)
	 */
	public boolean hasNewData(I_ImagePart another) {
		return ((this.pathId != another.getPathId()) ||
				(this.statusId != another.getStatusId()) ||
				((this.textDescription.equals(another.getTextDescription()) == false) ||
				(this.typeId != another.getTypeId())));
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		pathId = jarToDbNativeMap.get(pathId);
		statusId = jarToDbNativeMap.get(statusId);
		typeId = jarToDbNativeMap.get(typeId);
	}
	@Override
	public boolean equals(Object obj) {
		ThinImagePart another = (ThinImagePart) obj;
		return ((pathId == another.pathId) &&
				(statusId == another.statusId) && 
				(textDescription.equals(another.textDescription)) &&
				(typeId == another.typeId) &&
				(version == another.version));
	}
	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] {pathId, statusId, textDescription.hashCode(),
				typeId, version});
	}
	
	public I_ImagePart duplicate() {
		return new ThinImagePart(this);
	}
	
	public int getPositionId() {
		throw new UnsupportedOperationException();
	}

	public void setPositionId(int pid) {
		throw new UnsupportedOperationException();
	}
	

}
