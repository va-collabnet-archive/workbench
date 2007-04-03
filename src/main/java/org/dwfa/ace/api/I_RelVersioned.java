package org.dwfa.ace.api;

import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.vodb.jar.I_MapNativeToNative;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.TimePathId;

public interface I_RelVersioned {

	public abstract boolean addVersion(I_RelPart rel);

	public abstract boolean addVersionNoRedundancyCheck(I_RelPart rel);

	public abstract List<I_RelPart> getVersions();

	public abstract int versionCount();

	public abstract boolean addRetiredRec(int[] releases, int retiredStatusId);

	public abstract boolean removeRedundantRecs();

	public abstract int getC1Id();

	public abstract int getC2Id();

	public abstract int getRelId();

	public abstract List<I_RelTuple> getTuples();

	public abstract I_RelTuple getFirstTuple();

	public abstract I_RelTuple getLastTuple();

	public abstract void addTuples(IntSet allowedStatus, IntSet allowedTypes,
			Set<Position> positions, List<I_RelTuple> returnRels,
			boolean addUncommitted);

	public abstract void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public abstract boolean merge(I_RelVersioned jarRel);

	public abstract Set<TimePathId> getTimePathSet();

	public abstract void setC2Id(int destId);

}