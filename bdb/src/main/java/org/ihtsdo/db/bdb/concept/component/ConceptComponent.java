package org.ihtsdo.db.bdb.concept.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.TimePathId;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class ConceptComponent<P extends MutablePart<P>> implements I_AmTermComponent {
	public int nid;
	public boolean editable;
	public ArrayList<P> mutableParts;
	
	protected ConceptComponent(int nid, int listSize, boolean editable) {
		super();
		this.nid = nid;
		this.editable = editable;
		this.mutableParts = new ArrayList<P>(listSize);
	}
	
	public final List<P> getVersions() {
		if (editable) {
			return mutableParts;
		}
		return Collections.unmodifiableList(mutableParts);
	}

	public boolean addVersion(P newPart) {
		if (editable) {
			return mutableParts.add(newPart);
		}
		throw new RuntimeException("versions is not editable");
	}
	
	public boolean addVersionNoRedundancyCheck(P newPart) {
		return mutableParts.add(newPart);
	}
	
	public boolean hasVersion(P p) {
		return mutableParts.contains(p);
	}

	public final int versionCount() {
		return mutableParts.size();
	}

	public final int getNid() {
		return nid;
	}

	public final int getNativeId() {
		return nid;
	}


	public final Set<TimePathId> getTimePathSet() {
		Set<TimePathId> set = new TreeSet<TimePathId>();
		for (P p : mutableParts) {
			set.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return set;
	}


	public abstract void readComponentFromBdb(TupleInput input, int conceptNid, int listSize);
	
	public abstract void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid);

}
