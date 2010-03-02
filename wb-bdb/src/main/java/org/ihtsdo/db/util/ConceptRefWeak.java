package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.ihtsdo.concept.Concept;

public class ConceptRefWeak extends WeakReference<Concept> implements I_GetNid {
	protected int nid;

	public ConceptRefWeak(Concept referent, ReferenceQueue<? super Concept> q) {
		super(referent, q);
		nid = referent.getNid();
	}

	public int getNid() {
		return nid;
	}
}
