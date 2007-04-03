package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;

public class ThinDescTuple implements I_DescriptionTuple {
	I_DescriptionVersioned fixedPart;
	I_DescriptionPart variablePart;
	transient Integer hash;
	
	
	public ThinDescTuple(I_DescriptionVersioned fixedPart, I_DescriptionPart variablePart) {
		super();
		this.fixedPart = fixedPart;
		this.variablePart = variablePart;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getPathId()
	 */
	public int getPathId() {
		return variablePart.getPathId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getInitialCaseSignificant()
	 */
	public boolean getInitialCaseSignificant() {
		return variablePart.getInitialCaseSignificant();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getLang()
	 */
	public String getLang() {
		return variablePart.getLang();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getStatusId()
	 */
	public int getStatusId() {
		return variablePart.getStatusId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getText()
	 */
	public String getText() {
		return variablePart.getText();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getTypeId()
	 */
	public int getTypeId() {
		return variablePart.getTypeId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getVersion()
	 */
	public int getVersion() {
		return variablePart.getVersion();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getConceptId()
	 */
	public int getConceptId() {
		return fixedPart.getConceptId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getDescId()
	 */
	public int getDescId() {
		return fixedPart.getDescId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#setInitialCaseSignificant(boolean)
	 */
	public void setInitialCaseSignificant(boolean capStatus) {
		variablePart.setInitialCaseSignificant(capStatus);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#setLang(java.lang.String)
	 */
	public void setLang(String lang) {
		variablePart.setLang(lang);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#setPathId(int)
	 */
	public void setPathId(int pathId) {
		variablePart.setPathId(pathId);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#setStatusId(int)
	 */
	public void setStatusId(int status) {
		variablePart.setStatusId(status);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#setText(java.lang.String)
	 */
	public void setText(String text) {
		variablePart.setText(text);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#setTypeId(int)
	 */
	public void setTypeId(int typeInt) {
		variablePart.setTypeId(typeInt);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#setVersion(int)
	 */
	public void setVersion(int version) {
		variablePart.setVersion(version);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#duplicatePart()
	 */
	public I_DescriptionPart duplicatePart() {
		return variablePart.duplicate();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionTuple#getDescVersioned()
	 */
	public I_DescriptionVersioned getDescVersioned() {
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
