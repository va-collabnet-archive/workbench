/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api.contradiction;

import java.io.Serializable;
import java.util.*;
import org.ihtsdo.tk.api.ComponentVersionBI;

// TODO: Auto-generated Javadoc
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
public class LastCommitWinsContradictionResolutionStrategy extends ContradictionManagementStrategy implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ContradictionManagerBI#getDescription()
     */
    @Override
    public String getDescription() {
        return "<html>This resolution strategy implements resolution that"
            + "<ul><li>considers the most recent commit on the configured view paths to be the resolved state of an entity</li>"
            + "<li>resolves/filters the users view to reflect the resolution</li>"
            + "<li>only considers a contradiction to exist if an entity has two or more states on different paths with exactly the same commit time</ul>"
            + "This strategy is useful for single expert authoring where all editors are viewing each other's paths.</html>";
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ContradictionManagerBI#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return "Last commit wins resolution";
    }

    /**
     * Gets the sorted versions copy.
     *
     * @param <T> the generic type
     * @param originalVersions the original versions
     * @return the sorted versions copy
     */
    private <T extends ComponentVersionBI> Collection<List<T>> getSortedVersionsCopy(List<T> originalVersions) {
        Map<Integer, List<T>> map = new HashMap();

        for (T v : originalVersions) {
            List<T> versions;
            if (map.containsKey(v.getNid())) {
                versions = map.get(v.getNid());
            } else {
                versions = new ArrayList();
            }
            versions.add(v);
            map.put(v.getNid(), versions);
        }

        for (List<T> list : map.values()) {
            Collections.sort(list, new VersionDateOrderSortComparator(true));
        }

        return map.values();
    }

    /**
     * Gets the latest versions.
     *
     * @param <T> the generic type
     * @param versions the versions
     * @return the latest versions
     */
    private <T extends ComponentVersionBI> List<T> getLatestVersions(List<T> versions) {
        Collection<List<T>> sortedVersions = getSortedVersionsCopy(versions);

        List<T> returnList = new ArrayList();

        for (List<T> v : sortedVersions) {
            Iterator<T> iterator = v.iterator();
            T first = iterator.next();
            returnList.add(first);
            T version;
            while (iterator.hasNext() && (version = iterator.next()).getTime() == first.getTime()) {
                returnList.add(version);
            }
        }

        return returnList;
    }

   
    /**
     * Gets the sorted parts copy.
     *
     * @param <T> the generic type
     * @param versions the versions
     * @return the sorted parts copy
     */
    private <T extends ComponentVersionBI> List<T> getSortedPartsCopy(List<T> versions) {
        List<T> copy = new ArrayList(versions);
        Collections.sort(copy, new PartDateOrderSortComparator(true));

        return copy;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ContradictionManagerBI#resolveVersions(java.util.List)
     */
    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> tuples) {
        if (tuples == null || tuples.isEmpty()) {
            return tuples;
        }

        return getLatestVersions(tuples);
    }
    
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ContradictionManagerBI#resolveVersions(org.ihtsdo.tk.api.ComponentVersionBI, org.ihtsdo.tk.api.ComponentVersionBI)
     */
    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        ArrayList<T> values = new ArrayList();
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
