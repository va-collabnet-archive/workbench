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
package org.ihtsdo.helper.transform;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.ihtsdo.helper.transform.SctIdGenerator.TYPE;

// TODO: Auto-generated Javadoc
/**
 * The Interface I_MapUuidsToSnomed.
 */
public interface I_MapUuidsToSnomed {

    /**
     * Adds the fixed map.
     *
     * @param fixedMap the fixed map
     */
    public abstract void addFixedMap(Map<UUID, Long> fixedMap);

    /**
     * Gets the snomed uuid list map.
     *
     * @return the snomed uuid list map
     */
    public abstract Map<Long, List<UUID>> getSnomedUuidListMap();

    /**
     * Clear.
     */
    public abstract void clear();

    /**
     * Contains key.
     *
     * @param arg0 the arg0
     * @return true, if successful
     */
    public abstract boolean containsKey(Object arg0);

    /**
     * Contains value.
     *
     * @param arg0 the arg0
     * @return true, if successful
     */
    public abstract boolean containsValue(Object arg0);

    /**
     * Entry set.
     *
     * @return the sets the
     */
    public abstract Set<Entry<UUID, Long>> entrySet();

    /**
     * Gets the.
     *
     * @param key the key
     * @return the long
     */
    public abstract Long get(Object key);

    /**
     * Gets the with generation.
     *
     * @param key the key
     * @param type the type
     * @return the with generation
     */
    public abstract Long getWithGeneration(UUID key, TYPE type);

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    public abstract boolean isEmpty();

    /**
     * Key set.
     *
     * @return the sets the
     */
    public abstract Set<UUID> keySet();

    /**
     * Put.
     *
     * @param key the key
     * @param sctId the sct id
     * @return the long
     */
    public abstract Long put(UUID key, Long sctId);

    /**
     * Put all.
     *
     * @param map the map
     */
    public abstract void putAll(Map<? extends UUID, ? extends Long> map);

    /**
     * Removes the.
     *
     * @param key the key
     * @return the long
     */
    public abstract Long remove(Object key);

    /**
     * Size.
     *
     * @return the int
     */
    public abstract int size();

    /**
     * Values.
     *
     * @return the collection
     */
    public abstract Collection<Long> values();

    /**
     * Gets the max sequence.
     *
     * @return the max sequence
     */
    public abstract long getMaxSequence();

    /**
     * Write.
     *
     * @param f the f
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public abstract void write(File f) throws IOException;

    /**
     * Put effective date.
     *
     * @param sctId the sct id
     * @param date the date
     * @param update the update
     */
    public abstract void putEffectiveDate(Long sctId, String date, boolean update);

    /**
     * Gets the effective date.
     *
     * @param sctId the sct id
     * @return the effective date
     */
    public abstract String getEffectiveDate(Long sctId);

}
