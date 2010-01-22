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
import org.ihtsdo.db.bdb.concept.component.Revision;

public abstract class VersionComputer<C extends ConceptComponent<P, C>, 
									P extends Revision<P, C>> {

	private class SortPartsByTime implements Comparator<P> {

		public int compare(P p1, P p2) {

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
			List<P> matchingTuples, List<P> versions, C core) {

		HashSet<P> partsToAdd = new HashSet<P>();
		List<P> partsForPosition = new LinkedList<P>();
		PositionMapper mapper = Bdb.getStatusAtPositionDb().getMapper(
				viewPosition);
		for (P part : versions) {
			if (mapper.onRoute(part)) {
				if (partsForPosition.size() == 0) {
					partsForPosition.add(part);
				} else {
					ListIterator<P> latestIterator = partsForPosition
							.listIterator();
					boolean added = false;
					while (latestIterator.hasNext()) {
						P partToTest = latestIterator.next();
						switch (mapper.fastRelativePosition(part, partToTest)) {
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
		for (P part : partsForPosition) {
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
			PositionSetReadOnly positions, List<P> matchingTuples,
			boolean addUncommitted, List<P> versions, C core) {
		addTuples(allowedStatus, null, positions, matchingTuples,
				addUncommitted, versions, core);
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
			PositionSetReadOnly positions, List<P> matchingTuples,
			boolean addUncommitted, List<P> versions, C core) {
		if (positions == null) {
			addTuplesNullPositions(allowedStatus, allowedTypes, matchingTuples,
					addUncommitted, versions, core);
		} else {
			addTuplesWithPositions(allowedStatus, allowedTypes, positions,
					matchingTuples, versions, core);
		}
	}

	public void addTuplesWithPositions(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			List<P> matchingTuples, List<P> versions, C core) {
		HashSet<P> partsToAdd = new HashSet<P>();
		List<P> partsForPosition = new LinkedList<P>();
		for (I_Position p : positions) {
			PositionMapper mapper = Bdb.getStatusAtPositionDb().getMapper(p);
			for (P part : versions) {
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
						ListIterator<P> latestIterator = partsForPosition
								.listIterator();
						boolean added = false;
						while (latestIterator.hasNext()) {
							P partToTest = latestIterator.next();
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
												+ " should never happen. Data is malformed.");
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
			for (P part : partsForPosition) {
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
			I_IntSet allowedTypes, List<P> matchingTuples,
			boolean addUncommitted, List<P> versions, C core) {
		HashSet<P> partsToAdd = new HashSet<P>();
		HashSet<P> uncommittedParts = new HashSet<P>();
		HashSet<P> rejectedParts = new HashSet<P>();
		for (P part : versions) {
			if (part.getTime() == Long.MAX_VALUE) {
				if (addUncommitted) {
					uncommittedParts.add(part);
				}
				continue;
			}
			if (allowedStatus != null
					&& allowedStatus.contains(part.getStatusId()) == false) {
				rejectedParts.add(part);
				continue;
			}
			if (allowedTypes != null
					&& allowedTypes
							.contains(((I_AmTypedPart) part).getTypeId()) == false) {
				rejectedParts.add(part);
				continue;
			}
			partsToAdd.add(part);
		}
		ArrayList<P> partsToRemove = new ArrayList<P>();
		for (P reject : rejectedParts) {
			for (P possibleAdd : partsToAdd) {
				if (reject.getPathId() == possibleAdd.getPathId()) {
					if (reject.getVersion() > possibleAdd.getVersion()) {
						partsToRemove.add(possibleAdd);
					}
				}
			}
		}
		partsToAdd.removeAll(partsToRemove);

		SortedSet<P> sortedPartsToAdd = new TreeSet<P>(new SortPartsByTime());
		sortedPartsToAdd.addAll(partsToAdd);
		matchingTuples.addAll(sortedPartsToAdd);
		for (P part : uncommittedParts) {
			if (allowedTypes == null
					|| allowedTypes
							.contains(((I_AmTypedPart) part).getTypeId()) == true) {
				matchingTuples.add(part);
			}
		}
	}

}
