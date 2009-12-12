package org.ihtsdo.db.bdb.concept.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dwfa.ace.api.I_AmTermComponent;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class ConceptComponent<P extends Part<P>> implements I_AmTermComponent {
	public int nid;
	public boolean editable;
	public List<P> versions;
	
	protected ConceptComponent(int nid, int listSize, boolean editable) {
		super();
		this.nid = nid;
		this.editable = editable;
		this.versions = new ArrayList<P>(listSize);
	}
	
	public final List<P> getVersions() {
		if (editable) {
			return versions;
		}
		return Collections.unmodifiableList(versions);
	}

	public boolean addVersion(P newPart) {
		if (editable) {
			return versions.add(newPart);
		}
		throw new RuntimeException("versions is not editable");
	}

	public final int versionCount() {
		return versions.size();
	}

	public final int getNid() {
		return nid;
	}


	public abstract void readComponentFromBdb(TupleInput input, int conceptNid);
	
	public abstract void readPartFromBdb(TupleInput input);

	public abstract void writeComponentToBdb(TupleOutput output);

}
