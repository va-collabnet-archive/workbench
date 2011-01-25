package org.ihtsdo.db.bdb.computer.version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwfa.ace.api.I_AmTypedPart;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.PositionMapper.RELATIVE_POSITION;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class VersionComputer<V extends ConceptComponent<?, ?>.Version> {

    private void handlePart(HashSet<V> partsForPosition, PositionMapper mapper, V part, Precedence precedencePolicy, ContradictionManagerBI contradictionManager, NidSetBI allowedStatus) throws RuntimeException {
        List<V> partsToCompare =
                new ArrayList<V>(partsForPosition);
        for (V prevPartToTest : partsToCompare) {
            switch (mapper.fastRelativePosition(part,
                    prevPartToTest, precedencePolicy)) {
                case AFTER:
                    partsForPosition.remove(prevPartToTest);
                    partsForPosition.add(part);
                    break;
                case BEFORE:
                    break;
                case CONTRADICTION:
                    if (contradictionManager != null
                            && allowedStatus != null) {
                        partsForPosition.remove(prevPartToTest);
                        partsForPosition.addAll(
                                contradictionManager.resolveVersions(
                                part, prevPartToTest));
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
                    errorCount++;
                    if (errorCount < 5) {
                        AceLog.getAppLog().warning(
                                RELATIVE_POSITION.EQUAL
                                + " should never happen. "
                                + "Data is malformed. Part:\n"
                                + part
                                + " \n  Part to test: \n"
                                + prevPartToTest);
                    }
                    break;
                case UNREACHABLE:
                    // Should have failed mapper.onRoute(part)
                    // above.
                    throw new RuntimeException(
                            RELATIVE_POSITION.UNREACHABLE
                            + " should never happen.");
            }
        }
    }

    private class SortVersionsByTimeThenAuthor implements Comparator<V> {

        public int compare(V p1, V p2) {

            if (p1.getTime() < p2.getTime()) {
                return -1;
            }
            if (p1.getTime() == p2.getTime()) {
                if (p1.getPathNid() == p2.getPathNid()) {
                    if (p1.getAuthorNid() == p2.getAuthorNid()) {
                        return p1.getStatusNid() - p2.getStatusNid();
                    }
                    return p1.getAuthorNid() - p2.getAuthorNid();
                } else {
                    if (p1.getPathNid() > p2.getPathNid()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
            return 1;
        }
    }
    private int errorCount = 0;

    public void addSpecifiedVersions(NidSetBI allowedStatus,
            PositionBI viewPosition, List<V> specifiedVersions,
            List<V> versions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionMgr) {
        addSpecifiedVersions(allowedStatus, (NidSetBI) null,
                new PositionSetReadOnly(viewPosition),
                specifiedVersions, versions, precedencePolicy, contradictionMgr);
    }

    public Collection<V> getSpecifiedVersions(NidSetBI allowedStatus,
            PositionBI viewPosition,
            List<? extends V> versions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) {
        List<V> specifiedVersions = new ArrayList<V>();
        addSpecifiedVersions(allowedStatus, (NidSetBI) null,
                new PositionSetReadOnly(viewPosition),
                specifiedVersions, versions, precedencePolicy,
                contradictionManager);
        return specifiedVersions;

    }

    public void addSpecifiedVersions(NidSetBI allowedStatus,
            PositionSetBI positions, List<V> matchingTuples,
            List<V> versions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) {
        addSpecifiedVersions(allowedStatus, null, positions,
                matchingTuples, versions, precedencePolicy, contradictionManager);
    }

    public void addSpecifiedRelVersions(List<V> matchingVersions, List<V> versions, ViewCoordinate c) {
        if (c.getPositionSet() == null || c.getPositionSet().size() < 1) {
            addSpecifiedVersionsNullPositions(c.getAllowedStatusNids(), null,
                    matchingVersions, versions, c.getPrecedence(),
                    c.getContradictionManager(), null);
        } else {
           if (c.getRelAssertionType() == RelAssertionType.INFERRED) {
            addSpecifiedVersionsWithPositions(c.getAllowedStatusNids(), null,
                    c.getPositionSet(), matchingVersions, versions, c.getPrecedence(),
                    c.getContradictionManager(), new AuthorFilter(c.getClassifierNid()));
           } else if (c.getRelAssertionType() == RelAssertionType.STATED) {
            addSpecifiedVersionsWithPositions(c.getAllowedStatusNids(), null,
                    c.getPositionSet(), matchingVersions, versions, c.getPrecedence(),
                    c.getContradictionManager(), new AuthorAntiFilter(c.getClassifierNid()));
           } else if (c.getRelAssertionType() == RelAssertionType.INFERRED_THEN_STATED) {
              List<V> possibleValues = new ArrayList<V>();
               addSpecifiedVersionsWithPositions(c.getAllowedStatusNids(), null,
                    c.getPositionSet(), possibleValues, versions, c.getPrecedence(),
                    c.getContradictionManager(), new AuthorFilter(c.getClassifierNid()));
               if (possibleValues.isEmpty()) {
                  addSpecifiedVersionsWithPositions(c.getAllowedStatusNids(), null,
                    c.getPositionSet(), possibleValues, versions, c.getPrecedence(),
                    c.getContradictionManager(), new AuthorAntiFilter(c.getClassifierNid()));
               }
               matchingVersions.addAll(possibleValues);
           } else {
              throw new RuntimeException("Can't handle: " +
                      c.getRelAssertionType());
           }
         }
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
    public void addSpecifiedVersions(NidSetBI allowedStatus,
            NidSetBI allowedTypes, PositionSetBI positions,
            List<V> specifiedVersions, List<? extends V> versions,
            Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) {
        if (positions == null || positions.size() < 1) {
            addSpecifiedVersionsNullPositions(allowedStatus, allowedTypes,
                    specifiedVersions, versions, precedencePolicy,
                    contradictionManager, null);
        } else {
            addSpecifiedVersionsWithPositions(allowedStatus, allowedTypes,
                    positions, specifiedVersions, versions, precedencePolicy,
                    contradictionManager, null);
        }
    }

    private class AuthorAntiFilter extends AuthorFilter {

        private AuthorAntiFilter(int... nids) {
            super(nids);
        }

      @Override
        public boolean pass(V part) {
            return !super.pass(part);
        }
    }

    private class AuthorFilter {

        NidSetBI authorNids = new NidSet();

        private AuthorFilter(int... nids) {
            for (int nid : nids) {
                authorNids.add(nid);
            }
        }

        public boolean pass(V part) {
            return authorNids.contains(part.getAuthorNid());
        }
    }

    private void addSpecifiedVersionsWithPositions(NidSetBI allowedStatus,
            NidSetBI allowedTypes,
            PositionSetBI positions,
            List<V> specifiedVersions,
            List<? extends V> versions,
            Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, AuthorFilter filter) {
        HashSet<V> partsToAdd = new HashSet<V>();
        for (PositionBI p : positions) {
            HashSet<V> partsForPosition = new HashSet<V>();
            PositionMapper mapper = Bdb.getSapDb().getMapper(p);
            nextpart:
            for (V part : versions) {
                if (part.getTime() == Long.MIN_VALUE) {
                    continue nextpart;
                }
                if (filter != null && filter.pass(part)) {
                     continue nextpart;
                }
                if (allowedTypes != null) {
                    if (allowedTypes.contains(
                            ((I_AmTypedPart) part).getTypeNid()) == false) {
                        if (mapper.onRoute(part)) {
                            handlePart(partsForPosition, mapper, part, precedencePolicy, contradictionManager, allowedStatus);
                            partsForPosition.remove(part);
                        }
                        continue nextpart;
                    }
                }
                if (mapper.onRoute(part)) {
                    if (partsForPosition.isEmpty()) {
                        partsForPosition.add(part);
                    } else {
                        handlePart(partsForPosition, mapper, part, precedencePolicy, contradictionManager, allowedStatus);
                    }
                }
            }
            if (allowedStatus != null) {
                List<V> partsToCompare = new ArrayList<V>(partsForPosition);
                for (V part : partsToCompare) {
                    if (allowedStatus != null) {
                        if (!allowedStatus.contains(part.getStatusNid())) {
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
    private void addSpecifiedVersionsNullPositions(NidSetBI allowedStatus,
            NidSetBI allowedTypes,
            List<V> specifiedVersions,
            List<? extends V> versions,
            Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, AuthorFilter filter) {
        if (versions == null) {
            return;
        }
        HashSet<V> versionsToAdd = new HashSet<V>();
        HashSet<V> rejectedVersions = new HashSet<V>();
        nextpart:
        for (V part : versions) {
            if (part.getTime() == Long.MIN_VALUE || (filter != null && filter.pass(part))) {
                rejectedVersions.add(part);
                continue nextpart;
            }
            if (allowedStatus != null
                    && allowedStatus.contains(part.getStatusNid()) == false) {
                rejectedVersions.add(part);
                continue nextpart;
            }
            if (allowedTypes != null
                    && allowedTypes.contains(
                    ((I_AmTypedPart) part).getTypeNid()) == false) {
                rejectedVersions.add(part);
                continue nextpart;
            }
            versionsToAdd.add(part);
        }
        ArrayList<V> versionToRemove = new ArrayList<V>();
        for (V reject : rejectedVersions) {
            for (V possibleAdd : versionsToAdd) {
                if (reject.getPathNid() == possibleAdd.getPathNid()) {
                    if (reject.getTime() > possibleAdd.getTime()) {
                        versionToRemove.add(possibleAdd);
                    }
                }
            }
        }
        versionsToAdd.removeAll(versionToRemove);

        SortedSet<V> sortedVersionsToAdd =
                new TreeSet<V>(new SortVersionsByTimeThenAuthor());
        sortedVersionsToAdd.addAll(versionsToAdd);
        specifiedVersions.addAll(sortedVersionsToAdd);
    }
}
