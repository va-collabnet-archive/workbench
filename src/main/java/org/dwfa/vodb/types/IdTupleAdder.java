package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwfa.ace.api.I_AmTypedPart;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;

public class IdTupleAdder {
    private class AdmitAndSortParts implements Comparator<I_IdPart> {
        ArrayList<PathSortInfo> pathInfo = new ArrayList<PathSortInfo>();

        public AdmitAndSortParts(ArrayList<PathSortInfo> pathInfo) {
            super();
            this.pathInfo = pathInfo;
        }

        boolean admit(I_IdPart part) {
            for (PathSortInfo psi : pathInfo) {
                if (part.getPathId() == psi.pathId && part.getVersion() <= psi.maxVersion) {
                    return true;
                }
            }
            return false;
        }

        public int compare(I_IdPart p1, I_IdPart p2) {
            Integer p1PathOrder = null;
            Integer p2PathOrder = null;
            if (p1.getPathId() != p2.getPathId()) {
                for (PathSortInfo info : pathInfo) {
                    if (info.pathId == p1.getPathId()) {
                        p1PathOrder = info.order;
                    }
                    if (info.pathId == p2.getPathId()) {
                        p2PathOrder = info.order;
                    }
                    if (p1PathOrder != null && p2PathOrder != null) {
                        break;
                    }
                }
            }
            if (p1PathOrder != p2PathOrder) {
                if (p1PathOrder < p2PathOrder) {
                    return 1;
                }
                if (p1PathOrder > p2PathOrder) {
                    return -1;
                }
            }

            if (p1.getVersion() < p2.getVersion()) {
                return -1;
            }
            if (p1.getVersion() == p2.getVersion()) {
                return 0;
            }
            return 1;
        }

    }

    private class SortPartsByVersion implements Comparator<I_IdPart> {

        public int compare(I_IdPart p1, I_IdPart p2) {

            if (p1.getVersion() < p2.getVersion()) {
                return -1;
            }
            if (p1.getVersion() == p2.getVersion()) {
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

    private static class PathSortInfo {
        int order;
        int maxVersion;
        int pathId;

        public PathSortInfo(int order, int maxVersion, int pathId) {
            super();
            this.order = order;
            this.maxVersion = maxVersion;
            this.pathId = pathId;
        }

    }

    public void addPaths(ArrayList<PathSortInfo> pathInfo, int depth, int maxVersion, I_Path p) {
        pathInfo.add(new PathSortInfo(depth, maxVersion, p.getConceptId()));
        depth++;
        for (I_Position o : p.getOrigins()) {
            addPaths(pathInfo, depth, o.getVersion(), o.getPath());
        }
    }

    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<I_IdVersion> matchingTuples,
            boolean addUncommitted, List<I_IdPart> versions, ThinIdVersioned core) {
        addTuples(allowedStatus, null, positions, matchingTuples, addUncommitted, versions, core);
    }

    /**
     * 
     * @param allowedStatus <code>null</code> is a wildcard.
     * @param allowedTypes <code>null</code> is a wildcard.
     * @param positions <code>null</code> is a wildcard. Positions MUST be
     *            protected from concurrent modification externally.
     *            Synchronization within this call is to expensive for the
     *            environment.
     * @param matchingTuples
     * @param addUncommitted
     * @param versions
     * @param core
     */
    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            List<I_IdVersion> matchingTuples, boolean addUncommitted, List<I_IdPart> versions, ThinIdVersioned core) {
        HashSet<I_IdPart> partsToAdd = new HashSet<I_IdPart>();
        HashSet<I_IdPart> uncommittedParts = new HashSet<I_IdPart>();
        if (positions == null) {
            HashSet<I_IdPart> rejectedParts = new HashSet<I_IdPart>();
            for (I_IdPart part : versions) {
                if (addUncommitted && part.getVersion() == Integer.MAX_VALUE) {
                    uncommittedParts.add(part);
                    continue;
                } else if (part.getVersion() == Integer.MAX_VALUE) {
                    continue;
                }
                if (allowedStatus != null && allowedStatus.contains(part.getStatusId()) == false) {
                    rejectedParts.add(part);
                    continue;
                }
                if (allowedTypes != null && allowedTypes.contains(((I_AmTypedPart) part).getTypeId()) == false) {
                    rejectedParts.add(part);
                    continue;
                }
                partsToAdd.add(part);
            }
            ArrayList<I_IdPart> partsToRemove = new ArrayList<I_IdPart>();
            for (I_IdPart reject : rejectedParts) {
                for (I_IdPart possibleAdd : partsToAdd) {
                    if (reject.getPathId() == possibleAdd.getPathId()) {
                        if (reject.getVersion() > possibleAdd.getVersion()) {
                            partsToRemove.add(possibleAdd);
                        }
                    }
                }
            }
            partsToAdd.removeAll(partsToRemove);
        } else {
            HashSet<I_Position> positionCopy;
            synchronized (positions) {
                positionCopy = new HashSet<I_Position>(positions);
            }
            for (I_Position p : positionCopy) {
                ArrayList<PathSortInfo> pathInfo = new ArrayList<PathSortInfo>();
                if (p.getVersion() == Integer.MAX_VALUE) {
                    addPaths(pathInfo, 0, Integer.MAX_VALUE - 1, p.getPath());
                } else {
                    addPaths(pathInfo, 0, p.getVersion(), p.getPath());
                }
                AdmitAndSortParts partSorter = new AdmitAndSortParts(pathInfo);
                SortedSet<I_IdPart> partSet = new TreeSet<I_IdPart>(partSorter);
                for (I_IdPart part : versions) {
                    if (part.getVersion() == Integer.MAX_VALUE) {
                        uncommittedParts.add(part);
                    } else if (partSorter.admit(part)) {
                        partSet.add(part);
                    }
                }
                if (partSet.size() > 0) {
                    I_IdPart lastPart = partSet.last();
                    if (allowedStatus != null) {
                        if (allowedStatus.contains(lastPart.getStatusId()) == false) {
                            continue;
                        }
                    }
                    if (allowedTypes != null) {
                        if (allowedTypes.contains(((I_AmTypedPart) lastPart).getTypeId()) == false) {
                            continue;
                        }
                    }
                    partsToAdd.add(lastPart);
                }
            }
        }
        SortedSet<I_IdPart> sortedPartsToAdd = new TreeSet<I_IdPart>(new SortPartsByVersion());
        sortedPartsToAdd.addAll(partsToAdd);
        for (I_IdPart part : sortedPartsToAdd) {
            matchingTuples.add(makeTuple(part, core));
        }
        for (I_IdPart part : uncommittedParts) {
            boolean add = true;

            if (allowedTypes != null && allowedTypes.contains(((I_AmTypedPart) part).getTypeId()) == false) {
                add = false;
            }
            /*
             * adding allowed status has some unanticipated effect in the GUI
             * (not showing newly retired concepts prior to commit).
             * TODO expand API to allow the uncommitted to filter on status or
             * not, and on type or not...
             * if (allowedStatus != null
             * && allowedStatus.contains(part.getStatusId()) == false) {
             * add = false;
             * continue;
             * }
             */
            if (add) {
                matchingTuples.add(makeTuple(part, core));

            }
        }
    }

    public ThinIdTuple makeTuple(I_IdPart part, I_Identify core) {
        return new ThinIdTuple(core, part);
    }

}