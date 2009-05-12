package org.dwfa.ace.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public interface I_IdTuple extends I_AmPart {

	public abstract int getNativeId();

	public abstract Set<TimePathId> getTimePathSet();

	public abstract List<UUID> getUIDs();

	public abstract List<I_IdPart> getVersions();

	public abstract boolean hasVersion(I_IdPart newPart);

	public abstract void setNativeId(int nativeId);

	public abstract int getIdStatus();

	public abstract int getPathId();

	public abstract int getSource();

	public abstract Object getSourceId();

	public abstract int getVersion();

	public abstract I_IdVersioned getIdVersioned();

   public abstract I_IdPart duplicatePart();

   public abstract I_IdPart getPart();

}