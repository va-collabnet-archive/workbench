package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.vodb.jar.I_MapNativeToNative;

public class ThinRelVersioned implements I_RelVersioned {
	private int relId;

	private int componentOneId;

	private int componentTwoId;

	private List<I_RelPart> versions;

	public ThinRelVersioned(int relId, int componentOneId, int componentTwoId,
			int count) {
		super();
		this.relId = relId;
		this.componentOneId = componentOneId;
		this.componentTwoId = componentTwoId;
		this.versions = new ArrayList<I_RelPart>(count);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#addVersion(org.dwfa.vodb.types.I_RelPart)
	 */
	public boolean addVersion(I_RelPart rel) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(rel);
		} else if ((index >= 0) && (versions.get(index).hasNewData(rel))) {
			return versions.add(rel);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#addVersionNoRedundancyCheck(org.dwfa.vodb.types.ThinRelPart)
	 */
	public boolean addVersionNoRedundancyCheck(I_RelPart rel) {
		return versions.add(rel);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getVersions()
	 */
	public List<I_RelPart> getVersions() {
		return versions;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#versionCount()
	 */
	public int versionCount() {
		return versions.size();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#addRetiredRec(int[], int)
	 */
	public boolean addRetiredRec(int[] releases, int retiredStatusId) {
		I_RelPart lastRelVersion = versions.get(versions.size() - 1);
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

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#removeRedundantRecs()
	 */
	public boolean removeRedundantRecs() {
		I_RelVersioned compact = new ThinRelVersioned(relId, componentOneId,
				componentTwoId, versions.size());
		for (I_RelPart v : versions) {
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
		for (I_RelPart rel : versions) {
			buff.append("     ");
			buff.append(rel.toString());
			buff.append("\n");
		}

		return buff.toString();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getC1Id()
	 */
	public int getC1Id() {
		return componentOneId;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getC2Id()
	 */
	public int getC2Id() {
		return componentTwoId;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getRelId()
	 */
	public int getRelId() {
		return relId;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getTuples()
	 */
	public List<I_RelTuple> getTuples() {
		List<I_RelTuple> tuples = new ArrayList<I_RelTuple>();
		for (I_RelPart p : versions) {
			tuples.add(new ThinRelTuple(this, p));
		}
		return tuples;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getFirstTuple()
	 */
	public I_RelTuple getFirstTuple() {
		return new ThinRelTuple(this, versions.get(0));
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getLastTuple()
	 */
	public I_RelTuple getLastTuple() {
		return new ThinRelTuple(this, versions.get(versions.size() - 1));
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#addTuples(org.dwfa.ace.IntSet, org.dwfa.ace.IntSet, java.util.Set, java.util.List, boolean)
	 */
	public void addTuples(IntSet allowedStatus, IntSet allowedTypes,
			Set<Position> positions, List<I_RelTuple> returnRels, boolean addUncommitted) {
		Set<I_RelPart> uncommittedParts = new HashSet<I_RelPart>();
		if (positions == null) {
			List<I_RelPart> addedParts = new ArrayList<I_RelPart>();
			Set<I_RelPart> rejectedParts = new HashSet<I_RelPart>();
			for (I_RelPart part : versions) {
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
			for (I_RelPart part : addedParts) {
				boolean addPart = true;
				for (I_RelPart reject : rejectedParts) {
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

			Set<I_RelPart> addedParts = new HashSet<I_RelPart>();
			for (Position position : positions) {
				Set<I_RelPart> rejectedParts = new HashSet<I_RelPart>();
				ThinRelTuple possible = null;
				for (I_RelPart part : versions) {
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
							
							if (rejectedStatusPosition.path != null && 
									rejectedStatusPosition.isSubsequentOrEqualTo(possibleStatusPosition) && 
									position.isSubsequentOrEqualTo(rejectedStatusPosition)) {
								possible = null;
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
					for (I_RelPart reject : rejectedParts) {
						Position rejectedStatusPosition = new Position(reject
								.getVersion(), position.getPath()
								.getMatchingPath(reject.getPathId()));
						if (rejectedStatusPosition.path != null && 
								rejectedStatusPosition.isSubsequentOrEqualTo(possibleStatusPosition) && 
								position.isSubsequentOrEqualTo(rejectedStatusPosition)) {
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
			for (I_RelPart p: uncommittedParts) {
				returnRels.add(new ThinRelTuple(this, p));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		componentOneId = jarToDbNativeMap.get(componentOneId);
		componentTwoId = jarToDbNativeMap.get(componentTwoId);
		relId = jarToDbNativeMap.get(relId);
		for (I_RelPart p : versions) {
			p.convertIds(jarToDbNativeMap);
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#merge(org.dwfa.vodb.types.ThinRelVersioned)
	 */
	public boolean merge(I_RelVersioned jarRel) {
		HashSet<I_RelPart> versionSet = new HashSet<I_RelPart>(versions);
		boolean changed = false;
		for (I_RelPart jarPart : jarRel.getVersions()) {
			if (!versionSet.contains(jarPart)) {
				changed = true;
				versions.add(jarPart);
			}
		}
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#getTimePathSet()
	 */
	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>();
		for (I_RelPart p : versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_RelVersioned#setC2Id(int)
	 */
	public void setC2Id(int destId) {
		componentTwoId = destId;
		
	}

}
