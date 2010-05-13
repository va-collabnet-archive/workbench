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
/**
 * 
 */
package org.dwfa.vodb.conflict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;

/**
 * "Last commit wins" implementation of a conflict resolution strategy.
 * Specifically this resolution
 * strategy will resolve conflict by choosing the part or tuple of an entity
 * with the latest
 * commit time regardless of path.
 * 
 * "In conflict" is rare for this strategy. As the latest part of any given
 * entity
 * is chosen, and the differences between latest state on different paths is of
 * little importance the only
 * way to achieve a conflict is for two parts to have the same effective time.
 * This is rare, but
 * if it occurs will be considered a conflict.
 * 
 * When in conflict the "resolve" methods will arbitrarily return one of the
 * parts with the most
 * recent commit time.
 * 
 * @author Dion
 */
public class LastCommitWinsConflictResolutionStrategy extends ContradictionManagementStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    public String getDescription() {
        return "<html>This resolution strategy implements resolution that"
            + "<ul><li>considers the most recent commit on the configured view paths to be the resolved state of an entity</li>"
            + "<li>resolves/filters the users view to reflect the resolution</li>"
            + "<li>only considers a contradiction to exist if an entity has two or more states on different paths with exactly the same commit time</ul>"
            + "This strategy is useful for single expert authoring where all editors are viewing each other's paths.</html>";
    }

    public String getDisplayName() {
        return "Last commit wins resolution";
    }

    private <T extends I_AmTuple> Collection<List<T>> getSortedTuplesCopy(List<T> originalTuples) {
        Map<Integer, List<T>> map = new HashMap<Integer, List<T>>();

        for (T t : originalTuples) {
            I_AmTermComponent fixedPart = t.getFixedPart();
            List<T> tuples;
            if (map.containsKey(fixedPart.getNid())) {
                tuples = map.get(fixedPart.getNid());
            } else {
                tuples = new ArrayList<T>();
            }
            tuples.add(t);
            map.put(fixedPart.getNid(), tuples);
        }

        for (List<T> list : map.values()) {
            Collections.sort(list, new PartDateOrderSortComparator(true));
        }

        return map.values();
    }

    private <T extends I_AmTuple> List<T> getLatestTuples(List<T> tuples) {
        Collection<List<T>> sortedTuples = getSortedTuplesCopy(tuples);

        List<T> returnList = new ArrayList<T>();

        for (List<T> t : sortedTuples) {
            Iterator<T> iterator = t.iterator();
            T first = iterator.next();
            returnList.add(first);
            T tuple;
            while (iterator.hasNext() && (tuple = iterator.next()).getVersion() == first.getVersion()) {
                returnList.add(tuple);
            }
        }

        return returnList;
    }

    /**
     * Determines if a conflict exists in the list of versions passed to this
     * method. A conflict is determined to exist if more than one part has the
     * same version (commit time) yet differs in data (other than pathid) and
     * true will be returned. If the part/s with the most recent version (commit
     * time) have the same data aside from their path (or there is only one part
     * with the greatest version) then false will be returned.
     * 
     * @param <T>
     *            type of data in the list - extension of I_AmPart
     * @param versions
     *            list of parts to check for conflict
     * @return true if a conflict exists, false otherwise
     */
    protected <T extends I_AmPart> boolean doesConflictExist(List<T> versions) {
        if (versions.size() < 2) {
            return false;
        }

        List<T> copy = getSortedPartsCopy(versions);

        Iterator<T> copyIterator = copy.iterator();
        I_AmPart firstPart = copyIterator.next();

        I_AmPart firstPartDuplicate = firstPart.makeAnalog(firstPart.getStatusId(), 0, Long.MAX_VALUE);
 

        while (copyIterator.hasNext()) {
            T amPart = (T) copyIterator.next();
            if (amPart.getVersion() == firstPart.getVersion()) {
                I_AmPart amPartDuplicate = amPart.makeAnalog(amPart.getStatusId(), 0, Long.MAX_VALUE);


                if (amPartDuplicate.equals(firstPartDuplicate)) {
                    continue; // identical, no conflict with this one, check for
                    // more
                } else {
                    return true; // version the same but different data -
                    // conflict
                }
            } else {
                return false; // different version, no conflict
            }
        }

        // this means that all parts had the same version, but also the same
        // data
        // no conflict
        return false;
    }

    private <T extends I_AmPart> List<T> getSortedPartsCopy(List<T> versions) {
        List<T> copy = new ArrayList<T>(versions);
        Collections.sort(copy, new PartDateOrderSortComparator(true));

        return copy;
    }

    public <T extends I_AmTuple> List<T> resolveTuples(List<T> tuples) {
        if (tuples == null || tuples.size() == 0) {
            return tuples;
        }

        return getLatestTuples(tuples);
    }

    public <T extends I_AmPart> List<T> resolveParts(List<T> parts) {
        if (parts == null || parts.size() == 0) {
            return parts;
        }

        List<T> returnList = new ArrayList<T>();
        Iterator<T> iterator = getSortedPartsCopy(parts).iterator();
        T first = iterator.next();
        returnList.add(first);
        T part;
        while (iterator.hasNext() && (part = iterator.next()).getVersion() == first.getVersion()) {
            returnList.add(part);
        }

        return returnList;
    }
    
    @Override
    public <T extends I_AmPart> List<T> resolveParts(T part1, T part2) {
        ArrayList<T> values = new ArrayList<T>();
        if (part1.getTime() > part2.getTime()) {
            values.add(part1);
        } else if (part1.getTime() < part2.getTime()) {
            values.add(part2);
        } else {
            values.add(part1);
            values.add(part2);
        }
        return values;
    }

}
