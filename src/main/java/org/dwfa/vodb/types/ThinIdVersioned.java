package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ThinIdVersioned {
	public static final int SNOMED_CT_T3_PREFIX = 1;
	private int nativeId;
	private List<ThinIdPart> versions;
	
	public ThinIdVersioned(int nativeId, int count) {
		super();
		this.nativeId = nativeId;
		this.versions = new ArrayList<ThinIdPart>(count);
	}
	public int getNativeId() {
		return nativeId;
	}
	public void setNativeId(int nativeId) {
		this.nativeId = nativeId;
	}
	public List<ThinIdPart> getVersions() {
		return versions;
	}
	public List<UUID> getUIDs() {
		List<UUID> uids = new ArrayList<UUID>(versions.size());
		for (ThinIdPart p: versions) {
			if (UUID.class.isAssignableFrom(p.getSourceId().getClass())) {
				uids.add((UUID) p.getSourceId());
			}
		}
		return uids;
	}
	
	public boolean addVersion(ThinIdPart srcId) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(srcId);
		} else if ((index >= 0)
				&& (versions.get(index).hasNewData(srcId))) {
			return versions.add(srcId);
		}
		return false;
	}
	
	public boolean hasVersion(ThinIdPart newPart) {
		for (ThinIdPart p: versions) {
			if (p.equals(newPart)) {
				return true;
			}
		}
		return false;
	}
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("NativeId: ");
		buf.append(nativeId);
		buf.append(" parts: ");
		buf.append(versions.size());
		buf.append("\n  ");
		for (ThinIdPart p: versions) {
			buf.append(p);
			buf.append("\n  ");
		}
		return buf.toString();
	}
	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>(); 
		for (ThinIdPart p: versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}
	public List<ThinIdTuple> getTuples() {
		List<ThinIdTuple> tuples = new ArrayList<ThinIdTuple>();
		for (ThinIdPart p : versions) {
			tuples.add(new ThinIdTuple(this, p));
		}
		return tuples;
	}

}
