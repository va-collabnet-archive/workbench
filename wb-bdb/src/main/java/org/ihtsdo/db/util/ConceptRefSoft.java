package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import org.ihtsdo.concept.Concept;

public class ConceptRefSoft extends SoftReference<Concept> implements I_GetNid {

	protected int nid;

	public ConceptRefSoft(Concept referent, ReferenceQueue<? super Concept> q) {
		super(referent, q);
		nid = referent.getNid();
	}

	public int getNid() {
		return nid;
	}
}
