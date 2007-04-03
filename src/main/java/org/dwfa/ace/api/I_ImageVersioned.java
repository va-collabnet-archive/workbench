package org.dwfa.ace.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.vodb.jar.I_MapNativeToNative;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinImagePart;
import org.dwfa.vodb.types.ThinImageTuple;
import org.dwfa.vodb.types.ThinImageVersioned;
import org.dwfa.vodb.types.TimePathId;

public interface I_ImageVersioned {

	public abstract byte[] getImage();

	public abstract int getImageId();

	public abstract List<I_ImagePart> getVersions();

	public abstract boolean addVersion(ThinImagePart part);

	public abstract String getFormat();

	public abstract int getConceptId();

	public abstract I_ImageTuple getLastTuple();

	public abstract Collection<ThinImageTuple> getTuples();

	public abstract void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public abstract boolean merge(ThinImageVersioned jarImage);

	public abstract Set<TimePathId> getTimePathSet();

	public abstract void addTuples(IntSet allowedStatus, IntSet allowedTypes,
			Set<Position> positions, List<ThinImageTuple> returnImages);

}