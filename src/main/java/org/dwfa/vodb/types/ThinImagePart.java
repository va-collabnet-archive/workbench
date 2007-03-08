package org.dwfa.vodb.types;

import org.dwfa.vodb.jar.I_MapNativeToNative;

public class ThinImagePart {
	private int pathId;
	private int version;
	private int statusId;
	private String textDescription;
	private int typeId;
	public int getPathId() {
		return pathId;
	}
	public int getStatusId() {
		return statusId;
	}
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
	public ThinImagePart() {
		
	}
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	public void setStatusId(int status) {
		this.statusId = status;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getTextDescription() {
		return textDescription;
	}
	public void setTextDescription(String name) {
		this.textDescription = name;
	}
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int type) {
		this.typeId = type;
	}
	
	public boolean hasNewData(ThinImagePart another) {
		return ((this.pathId != another.pathId) ||
				(this.statusId != another.statusId) ||
				((this.textDescription.equals(another.textDescription) == false) ||
				(this.typeId != another.typeId)));
	}
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
	
	

}
