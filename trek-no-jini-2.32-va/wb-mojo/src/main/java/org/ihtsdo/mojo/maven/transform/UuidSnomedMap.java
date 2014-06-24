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
package org.ihtsdo.mojo.maven.transform;

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

import org.ihtsdo.mojo.maven.transform.SctIdGenerator.NAMESPACE;
import org.ihtsdo.mojo.maven.transform.SctIdGenerator.PROJECT;
import org.ihtsdo.mojo.maven.transform.SctIdGenerator.TYPE;

public class UuidSnomedMap implements Map<UUID, Long>, I_MapUuidsToSnomed {

    private static Calendar now = Calendar.getInstance();
    private boolean modified = false;

    private long maxSequence = 0;

    private Map<UUID, Long> uuidSnomedMap = new HashMap<UUID, Long>();

    private List<Map<UUID, Long>> fixedMaps = new ArrayList<Map<UUID, Long>>();
    private Map<Long, String> effectiveDateOfSctId = new HashMap<Long, String>();

    private PROJECT project = null;
    private NAMESPACE namespace = null;
    private int namespaceId;
    private int projectId;

    private UuidSnomedMap(PROJECT project, NAMESPACE namespace) {
        super();
        this.project = project;
        this.namespace = namespace;
    }

    private UuidSnomedMap(int projectId, int namespaceId) {
        super();
        this.projectId = projectId;
        this.namespaceId = namespaceId;
    }

    private UuidSnomedMap() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#addFixedMap(java.util.Map)
     */
    public void addFixedMap(Map<UUID, Long> fixedMap) {
        fixedMaps.add(fixedMap);
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#getSnomedUuidListMap()
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

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object arg0) {
        return uuidSnomedMap.containsKey(arg0);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#containsValue(java.lang.Object
     * )
     */
    public boolean containsValue(Object arg0) {
        return uuidSnomedMap.containsValue(arg0);
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#entrySet()
     */
    public Set<Entry<UUID, Long>> entrySet() {
        return uuidSnomedMap.entrySet();
    }

    public boolean equals(Object obj) {
        return uuidSnomedMap.equals(obj);
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#get(java.lang.Object)
     */
    public Long get(Object key) {
        for (Map<UUID, Long> fixed : fixedMaps) {
            if (fixed.containsKey(key)) {
                return fixed.get(key);
            }
        }
        return uuidSnomedMap.get(key);
    }

    private static long MAX_SCT_ID = 999999999999999999L;

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#getWithGeneration(java.util
     * .UUID, org.ihtsdo.mojo.maven.transform.SctIdGenerator.TYPE)
     */
    public Long getWithGeneration(UUID key, TYPE type) {
        Long returnValue = get(key);
        if (returnValue == null) {
            modified = true;
            returnValue = Long.parseLong(SctIdGenerator.generate(++maxSequence, projectId, namespaceId, type));
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

    public int hashCode() {
        return uuidSnomedMap.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#isEmpty()
     */
    public boolean isEmpty() {
        return uuidSnomedMap.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#keySet()
     */
    public Set<UUID> keySet() {
        return uuidSnomedMap.keySet();
    }

    private static long getSequence(Long sctId) throws StringIndexOutOfBoundsException {
        String sctIdStr = sctId.toString();
        String sequence = sctIdStr.substring(0, sctIdStr.length() - "011000036106".length());
        return Long.parseLong(sequence);
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#put(java.util.UUID,
     * java.lang.Long)
     */
    public Long put(UUID key, Long sctId) {
        /*
         * try {
         * maxSequence = Math.max(maxSequence, getSequence(sctId)); TODO
         * } catch (StringIndexOutOfBoundsException e) {
         * System.out.println("Inserted sctid which is not NEHTA owned: " + sctId);
         * }
         */
        return uuidSnomedMap.put(key, sctId);
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#putAll(java.util.Map)
     */
    public void putAll(Map<? extends UUID, ? extends Long> map) {
        for (Entry<? extends UUID, ? extends Long> entry : map.entrySet()) {
            maxSequence = Math.max(maxSequence, entry.getValue());
            put(entry.getKey(), entry.getValue());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#remove(java.lang.Object)
     */
    public Long remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#size()
     */
    public int size() {
        return uuidSnomedMap.size();
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#values()
     */
    public Collection<Long> values() {
        return uuidSnomedMap.values();
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#getMaxSequence()
     */
    public long getMaxSequence() {
        return maxSequence;
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#write(java.io.File)
     */
    public void write(File f) throws IOException {
        if (modified) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));

            Map<Long, List<UUID>> snomedUuidMap = this.getSnomedUuidListMap();
            int total = snomedUuidMap.keySet().size();
            int count = 0;
            SortedSet<Long> sortedKeys = new TreeSet<Long>(snomedUuidMap.keySet());
            for (Long sctId : sortedKeys) {
                if (++count % 1000 == 0) {
                    System.out.println("Written " + count + " of " + total + " for map " + f);
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
            System.out.println(" Map was not modified, no write required to: " + f.getName());
        }
    }

    public static UuidSnomedMap read(File f) throws IOException {
        UuidSnomedMap map = new UuidSnomedMap();
        readData(f, map);
        return map;
    }

    public static UuidSnomedMap read(File f, NAMESPACE namespace, PROJECT project) throws IOException {
        UuidSnomedMap map = new UuidSnomedMap(project, namespace);
        readData(f, map);
        System.out.println(" maxSequence on read: " + map.maxSequence);
        return map;
    }

    public static UuidSnomedMap read(File f, int namespaceId, int projectId) throws IOException {
        UuidSnomedMap map = new UuidSnomedMap(projectId, namespaceId);
        readData(f, map);
        System.out.println(" maxSequence on read: " + map.maxSequence);
        return map;
    }

    private static void readData(File f, UuidSnomedMap map) throws FileNotFoundException, IOException {
        System.out.println("Reading map file: " + f.getAbsolutePath());
        BufferedReader br = new BufferedReader(new FileReader(f));

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

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#putEffectiveDate(java.lang
     * .Long, java.lang.String, boolean)
     */
    public void putEffectiveDate(Long sctId, String date, boolean update) {
        effectiveDateOfSctId.put(sctId, date);
        modified = update;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.mojo.maven.transform.I_MapUuidsToSnomed#getEffectiveDate(java.lang
     * .Long)
     */
    public String getEffectiveDate(Long sctId) {
        return effectiveDateOfSctId.get(sctId);
    }

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

}
