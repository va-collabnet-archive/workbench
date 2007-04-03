package org.dwfa.ace.api;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.dwfa.ace.IntSet;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.vodb.jar.I_MapNativeToNative;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinDescTuple;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.TimePathId;

public interface I_DescriptionVersioned {

	public abstract boolean addVersion(I_DescriptionPart newPart);

	public abstract List<I_DescriptionPart> getVersions();

	public abstract int versionCount();

	public abstract boolean matches(Pattern p);

	public abstract int getConceptId();

	public abstract int getDescId();

	public abstract List<ThinDescTuple> getTuples();

	public abstract I_DescriptionTuple getFirstTuple();

	public abstract I_DescriptionTuple getLastTuple();

	public abstract void addTuples(IntSet allowedStatus, IntSet allowedTypes,
			Set<Position> positions, List<I_DescriptionTuple> matchingTuples);

	public abstract void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public abstract boolean merge(ThinDescVersioned jarDesc);

	public abstract Set<TimePathId> getTimePathSet();

	public abstract I_DescribeConceptLocally toLocalFixedDesc();

}