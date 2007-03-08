package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.vodb.jar.I_MapNativeToNative;

public class ThinConVersioned {
	private int conId;

	private List<ThinConPart> versions;

	public ThinConVersioned(int conId, int count) {
		super();
		this.conId = conId;
		this.versions = new ArrayList<ThinConPart>(count);
	}

	public boolean addVersion(ThinConPart con) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(con);
		} else if ((index >= 0) && (versions.get(index).hasNewData(con))) {
			return versions.add(con);
		}
		return false;
	}

	public List<ThinConPart> getVersions() {
		return versions;
	}

	public int versionCount() {
		return versions.size();
	}

	public int getConId() {
		return conId;
	}

	public List<ThinConTuple> getTuples() {
		List<ThinConTuple> tuples = new ArrayList<ThinConTuple>();
		for (ThinConPart p : versions) {
			tuples.add(new ThinConTuple(this, p));
		}
		return tuples;
	}

	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		conId = jarToDbNativeMap.get(conId);
		for (ThinConPart part : versions) {
			part.convertIds(jarToDbNativeMap);
		}

	}

	public boolean merge(ThinConVersioned jarCon) {
		HashSet<ThinConPart> versionSet = new HashSet<ThinConPart>(versions);
		boolean changed = false;
		for (ThinConPart jarPart : jarCon.versions) {
			if (!versionSet.contains(jarPart)) {
				changed = true;
				versions.add(jarPart);
			}
		}
		return changed;
	}

	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>();
		for (ThinConPart p : versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	public void addTuples(IntSet allowedStatus, IntSet allowedTypes,
			Set<Position> positions, List<ThinConTuple> returnImages) {
		Set<ThinConPart> uncommittedParts = new HashSet<ThinConPart>();
		if (positions == null) {
			List<ThinConPart> addedParts = new ArrayList<ThinConPart>();
			Set<ThinConPart> rejectedParts = new HashSet<ThinConPart>();
			for (ThinConPart part : versions) {
				if (part.getVersion() == Integer.MAX_VALUE) {
					uncommittedParts.add(part);
				} else {
					if ((allowedStatus != null)
							&& (!allowedStatus
									.contains(part.getConceptStatus()))) {
						rejectedParts.add(part);
						continue;
					}
					addedParts.add(part);
				}
			}
			for (ThinConPart part : addedParts) {
				boolean addPart = true;
				for (ThinConPart reject : rejectedParts) {
					if ((part.getVersion() <= reject.getVersion())
							&& (part.getPathId() == reject.getPathId())) {
						addPart = false;
						continue;
					}
				}
				if (addPart) {
					returnImages.add(new ThinConTuple(this, part));
				}
			}
		} else {

			Set<ThinConPart> addedParts = new HashSet<ThinConPart>();
			for (Position position : positions) {
				Set<ThinConPart> rejectedParts = new HashSet<ThinConPart>();
				ThinConTuple possible = null;
				for (ThinConPart part : versions) {
					if (part.getVersion() == Integer.MAX_VALUE) {
						uncommittedParts.add(part);
						continue;
					} else if ((allowedStatus != null)
							&& (!allowedStatus
									.contains(part.getConceptStatus()))) {
						if (possible != null) {
							Position rejectedStatusPosition = new Position(part
									.getVersion(), position.getPath()
									.getMatchingPath(part.getPathId()));
							Path possiblePath = position.getPath()
									.getMatchingPath(possible.getPathId());
							Position possibleStatusPosition = new Position(
									possible.getVersion(), possiblePath);
							if (position
									.isSubsequentOrEqualTo(rejectedStatusPosition)) {
								if (rejectedStatusPosition
										.isSubsequentOrEqualTo(possibleStatusPosition)) {
									possible = null;
								}
							}
						}
						rejectedParts.add(part);
						continue;
					}
					if (position.isSubsequentOrEqualTo(part.getVersion(), part
							.getPathId())) {
						if (possible == null) {
							if (!addedParts.contains(part)) {
								possible = new ThinConTuple(this, part);
								addedParts.add(part);
							}
						} else {
							if (possible.getPathId() == part.getPathId()) {
								if (part.getVersion() > possible.getVersion()) {
									if (!addedParts.contains(part)) {
										possible = new ThinConTuple(this, part);
										addedParts.add(part);
									}
								}
							} else {
								if (position.getDepth(part.getPathId()) < position
										.getDepth(possible.getPathId())) {
									if (!addedParts.contains(part)) {
										possible = new ThinConTuple(this, part);
										addedParts.add(part);
									}
								}
							}
						}
					}

				}
				if (possible != null) {
					Path possiblePath = position.getPath().getMatchingPath(
							possible.getPathId());
					Position possibleStatusPosition = new Position(possible
							.getVersion(), possiblePath);
					boolean addPart = true;
					for (ThinConPart reject : rejectedParts) {
						Position rejectedStatusPosition = new Position(reject
								.getVersion(), position.getPath()
								.getMatchingPath(reject.getPathId()));
						if ((rejectedStatusPosition
								.isSubsequentOrEqualTo(possibleStatusPosition))
								&& (position
										.isSubsequentOrEqualTo(rejectedStatusPosition))) {
							addPart = false;
							continue;
						}
					}
					if (addPart) {
						returnImages.add(possible);
					}
				}
			}
		}
		for (ThinConPart p : uncommittedParts) {
			returnImages.add(new ThinConTuple(this, p));
		}
	}

}
