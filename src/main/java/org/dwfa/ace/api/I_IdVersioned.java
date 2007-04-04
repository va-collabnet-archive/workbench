package org.dwfa.ace.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public interface I_IdVersioned {

	public abstract int getNativeId();

	public abstract void setNativeId(int nativeId);

	public abstract List<I_IdPart> getVersions();

	public abstract List<UUID> getUIDs();

	public abstract boolean addVersion(I_IdPart srcId);

	public abstract boolean hasVersion(I_IdPart newPart);

	public abstract Set<TimePathId> getTimePathSet();

	public abstract List<I_IdTuple> getTuples();

}