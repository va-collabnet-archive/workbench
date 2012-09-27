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

// TODO: Auto-generated Javadoc
/**
 * The Class UuidSnomedFixedMap.
 */
public class UuidSnomedFixedMap implements Map<UUID, Long> {

    /** The uuid snomed map. */
    private Map<UUID, Long> uuidSnomedMap = new HashMap<UUID, Long>();

    /** The fixed maps. */
    private List<Map<UUID, Long>> fixedMaps = new ArrayList<Map<UUID, Long>>();

    /**
     * Instantiates a new uuid snomed fixed map.
     */
    private UuidSnomedFixedMap() {
        super();
    }

    /**
     * Adds the fixed map.
     *
     * @param fixedMap the fixed map
     */
    public void addFixedMap(Map<UUID, Long> fixedMap) {
        fixedMaps.add(fixedMap);
    }

    /**
     * Gets the snomed uuid list map.
     *
     * @return the snomed uuid list map
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

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object arg0) {
        return uuidSnomedMap.containsKey(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object arg0) {
        return uuidSnomedMap.containsValue(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<Entry<UUID, Long>> entrySet() {
        return uuidSnomedMap.entrySet();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return uuidSnomedMap.equals(obj);
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Long get(Object key) {
        for (Map<UUID, Long> fixed : fixedMaps) {
            if (fixed.containsKey(key)) {
                return fixed.get(key);
            }
        }
        return uuidSnomedMap.get(key);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return uuidSnomedMap.hashCode();
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return uuidSnomedMap.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<UUID> keySet() {
        return uuidSnomedMap.keySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Long put(UUID key, Long sctId) {
        return uuidSnomedMap.put(key, sctId);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends UUID, ? extends Long> map) {
        for (Entry<? extends UUID, ? extends Long> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Long remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return uuidSnomedMap.size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection<Long> values() {
        return uuidSnomedMap.values();
    }

    /**
     * Write.
     *
     * @param f the f
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(File f) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));

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
     * Read.
     *
     * @param f the f
     * @return the uuid snomed fixed map
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static UuidSnomedFixedMap read(File f) throws IOException {
        UuidSnomedFixedMap map = new UuidSnomedFixedMap();
        readData(f, map);
        return map;
    }

    /*
     * public static UuidSnomedFixedMap read(File f, NAMESPACE namespace, PROJECT project) throws IOException {
     * UuidSnomedFixedMap map = new UuidSnomedFixedMap();
     * readData(f, map);
     * return map;
     * }
     */

    /**
     * Read.
     *
     * @param f the f
     * @param namespace the namespace
     * @param project the project
     * @return the uuid snomed fixed map
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static UuidSnomedFixedMap read(File f, int namespace, int project) throws IOException {
        UuidSnomedFixedMap map = new UuidSnomedFixedMap();
        readData(f, map);
        return map;
    }

    /**
     * Read data.
     *
     * @param f the f
     * @param map the map
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void readData(File f, UuidSnomedFixedMap map) throws FileNotFoundException, IOException {
        System.out.println("Reading map file: " + f.getAbsolutePath());
        BufferedReader br = new BufferedReader(new FileReader(f));

        String uuidLineStr;
        String sctIdLineStr;

        while ((uuidLineStr = br.readLine()) != null) { // while loop begins
            // here
            sctIdLineStr = br.readLine();
            Long sctId = Long.parseLong(sctIdLineStr);
            for (String uuidStr : uuidLineStr.split("\t")) {
                UUID uuid = UUID.fromString(uuidStr);
                map.put(uuid, sctId);
            }
        } // end while
        br.close();
    }

}
