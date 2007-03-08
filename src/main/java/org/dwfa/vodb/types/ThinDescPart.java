package org.dwfa.vodb.types;

import org.dwfa.vodb.jar.I_MapNativeToNative;


public class ThinDescPart {
	private int pathId;
	private int version;
	private int statusId;
	private String text;
	private boolean initialCaseSignificant;
	private int typeId; 
	private String lang;
	
	public boolean hasNewData(ThinDescPart another) {
		return ((this.pathId != another.pathId) ||
				(this.statusId != another.statusId) ||
				((this.text.equals(another.text) == false) ||
				(this.initialCaseSignificant != another.initialCaseSignificant) ||
				(this.typeId != another.typeId) ||
				((this.lang.equals(another.lang) == false))));
	}

	public int getPathId() {
		return pathId;
	}
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	public boolean getInitialCaseSignificant() {
		return initialCaseSignificant;
	}
	public void setInitialCaseSignificant(boolean capStatus) {
		this.initialCaseSignificant = capStatus;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public int getStatusId() {
		return statusId;
	}
	public void setStatusId(int status) {
		this.statusId = status;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeInt) {
		this.typeId = typeInt;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("ThinDescPart pathId: ");
		buff.append(pathId);
		buff.append(" version: ");
		buff.append(version);
		buff.append(" statusId: ");
		buff.append(statusId);
		buff.append(" text: ");
		buff.append(text);
		buff.append(" typeId: ");
		buff.append(typeId);
		buff.append(" init case sig: ");
		buff.append(initialCaseSignificant);
		buff.append(" lang: ");
		buff.append(lang);
		
		return buff.toString();
	}

	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		pathId = jarToDbNativeMap.get(pathId);
		statusId = jarToDbNativeMap.get(statusId);
		typeId = jarToDbNativeMap.get(typeId);
	}

	@Override
	public boolean equals(Object obj) {
		ThinDescPart another = (ThinDescPart) obj;
		return ((initialCaseSignificant == another.initialCaseSignificant) &&
				(lang.equals(another.lang)) && 
				(pathId == another.pathId) && 
				(text.equals(another.text)) &&
				(typeId == another.typeId) &&
				(version == another.version));
	}

	@Override
	public int hashCode() {
		int bhash = 0;
		if (initialCaseSignificant) {
			bhash = 1;
		}
		return HashFunction.hashCode(new int[] {
				bhash, lang.hashCode(), pathId, 
				statusId, text.hashCode(),
				typeId, version
		});
	}

	public ThinDescPart duplicate() {
		ThinDescPart newPart = new ThinDescPart();
		newPart.pathId = pathId;
		newPart.version = version;
		newPart.statusId = statusId;
		newPart.text = text;
		newPart.initialCaseSignificant = initialCaseSignificant;
		newPart.typeId = typeId; 
		newPart.lang = lang;
		return newPart;
	}
	
	
}
