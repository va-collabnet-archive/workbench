package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.ihtsdo.concept.component.ConceptComponent;

public class ConceptComponentRefWeakOrStrong extends ConceptComponentRefWeak {
	private boolean strong = false;
	private static ConcurrentHashMap<Integer, ConceptComponent<?, ?>> strongReferences = 
			new ConcurrentHashMap<Integer, ConceptComponent<?, ?>>();
	
	public ConceptComponentRefWeakOrStrong(ConceptComponent<?, ?> referent, ReferenceQueue<ConceptComponent<?, ?>> q) {
		super(referent, q);
	}

	public boolean isStrong() {
		return strong;
	}

	public void setStrong(boolean strong) {
		if (strong == this.strong) {
			return;
		}
		if (strong) {
			strongReferences.put(nid, get());
		} else {
			strongReferences.remove(nid);
		}
		this.strong = strong;
	}

}
