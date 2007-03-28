package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.vodb.jar.I_MapNativeToNative;

public class ThinRelVersioned {
	private int relId;

	private int componentOneId;

	private int componentTwoId;

	private List<ThinRelPart> versions;

	public ThinRelVersioned(int relId, int componentOneId, int componentTwoId,
			int count) {
		super();
		this.relId = relId;
		this.componentOneId = componentOneId;
		this.componentTwoId = componentTwoId;
		this.versions = new ArrayList<ThinRelPart>(count);
	}

	public boolean addVersion(ThinRelPart rel) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(rel);
		} else if ((index >= 0) && (versions.get(index).hasNewData(rel))) {
			return versions.add(rel);
		}
		return false;
	}

	public boolean addVersionNoRedundancyCheck(ThinRelPart rel) {
		return versions.add(rel);
	}

	public List<ThinRelPart> getVersions() {
		return versions;
	}

	public int versionCount() {
		return versions.size();
	}

	public boolean addRetiredRec(int[] releases, int retiredStatusId) {
		ThinRelPart lastRelVersion = versions.get(versions.size() - 1);
		if (lastRelVersion.getVersion() != releases[releases.length - 1]) {
			// last version is not from the last release,
			// so we need to inactivate the relationship.
			int lastMention = 0;
			for (; releases[lastMention] != lastRelVersion.getVersion(); lastMention++) {

			}
			int retiredVersion = releases[lastMention + 1];
			ThinRelPart retiredRel = new ThinRelPart();
			retiredRel.setPathId(lastRelVersion.getPathId());
			retiredRel.setVersion(retiredVersion);
			retiredRel.setStatusId(retiredStatusId);
			retiredRel
					.setCharacteristicId(lastRelVersion.getCharacteristicId());
			retiredRel.setGroup(lastRelVersion.getGroup());
			retiredRel.setRefinabilityId(lastRelVersion.getRefinabilityId());
			retiredRel.setRelTypeId(lastRelVersion.getRelTypeId());
			versions.add(retiredRel);
			return true;
		}
		return false;
	}

	public boolean removeRedundantRecs() {
		ThinRelVersioned compact = new ThinRelVersioned(relId, componentOneId,
				componentTwoId, versions.size());
		for (ThinRelPart v : versions) {
			compact.addVersion(v);
		}
		if (versions.size() == compact.getVersions().size()) {
			return false;
		}
		versions = compact.getVersions();
		return true;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("ThinRelVersioned: relId: ");
		buff.append(relId);
		buff.append(" c1id: ");
		buff.append(componentOneId);
		buff.append(" c2id: ");
		buff.append(componentTwoId);
		buff.append("\n");
		for (ThinRelPart rel : versions) {
			buff.append("     ");
			buff.append(rel.toString());
			buff.append("\n");
		}

		return buff.toString();
	}

	public int getC1Id() {
		return componentOneId;
	}

	public int getC2Id() {
		return componentTwoId;
	}

	public int getRelId() {
		return relId;
	}

	public List<ThinRelTuple> getTuples() {
		List<ThinRelTuple> tuples = new ArrayList<ThinRelTuple>();
		for (ThinRelPart p : versions) {
			tuples.add(new ThinRelTuple(this, p));
		}
		return tuples;
	}

	public ThinRelTuple getFirstTuple() {
		return new ThinRelTuple(this, versions.get(0));
	}

	public ThinRelTuple getLastTuple() {
		return new ThinRelTuple(this, versions.get(versions.size() - 1));
	}

	public void addTuples(IntSet allowedStatus, IntSet allowedTypes,
			Set<Position> positions, List<ThinRelTuple> returnRels, boolean addUncommitted) {
		Set<ThinRelPart> uncommittedParts = new HashSet<ThinRelPart>();
		if (positions == null) {
			List<ThinRelPart> addedParts = new ArrayList<ThinRelPart>();
			Set<ThinRelPart> rejectedParts = new HashSet<ThinRelPart>();
			for (ThinRelPart part : versions) {
				if (part.getVersion() == Integer.MAX_VALUE) {
					uncommittedParts.add(part);
				} else {
					if ((allowedStatus != null)
							&& (!allowedStatus.contains(part.getStatusId()))) {
						rejectedParts.add(part);
						continue;
					}
					if ((allowedTypes != null)
							&& (!allowedTypes.contains(part.getRelTypeId()))) {
						rejectedParts.add(part);
						continue;
					}
					addedParts.add(part);
				}
			}
			for (ThinRelPart part : addedParts) {
				boolean addPart = true;
				for (ThinRelPart reject : rejectedParts) {
					if ((part.getVersion() <= reject.getVersion())
							&& (part.getPathId() == reject.getPathId())) {
						addPart = false;
						continue;
					}
				}
				if (addPart) {
					returnRels.add(new ThinRelTuple(this, part));
				}
			}
		} else {

			Set<ThinRelPart> addedParts = new HashSet<ThinRelPart>();
			for (Position position : positions) {
				Set<ThinRelPart> rejectedParts = new HashSet<ThinRelPart>();
				ThinRelTuple possible = null;
				for (ThinRelPart part : versions) {
					if (part.getVersion() == Integer.MAX_VALUE) {
						uncommittedParts.add(part);
						continue;
					} else if ((allowedStatus != null)
							&& (!allowedStatus.contains(part.getStatusId()))) {
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
					if ((allowedTypes != null)
							&& (!allowedTypes.contains(part.getRelTypeId()))) {
						rejectedParts.add(part);
						continue;
					}
					if (position.isSubsequentOrEqualTo(part.getVersion(), part
							.getPathId())) {
						if (possible == null) {
							if (!addedParts.contains(part)) {
								possible = new ThinRelTuple(this, part);
								addedParts.add(part);
							}
						} else {
							if (possible.getPathId() == part.getPathId()) {
								if (part.getVersion() > possible.getVersion()) {
									if (!addedParts.contains(part)) {
										possible = new ThinRelTuple(this, part);
										addedParts.add(part);
									}
								}
							} else {
								if (position.getDepth(part.getPathId()) < position
										.getDepth(possible.getPathId())) {
									if (!addedParts.contains(part)) {
										possible = new ThinRelTuple(this, part);
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
					for (ThinRelPart reject : rejectedParts) {
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
						returnRels.add(possible);
					}
				}
			}
		}
		if (addUncommitted) {
			for (ThinRelPart p: uncommittedParts) {
				returnRels.add(new ThinRelTuple(this, p));
			}
		}
	}

	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		componentOneId = jarToDbNativeMap.get(componentOneId);
		componentTwoId = jarToDbNativeMap.get(componentTwoId);
		relId = jarToDbNativeMap.get(relId);
		for (ThinRelPart p : versions) {
			p.convertIds(jarToDbNativeMap);
		}

	}

	public boolean merge(ThinRelVersioned jarRel) {
		HashSet<ThinRelPart> versionSet = new HashSet<ThinRelPart>(versions);
		boolean changed = false;
		for (ThinRelPart jarPart : jarRel.versions) {
			if (!versionSet.contains(jarPart)) {
				changed = true;
				versions.add(jarPart);
			}
		}
		return changed;
	}

	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>();
		for (ThinRelPart p : versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	public void setC2Id(int destId) {
		componentTwoId = destId;
		
	}

}
