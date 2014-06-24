/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 *
 */
package org.ihtsdo.tk.api.contradiction;

import java.io.Serializable;
import java.util.*;
import org.ihtsdo.tk.api.ComponentVersionBI;

/**
 * "Last commit wins" implementation of a conflict resolution strategy.
 * Specifically this resolution strategy will resolve conflict by choosing the
 * part or tuple of an entity with the latest commit time regardless of path.
 *
 * "In conflict" is rare for this strategy. As the latest part of any given
 * entity is chosen, and the differences between latest state on different paths
 * is of little importance the only way to achieve a conflict is for two parts
 * to have the same effective time. This is rare, but if it occurs will be
 * considered a conflict.
 *
 * When in conflict the "resolve" methods will arbitrarily return one of the
 * parts with the most recent commit time.
 *
 */
public class LastCommitWinsContradictionResolutionStrategy extends ContradictionManagementStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @return a description of this last commit wins contradiction strategy
     */
    @Override
    public String getDescription() {
        return "<html>This resolution strategy implements resolution that"
                + "<ul><li>considers the most recent commit on the configured view paths to be the resolved state of an entity</li>"
                + "<li>resolves/filters the users view to reflect the resolution</li>"
                + "<li>only considers a contradiction to exist if an entity has two or more states on different paths with exactly the same commit time</ul>"
                + "This strategy is useful for single expert authoring where all editors are viewing each other's paths.</html>";
    }

    /**
     *
     * @return the display name of this last commit wins contradiction strategy
     */
    @Override
    public String getDisplayName() {
        return "Last commit wins resolution";
    }

    /**
     * Gets a copy of the versions sorted by date.
     *
     * @param <T> the generic type of component versions
     * @param originalVersions the component versions to sort
     * @return a list of date sorted versions
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
     * Gets the version, or versions, with the most recent date and time. More
     * than one version is returned if more than one version has the same latest
     * time.
     *
     * @param <T> the generic component type
     * @param versions the possible component versions
     * @return the latest version or versions
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
     * Gets a copy of the versions sorted by date.
     *
     * @param <T> the generic type of component versions
     * @param versions the component versions to sort
     * @return a list of date sorted versions
     */
    private <T extends ComponentVersionBI> List<T> getSortedPartsCopy(List<T> versions) {
        List<T> copy = new ArrayList(versions);
        Collections.sort(copy, new PartDateOrderSortComparator(true));

        return copy;
    }

    /**
     *
     * @param <T> the generic type of component version
     * @param versions the versions to resolve
     * @return the component versions resolved according to this last commit
     * wins contradiction strategy
     */
    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
        if (versions == null || versions.isEmpty()) {
            return versions;
        }

        return getLatestVersions(versions);
    }

    /**
     *
     * @param <T> the generic type of component versions
     * @param part1 the first part
     * @param part2 the second part
     * @return parts resolved according to this last commit wins contradiction
     * strategy
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
