package org.dwfa.vodb.types;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;

public class ThinIdTuple implements I_IdTuple {
	I_IdVersioned core;
	I_IdPart part;
	public ThinIdTuple(I_IdVersioned core, I_IdPart part) {
		super();
		this.core = core;
		this.part = part;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getNativeId()
	 */
	public int getNativeId() {
		return core.getNativeId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getTimePathSet()
	 */
	public Set<TimePathId> getTimePathSet() {
		return core.getTimePathSet();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getUIDs()
	 */
	public List<UUID> getUIDs() {
		return core.getUIDs();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getVersions()
	 */
	public List<I_IdPart> getVersions() {
		return core.getVersions();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#hasVersion(org.dwfa.vodb.types.I_IdPart)
	 */
	public boolean hasVersion(I_IdPart newPart) {
		return core.hasVersion(newPart);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#setNativeId(int)
	 */
	public void setNativeId(int nativeId) {
		core.setNativeId(nativeId);
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getIdStatus()
	 */
	public int getIdStatus() {
		return part.getIdStatus();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getPathId()
	 */
	public int getPathId() {
		return part.getPathId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getSource()
	 */
	public int getSource() {
		return part.getSource();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getSourceId()
	 */
	public Object getSourceId() {
		return part.getSourceId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getVersion()
	 */
	public int getVersion() {
		return part.getVersion();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#getIdVersioned()
	 */
	public I_IdVersioned getIdVersioned() {
		return core;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdTuple#duplicatePart()
	 */
	public I_IdPart duplicatePart() {
		ThinIdPart newPart = new ThinIdPart();
		newPart.setPathId(getPathId());
		newPart.setVersion(getVersion());
		newPart.setIdStatus(getIdStatus());
		newPart.setSource(getSource());
		newPart.setSourceId(getSourceId());
		return newPart;
	}

}
