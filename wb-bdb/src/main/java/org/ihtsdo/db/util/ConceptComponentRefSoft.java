package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import org.ihtsdo.concept.component.ConceptComponent;

public class ConceptComponentRefSoft extends SoftReference<ConceptComponent<?, ?>> 
	implements I_GetNid {

	protected int nid;

	public ConceptComponentRefSoft(ConceptComponent<?, ?> referent, 
			ReferenceQueue<ConceptComponent<?, ?>> q) {
		super(referent, q);
		nid = referent.getNid();
	}

	public int getNid() {
		return nid;
	}
}
