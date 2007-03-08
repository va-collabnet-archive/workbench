package org.dwfa.vodb.types;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ThinIdTuple {
	ThinIdVersioned core;
	ThinIdPart part;
	public ThinIdTuple(ThinIdVersioned core, ThinIdPart part) {
		super();
		this.core = core;
		this.part = part;
	}
	public int getNativeId() {
		return core.getNativeId();
	}
	public Set<TimePathId> getTimePathSet() {
		return core.getTimePathSet();
	}
	public List<UUID> getUIDs() {
		return core.getUIDs();
	}
	public List<ThinIdPart> getVersions() {
		return core.getVersions();
	}
	public boolean hasVersion(ThinIdPart newPart) {
		return core.hasVersion(newPart);
	}
	public void setNativeId(int nativeId) {
		core.setNativeId(nativeId);
	}
	public int getIdStatus() {
		return part.getIdStatus();
	}
	public int getPathId() {
		return part.getPathId();
	}
	public int getSource() {
		return part.getSource();
	}
	public Object getSourceId() {
		return part.getSourceId();
	}
	public int getVersion() {
		return part.getVersion();
	}
	public ThinIdVersioned getIdVersioned() {
		return core;
	}
	public ThinIdPart duplicatePart() {
		ThinIdPart newPart = new ThinIdPart();
		newPart.setPathId(getPathId());
		newPart.setVersion(getVersion());
		newPart.setIdStatus(getIdStatus());
		newPart.setSource(getSource());
		newPart.setSourceId(getSourceId());
		return newPart;
	}

}
