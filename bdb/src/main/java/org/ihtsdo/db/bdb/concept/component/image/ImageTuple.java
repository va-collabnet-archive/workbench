package org.ihtsdo.db.bdb.concept.component.image;

import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.ihtsdo.db.bdb.concept.component.Tuple;

public class ImageTuple 
	extends Tuple<ImageVariablePart, Image> 
	implements I_ImageTuple {

	protected ImageTuple(Image component, ImageVariablePart part) {
		super(component, part);
	}

	@Override
	public int getConceptId() {
		return getFixedPart().getConceptId();
	}

	@Override
	public String getFormat() {
		return getFixedPart().getFormat();
	}

	@Override
	public byte[] getImage() {
		return getFixedPart().getImage();
	}

	@Override
	public int getImageId() {
		return getFixedPart().getImageId();
	}

	@Override
	public String getTextDescription() {
		return getPart().getTextDescription();
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
