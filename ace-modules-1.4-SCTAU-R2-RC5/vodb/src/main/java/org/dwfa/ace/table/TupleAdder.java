/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTypedPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;

public abstract class TupleAdder<V, W> {
    private class AdmitAndSortParts implements Comparator<I_AmPart> {
        ArrayList<PathSortInfo> pathInfo = new ArrayList<PathSortInfo>();

        public AdmitAndSortParts(ArrayList<PathSortInfo> pathInfo) {
            super();
            this.pathInfo = pathInfo;
        }

        boolean admit(I_AmPart part) {
            for (PathSortInfo psi : pathInfo) {
                if (part.getPathId() == psi.pathId && part.getVersion() <= psi.maxVersion) {
                    return true;
                }
            }
            return false;
        }

        public int compare(I_AmPart p1, I_AmPart p2) {
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

    private class SortPartsByVersion implements Comparator<I_AmPart> {

        public int compare(I_AmPart p1, I_AmPart p2) {

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

    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<V> matchingTuples,
            boolean addUncommitted, List<? extends I_AmPart> versions, W core) {
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
            List<V> matchingTuples, boolean addUncommitted, List<? extends I_AmPart> versions, W core) {
        HashSet<I_AmPart> partsToAdd = new HashSet<I_AmPart>();
        HashSet<I_AmPart> uncommittedParts = new HashSet<I_AmPart>();
        if (positions == null) {
            HashSet<I_AmPart> rejectedParts = new HashSet<I_AmPart>();
            for (I_AmPart part : versions) {
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
            ArrayList<I_AmPart> partsToRemove = new ArrayList<I_AmPart>();
            for (I_AmPart reject : rejectedParts) {
                for (I_AmPart possibleAdd : partsToAdd) {
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
                SortedSet<I_AmPart> partSet = new TreeSet<I_AmPart>(partSorter);
                for (I_AmPart part : versions) {
                    if (part.getVersion() == Integer.MAX_VALUE) {
                        uncommittedParts.add(part);
                    } else if (partSorter.admit(part)) {
                        partSet.add(part);
                    }
                }
                if (partSet.size() > 0) {
                    I_AmPart lastPart = partSet.last();
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
        SortedSet<I_AmPart> sortedPartsToAdd = new TreeSet<I_AmPart>(new SortPartsByVersion());
        sortedPartsToAdd.addAll(partsToAdd);
        for (I_AmPart part : sortedPartsToAdd) {
            matchingTuples.add(makeTuple(part, core));
        }
        for (I_AmPart part : uncommittedParts) {
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

    public abstract V makeTuple(I_AmPart part, W core);

}
