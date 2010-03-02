package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.ihtsdo.concept.Concept;

public class ConceptRefSoftOrStrong extends ConceptRefSoft {

	private boolean strong = false;
	private static ConcurrentHashMap<Integer, Concept> strongReferences = 
			new ConcurrentHashMap<Integer, Concept>();
	
	public ConceptRefSoftOrStrong(Concept referent, ReferenceQueue<? super Concept> q) {
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
