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
package org.dwfa.maven.sctid.transform;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.maven.sctid.UuidSnomedDbMapHandler;
import org.dwfa.maven.transform.AbstractTransform;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

/**
 * Using <code>UuidSnomedDbMapHandler</code> manage the generated sctid for
 * types and namespaces.
 *
 * <code>buildDirector</code> is not used but is matained for backward
 * compatibility and to
 * fullfill the contract I_ReadAndTransform
 * DB file is read/created in the source as
 * <code>sourceDirectory/UuidSnomedDb.db</code>
 *
 * @author Ean Dungey
 */
public abstract class UuidToSctIdWithGeneration extends AbstractTransform implements I_ReadAndTransform {
    private static Logger logger = Logger.getLogger(UuidToSctIdWithGeneration.class.getName());
    /** Static DB Map handler. */
    static UuidSnomedDbMapHandler map;

    /**
     * Setup the handler for this Transform using the Transform source directory
     * value.
     *
     * @param transform Transform
     */
    @Override
    public void setupImpl(Transform transformer) throws IOException, ClassNotFoundException {
        try {
            initMap();
        } catch (Exception e) {
            throw new RuntimeException("Cannot load DB", e);
        }
    }

    public void setupImpl() throws IOException, ClassNotFoundException {
        setupImpl(null);
    }

    /**
     * Creates a new instance of <code>map</code> if it is null else does
     * nothing.
     *
     * @param idGeneratedDir NOT USED
     * @param sourceDirectoryToSet location for the DB file
     * @throws IOException reading files.
     * @throws ClassNotFoundException error opening DB.
     * @throws SQLException creating DB object
     */
    private static synchronized void initMap() throws Exception,
            ClassNotFoundException, SQLException {
        logger.info("initMap");
        if (map == null) {
            map = UuidSnomedDbMapHandler.getInstance();
            logger.info("new UuidSnomedDbMapHandler");
        }
    }

    /**
     * Sets the last Transform values and returns a new or existing SctId.
     *
     * @param uuidStr String
     * @param namespace NAMESPACE
     * @return SctId as a String
     * @throws Exception reading DB or creating the new SctId
     */
    public String transform(String uuidStr, NAMESPACE namespace, PROJECT project) throws Exception {
        UUID uuid = UUID.fromString(uuidStr);
        return setLastTransform(Long.toString(map.getWithGeneration(uuid, namespace, getType(), project)));
    }

    /**
     * Similar to the transform method, however this method will process a list
     * of UUIDs
     * and find a matching sctid.
     *
     * If no match a new sctid is generated for the first uuid
     *
     * @param uuids
     * @param namespace
     * @return
     * @throws Exception
     */
    public String transform(List<UUID> uuids, NAMESPACE namespace, PROJECT project) throws Exception {
        Long result = null;
        for (UUID uuid : uuids) {
            result = map.getWithoutGeneration(uuid, namespace, getType());
            if (result != null) {
                break;
            }
        }

        if (result == null) {
            result = map.getWithGeneration(uuids.get(0), namespace, getType(), project);
        }

        return result.toString();
    }

    /**
     * Namespace are no longer configured using file names so this is not
     * supported
     *
     * @see String transform(String uuidStr, NAMESPACE namespace)
     */
    public String transform(String input) throws Exception {
        return transform(input, getNamespace(), null);
    }

    /**
     * Gets the mapped SctId for the UUID.
     *
     * @param uuid UUID
     * @param namespace NAMESPACE
     * @return SctId Long null is returned if no current mapping exists.
     * @throws Exception reading DB
     */
    public Long getMappedId(UUID uuid, NAMESPACE namespace) throws Exception {
        return map.getWithoutGeneration(uuid, namespace, getType());
    }

    /**
     * Writes out all the new mapped SctId to the DB.
     *
     * @throws Exception writing to the DB
     */
    public void cleanup(Transform transformer) throws Exception {
        if (map != null) {
            map.writeMaps();
            map.close();
            map = null;
        }
    }

    /**
     * The Type implemented.
     *
     * @return
     */
    protected abstract TYPE getType();
}
