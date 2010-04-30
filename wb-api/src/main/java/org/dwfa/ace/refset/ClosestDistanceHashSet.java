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
package org.dwfa.ace.refset;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClosestDistanceHashSet implements Map<Integer, ConceptRefsetInclusionDetails> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    HashMap<Integer, ConceptRefsetInclusionDetails> map = new HashMap<Integer, ConceptRefsetInclusionDetails>();

    public void add(ConceptRefsetInclusionDetails o) {

        ConceptRefsetInclusionDetails oldObject = map.get(o.getConceptId());

        if (oldObject == null) {
            map.put(o.getConceptId(), o);
        } else {
            if (o.getDistance() < oldObject.getDistance()) {
                map.put(o.getConceptId(), o);
            }
        }
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set<Entry<Integer, ConceptRefsetInclusionDetails>> entrySet() {
        return map.entrySet();
    }

    public ConceptRefsetInclusionDetails get(Object key) {
        return map.get(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<Integer> keySet() {
        return map.keySet();
    }

    public ConceptRefsetInclusionDetails remove(Object key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public String toString() {
        return map.toString();
    }

    public Collection<ConceptRefsetInclusionDetails> values() {
        return map.values();
    }

    public ConceptRefsetInclusionDetails put(Integer arg0, ConceptRefsetInclusionDetails arg1) {
        throw new UnsupportedOperationException(
            "This method is unsupported for this class, use the add(ConceptRefsetInclusionDetails) method instead");
    }

    public void putAll(Map<? extends Integer, ? extends ConceptRefsetInclusionDetails> arg0) {
        throw new UnsupportedOperationException(
            "This method is unsupported for this class, use the add(ConceptRefsetInclusionDetails) method instead");
    }

    public void removeAll(ClosestDistanceHashSet newMembersToBeRemoved) {
        for (Integer key : newMembersToBeRemoved.keySet()) {
            map.remove(key);
        }
    }

}
