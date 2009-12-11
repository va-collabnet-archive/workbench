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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.maven.sctid.UuidSnomedDbMapHandler;
import org.dwfa.maven.transform.AbstractTransform;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

/**
 * Using <code>UuidSnomedDbMapHandler</code> manage the generated sctid for
 * types and namespaces.
 *
 * <code>buildDirector</code> is not used but is matained for backward compatibility and to
 * fullfill the contract I_ReadAndTransform
 * DB file is read/created in the source as
 * <code>sourceDirectory/UuidSnomedDb.db</code>
 *
 * @author Ean Dungey
 */
public abstract class UuidToSctIdWithGeneration extends AbstractTransform implements I_ReadAndTransform {

    /** Directory for DB file. */
    private File sourceDirectory = null;
    /** Static DB Map handler. */
    static UuidSnomedDbMapHandler map;

    /**
     * Setup the handler for this Transform using the Transform source directory value.
     *
     * @param transform Transform
     */
    public void setupImpl(Transform transformer) throws IOException, ClassNotFoundException {
        setupImpl(transformer.getBuildDirectory(), transformer.getSourceDirectory());
    }

    /**
     * Sets up the <code>map</code> for the source directory.
     *
     * NB the map is static so the first Transform source directory is used to read/create the DB file.
     *
     * @param buildDirectory NOT USED
     * @param sourceDirectoryToSet location for the DB file
     * @throws IOException reading files.
     * @throws ClassNotFoundException error opening DB.
     */
    public void setupImpl(File buildDirectory, File sourceDirectoryToSet) throws IOException, ClassNotFoundException {
        sourceDirectory = sourceDirectoryToSet;

        try {
            initMap(null, sourceDirectory);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot load DB", e);
        }
    }

    /**
     * Creates a new instance of <code>map</code> if it is null else does nothing.
     *
     * @param idGeneratedDir NOT USED
     * @param sourceDirectoryToSet location for the DB file
     * @throws IOException reading files.
     * @throws ClassNotFoundException error opening DB.
     * @throws SQLException creating DB object
     */
    private static synchronized void initMap(File idGeneratedDir, File sourceDirectory) throws IOException, ClassNotFoundException, SQLException {
        if (map == null) {
            map = new UuidSnomedDbMapHandler(sourceDirectory);
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
    public String transform(String uuidStr, NAMESPACE namespace) throws Exception {
        UUID uuid = UUID.fromString(uuidStr);
        return setLastTransform(Long.toString(map.getWithGeneration(uuid, namespace, getType())));
    }

    /**
     * Similar to the transform method, however this method will process a list of UUIDs
     * and add all of the UUIDs to the map for the new SCTID
     *
     * @param uuids
     * @param namespace
     * @return
     * @throws Exception
     */
    public String transform(List<UUID> uuids, NAMESPACE namespace) throws Exception {
        Long result = null;
        for (UUID uuid : uuids) {
            result = map.getWithoutGeneration(uuid, namespace, getType());
            if (result != null) {
                break;
            }
        }

        for (UUID uuid : uuids) {
            if (result == null) {
                result = map.getWithGeneration(uuid, namespace, getType());
            } else {
                map.addMap(uuid, result, namespace, getType());
            }
        }

        return result.toString();
    }

    /**
     * Namespace are no longer configured using file names so this is not supported
     *
     * @see String transform(String uuidStr, NAMESPACE namespace)
     */
    public String transform(String input) throws Exception {
        return transform(input, getNamespace());
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
        }
    }

    /**
     * The Type implemented.
     * @return
     */
    protected abstract TYPE getType();
}
