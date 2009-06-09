package org.dwfa.ace.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public interface I_IdTuple extends I_AmTuple {

	public abstract int getNativeId();

	public abstract Set<TimePathId> getTimePathSet();

	public abstract List<UUID> getUIDs();

	public abstract List<I_IdPart> getVersions();

	public abstract boolean hasVersion(I_IdPart newPart);

	public abstract void setNativeId(int nativeId);

	/**
	 * @deprecated Use {@link #getStatusId()}
	 */
	@Deprecated
	public abstract int getIdStatus();

	public abstract int getSource();

	public abstract Object getSourceId();

	public abstract I_IdVersioned getIdVersioned();

	/**
	 * @deprecated Use {@link #duplicate()}
	 */
	@Deprecated
	public abstract I_IdPart duplicatePart();

	public I_IdPart duplicate();
	
   public abstract I_IdPart getPart();

}