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

/**
 * The Class DtoExtract extracts changesets, for a particular concept, from the
 * database to changeset files. This allows the developers to see all the
 * changesets associated with a given concept and can assist with debugging.
 */
public class DtoExtract {

    /**
     * Extracts the changesets associated with the specified concept.
     *
     * @param changeSetFile the directory to write the changeset files to
     * @param cUuids the concepts to extract changsets for
     * @param extractFile the exported changeset file
     * @return <code>true</code>, if the export is successful
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates that class not found exception
     * has occurred
     */
    public static boolean extract(File changeSetFile, Collection<UUID> cUuids, File extractFile)
            throws IOException, ClassNotFoundException {
        boolean foundConcept = false;
        FileInputStream fis = new FileInputStream(changeSetFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dataInStream = new DataInputStream(bis);
        DataOutputStream dataOutStream = null;

        try {
            int count = 0;

            while (dataInStream.available() > 0) {
                long nextCommit = dataInStream.readLong();
                TkConcept eConcept = new TkConcept(dataInStream);

                if (cUuids.contains(eConcept.getPrimordialUuid())) {
                    if (dataOutStream == null) {
                        FileOutputStream fos = new FileOutputStream(extractFile);
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
     * Extracts the changesets associated with the specified concept and assigns
     * new identifiers.
     *
     * @param changeSetFile the directory to write the changeset files to
     * @param cUuids the concepts to extract changsets for
     * @param extractFile the exported changeset file
     * @param map the uuid-uuid map specifying the mappings for the new uuids
     * @return <code>true</code>, if the export is successful
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates that class not found exception
     * has occurred
     */
    public static boolean extractChangeSetsAndAssignNewNids(File changeSetFile, Collection<UUID> cUuids,
            File extractFile, Map<UUID, UUID> map)
            throws IOException, ClassNotFoundException {
        boolean foundConcept = false;
        FileInputStream fis = new FileInputStream(changeSetFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dataInStream = new DataInputStream(bis);
        DataOutputStream dataOutStream = null;

        try {
            int count = 0;

            while (dataInStream.available() > 0) {
                long nextCommit = dataInStream.readLong();
                TkConcept eConcept = new TkConcept(dataInStream);

                if (cUuids.contains(eConcept.getPrimordialUuid())) {
                    if (dataOutStream == null) {
                        FileOutputStream fos = new FileOutputStream(extractFile);
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
     * The Class DynamicMap will create a new mapping for the specified key if a
     * mapping is requested for a key that does not yet exist.
     */
    public static class DynamicMap implements Map<UUID, UUID> {

        /**
         * The map backing this dynamic map.
         */
        HashMap<UUID, UUID> map = new HashMap<UUID, UUID>();

        //~--- methods ----------------------------------------------------------
        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#clear()
         */
        @Override
        public void clear() {
            map.clear();
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.lang.Object#clone()
         */
        @Override
        public Object clone() {
            return map.clone();
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#entrySet()
         */
        @Override
        public Set<Entry<UUID, UUID>> entrySet() {
            return map.entrySet();
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#keySet()
         */
        @Override
        public Set<UUID> keySet() {
            return map.keySet();
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        @Override
        public UUID put(UUID key, UUID value) {
            return map.put(key, value);
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#putAll(java.util.Map)
         */
        @Override
        public void putAll(Map<? extends UUID, ? extends UUID> m) {
            map.putAll(m);
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#remove(java.lang.Object)
         */
        @Override
        public UUID remove(Object key) {
            return map.remove(key);
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#size()
         */
        @Override
        public int size() {
            return map.size();
        }

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#values()
         */
        @Override
        public Collection<UUID> values() {
            return map.values();
        }

        //~--- get methods ------------------------------------------------------
        /**
         * If the map does not contain the
         * <code>key</code>, a new mapping is generated using the
         * <code>key</code> and associating a random uuid as the value. The new
         * entry is printed to the screen/log.
         *
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

        /**
         * Uses the
         * <code>Map</code> implementation.
         *
         * @see java.util.Map#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }
    }
}
