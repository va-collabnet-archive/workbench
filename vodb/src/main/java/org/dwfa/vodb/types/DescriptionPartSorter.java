/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_DescriptionPart;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public final class DescriptionPartSorter {
    private final DescriptionPartComparator comparator;

    public DescriptionPartSorter(final DescriptionPartComparator comparator) {
        this.comparator = comparator;
    }

    public I_DescriptionPart sort(final Collection<I_DescriptionPart> descriptionParts) {
        Set<I_DescriptionPart> sortedSet = new TreeSet<I_DescriptionPart>(comparator);
        sortedSet.addAll(descriptionParts);
        return sortedSet.toArray(new I_DescriptionPart[sortedSet.size()])[sortedSet.size() - 1];
    }
}
