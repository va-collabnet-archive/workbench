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



package org.ihtsdo.helper.dto;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.dto.concept.TkConcept;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class DtoExtract.
 *
 * @author kec
 */
public class DtoExtract {
   
   /**
    * Extract.
    *
    * @param changeSetFile the change set file
    * @param cUuids the c uuids
    * @param extractFile the extract file
    * @return <code>true</code>, if successful
    * @throws IOException signals that an I/O exception has occurred
    * @throws ClassNotFoundException the class not found exception
    */
   public static boolean extract(File changeSetFile, Collection<UUID> cUuids, File extractFile)
           throws IOException, ClassNotFoundException {
      boolean             foundConcept  = false;
      FileInputStream     fis           = new FileInputStream(changeSetFile);
      BufferedInputStream bis           = new BufferedInputStream(fis);
      DataInputStream     dataInStream  = new DataInputStream(bis);
      DataOutputStream    dataOutStream = null;

      try {
         int count = 0;

         while (dataInStream.available() > 0) {
            long      nextCommit = dataInStream.readLong();
            TkConcept eConcept   = new TkConcept(dataInStream);

            if (cUuids.contains(eConcept.getPrimordialUuid())) {
               if (dataOutStream == null) {
                  FileOutputStream     fos = new FileOutputStream(extractFile);
                  BufferedOutputStream bos = new BufferedOutputStream(fos);

                  dataOutStream = new DataOutputStream(bos);
               }

               dataOutStream.writeLong(nextCommit);
               eConcept.writeExternal(dataOutStream);
               foundConcept = true;
            }

            count++;
         }
      } catch (EOFException ex) {

         // Nothing to do...
      } finally {
         dataInStream.close();

         if (dataOutStream != null) {
            dataOutStream.close();
         }
      }

      return foundConcept;
   }

   /**
    * Extract change sets and assign new nids.
    *
    * @param changeSetFile the change set file
    * @param cUuids the c uuids
    * @param extractFile the extract file
    * @param map the map
    * @return <code>true</code>, if successful
    * @throws IOException signals that an I/O exception has occurred
    * @throws ClassNotFoundException the class not found exception
    */
   public static boolean extractChangeSetsAndAssignNewNids(File changeSetFile, Collection<UUID> cUuids,
           File extractFile, Map<UUID, UUID> map)
           throws IOException, ClassNotFoundException {
      boolean             foundConcept  = false;
      FileInputStream     fis           = new FileInputStream(changeSetFile);
      BufferedInputStream bis           = new BufferedInputStream(fis);
      DataInputStream     dataInStream  = new DataInputStream(bis);
      DataOutputStream    dataOutStream = null;

      try {
         int        count = 0;
 
         while (dataInStream.available() > 0) {
            long      nextCommit = dataInStream.readLong();
            TkConcept eConcept   = new TkConcept(dataInStream);

            if (cUuids.contains(eConcept.getPrimordialUuid())) {
               if (dataOutStream == null) {
                  FileOutputStream     fos = new FileOutputStream(extractFile);
                  BufferedOutputStream bos = new BufferedOutputStream(fos);

                  dataOutStream = new DataOutputStream(bos);
               }

               dataOutStream.writeLong(nextCommit);

               TkConcept mapped = new TkConcept(eConcept, map, 2 * 86400000, false);

               mapped.writeExternal(dataOutStream);
               foundConcept = true;
            }

            count++;
         }
      } catch (EOFException ex) {

         // Nothing to do...
      } finally {
         dataInStream.close();

         if (dataOutStream != null) {
            dataOutStream.close();
         }
      }

      return foundConcept;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class DynamicMap.
    */
   public static class DynamicMap implements Map<UUID, UUID> {
      
      /** The map. */
      HashMap<UUID, UUID> map = new HashMap<UUID, UUID>();

      //~--- methods ----------------------------------------------------------

      /* (non-Javadoc)
       * @see java.util.Map#clear()
       */
      @Override
      public void clear() {
         map.clear();
      }

      /* (non-Javadoc)
       * @see java.lang.Object#clone()
       */
      @Override
      public Object clone() {
         return map.clone();
      }

      /* (non-Javadoc)
       * @see java.util.Map#containsKey(java.lang.Object)
       */
      @Override
      public boolean containsKey(Object key) {
         return map.containsKey(key);
      }

      /* (non-Javadoc)
       * @see java.util.Map#containsValue(java.lang.Object)
       */
      @Override
      public boolean containsValue(Object value) {
         return map.containsValue(value);
      }

      /* (non-Javadoc)
       * @see java.util.Map#entrySet()
       */
      @Override
      public Set<Entry<UUID, UUID>> entrySet() {
         return map.entrySet();
      }

      /* (non-Javadoc)
       * @see java.util.Map#keySet()
       */
      @Override
      public Set<UUID> keySet() {
         return map.keySet();
      }

      /* (non-Javadoc)
       * @see java.util.Map#put(java.lang.Object, java.lang.Object)
       */
      @Override
      public UUID put(UUID key, UUID value) {
         return map.put(key, value);
      }

      /* (non-Javadoc)
       * @see java.util.Map#putAll(java.util.Map)
       */
      @Override
      public void putAll(Map<? extends UUID, ? extends UUID> m) {
         map.putAll(m);
      }

      /* (non-Javadoc)
       * @see java.util.Map#remove(java.lang.Object)
       */
      @Override
      public UUID remove(Object key) {
         return map.remove(key);
      }

      /* (non-Javadoc)
       * @see java.util.Map#size()
       */
      @Override
      public int size() {
         return map.size();
      }

      /* (non-Javadoc)
       * @see java.util.Map#values()
       */
      @Override
      public Collection<UUID> values() {
         return map.values();
      }

      //~--- get methods ------------------------------------------------------

      /* (non-Javadoc)
       * @see java.util.Map#get(java.lang.Object)
       */
      @Override
      public UUID get(Object key) {
         if (!map.containsKey((UUID) key)) {
            map.put((UUID) key, UUID.randomUUID());
            System.out.println("Creating new map: [" + key + ", " + map.get(key) + "]");
         }

         return map.get(key);
      }

      /* (non-Javadoc)
       * @see java.util.Map#isEmpty()
       */
      @Override
      public boolean isEmpty() {
         return map.isEmpty();
      }
   }
}
