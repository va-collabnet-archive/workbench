package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.ihtsdo.concept.component.ConceptComponent;

public class ConceptComponentRefWeak extends WeakReference<ConceptComponent<?, ?>> 
	implements I_GetNid {

	protected int nid;

	public ConceptComponentRefWeak(ConceptComponent<?, ?> referent, ReferenceQueue<ConceptComponent<?, ?>> q) {
		super(referent, q);
		nid = referent.getNid();
	}

	public int getNid() {
		return nid;
	}
}
