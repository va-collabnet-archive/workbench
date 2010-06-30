package org.ihtsdo.db.bdb.computer.version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwfa.ace.api.I_AmTypedPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.ConceptComponent.Version;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.PositionMapper.RELATIVE_POSITION;

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

    public void addSpecifiedVersions(I_IntSet allowedStatus, I_Position viewPosition, List<V> specifiedVersions,
            List<V> versions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) {
        addSpecifiedVersions(allowedStatus, (I_IntSet) null, new PositionSetReadOnly(viewPosition),
            specifiedVersions, versions, precedencePolicy, contradictionManager);
     }

    public Collection<V> getSpecifiedVersions(I_IntSet allowedStatus, I_Position viewPosition,
            List<? extends V> versions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) {
        List<V> specifiedVersions = new ArrayList<V>();
        addSpecifiedVersions(allowedStatus, (I_IntSet) null, new PositionSetReadOnly(viewPosition),
            specifiedVersions, versions, precedencePolicy, contradictionManager);
        return specifiedVersions;

    }

    public void addSpecifiedVersions(I_IntSet allowedStatus, PositionSetReadOnly positions, List<V> matchingTuples,
            List<V> versions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) {
        addSpecifiedVersions(allowedStatus, null, positions, matchingTuples, versions, precedencePolicy, contradictionManager);
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
    public void addSpecifiedVersions(I_IntSet allowedStatus, I_IntSet allowedTypes, PositionSetReadOnly positions,
            List<V> specifiedVersions, List<? extends V> versions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) {
        if (positions == null || positions.size() < 1) {
            addSpecifiedVersionsNullPositions(allowedStatus, allowedTypes, specifiedVersions, versions, precedencePolicy, contradictionManager);
        } else {
            addSpecifiedVersionsWithPositions(allowedStatus, allowedTypes, positions, specifiedVersions, versions, precedencePolicy, contradictionManager);
        }
    }

    @SuppressWarnings("unchecked")
    private void addSpecifiedVersionsWithPositions(I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positions, List<V> specifiedVersions, List<? extends V> versions, 
            PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) {
        HashSet<V> partsToAdd = new HashSet<V>();
        for (I_Position p : positions) {
            HashSet<V> partsForPosition = new HashSet<V>();
            PositionMapper mapper = Bdb.getSapDb().getMapper(p);
            nextpart: for (V part : versions) {
               if (part.getTime() == Long.MIN_VALUE) {
                    continue nextpart;
                }
                if (allowedTypes != null) {
                    if (allowedTypes.contains(((I_AmTypedPart) part).getTypeId()) == false) {
                        continue nextpart;
                    }
                }
                if (mapper.onRoute(part)) {
                    if (partsForPosition.size() == 0) {
                        partsForPosition.add(part);
                    } else {
                        List<V> partsToCompare = new ArrayList<V>(partsForPosition);
                        for (V prevPartToTest : partsToCompare) {
                            switch (mapper.fastRelativePosition(part, prevPartToTest, precedencePolicy)) {
                            case AFTER:
                                partsForPosition.remove(prevPartToTest);
                                partsForPosition.add(part);
                                break;
                            case BEFORE:
                                break;
                            case CONTRADICTION:
                                if (contradictionManager != null && allowedStatus != null) {
                                    partsForPosition.remove(prevPartToTest);
                                    partsForPosition.addAll(contradictionManager.resolveParts(part, prevPartToTest));
                                } else {
                                    partsForPosition.add(part);
                                    partsForPosition.add(prevPartToTest);
                                }
                                break;
                            case EQUAL:
                                // Can only have one part per time/path
                                // combination.
                                if (prevPartToTest.equals(part)) {
                                    // part already added from another position.
                                    // No need to add again.
                                    break;
                                }
                                // Duplicate values encountered.
                                AceLog.getAppLog().warning(
                                    RELATIVE_POSITION.EQUAL + " should never happen. Data is malformed. Part:\n" + part
                                        + " \n  Part to test: \n" + prevPartToTest);
                                try {
                                	throw new Exception("Equal!!!");

                                }
                                catch(Exception Ex) {
                                	Ex.printStackTrace();
                                }
                                partsForPosition.remove(part);
                                partsForPosition.remove(prevPartToTest);
                                Version dup = part.removeDuplicates(part, prevPartToTest);
                                partsForPosition.remove(dup);
                                break;
                            case UNREACHABLE:
                                // Should have failed mapper.onRoute(part)
                                // above.
                                throw new RuntimeException(RELATIVE_POSITION.UNREACHABLE + " should never happen.");
                            }
                        }
                    }
                }
            }
            if (allowedStatus != null) {
                List<V> partsToCompare = new ArrayList<V>(partsForPosition);
                for (V part : partsToCompare) {
                    if (allowedStatus != null) {
                        if (!allowedStatus.contains(part.getStatusId())) {
                            partsForPosition.remove(part);
                        }
                    }
                }
            }
            if (partsForPosition.size() > 0) {
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
    private void addSpecifiedVersionsNullPositions(I_IntSet allowedStatus, I_IntSet allowedTypes,
            List<V> specifiedVersions, List<? extends V> versions, 
            PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) {
        if (versions == null) {
            return;
        }
        HashSet<V> versionsToAdd = new HashSet<V>();
        HashSet<V> rejectedVersions = new HashSet<V>();
        nextpart: for (V part : versions) {
            if (part.getTime() == Long.MIN_VALUE) {
                rejectedVersions.add(part);
                continue nextpart;
            }
            if (allowedStatus != null && allowedStatus.contains(part.getStatusId()) == false) {
                rejectedVersions.add(part);
                continue nextpart;
            }
            if (allowedTypes != null && allowedTypes.contains(((I_AmTypedPart) part).getTypeId()) == false) {
                rejectedVersions.add(part);
                continue nextpart;
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
    }

}
