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

// TODO: Auto-generated Javadoc
/**
 * The Class UuidSnomedMapHandler.
 */
public class UuidSnomedMapHandler {
    
    /**
     * The Class SctMapRwFilter.
     */
    private final class SctMapRwFilter implements FileFilter {
        
        /** The type. */
        private final TYPE type;

        /**
         * Instantiates a new sct map rw filter.
         *
         * @param type the type
         */
        private SctMapRwFilter(TYPE type) {
            this.type = type;
        }

        /* (non-Javadoc)
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f) {
            return f.getName().endsWith(type + "-sct-map-rw.txt");
        }
    }

    /**
     * The Class SctMapFilter.
     */
    private final class SctMapFilter implements FileFilter {
        
        /* (non-Javadoc)
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f) {
            return f.getName().endsWith("sct-map.txt");
        }
    }

    /** The map map. */
    private Map<TYPE, UuidSnomedMap> mapMap;
    
    /** The file map. */
    private Map<TYPE, File> fileMap;
    
    /** The namespace. */
    private String namespace;

    /**
     * Instantiates a new uuid snomed map handler.
     *
     * @param idGeneratedDir the id generated dir
     * @param sourceDirectory the source directory
     * @throws IOException signals that an I/O exception has occurred.
     */
    public UuidSnomedMapHandler(File idGeneratedDir, File sourceDirectory) throws IOException {
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
            if (idGeneratedDir.listFiles() != null) {
                for (File fixedMapFile : idGeneratedDir.listFiles(new SctMapFilter())) {
                    UuidSnomedFixedMap fixedMap = UuidSnomedFixedMap.read(fixedMapFile);
                    for (TYPE type : TYPE.values()) {
                        mapMap.get(type).addFixedMap(fixedMap);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new uuid snomed map handler.
     *
     * @param idGeneratedDir the id generated dir
     * @param sourceDirectory the source directory
     * @param namespace the namespace
     * @param project the project
     * @throws IOException signals that an I/O exception has occurred.
     */
    public UuidSnomedMapHandler(File idGeneratedDir, File sourceDirectory, int namespace, int project)
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
            if (idGeneratedDir.listFiles() != null) {
                for (File fixedMapFile : idGeneratedDir.listFiles(new SctMapFilter())) {
                    UuidSnomedFixedMap fixedMap = UuidSnomedFixedMap.read(fixedMapFile);
                    for (TYPE type : TYPE.values()) {
                        mapMap.get(type).addFixedMap(fixedMap);
                    }
                }
            }
        }
    }

    /**
     * Gets the with generation.
     *
     * @param id the id
     * @param type the type
     * @return the with generation
     */
    public Long getWithGeneration(UUID id, TYPE type) {
        UuidSnomedMap map = mapMap.get(type);
        map.setNamespaceId(Integer.parseInt(namespace));
        return mapMap.get(type).getWithGeneration(id, type);
    }

    /**
     * Gets the without generation.
     *
     * @param id the id
     * @param type the type
     * @return the without generation
     */
    public Long getWithoutGeneration(UUID id, TYPE type) {
        return mapMap.get(type).get(id);
    }

    /**
     * Write maps.
     *
     * @throws IOException signals that an I/O exception has occurred.
     */
    public void writeMaps() throws IOException {
        for (TYPE type : TYPE.values()) {
            mapMap.get(type).write(fileMap.get(type));
        }
    }

    /**
     * Gets the namespace.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace.
     *
     * @param namespace the new namespace
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    
}
