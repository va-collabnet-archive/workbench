package org.ihtsdo.db.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwfa.ace.api.I_AmTypedPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.PositionMapper;
import org.ihtsdo.db.bdb.PositionMapper.RELATIVE_POSITION;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;

public class VersionComputer<V extends ConceptComponent<?, ?>.Version> {

	private class SortVersionsByTime implements Comparator<V> {

		public int compare(V p1, V p2) {

			if (p1.getTime() < p2.getTime()) {
				return -1;
			}
			if (p1.getTime() == p2.getTime()) {
				if (p1.getPathId() == p2.getPathId()) {
					return 0;
				} else {
					if (p1.getPathId() > p2.getPathId()) {
						return -1;
					} else {
						return 1;
					}
				}
			}
			return 1;
		}

	}

	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<V> matchingTuples, List<V> versions) {

		HashSet<V> partsToAdd = new HashSet<V>();
		List<V> partsForPosition = new LinkedList<V>();
		PositionMapper mapper = Bdb.getStatusAtPositionDb().getMapper(
				viewPosition);
		for (V version : versions) {
			if (mapper.onRoute(version)) {
				if (partsForPosition.size() == 0) {
					partsForPosition.add(version);
				} else {
					ListIterator<V> latestIterator = partsForPosition
							.listIterator();
					boolean added = false;
					while (latestIterator.hasNext()) {
						V partToTest = latestIterator.next();
						switch (mapper.fastRelativePosition(version, partToTest)) {
						case AFTER:
							if (added) {
								latestIterator.remove();
							} else {
								latestIterator.set(partToTest);
							}
							break;
						case BEFORE:
							break;
						case CONFLICTING:
							if (added == false) {
								latestIterator.add(partToTest);
								added = true;
							}
							break;
						case EQUAL:
							// Can only have one part per time/path combination.
							throw new RuntimeException(
									RELATIVE_POSITION.EQUAL
											+ " should never happen. Data is malformed.");
						case UNREACHABLE:
							if (added == false) {
								latestIterator.add(partToTest);
								added = true;
							}
						}
					}
				}
			}
		}
		boolean addParts = false;
		for (V part : partsForPosition) {
			if (allowedStatus != null) {
				if (allowedStatus.contains(part.getStatusId())) {
					addParts = true;
				}
			}
		}
		if (addParts) {
			partsToAdd.addAll(partsForPosition);
		}

		matchingTuples.addAll(partsToAdd);
	}

	public void addTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions, List<V> matchingTuples,
			boolean addUncommitted, List<V> versions) {
		addTuples(allowedStatus, null, positions, matchingTuples,
				addUncommitted, versions);
	}

	/**
	 * 
	 * @param allowedStatus
	 *            <code>null</code> is a wildcard.
	 * @param allowedTypes
	 *            <code>null</code> is a wildcard.
	 * @param positions
	 *            <code>null</code> is a wildcard. Positions MUST be protected
	 *            from concurrent modification externally. Synchronization
	 *            within this call is to expensive for the environment.
	 * @param matchingTuples
	 * @param addUncommitted
	 * @param versions
	 * @param core
	 */
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<V> matchingTuples,
			boolean addUncommitted, List<V> versions) {
		if (positions == null) {
			addTuplesNullPositions(allowedStatus, allowedTypes, matchingTuples,
					addUncommitted, versions);
		} else {
			addTuplesWithPositions(allowedStatus, allowedTypes, positions,
					matchingTuples, versions);
		}
	}

	public void addTuplesWithPositions(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			List<V> matchingTuples, List<V> versions) {
		HashSet<V> partsToAdd = new HashSet<V>();
		List<V> partsForPosition = new LinkedList<V>();
		for (I_Position p : positions) {
			PositionMapper mapper = Bdb.getStatusAtPositionDb().getMapper(p);
			for (V part : versions) {
				if (allowedTypes != null) {
					if (allowedTypes.contains(((I_AmTypedPart) part)
							.getTypeId()) == false) {
						continue;
					}
				}
				if (mapper.onRoute(part)) {
					if (partsForPosition.size() == 0) {
						partsForPosition.add(part);
					} else {
						ListIterator<V> latestIterator = partsForPosition
								.listIterator();
						boolean added = false;
						while (latestIterator.hasNext()) {
							V partToTest = latestIterator.next();
							switch (mapper.fastRelativePosition(part,
									partToTest)) {
							case AFTER:
								if (added) {
									latestIterator.remove();
								} else {
									latestIterator.set(partToTest);
								}
								break;
							case BEFORE:
								break;
							case CONFLICTING:
								if (added == false) {
									latestIterator.add(partToTest);
									added = true;
								}
								break;
							case EQUAL:
								// Can only have one part per time/path
								// combination.
								throw new RuntimeException(
										RELATIVE_POSITION.EQUAL
										+ " should never happen. Data is malformed. Part:\n" + 
										part + " \n  Part to test: \n" + partToTest);
							case UNREACHABLE:
								// Should have failed mapper.onRoute(part)
								// above.
								throw new RuntimeException(
										RELATIVE_POSITION.UNREACHABLE
												+ " should never happen.");
							}
						}
					}
				}
			}
			boolean addParts = false;
			for (V part : partsForPosition) {
				if (allowedStatus != null) {
					if (allowedStatus.contains(part.getStatusId())) {
						addParts = true;
					}
				}
			}
			if (addParts) {
				partsToAdd.addAll(partsForPosition);
			}
		}
		matchingTuples.addAll(partsToAdd);
	}

	/**
	 * 
	 * @param allowedStatus
	 * @param allowedTypes
	 * @param matchingTuples
	 * @param addUncommitted
	 * @param versions
	 * @param core
	 */
	public void addTuplesNullPositions(I_IntSet allowedStatus,
			I_IntSet allowedTypes, List<V> matchingTuples,
			boolean addUncommitted, List<V> versions) {
		if (versions == null) {
			return;
		}
		HashSet<V> versionsToAdd = new HashSet<V>();
		HashSet<V> uncommittedVersions = new HashSet<V>();
		HashSet<V> rejectedVersions = new HashSet<V>();
		for (V part : versions) {
			if (part.getTime() == Long.MAX_VALUE) {
				if (addUncommitted) {
					uncommittedVersions.add(part);
				}
				continue;
			}
			if (allowedStatus != null
					&& allowedStatus.contains(part.getStatusId()) == false) {
				rejectedVersions.add(part);
				continue;
			}
			if (allowedTypes != null
					&& allowedTypes
							.contains(((I_AmTypedPart) part).getTypeId()) == false) {
				rejectedVersions.add(part);
				continue;
			}
			versionsToAdd.add(part);
		}
		ArrayList<V> versionToRemove = new ArrayList<V>();
		for (V reject : rejectedVersions) {
			for (V possibleAdd : versionsToAdd) {
				if (reject.getPathId() == possibleAdd.getPathId()) {
					if (reject.getVersion() > possibleAdd.getVersion()) {
						versionToRemove.add(possibleAdd);
					}
				}
			}
		}
		versionsToAdd.removeAll(versionToRemove);

		SortedSet<V> sortedVersionsToAdd = new TreeSet<V>(new SortVersionsByTime());
		sortedVersionsToAdd.addAll(versionsToAdd);
		matchingTuples.addAll(sortedVersionsToAdd);
		for (V version : uncommittedVersions) {
			if (allowedTypes == null
					|| allowedTypes
							.contains(((I_AmTypedPart) version).getTypeId()) == true) {
				matchingTuples.add(version);
			}
		}
	}

}
