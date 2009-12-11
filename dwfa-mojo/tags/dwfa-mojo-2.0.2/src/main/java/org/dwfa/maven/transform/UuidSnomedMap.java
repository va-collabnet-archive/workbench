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
package org.dwfa.maven.transform;

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

import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public class UuidSnomedMap implements Map<UUID, Long> {

   private long maxSequence = 0;

   private Map<UUID, Long> uuidSnomedMap = new HashMap<UUID, Long>();
   
   private List<Map<UUID, Long>> fixedMaps = new ArrayList<Map<UUID,Long>>();
   
   private PROJECT project = null;
   private NAMESPACE namespace = null;
   
   private UuidSnomedMap(PROJECT project, NAMESPACE namespace) {
      super();
      this.project = project;
      this.namespace = namespace;
   }
   private UuidSnomedMap() {
      super();
   }

   public void addFixedMap(Map<UUID, Long> fixedMap) {
      fixedMaps.add(fixedMap);
   }

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

   public void clear() {
      throw new UnsupportedOperationException();
   }

   public boolean containsKey(Object arg0) {
      return uuidSnomedMap.containsKey(arg0);
   }

   public boolean containsValue(Object arg0) {
      return uuidSnomedMap.containsValue(arg0);
   }

   public Set<Entry<UUID, Long>> entrySet() {
      return uuidSnomedMap.entrySet();
   }

   public boolean equals(Object obj) {
      return uuidSnomedMap.equals(obj);
   }

   public Long get(Object key) {
      for (Map<UUID, Long> fixed: fixedMaps) {
         if (fixed.containsKey(key)) {
            return fixed.get(key);
         }
      }
      return uuidSnomedMap.get(key);
   }

   private static long MAX_SCT_ID = 999999999999999999L;
   public Long getWithGeneration(UUID key, TYPE type) {
      Long returnValue = get(key);
      if (returnValue == null) {
         returnValue = Long.parseLong(SctIdGenerator.generate(++maxSequence, project, namespace, type));
         if (returnValue > MAX_SCT_ID) {
            throw new RuntimeException("SCT ID exceeds max allowed (" + MAX_SCT_ID + "): " + returnValue);
         }
         put(key, returnValue);
      }
      return returnValue;
   }

   public int hashCode() {
      return uuidSnomedMap.hashCode();
   }

   public boolean isEmpty() {
      return uuidSnomedMap.isEmpty();
   }

   public Set<UUID> keySet() {
      return uuidSnomedMap.keySet();
   }
   private static long getSequence(Long sctId) {
      String sctIdStr = sctId.toString();
      String sequence = sctIdStr.substring(0, sctIdStr.length() - "011000036106".length());
      return Long.parseLong(sequence);
   }
   public Long put(UUID key, Long sctId) {
      maxSequence = Math.max(maxSequence, getSequence(sctId));
      return uuidSnomedMap.put(key, sctId);
   }

   public void putAll(Map<? extends UUID, ? extends Long> map) {
      for (Entry<? extends UUID, ? extends Long> entry : map.entrySet()) {
         maxSequence = Math.max(maxSequence, entry.getValue());
         put(entry.getKey(), entry.getValue());
      }
   }

   public Long remove(Object key) {
      throw new UnsupportedOperationException();
   }

   public int size() {
      return uuidSnomedMap.size();
   }

   public Collection<Long> values() {
      return uuidSnomedMap.values();
   }

   public long getMaxSequence() {
      return maxSequence;
   }
   
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
      System.out.println(" maxSequence on write: " + maxSequence);
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

   private static void readData(File f, UuidSnomedMap map) throws FileNotFoundException, IOException {
      System.out.println("Reading map file: " + f.getAbsolutePath());
      BufferedReader br = new BufferedReader(new FileReader(f));

      String uuidLineStr;
      String sctIdLineStr;

      while ((uuidLineStr = br.readLine()) != null) { // while loop begins here
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
