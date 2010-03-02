package org.ihtsdo.db.util;

import java.lang.ref.Reference;

import org.ihtsdo.concept.component.ConceptComponent;

public class GCValueComponentMap extends GCValueMap<ConceptComponent<?, ?>> {

	public GCValueComponentMap(int size, ReferenceType refType) {
		super(size, refType);
	}

	public GCValueComponentMap(ReferenceType refType) {
		super(refType);
	}


	protected Reference<ConceptComponent<?, ?>> makeReference(
			ConceptComponent<?, ?> c,
			Reference<ConceptComponent<?, ?>> newRef) {
		switch (refType) {
		case SOFT:
			newRef = new ConceptComponentRefSoft(c, queue);
			break;
		case SOFT_OR_STRONG:
			newRef = new ConceptComponentRefSoftOrStrong(c, queue);
			break;
		case WEAK:
			newRef = new ConceptComponentRefWeak(c, queue);
			break;
		case WEAK_OR_STRONG:
			newRef = new ConceptComponentRefWeakOrStrong(c, queue);
			break;
		case STRONG:
			newRef = new ConceptComponentRefStrong(c, queue);
			break;
		default:
			throw new RuntimeException("Can't handle type: " + refType);
		}
		return newRef;
	}

}
