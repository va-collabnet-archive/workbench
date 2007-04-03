package org.dwfa.ace.api;

import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.vodb.jar.I_MapNativeToNative;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.TimePathId;

public interface I_ConceptAttributeVersioned {

	public abstract boolean addVersion(I_ConceptAttributePart part);

	public abstract List<I_ConceptAttributePart> getVersions();

	public abstract int versionCount();

	public abstract int getConId();

	public abstract List<I_ConceptAttributeTuple> getTuples();

	public abstract void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public abstract boolean merge(I_ConceptAttributeVersioned jarCon);

	public abstract Set<TimePathId> getTimePathSet();

	public abstract void addTuples(IntSet allowedStatus,
			Set<Position> positions, List<I_ConceptAttributeTuple> returnTuples);

	public abstract I_ConceptualizeLocally getLocalFixedConcept();

}