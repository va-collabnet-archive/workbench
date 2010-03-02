package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import org.ihtsdo.concept.Concept;

public class ConceptRefStrong extends SoftReference<Concept> implements I_GetNid {
	private boolean strong = false;
	private int nid;
	private static ConcurrentHashMap<Integer, Concept> strongReferences = 
			new ConcurrentHashMap<Integer, Concept>();
	
	public ConceptRefStrong(Concept referent, ReferenceQueue<? super Concept> q) {
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
