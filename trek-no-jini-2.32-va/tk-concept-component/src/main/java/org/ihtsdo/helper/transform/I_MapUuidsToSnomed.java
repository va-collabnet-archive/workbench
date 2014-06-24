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

/**
 * The Interface I_MapUuidsToSnomed provides methods for interacting with a map
 * of uuids to SNOMED IDs (SCT IDs) including importing existing maps and
 * writing mapping files.
 *
 */
public interface I_MapUuidsToSnomed {

    /**
     * Allows an existing uuid-snomed map to be added to this map. The
     * <code>fixedMap</code> will be checked any time an id is asked for.
     *
     * @param fixedMap the existing uuid-snomed map
     */
    public abstract void addFixedMap(Map<UUID, Long> fixedMap);

    /**
     * Generates a map of SNOMED IDss mapped to a list of associated uuids.
     *
     * @return the snomed to uuid-list map
     */
    public abstract Map<Long, List<UUID>> getSnomedUuidListMap();

    /**
     * Clears the map.
     */
    public abstract void clear();

    /**
     * Checks is this map contains the specified
     * <code>key</code>.
     *
     * @param key the uuid in question
     * @return <code>true</code>, if this map contains the specified key
     */
    public abstract boolean containsKey(Object key);

    /**
     * Checks is this map contains the specified
     * <code>value</code>.
     *
     * @param value the SNOMED ID in question
     * @return <code>true</code>, if this map contains the specified value
     */
    public abstract boolean containsValue(Object value);

    /**
     * Gets a
     * <code>Set</code> representation of the uuid-snomed mappings contained in
     * this map.
     *
     * @return a <code>Set</code> representation of the uuid-snomed mappings
     */
    public abstract Set<Entry<UUID, Long>> entrySet();

    /**
     * Gets the mapped SNOMED ID associated with the uuid
     * <code>key</code>.
     *
     * @param key the uuid associated with the desired SCT ID
     * @return the specified SCT ID
     */
    public abstract Long get(Object key);

    /**
     * Gets the mapped SNOMED ID associated with the uuid
     * <code>key</code>. If no SCT ID is found one will be generated of the
     * <code>type</code> specified.
     *
     * @param key the uuid associated with the desired SCT ID
     * @param type the type of SCT ID to generate if not found
     * @return the specified SCT ID
     */
    public abstract Long getWithGeneration(UUID key, TYPE type);

    /**
     * Checks if this uuid-snomed map is empty.
     *
     * @return <code>true</code>, if this map is empty
     */
    public abstract boolean isEmpty();

    /**
     * Gets a
     * <code>Set</code> of the uuid keys contained in this map
     *
     * @return a set of uuids keys for this uuid-snomed map
     */
    public abstract Set<UUID> keySet();

    /**
     * Puts the uuid
     * <code>key</code> in this map with the associated
     * <code>sctId</code>.
     *
     * @param key the uuid key
     * @param sctId the associated SCT ID
     * @return the SCT ID previously associated with the specified
     * uuid, <code>null</code> if no previous value was associated
     */
    public abstract Long put(UUID key, Long sctId);

    /**
     * Puts all the entries in the given
     * <code>map</code> into this uuid-snomed map.
     *
     * @param map the entries to add
     */
    public abstract void putAll(Map<? extends UUID, ? extends Long> map);

    /**
     * Removes the entry associated with the
     * <code>key</code>.
     *
     * @param key the uuid key
     * @return the SCT ID associated with the key
     */
    public abstract Long remove(Object key);

    /**
     * Gets the size of this uuid-snomed map.
     *
     * @return the size of this map
     */
    public abstract int size();

    /**
     * Gets a
     * <code>Collection</code> of the SCT ID values in this uuid-snomed map.
     *
     * @return the SCT ID values in this map
     */
    public abstract Collection<Long> values();

    /**
     * Gets the max sequence value representing the item identifier digits in
     * the mapped SCT IDs.
     *
     * @return the item identifier digits sequence in the mapped SCT IDs
     */
    public abstract long getMaxSequence();

    /**
     * Writes the uuid-snomed map to the specified text file. The file is made
     * up of sct to uuid mappings. A mapping consists of two lines, where the
     * first line contains all uuids associated with the SCT ID, and the second
     * contains the SCT ID and the effective date. All lines are tab delimited.
     *
     * @param file the file to write to
     * @throws IOException signals that an I/O exception has occurred
     */
    public abstract void write(File file) throws IOException;

    /**
     * Associates an effective date of publication with an SCT ID.
     *
     * @param sctId the SCT ID
     * @param date the date of publication
     * @param update set to <code>true</code> to indicate the file has been
     * modified since its last write
     */
    public abstract void putEffectiveDate(Long sctId, String date, boolean update);

    /**
     * Gets the effective date for the given
     * <code>sctId</code>.
     *
     * @param sctId the SCT ID in question
     * @return the effective date of publication associated with the SCT ID
     */
    public abstract String getEffectiveDate(Long sctId);
}
