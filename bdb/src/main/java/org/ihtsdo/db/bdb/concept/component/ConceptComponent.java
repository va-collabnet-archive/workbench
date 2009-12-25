package org.ihtsdo.db.bdb.concept.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.tapi.TerminologyException;

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
	
	public final boolean addMutablePart(P part) {
		return mutableParts.add(part);
	}

	public final List<P> getMutableParts() {
		if (editable) {
			return mutableParts;
		}
		return Collections.unmodifiableList(mutableParts);
	}
	

	public final List<P> getMutableParts(boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
		if (returnConflictResolvedLatestState) {
	        List<P> returnList = new ArrayList<P>(mutableParts.size());
	        if (returnConflictResolvedLatestState) {
	            I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
	            returnList = config.getConflictResolutionStrategy().resolveParts(returnList);
	        }
	        return returnList;
		}
		return getMutableParts();
	}

	
	public final int getMutablePartCount() {
		return mutableParts.size();
	}

	public final int getNid() {
		return nid;
	}

	public final int getNativeId() {
		return nid;
	}


	public abstract void readComponentFromBdb(TupleInput input, int conceptNid, int listSize);
	
	public abstract void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid);

	
	/*
	 * Below methods have confusing naming, and should be considered for deprecation...
	 */
	public final List<P> getVersions() {
		return getMutableParts();
	}

	public final boolean addVersion(P newPart) {
		if (editable) {
			return mutableParts.add(newPart);
		}
		throw new RuntimeException("versions is not editable");
	}
	
	public final boolean addVersionNoRedundancyCheck(P newPart) {
		return mutableParts.add(newPart);
	}
	
	public final boolean hasVersion(P p) {
		return mutableParts.contains(p);
	}
	
	public final List<? extends P> getVersions(boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		return getMutableParts(returnConflictResolvedLatestState);
	}

	public final int versionCount() {
		return mutableParts.size();
	}
	
	public final Set<TimePathId> getTimePathSet() {
		Set<TimePathId> set = new TreeSet<TimePathId>();
		for (P p : mutableParts) {
			set.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return set;
	}
}
