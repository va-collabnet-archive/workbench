package org.ihtsdo.db.bdb.concept.component.description;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.ihtsdo.db.bdb.concept.component.Version;

public class DescriptionVersion 
	extends Version<DescriptionMutablePart, Description> 
	implements I_DescriptionTuple {

	protected DescriptionVersion(Description component, DescriptionMutablePart part) {
		super(component, part);
	}

	@Override
	public int getConceptId() {
		return getFixedPart().getConceptId();
	}

	@Override
	public int getDescId() {
		return getFixedPart().getDescId();
	}

	@Override
	public Description getDescVersioned() {
		return getFixedPart();
	}

	@Override
	public boolean getInitialCaseSignificant() {
		return getPart().getInitialCaseSignificant();
	}

	@Override
	public String getLang() {
		return getPart().getLang();
	}
	
	@Override
	public String getText() {
		return getPart().getText();
	}

	@Override
	public void setInitialCaseSignificant(boolean capStatus) {
		getPart().setInitialCaseSignificant(capStatus);
	}

	@Override
	public void setLang(String lang) {
		getPart().setLang(lang);
	}

	@Override
	public void setText(String text) {
		getPart().setText(text);
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTypeId() {
		return getPart().getTypeId();
	}

	@Override
	public void setTypeId(int typeNid) {
		getPart().setTypeId(typeNid);
	}
	
}
