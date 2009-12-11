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
package org.dwfa.maven.sctid;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.maven.transform.SctIdGenerator;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

/**
 * Manages the maps for the UUIDs and sctIds.
 *
 * Given a UUID NAMESPACE and TYPE this class will return a matching SctId. The
 * SctId may require generation, in this case the new SctId is generated and
 * stored in a map.
 *
 * The same UUID will result in the same SctId over the life of the DB.
 *
 * @author Ean Dungey
 */
public class UuidSnomedDbMapHandler implements UuidSnomedHandler {
    /** Class logger */
    private Logger logger = Logger.getLogger(this.getClass().getName());
    /** The currently mapped UUIDs. */
    private UuidSctidMapDb uuidSctidMapDb;
    /** The newly mapped UUIDs. Mapped by Namespace + Type then by UUID */
    private Map<String, Map<UUID, Long>> memoryUuidSctidMap;
    /** The next sequence for a given namespace and type.*/
    private Map<String, Long> nextSctSequenceMap;
    /** Maximum number of ids to cache before writing to DB. */
    private int MAX_CACHE_SIZE = 50000;

    /**
     * Create or reads the database.
     *
     * Sets up the <code>nextSctSequenceMap</code> and <code>memoryUuidSctidMap</code>
     *
     * @param databaseFile File
     * @throws IOException Reading the DB file
     * @throws SQLException Running and setting setup SQL statements
     * @throws ClassNotFoundException error creating the DB.
     */
    public UuidSnomedDbMapHandler(File databaseFile) throws IOException, SQLException, ClassNotFoundException {
        uuidSctidMapDb = new UuidSctidMapDb(databaseFile);

        nextSctSequenceMap = new HashMap<String, Long>();
        memoryUuidSctidMap = new HashMap<String, Map<UUID,Long>>();

        for (TYPE type : TYPE.values()) {
            for (NAMESPACE namespace : NAMESPACE.values()) {
                memoryUuidSctidMap.put(getNamespaceTypeKey(namespace, type),
                        new HashMap<UUID, Long>());

                nextSctSequenceMap.put(getNamespaceTypeKey(namespace, type),
                        uuidSctidMapDb.getSctSequenceId(namespace, type));
            }
        }
    }

    /**
     * If no mapping is found generate a new SctId and store in the memory map.
     *
     * Once the memory map is greater than <code>MAX_CACHE_SIZE</code> the map is written to the DB.
     *
     * @see org.dwfa.maven.sctid.UuidSnomedHandler#getWithGeneration(java.util.UUID, org.dwfa.maven.transform.SctIdGenerator.NAMESPACE, org.dwfa.maven.transform.SctIdGenerator.TYPE)
     */
    public Long getWithGeneration(UUID uuid, NAMESPACE namespace, TYPE type) throws Exception {
        Long sctID = getWithoutGeneration(uuid, namespace, type);

        if(sctID == null) {
            sctID = Long.valueOf(SctIdGenerator.generate(nextSctSequenceMap.get(getNamespaceTypeKey(namespace, type)) + 1, namespace, type));

            addMap(uuid, sctID, namespace, type);
        }

        return sctID;
    }


    public void addMap(UUID uuid, Long sctID, NAMESPACE namespace, TYPE type) throws Exception {
        memoryUuidSctidMap.get(getNamespaceTypeKey(namespace, type)).put(uuid, sctID);

        nextSctSequenceMap.put(getNamespaceTypeKey(namespace, type),
            Math.max(nextSctSequenceMap.get(getNamespaceTypeKey(namespace, type)),
                    getSctIdSequencePart(sctID, namespace, type)));
        
        if (memoryUuidSctidMap.get(getNamespaceTypeKey(namespace, type)).size() > MAX_CACHE_SIZE) {
            writeMaps();
        }
    }
    
    /**
     * If the UUID has not been mapped null is returned. Checks both the DB and memory maps.
     *
     * @see org.dwfa.maven.sctid.UuidSnomedHandler#getWithoutGeneration(java.util.UUID, org.dwfa.maven.transform.SctIdGenerator.NAMESPACE, org.dwfa.maven.transform.SctIdGenerator.TYPE)
     */
    public Long getWithoutGeneration(UUID uuid, NAMESPACE namespace, TYPE type) throws Exception {
        Long sctId = uuidSctidMapDb.getSctId(uuid);

        if(sctId == null){
            sctId = memoryUuidSctidMap.get(getNamespaceTypeKey(namespace, type)).get(uuid);
        }

        return sctId;
    }

    /**
     * Write the mapped UUIDs to the <code>uuidSctidMapDb</code>.
     *
     * @throws Exception if cannot write maps to the DB.
     */
    public void writeMaps() throws Exception{
        for (Map<UUID,Long> uuidMap : memoryUuidSctidMap.values()) {
            uuidSctidMapDb.addUUIDSctIdEntryList(uuidMap);
            uuidMap.clear();
        }
        logger.info("Committed memory map to DB");
    }

    /**
     * Gets the sequence part of the sctid using the namespace and type.
     *
     * @param sctId Long
     * @param namespace NAMESPACE
     * @param type TYPE
     * @return Long
     */
    private Long getSctIdSequencePart(Long sctId, NAMESPACE namespace, TYPE type){
        String sctIdStr = sctId.toString();

        return Long.valueOf(sctIdStr.substring(0, sctIdStr.length()
                - (namespace.getDigits().length() + type.getDigits().length() + 1)));
    }

    /**
     * Help function to get the map key. Simply concats the NAMESPACE and TYPE codes.
     *
     * @param namespace NAMESPACE
     * @param type TYPE
     * @return String
     */
    private String getNamespaceTypeKey(NAMESPACE namespace, TYPE type) {
        return namespace.getDigits() + type.getDigits();
    }

}
