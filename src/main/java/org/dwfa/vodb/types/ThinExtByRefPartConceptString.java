package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConceptString extends ThinExtByRefPart implements
		I_ThinExtByRefPartConceptString {
	
	private int c1id;
	private String str;

	public ThinExtByRefPartConceptString(ThinExtByRefPartConceptString another) {
		super(another);
		this.c1id = another.c1id;
		this.str = another.str;
	}

	public ThinExtByRefPartConceptString() {
		super();
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			if (ThinExtByRefPartConceptString.class.isAssignableFrom(obj
					.getClass())) {
				ThinExtByRefPartConceptString another = (ThinExtByRefPartConceptString) obj;
				return c1id == another.c1id && str.equals(another.str);
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
		UniversalAceExtByRefPartConceptString universalPart = new UniversalAceExtByRefPartConceptString();
		universalPart.setC1UuidCollection(tf.getUids(getC1id()));
		universalPart.setStr(getStr());
		universalPart.setPathUid(tf.getUids(getPathId()));
		universalPart.setStatusUid(tf.getUids(getStatusId()));
		universalPart.setTime(ThinVersionHelper.convert(getVersion()));
		return universalPart;
	}

	public I_ThinExtByRefPart duplicate() {
		return new ThinExtByRefPartConceptString(this);
	}

	public int compareTo(I_ThinExtByRefPart o) {
		if (ThinExtByRefPartConceptString.class.isAssignableFrom(o.getClass())) {
			ThinExtByRefPartConceptString otherPart = (ThinExtByRefPartConceptString) o;
			if (c1id != otherPart.c1id) {
				return c1id - otherPart.c1id;
			}
			return str.compareTo(otherPart.str);
		}
		return 1;
	}

	public int getC1id() {
		return c1id;
	}

	public void setC1id(int c1id) {
		this.c1id = c1id;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}
}
