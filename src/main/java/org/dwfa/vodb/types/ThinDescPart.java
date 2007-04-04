package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_MapNativeToNative;


public class ThinDescPart implements I_DescriptionPart {
	private int pathId;
	private int version;
	private int statusId;
	private String text;
	private boolean initialCaseSignificant;
	private int typeId; 
	private String lang;
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#hasNewData(org.dwfa.vodb.types.ThinDescPart)
	 */
	public boolean hasNewData(I_DescriptionPart another) {
		return ((this.pathId != another.getPathId()) ||
				(this.statusId != another.getStatusId()) ||
				((this.text.equals(another.getText()) == false) ||
				(this.initialCaseSignificant != another.getInitialCaseSignificant()) ||
				(this.typeId != another.getTypeId()) ||
				((this.lang.equals(another.getLang()) == false))));
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getPathId()
	 */
	public int getPathId() {
		return pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setPathId(int)
	 */
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getInitialCaseSignificant()
	 */
	public boolean getInitialCaseSignificant() {
		return initialCaseSignificant;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setInitialCaseSignificant(boolean)
	 */
	public void setInitialCaseSignificant(boolean capStatus) {
		this.initialCaseSignificant = capStatus;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getLang()
	 */
	public String getLang() {
		return lang;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setLang(java.lang.String)
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getStatusId()
	 */
	public int getStatusId() {
		return statusId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setStatusId(int)
	 */
	public void setStatusId(int status) {
		this.statusId = status;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getText()
	 */
	public String getText() {
		return text;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setText(java.lang.String)
	 */
	public void setText(String text) {
		this.text = text;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getTypeId()
	 */
	public int getTypeId() {
		return typeId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setTypeId(int)
	 */
	public void setTypeId(int typeInt) {
		this.typeId = typeInt;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#getVersion()
	 */
	public int getVersion() {
		return version;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#setVersion(int)
	 */
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

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
	 */
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

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionPart#duplicate()
	 */
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
