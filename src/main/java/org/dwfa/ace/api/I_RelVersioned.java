package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.tapi.TerminologyException;

public interface I_RelVersioned {

	public boolean addVersion(I_RelPart rel);

	public boolean addVersionNoRedundancyCheck(I_RelPart rel);

	public List<I_RelPart> getVersions();

	public int versionCount();

	public boolean addRetiredRec(int[] releases, int retiredStatusId);

	public boolean removeRedundantRecs();

	public int getC1Id();

	public int getC2Id();

	public int getRelId();

	public List<I_RelTuple> getTuples();

	public I_RelTuple getFirstTuple();

	public I_RelTuple getLastTuple();

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, List<I_RelTuple> returnRels,
			boolean addUncommitted);

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public boolean merge(I_RelVersioned jarRel);

	public Set<TimePathId> getTimePathSet();

	public void setC2Id(int destId);

	public UniversalAceRelationship getUniversal() throws IOException,
			TerminologyException;

}