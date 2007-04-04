package org.dwfa.ace.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface I_ImageVersioned {

	public abstract byte[] getImage();

	public abstract int getImageId();

	public abstract List<I_ImagePart> getVersions();

	public abstract boolean addVersion(I_ImagePart part);

	public abstract String getFormat();

	public abstract int getConceptId();

	public abstract I_ImageTuple getLastTuple();

	public abstract Collection<I_ImageTuple> getTuples();

	public abstract void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public abstract boolean merge(I_ImageVersioned jarImage);

	public abstract Set<TimePathId> getTimePathSet();

	public abstract void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, List<I_ImageTuple> returnImages);

}