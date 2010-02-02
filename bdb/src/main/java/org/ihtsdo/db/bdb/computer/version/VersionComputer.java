package org.ihtsdo.db.bdb.computer.version;

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
import org.ihtsdo.db.bdb.computer.version.PositionMapper.RELATIVE_POSITION;
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

	public void addSpecifiedVersions(I_IntSet allowedStatus, I_Position viewPosition,
			List<V> specifiedVersions, List<V> versions) {

		HashSet<V> partsToAdd = new HashSet<V>();
		List<V> partsForPosition = new LinkedList<V>();
		PositionMapper mapper = Bdb.getSapDb().getMapper(
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

		specifiedVersions.addAll(partsToAdd);
	}

	public void addSpecifiedVersions(I_IntSet allowedStatus,
			PositionSetReadOnly positions, List<V> matchingTuples,
			boolean addUncommitted, List<V> versions) {
		addSpecifiedVersions(allowedStatus, null, positions, matchingTuples,
				addUncommitted, versions);
	}

	/**
	 * 
	 * @param allowedStatus
	 *            <code>null</code> is a wildcard.
	 * @param allowedTypes
	 *            <code>null</code> is a wildcard.
	 * @param positions
	 *            <code>null</code> is a wildcard. 
	 * @param specifiedVersions
	 * @param addUncommitted
	 * @param versions
	 * @param core
	 */
	public void addSpecifiedVersions(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<V> specifiedVersions,
			boolean addUncommitted, List<V> versions) {
		if (positions == null) {
			addSpecifiedVersionsNullPositions(allowedStatus, allowedTypes, specifiedVersions,
					addUncommitted, versions);
		} else {
			addSpecifiedVersionsWithPositions(allowedStatus, allowedTypes, positions,
					specifiedVersions, versions);
		}
	}

	private void addSpecifiedVersionsWithPositions(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			List<V> specifiedVersions, List<V> versions) {
		HashSet<V> partsToAdd = new HashSet<V>();
		List<V> partsForPosition = new LinkedList<V>();
		for (I_Position p : positions) {
			PositionMapper mapper = Bdb.getSapDb().getMapper(p);
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
								if (partToTest.equals(part)) {
									// part already added from another position. 
									// No need to add again. 
									break;
								} else {
									throw new RuntimeException(
											RELATIVE_POSITION.EQUAL
											+ " should never happen. Data is malformed. Part:\n" + 
											part + " \n  Part to test: \n" + partToTest);
								}
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
		specifiedVersions.addAll(partsToAdd);
	}

	/**
	 * 
	 * @param allowedStatus
	 * @param allowedTypes
	 * @param specifiedVersions
	 * @param addUncommitted
	 * @param versions
	 * @param core
	 */
	private void addSpecifiedVersionsNullPositions(I_IntSet allowedStatus,
			I_IntSet allowedTypes, List<V> specifiedVersions,
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
		specifiedVersions.addAll(sortedVersionsToAdd);
		for (V version : uncommittedVersions) {
			if (allowedTypes == null
					|| allowedTypes
							.contains(((I_AmTypedPart) version).getTypeId()) == true) {
				specifiedVersions.add(version);
			}
		}
	}

}
