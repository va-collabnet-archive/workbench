package org.ihtsdo.db.util;

import java.lang.ref.Reference;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;

/**
 * Garbage Collectible Value Concept Map
 * @author kec
 *
 */
public class GCValueConceptMap extends GCValueMap<Concept> {


	public GCValueConceptMap(int size, ReferenceType refType) {
		super(size, refType);
	}
	
	public GCValueConceptMap(ReferenceType refType) {
		super(refType);
	}


	protected Reference<Concept> makeReference(Concept c,
			Reference<Concept> newRef) {
		switch (refType) {
		case SOFT:
			newRef = new ConceptRefSoft(c, queue);
			break;
		case SOFT_OR_STRONG:
			newRef = new ConceptRefSoftOrStrong(c, queue);
			break;
		case WEAK:
			newRef = new ConceptRefWeak(c, queue);
			break;
		case WEAK_OR_STRONG:
			newRef = new ConceptRefWeakOrStrong(c, queue);
			break;
		case STRONG:
			newRef = new ConceptRefStrong(c, queue);
			break;
		default:
			throw new RuntimeException("Can't handle type: " + refType);
		}
		return newRef;
	}
}
