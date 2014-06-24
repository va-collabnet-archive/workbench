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
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.ihtsdo.helper.transform.SctIdGenerator.TYPE;

/**
 * The Class UuidSnomedMap represents a map of uuids to SNOMED IDs (SCT IDs) and
 * contains methods for importing and exporting a file representation of this
 * map.
 */
public class UuidSnomedMap implements Map<UUID, Long>, I_MapUuidsToSnomed {

    private static Calendar now = Calendar.getInstance();
    private boolean modified = false;
    private long maxSequence = 0;
    private Map<UUID, Long> uuidSnomedMap = new HashMap<UUID, Long>();
    private List<Map<UUID, Long>> fixedMaps = new ArrayList<Map<UUID, Long>>();
    private Map<Long, String> effectiveDateOfSctId = new HashMap<Long, String>();
    private int namespaceId;
    private int projectId;

    /**
     * Instantiates a new uuid-snomed map for the given
     * <code>projectId</code> and
     * <code>namespace</code>.
     *
     * @param projectId an <code>int</code> representing the mapping project id
     * @param namespaceId an <code>int</code> representing the the namespace
     * associated with the mapped ids
     */
    private UuidSnomedMap(int projectId, int namespaceId) {
        super();
        this.projectId = projectId;
        this.namespaceId = namespaceId;
    }

    /**
     * Instantiates a new uuid-snomed map.
     */
    private UuidSnomedMap() {
        super();
    }

    /**
     *
     * @param fixedMap the uuid-snomed map to add
     */
    @Override
    public void addFixedMap(Map<UUID, Long> fixedMap) {
        fixedMaps.add(fixedMap);
    }

    /**
     *
     * @return the snomed to uuid-list map
     */
    @Override
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
     *
     * @param key the uuid in question
     * @return <code>true</code>, if this map contains the specified key
     */
    @Override
    public boolean containsKey(Object key) {
        return uuidSnomedMap.containsKey(key);
    }

    /**
     *
     * @param value the SNOMED ID in question
     * @return <code>true</code>, if this map contains the specified value
     */
    @Override
    public boolean containsValue(Object value) {
        return uuidSnomedMap.containsValue(value);
    }

    /**
     *
     * @return a <code>Set</code> representation of the uuid-snomed mappings
     * @see Map#entrySet()
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
     *
     * @param key the uuid associated with the desired SCT ID
     * @return the <code>long</code> representing the specified SCT ID
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
    private static long MAX_SCT_ID = 999999999999999999L;

    /**
     *
     * @param key the uuid associated with the desired SCT ID
     * @param type the type of SCT ID to generate if not found
     * @return the <code>long</code> representing the specified SCT ID
     */
    @Override
    public Long getWithGeneration(UUID key, TYPE type) {
        Long returnValue = get(key);
        if (returnValue == null) {
            modified = true;
            returnValue = Long.parseLong(SctIdGenerator.generate(++maxSequence, namespaceId, type));
            if (returnValue > MAX_SCT_ID) {
                throw new RuntimeException("SCT ID exceeds max allowed (" + MAX_SCT_ID + "): " + returnValue);
            }
            if (this.keySet().size() % 1000 == 0) {
                System.out.println("Map for type " + type + " is now up to " + this.keySet().size());
            }
            put(key, returnValue);
        }
        return returnValue;
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
     *
     * @return <code>true</code>, if this map is empty
     */
    @Override
    public boolean isEmpty() {
        return uuidSnomedMap.isEmpty();
    }

    /**
     *
     * @return a set of uuids keys for this uuid-snomed map
     * @see Map#keySet()
     */
    @Override
    public Set<UUID> keySet() {
        return uuidSnomedMap.keySet();
    }

    /**
     * Gets the
     * <code>long</code> sequence representing the item identifier digits in the
     * SCT ID.
     *
     * @param sctId the SCT ID containing the sequence
     * @return the sequence representing the item identifier digits in the SCT
     * ID
     * @throws StringIndexOutOfBoundsException indicates a string index out of
     * bounds exception has occurred
     */
    private static long getSequence(Long sctId) throws StringIndexOutOfBoundsException {
        String sctIdStr = sctId.toString();
        String sequence = sctIdStr.substring(0, sctIdStr.length() - "011000036106".length());
        return Long.parseLong(sequence);
    }

    /**
     *
     * @param key the uuid key
     * @param sctId the associated SCT ID
     * @return the SCT ID previously associated with the spcified * *      * uuid, <code>null</code> if no previous value was associated
     * @see Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Long put(UUID key, Long sctId) {
        /*
         * try {
         * maxSequence = Math.max(maxSequence, getSequence(sctId)); TODO
         * } catch (StringIndexOutOfBoundsException e) {
         * System.out.println("Inserted sctid which is not NEHTA owned: " + sctId);
         * }
         */
        String sctIdString = sctId.toString();
        sctIdString = sctIdString.substring(0, sctIdString.length() - 10);
        maxSequence = Math.max(maxSequence, Integer.parseInt(sctIdString));
        return uuidSnomedMap.put(key, sctId);
    }

    /**
     *
     * @param map the entries to add
     */
    @Override
    public void putAll(Map<? extends UUID, ? extends Long> map) {
        for (Entry<? extends UUID, ? extends Long> entry : map.entrySet()) {
            maxSequence = Math.max(maxSequence, entry.getValue());
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Not supported by this class.
     *
     * @throws UnsupportedOperationException indicates this operation is not
     * supported
     */
    @Override
    public Long remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return the size of this map
     * @see Map#size()
     */
    @Override
    public int size() {
        return uuidSnomedMap.size();
    }

    /**
     *
     * @return the SCT ID values in this map
     * @see Map#values()
     */
    @Override
    public Collection<Long> values() {
        return uuidSnomedMap.values();
    }

    /**
     *
     * @return the item identifier digits sequence in the mapped SCT IDs
     */
    @Override
    public long getMaxSequence() {
        return maxSequence;
    }

    /**
     *
     * @param file the file to write to
     * @throws IOException
     */
    @Override
    public void write(File file) throws IOException {
        if (modified) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            Map<Long, List<UUID>> snomedUuidMap = this.getSnomedUuidListMap();
            int total = snomedUuidMap.keySet().size();
            int count = 0;
            SortedSet<Long> sortedKeys = new TreeSet<Long>(snomedUuidMap.keySet());
            for (Long sctId : sortedKeys) {
                if (++count % 1000 == 0) {
                    System.out.println("Written " + count + " of " + total + " for map " + file);
                }
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
                // Add an effective data to the sct after writing it,
                // separated from the sctId with a tab
                bw.append("\t");
                String effectiveDate = effectiveDateOfSctId.get(sctId);
                if (effectiveDate == null) {
                    effectiveDate = getCurrentEffectiveDate();
                }
                bw.append(effectiveDate);
                bw.append("\n");
            }
            bw.close();
            System.out.println(" maxSequence on write: " + maxSequence);
        } else {
            System.out.println(" Map was not modified, no write required to: " + file.getName());
        }
    }

    /**
     * Reads the text
     * <code>file</code> representing uuid-snomed mappings and creates a
     * uuid-snomed map.
     *
     * @param file the text file containing the uuid-snomed mappings
     * @return a map generated from the specified text file
     * @throws IOException signals that an I/O exception has occurred
     * @see UuidSnomedMap#write(java.io.File)
     */
    public static UuidSnomedMap read(File file) throws IOException {
        UuidSnomedMap map = new UuidSnomedMap();
        readData(file, map);
        System.out.println(" maxSequence on read: " + map.maxSequence);
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
    public static UuidSnomedMap read(File file, int namespaceId, int projectId) throws IOException {
        UuidSnomedMap map = new UuidSnomedMap(projectId, namespaceId);
        readData(file, map);
        System.out.println(" maxSequence on read: " + map.maxSequence);
        return map;
    }

    /**
     * Reads the text
     * <code>file</code> representing uuid-snomed mappings puts the entries into
     * the specified
     * <code>map</code>.
     *
     * @param file the text file containing the uuid-snomed mappings
     * @param map the uuid-snomed map to update
     * @throws FileNotFoundException if a specified file was not found
     * @throws IOException signals that an I/O exception has occurred
     * @see UuidSnomedMap#write(java.io.File)
     */
    private static void readData(File file, UuidSnomedMap map) throws FileNotFoundException, IOException {
        System.out.println("Reading map file: " + file.getAbsolutePath());
        BufferedReader br = new BufferedReader(new FileReader(file));

        String uuidLineStr;
        String sctIdLineStr;

        while ((uuidLineStr = br.readLine()) != null) { // while loop begins
            // here
            sctIdLineStr = br.readLine();
            String[] parts = sctIdLineStr.split("\t");
            String sctIdPart = parts[0];
            String effectiveDatePart = "";
            boolean update = false;
            if (parts.length > 1) {
                effectiveDatePart = parts[1];
            } else if (parts.length == 1) {
                effectiveDatePart = getCurrentEffectiveDate();
                update = true;
            }
            Long sctId = Long.parseLong(sctIdPart);
            map.putEffectiveDate(sctId, effectiveDatePart, update);
            for (String uuidStr : uuidLineStr.split("\t")) {
                UUID uuid = UUID.fromString(uuidStr);
                map.put(uuid, sctId);
            }
        } // end while
        br.close();
    }

    /**
     * 
     * @param sctId the published SCT ID
     * @param date the date of publication
     * @param update set to <code>true</code> to indicate the file has been
     * modified since its last write
     */
    @Override
    public void putEffectiveDate(Long sctId, String date, boolean update) {
        effectiveDateOfSctId.put(sctId, date);
        modified = update;
    }

    /**
     * 
     * @param sctId the SCT ID in question
     * @return the effective date of publication associated with the SCT ID
     */
    @Override
    public String getEffectiveDate(Long sctId) {
        return effectiveDateOfSctId.get(sctId);
    }

    /**
     * Gets the current time/date in the correct format for an effective date.
     *
     * @return the current  time represented as an effective date
     */
    public static String getCurrentEffectiveDate() {
        int month = now.get(Calendar.MONTH);
        month++;
        Integer date = now.get(Calendar.DATE);
        String m = new Integer(month).toString();
        String d = date.toString();
        if (month < 10) {
            m = "0" + m;
        }
        if (date < 10) {
            d = "0" + d;
        }

        return now.get(Calendar.YEAR) + "-" + m + "-" + d + " 00:00:00";

    }

    /**
     * Gets the namespace id responsible for the ids in this uuid-snomed map.
     *
     * @return the namespace id
     */
    public int getNamespaceId() {
        return namespaceId;
    }

    /**
     * Sets the namespace id responsible for the ids in this uuid-snomed map.
     *
     * @param namespaceId the namespace id to associate with this uuid-snomed map
     */
    public void setNamespaceId(int namespaceId) {
        this.namespaceId = namespaceId;
    }
}
