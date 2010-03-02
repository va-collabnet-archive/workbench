package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.ihtsdo.concept.component.ConceptComponent;

public class ConceptComponentRefSoftOrStrong extends ConceptComponentRefSoft {

	private boolean strong = false;
	private static ConcurrentHashMap<Integer, ConceptComponent<?, ?>> strongReferences = 
			new ConcurrentHashMap<Integer, ConceptComponent<?, ?>>();
	
	public ConceptComponentRefSoftOrStrong(ConceptComponent<?, ?> referent, 
			ReferenceQueue<ConceptComponent<?,?>> q) {
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
