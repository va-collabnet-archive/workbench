package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptInt;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConceptInt extends ThinExtByRefPartConcept
		implements I_ThinExtByRefPartConceptInt {

	private int intValue;

	public ThinExtByRefPartConceptInt(ThinExtByRefPartConceptInt another) {
		super(another);
		this.intValue = another.intValue;
	}

	public ThinExtByRefPartConceptInt() {
		super();
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			if (ThinExtByRefPartConceptInt.class.isAssignableFrom(obj
					.getClass())) {
				ThinExtByRefPartConceptInt another = (ThinExtByRefPartConceptInt) obj;
				return intValue == another.intValue;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ThinExtByRefPartConcept#getUniversalPart()
	 */
	@Override
	public UniversalAceExtByRefPart getUniversalPart()
			throws TerminologyException, IOException {
		I_TermFactory tf = LocalVersionedTerminology.get();
		UniversalAceExtByRefPartConceptInt universalPart = new UniversalAceExtByRefPartConceptInt();
		universalPart.setConceptUid(tf.getUids(getConceptId()));
		universalPart.setIntValue(getIntValue());
		universalPart.setPathUid(tf.getUids(getPathId()));
		universalPart.setStatusUid(tf.getUids(getStatus()));
		universalPart.setTime(ThinVersionHelper.convert(getVersion()));
		return universalPart;
	}

	@Override
	public I_ThinExtByRefPart duplicatePart() {
		return new ThinExtByRefPartConceptInt(this);
	}
	   public int compareTo(ThinExtByRefPart o) {
	       if (ThinExtByRefPartConceptInt.class.isAssignableFrom(o.getClass())) {
	           ThinExtByRefPartConceptInt otherPart = (ThinExtByRefPartConceptInt) o;
	           return this.intValue - otherPart.intValue;
	       }
	       return 1;
	   }

}
