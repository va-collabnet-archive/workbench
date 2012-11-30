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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;


/**
 * The Class UuidSnomedFixedMap contains two uuid-snomed maps. One is fixed and
 * not modifiable, the other is modifiable. The fixed map is queried when an SCT
 * ID is asked for. Any other map-based operations are performed on the
 * modifiable map.
 */
public class UuidSnomedFixedMap implements Map<UUID, Long> {

    private Map<UUID, Long> uuidSnomedMap = new HashMap<UUID, Long>();
    private List<Map<UUID, Long>> fixedMaps = new ArrayList<Map<UUID, Long>>();

    /**
     * Instantiates a new uuid-snomed fixed map.
     */
    private UuidSnomedFixedMap() {
        super();
    }

    /**
     * Adds the fixed uuid-snomed map. This is an unmodifiable map.
     *
     * @param fixedMap the fixed uuid-snomed map
     */
    public void addFixedMap(Map<UUID, Long> fixedMap) {
        fixedMaps.add(fixedMap);
    }

    /**
     * Generates a map of SNOMED IDss mapped to a list of associated uuids.
     *
     * @return the snomed to uuid-list map
     */
    public Map<Long, List<UUID>> getSnomedUuidListMap() {
        Map<Long, List<UUID>> snomedUuidListMap = new HashMap<Long, List<UUID>>();
        for (Entry<UUID, Long> entry : uuidSnomedMap.entrySet()) {
            if (snomedUuidListMap.get(entry.getValue()) == null) {
                List<UUID> uuidList = new ArrayList<UUID>(1);
                uuidList.add(entry.getKey());
                snomedUuidListMap.put(entry.getValue(), uuidList);
            } else {
                snomedUuidListMap.get(entry.getValue()).add(entry.getKey());
            }
        }
        return snomedUuidListMap;
    }

    /**
     * Not supported by this class.
     *
     * @throws UnsupportedOperationException indicates this operation is not
     * supported
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks is this map contains the specified
     * <code>key</code>.
     *
     * @param key the uuid in question
     * @return <code>true</code>, if this map contains the specified key
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return uuidSnomedMap.containsKey(key);
    }

    /**
     * Checks is this map contains the specified
     * <code>value</code>.
     *
     * @param value the SNOMED ID in question
     * @return <code>true</code>, if this map contains the specified value
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return uuidSnomedMap.containsValue(value);
    }

    /**
     * Gets a
     * <code>Set</code> representation of the uuid-snomed mappings contained in
     * this map.
     *
     * @return a <code>Set</code> representation of the uuid-snomed mappings
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Entry<UUID, Long>> entrySet() {
        return uuidSnomedMap.entrySet();
    }

    /**
     * Checks if this uuid-snomed mappings are equal to the specified other map
     * <code>obj</code>.
     *
     * @param obj the object to check for equality, another uuid-snomed map
     * @return <code>true</code>, if this uuid-snomed map is equal to the other
     * @see Map#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return uuidSnomedMap.equals(obj);
    }

    /**
     * Gets the mapped SNOMED ID associated with the uuid
     * <code>key</code>. Checks the fixed map first, then the modifiable map.
     *
     * @param key the uuid associated with the desired SCT ID
     * @return the specified SCT ID
     */
    @Override
    public Long get(Object key) {
        for (Map<UUID, Long> fixed : fixedMaps) {
            if (fixed.containsKey(key)) {
                return fixed.get(key);
            }
        }
        return uuidSnomedMap.get(key);
    }

    /**
     * Gets the hash code for this uuid-snomed map.
     *
     * @return the hash code value for this uuid-snomed map
     * @see Map#hashCode()
     */
    @Override
    public int hashCode() {
        return uuidSnomedMap.hashCode();
    }

    /**
     * Checks if this uuid-snomed map is empty.
     *
     * @return <code>true</code>, if this map is empty
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return uuidSnomedMap.isEmpty();
    }

    /**
     * Gets a
     * <code>Set</code> of the uuid keys contained in this map
     *
     * @return a set of uuids keys for this uuid-snomed map
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<UUID> keySet() {
        return uuidSnomedMap.keySet();
    }

    /**
     * Puts the uuid
     * <code>key</code> in this map with the associated
     * <code>sctId</code>.
     *
     * @param key the uuid key
     * @param sctId the associated SCT ID
     * @return the SCT ID previously associated with the specified
     * uuid, <code>null</code> if no previous value was associated
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Long put(UUID key, Long sctId) {
        return uuidSnomedMap.put(key, sctId);
    }

    /**
     * Puts all the entries in the given
     * <code>map</code> into this uuid-snomed map.
     *
     * @param map the entries to add
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends UUID, ? extends Long> map) {
        for (Entry<? extends UUID, ? extends Long> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes the entry associated with the
     * <code>key</code>.
     *
     * @param key the uuid key
     * @return the SCT ID associated with the key
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Long remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the size of this uuid-snomed map.
     *
     * @return the size of this map
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return uuidSnomedMap.size();
    }

    /**
     * Gets a
     * <code>Collection</code> of the SCT ID values in this uuid-snomed map.
     *
     * @return the SCT ID values in this map
     * @see java.util.Map#values()
     */
    @Override
    public Collection<Long> values() {
        return uuidSnomedMap.values();
    }

    /**
     * Writes the uuid-snomed map to the specified text file. The file is made
     * up of sct to uuid mappings. A mapping consists of two lines, where the
     * first line contains all uuids associated with the SCT ID, and the second
     * contains the SCT ID. All lines are tab delimited.
     *
     * @param file the file to write to
     * @throws IOException signals that an I/O exception has occurred
     */
    public void write(File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        Map<Long, List<UUID>> snomedUuidMap = this.getSnomedUuidListMap();
        SortedSet<Long> sortedKeys = new TreeSet<Long>(snomedUuidMap.keySet());
        for (Long sctId : sortedKeys) {
            List<UUID> idList = snomedUuidMap.get(sctId);
            for (int i = 0; i < idList.size(); i++) {
                UUID id = idList.get(i);
                bw.append(id.toString());
                if (i < idList.size() - 1) {
                    bw.append("\t");
                }
            }
            bw.append("\n");
            bw.append(sctId.toString());
            bw.append("\n");
        }
        bw.close();
    }

    /**
     *  Reads the text
     * <code>file</code> representing uuid-snomed mappings and creates a
     * uuid-snomed map.
     *
     * @param file the text file containing the uuid-snomed mappings
     * @return a map generated from the specified text file
     * @throws IOException signals that an I/O exception has occurred
     * @see UuidSnomedFixedMap#write(java.io.File)
     */
    public static UuidSnomedFixedMap read(File f) throws IOException {
        UuidSnomedFixedMap map = new UuidSnomedFixedMap();
        readData(f, map);
        return map;
    }
    
    /**
     * Reads the text
     * <code>file</code> representing uuid-snomed mappings and creates a
     * uuid-snomed map for the given
     * <code>namespaceId</code> and
     * <code>projectId</code>.
     *
     * @param file the text file containing the uuid-snomed mappings
     * @param namespaceId the namespace id responsible for the SCT IDs
     * @param projectId the project id for this mapping
     * @return a map generated from the specified text file
     * @throws IOException signals that an I/O exception has occurred
     * @see UuidSnomedMap#write(java.io.File)
     */
    public static UuidSnomedFixedMap read(File f, int namespaceId, int projectId) throws IOException {
        UuidSnomedFixedMap map = new UuidSnomedFixedMap();
        readData(f, map);
        return map;
    }

    /**
     * Reads the text
     * <code>file</code> representing uuid-snomed mappings puts the entries into
     * the specified fixed
     * <code>map</code>.
     *
     * @param file the text file containing the uuid-snomed mappings
     * @param map the fixed uuid-snomed map to update
     * @throws FileNotFoundException if a specified file was not found
     * @throws IOException signals that an I/O exception has occurred
     * @see UuidSnomedMap#write(java.io.File)
     */
    private static void readData(File file, UuidSnomedFixedMap map) throws FileNotFoundException, IOException {
        System.out.println("Reading map file: " + file.getAbsolutePath());
        BufferedReader br = new BufferedReader(new FileReader(file));

        String uuidLineStr;
        String sctIdLineStr;

        while ((uuidLineStr = br.readLine()) != null) {
            sctIdLineStr = br.readLine();
            Long sctId = Long.parseLong(sctIdLineStr);
            for (String uuidStr : uuidLineStr.split("\t")) {
                UUID uuid = UUID.fromString(uuidStr);
                map.put(uuid, sctId);
            }
        }
        br.close();
    }
}
