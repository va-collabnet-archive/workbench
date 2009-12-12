package org.ihtsdo.db.bdb.concept.component.description;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.ihtsdo.db.bdb.concept.component.Tuple;

public class DescriptionTuple 
	extends Tuple<DescriptionPart, Description> 
	implements I_DescriptionTuple<DescriptionPart, DescriptionTuple, Description> {
	
	private Description fixed;
	private DescriptionPart part;
	
	DescriptionTuple(Description immutable, DescriptionPart part) {
		super();
		this.fixed = immutable;
		this.part = part;
	}

	@Override
	public DescriptionPart duplicate() {
		return part.duplicate();
	}

	@Override
	public int getConceptId() {
		return fixed.getConceptId();
	}

	@Override
	public int getDescId() {
		return fixed.getDescId();
	}

	public Description getDescVersioned() {
		return fixed;
	}

	@Override
	public boolean getInitialCaseSignificant() {
		return part.getInitialCaseSignificant();
	}

	@Override
	public String getLang() {
		return part.getLang();
	}

	@Override
	public DescriptionPart getPart() {
		return part;
	}

	@Override
	public String getText() {
		return part.getText();
	}

	@Override
	public void setInitialCaseSignificant(boolean capStatus) {
		part.setInitialCaseSignificant(capStatus);
	}

	@Override
	public void setLang(String lang) {
		part.setLang(lang);
	}

	@Override
	public void setText(String text) {
		part.setText(text);
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTypeId() {
		return part.getTypeId();
	}

	@Override
	public void setTypeId(int typeNid) {
		part.setTypeId(typeNid);
	}

	@Override
	public ArrayIntList getPartComponentNids() {
		ArrayIntList componentNids = new ArrayIntList();
		componentNids.add(part.getPathId());
		componentNids.add(part.getStatusId());
		componentNids.add(part.getTypeId());
		return componentNids;
	}

	@Override
	public int getPathId() {
		return part.getPathId();
	}

	@Override
	public int getStatusId() {
		return part.getStatusId();
	}

	@Override
	public int getVersion() {
		return part.getVersion();
	}

	@Override
	public void setPathId(int pathId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatusId(int statusId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setVersion(int version) {
		throw new UnsupportedOperationException();
	}

	public Description getFixedPart() {
		return fixed;
	}

	public int getFixedPartId() {
		return fixed.getDescId();
	}

	public int getStatusAtPositionNid() {
		return part.getStatusAtPositionNid();
	}

	public long getTime() {
		return part.getTime();
	}

	public boolean hasNewData(DescriptionPart another) {
		return part.hasNewData(another);
	}

	public DescriptionPart makeAnalog(int statusNid, int pathNid, long time) {
		return part.makeAnalog(statusNid, pathNid, time);
	}
	

}
