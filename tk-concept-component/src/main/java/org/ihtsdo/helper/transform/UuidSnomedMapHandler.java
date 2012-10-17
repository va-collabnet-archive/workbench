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
/**
 *
 */
package org.ihtsdo.helper.transform;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.ihtsdo.helper.transform.SctIdGenerator.TYPE;


/**
 * The Class UuidSnomedMapHandler handles uuid-snomed mappings and contains
 * methods for reading or writing a set of mapping files. additionally it
 * provides a way of finding if an SCT ID already exists for a UUID, and if not
 * can generate an SCT ID using the proper sequence for the item identifier.
 * This is the preferred class to use for generating SCT IDs.
 *
 * @see <a href="http://www.snomed.org/tig?t=trg2main_sctid">IHTSDO Technical
 * Implementation Guide - SCT ID</a>
 */
public class UuidSnomedMapHandler {

    /**
     * The Class SctMapRwFilter filters a file directory to only return files
     * ending with the specified type and "-sct-map-rw.txt".
     */
    private final class SctMapRwFilter implements FileFilter {

        private final TYPE type;

        /**
         * Instantiates a new sct map rw filter based on the specified SCT ID
         * <code>type</code>.
         *
         * @param type the type
         */
        private SctMapRwFilter(TYPE type) {
            this.type = type;
        }

        /**
         * Only returns file that end with the specified type plus
         * "-sct-map-rw.txt".
         *
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f) {
            return f.getName().endsWith(type + "-sct-map-rw.txt");
        }
    }

    /**
     * The Class SctMapFilter filters a file directory to only return files
     * ending with "-sct-map-rw.txt".
     */
    private final class SctMapFilter implements FileFilter {

        /**
         * Only returns file that end with "-sct-map-rw.txt".
         *
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f) {
            return f.getName().endsWith("sct-map.txt");
        }
    }
    private Map<TYPE, UuidSnomedMap> mapMap;
    private Map<TYPE, File> fileMap;
    private String namespace;

    /**
     * Instantiates a new uuid snomed map handler that will read a mapping file
     * from the
     * <code>sourceDirectory</code> and write a mapping file to the
     * <code>idGeneratedDirectory</code>.
     *
     * @param idGeneratedDirectory the directory to write mapping files to
     * @param sourceDirectory the directory to read mapping files from
     * @throws IOException signals that an I/O exception has occurred
     */
    public UuidSnomedMapHandler(File idGeneratedDirectory, File sourceDirectory) throws IOException {
        if (mapMap == null) {
            mapMap = new HashMap<TYPE, UuidSnomedMap>();
            fileMap = new HashMap<TYPE, File>();
            File idSourceDir = new File(sourceDirectory.getParentFile(), "sct-uuid-maps");
            for (final TYPE type : TYPE.values()) {
                File[] rwMapFileArray = idSourceDir.listFiles(new SctMapRwFilter(type));
                if (rwMapFileArray == null || rwMapFileArray.length != 1) {
                    throw new IOException(
                            "RW mapping file not found. There must be one--and only one--file of format [namespace]-"
                            + type + "-sct-map-rw.txt in the directory " + idSourceDir.getAbsolutePath());
                }

                fileMap.put(type, rwMapFileArray[0]);
                mapMap.put(type, UuidSnomedMap.read(fileMap.get(type)));
            }
            for (File fixedMapFile : idSourceDir.listFiles(new SctMapFilter())) {
                UuidSnomedFixedMap fixedMap = UuidSnomedFixedMap.read(fixedMapFile);
                for (TYPE type : TYPE.values()) {
                    mapMap.get(type).addFixedMap(fixedMap);
                }
            }
            if (idGeneratedDirectory.listFiles() != null) {
                for (File fixedMapFile : idGeneratedDirectory.listFiles(new SctMapFilter())) {
                    UuidSnomedFixedMap fixedMap = UuidSnomedFixedMap.read(fixedMapFile);
                    for (TYPE type : TYPE.values()) {
                        mapMap.get(type).addFixedMap(fixedMap);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new uuid snomed map handler that will read a mapping file
     * from the
     * <code>sourceDirectory</code> and write a mapping file to the
     * <code>idGeneratedDirectory</code>. Allows the namespace and project ids
     * to be specified.
     *
     * @param idGeneratedDirectory the directory to write mapping files to
     * @param sourceDirectory the directory to read mapping files from
     * @param namespace the namespace id responsible for the mapped identifiers
     * @param project the id of the mapping project
     * @throws IOException signals that an I/O exception has occurred
     */
    public UuidSnomedMapHandler(File idGeneratedDirectory, File sourceDirectory, int namespace, int project)
            throws IOException {
        if (mapMap == null) {
            mapMap = new HashMap<TYPE, UuidSnomedMap>();
            fileMap = new HashMap<TYPE, File>();
            File idSourceDir = new File(sourceDirectory.getParentFile(), "sct-uuid-maps");
            for (final TYPE type : TYPE.values()) {
                File[] rwMapFileArray = idSourceDir.listFiles(new SctMapRwFilter(type));
                if (rwMapFileArray == null || rwMapFileArray.length != 1) {
                    throw new IOException(
                            "RW mapping file not found. There must be one--and only one--file of format [namespace]-[project]-"
                            + type + "-sct-map-rw.txt in the directory " + idSourceDir.getAbsolutePath());
                }
                fileMap.put(type, rwMapFileArray[0]);
                mapMap.put(type, UuidSnomedMap.read(fileMap.get(type)));
            }
            for (File fixedMapFile : idSourceDir.listFiles(new SctMapFilter())) {
                UuidSnomedFixedMap fixedMap = UuidSnomedFixedMap.read(fixedMapFile);
                for (TYPE type : TYPE.values()) {
                    mapMap.get(type).addFixedMap(fixedMap);
                }
            }
            if (idGeneratedDirectory.listFiles() != null) {
                for (File fixedMapFile : idGeneratedDirectory.listFiles(new SctMapFilter())) {
                    UuidSnomedFixedMap fixedMap = UuidSnomedFixedMap.read(fixedMapFile);
                    for (TYPE type : TYPE.values()) {
                        mapMap.get(type).addFixedMap(fixedMap);
                    }
                }
            }
        }
    }

    /**
     * Gets the mapped SNOMED ID associated with the
     * <code>uuid</code>. If no SCT ID is found one will be generated of the
     * <code>type</code> specified.
     *
     * @param uuid the uuid associated with the desired SCT ID
     * @param type the type of SCT ID to generate if not found
     * @return the specified SCT ID
     */
    public Long getWithGeneration(UUID uuid, TYPE type) {
        UuidSnomedMap map = mapMap.get(type);
        map.setNamespaceId(Integer.parseInt(namespace));
        return mapMap.get(type).getWithGeneration(uuid, type);
    }

    /**
     * Gets the mapped SNOMED ID associated with the
     * <code>uuid</code>.
     *
     * @param uuid the uuid associated with the desired SCT ID
     * @param type the type of SCT ID to generate if not found
     * @return the specified SCT ID, <code>null</code> if not found
     */
    public Long getWithoutGeneration(UUID uuid, TYPE type) {
        return mapMap.get(type).get(uuid);
    }

    /**
     * Writes the uuid-snomed maps for each SCT ID type.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    public void writeMaps() throws IOException {
        for (TYPE type : TYPE.values()) {
            mapMap.get(type).write(fileMap.get(type));
        }
    }

    /**
     * Gets a
     * <code>String</code> representing the namespace responsible for the SCT
     * IDs.
     *
     * @return the namespace responsible for the SCT IDs
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace responsible for the SCT IDs. This a value is used as
     * the namespace for SCT IDs that are generated.
     *
     * @param namespace a <code>String</code> representing the namespace
     * responsible for the SCT IDs
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
