package org.ihtsdo.db.bdb.concept.component.attributes;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.ihtsdo.db.bdb.concept.component.Version;

public class ConceptAttributesVersion 
	extends Version<ConceptAttributesMutablePart, ConceptAttributes>
	implements I_ConceptAttributeTuple {

	protected ConceptAttributesVersion(ConceptAttributes component,
			ConceptAttributesMutablePart part) {
		super(component, part);
	}

	@Override
	public int getConId() {
		return getFixedPartId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ConceptAttributes getConVersioned() {
		return getFixedPart();
	}

	@Override
	public int getConceptStatus() {
		return getPart().getStatusId();
	}

	@Override
	public boolean isDefined() {
		return getPart().isDefined();
	}

	@Override
	public void setDefined(boolean defined) {
		throw new UnsupportedOperationException();
	}
}
