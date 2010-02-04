package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.ihtsdo.db.bdb.concept.Concept;

public class WeakConceptRef extends WeakReference<Concept> {
	protected int nid;

	public WeakConceptRef(Concept referent, ReferenceQueue<Concept> q) {
		super(referent, q);
		nid = referent.getNid();
	}
}
