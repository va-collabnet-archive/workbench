package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import org.ihtsdo.concept.component.ConceptComponent;

public class ConceptComponentRefStrong extends SoftReference<ConceptComponent<?, ?>> implements I_GetNid {
	private boolean strong = false;
	private int nid;
	private static ConcurrentHashMap<Integer, ConceptComponent<?, ?>> strongReferences = 
			new ConcurrentHashMap<Integer, ConceptComponent<?, ?>>();
	
	public ConceptComponentRefStrong(ConceptComponent<?, ?> referent, ReferenceQueue<ConceptComponent<?, ?>> q) {
		super(referent, q);
		nid = referent.getNid();
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

	@Override
	public int getNid() {
		return nid;
	}
	
}
