package org.dwfa.vodb.types;

public class ThinDescTuple {
	ThinDescVersioned fixedPart;
	ThinDescPart variablePart;
	transient Integer hash;
	
	
	public ThinDescTuple(ThinDescVersioned fixedPart, ThinDescPart variablePart) {
		super();
		this.fixedPart = fixedPart;
		this.variablePart = variablePart;
	}
	public int getPathId() {
		return variablePart.getPathId();
	}
	public boolean getInitialCaseSignificant() {
		return variablePart.getInitialCaseSignificant();
	}
	public String getLang() {
		return variablePart.getLang();
	}
	public int getStatusId() {
		return variablePart.getStatusId();
	}
	public String getText() {
		return variablePart.getText();
	}
	public int getTypeId() {
		return variablePart.getTypeId();
	}
	public int getVersion() {
		return variablePart.getVersion();
	}
	public int getConceptId() {
		return fixedPart.getConceptId();
	}
	public int getDescId() {
		return fixedPart.getDescId();
	}
	public void setInitialCaseSignificant(boolean capStatus) {
		variablePart.setInitialCaseSignificant(capStatus);
	}
	public void setLang(String lang) {
		variablePart.setLang(lang);
	}
	public void setPathId(int pathId) {
		variablePart.setPathId(pathId);
	}
	public void setStatusId(int status) {
		variablePart.setStatusId(status);
	}
	public void setText(String text) {
		variablePart.setText(text);
	}
	public void setTypeId(int typeInt) {
		variablePart.setTypeId(typeInt);
	}
	public void setVersion(int version) {
		variablePart.setVersion(version);
	}
	public ThinDescPart duplicatePart() {
		return variablePart.duplicate();
	}
	public ThinDescVersioned getDescVersioned() {
		return fixedPart;
	}
	@Override
	public boolean equals(Object obj) {
		ThinDescTuple another = (ThinDescTuple) obj;
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
