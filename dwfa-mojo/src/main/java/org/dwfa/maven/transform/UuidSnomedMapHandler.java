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
/**
 *
 */
package org.dwfa.maven.transform;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dwfa.derby.util.DerbyBackedUuidSctidFixedMap;
import org.dwfa.derby.util.DerbyBackedUuidSctidMap;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public class UuidSnomedMapHandler {
    private final class SctMapRwFilter implements FileFilter {
        private final TYPE type;

        private SctMapRwFilter(TYPE type) {
            this.type = type;
        }

        public boolean accept(File f) {
            return f.getName().endsWith(type + "-sct-map-rw.txt");
        }
    }

    private final class SctMapFilter implements FileFilter {
        public boolean accept(File f) {
            return f.getName().endsWith("sct-map.txt");
        }
    }

    private Map<TYPE, UuidSnomedMap> mapMap;
    private Map<TYPE, File> fileMap;

    public UuidSnomedMapHandler(File idGeneratedDir, File sourceDirectory, boolean useDbSctIdMap) throws IOException,
            ClassNotFoundException {
        init(idGeneratedDir, sourceDirectory, useDbSctIdMap);
    }

    public UuidSnomedMapHandler(File idGeneratedDir, File sourceDirectory) throws IOException, ClassNotFoundException {
        init(idGeneratedDir, sourceDirectory, Boolean.TRUE);
    }

    private void init(File idGeneratedDir, File sourceDirectory, final boolean useDbSctIdMap) throws IOException,
            ClassNotFoundException {
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
                String[] nameParts = fileMap.get(type).getName().split("-");
                NAMESPACE namespace = NAMESPACE.valueOf(nameParts[0]);
                PROJECT project = PROJECT.valueOf(nameParts[1]);
                mapMap.put(type, DerbyBackedUuidSctidMap.read(fileMap.get(type), namespace, project));
            }
            for (File fixedMapFile : idSourceDir.listFiles(new SctMapFilter())) {
                Map<UUID, Long> fixedMap;
                if (useDbSctIdMap) {
                    fixedMap = DerbyBackedUuidSctidFixedMap.read(fixedMapFile);
                } else {
                    fixedMap = UuidSnomedFixedMap.read(fixedMapFile);
                }
                for (TYPE type : TYPE.values()) {
                    mapMap.get(type).addFixedMap(fixedMap);
                }
            }
            if (idGeneratedDir.listFiles() != null) {
                for (File fixedMapFile : idGeneratedDir.listFiles(new SctMapFilter())) {

                    Map<UUID, Long> fixedMap;
                    if (useDbSctIdMap) {
                        fixedMap = DerbyBackedUuidSctidFixedMap.read(fixedMapFile);
                    } else {
                        fixedMap = UuidSnomedFixedMap.read(fixedMapFile);
                    }
                    for (TYPE type : TYPE.values()) {
                        mapMap.get(type).addFixedMap(fixedMap);
                    }
                }
            }
        }
    }

    public Long getWithGeneration(UUID id, TYPE type) {
        return mapMap.get(type).getWithGeneration(id, type);
    }

    public Long getWithoutGeneration(UUID id, TYPE type) {
        return mapMap.get(type).get(id);
    }

    public void writeMaps() throws IOException {
        for (TYPE type : TYPE.values()) {
            mapMap.get(type).write(fileMap.get(type));
        }
    }
}
