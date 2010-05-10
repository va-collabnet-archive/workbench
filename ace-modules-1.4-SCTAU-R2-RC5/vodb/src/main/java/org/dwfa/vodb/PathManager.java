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
package org.dwfa.vodb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import org.dwfa.ace.api.BeanPropertyMap;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TerminologyHelper;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.ThinExtByRefPartProperty;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;

/**
 * Path management.
 * 
 * Defines methods for obtaining and modifying paths. Paths are now
 * stored/defined in
 * reference sets (extension by reference).
 * 
 * This implementation avoids the use of the redundant Path store and instead
 * marshals
 * to to the Extension store (indirectly).
 * 
 */
public class PathManager implements I_Manage<I_Path> {

    static Map<Integer, I_Path> pathCache = new WeakHashMap<Integer, I_Path>();

    protected int pathRefsetId;
    protected int pathOriginRefsetId;
    protected int pathConceptId;

    protected boolean autoCommit = false;

    protected RefsetHelper refsetHelper;

    private Logger logger = Logger.getLogger(PathManager.class.getName());

    public PathManager() throws TerminologyException {
        try {
            refsetHelper = new RefsetHelper();
            pathRefsetId = RefsetAuxiliary.Concept.REFSET_PATHS.localize().getNid();
            pathOriginRefsetId = RefsetAuxiliary.Concept.REFSET_PATH_ORIGINS.localize().getNid();
            pathConceptId = ArchitectonicAuxiliary.Concept.PATH.localize().getNid();

            int auxPathId = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.localize().getNid();
            refsetHelper.setEditPaths(new Path(auxPathId, null));

        } catch (Exception e) {
            throw new TerminologyException("Unable to initialise path management.", e);
        }
    }

    public boolean exists(int nid) throws TerminologyException {
        if (cacheContainsPath(nid)) {
            return true;
        }

        try {
            return refsetHelper.hasCurrentRefsetExtension(pathRefsetId, pathConceptId, new BeanPropertyMap().with(
                ThinExtByRefPartProperty.CONCEPT_ONE, nid));

        } catch (Exception e) {
            throw new TerminologyException("Unable to determine if path exists.", e);
        }
    }

    public I_Path get(int nid) throws PathNotExistsException, TerminologyException {
        I_Path path = cacheGet(nid);
        if (path == null) {
            if (exists(nid)) {
                path = new Path(nid, getPathOrigins(nid));
                cachePut(nid, path);
            } else {
                throw new PathNotExistsException("Path not found: " + TerminologyHelper.conceptToString(nid));
            }
        }

        return path;
    }

    public Set<I_Path> getAll() throws TerminologyException {
        try {
            HashSet<I_Path> result = new HashSet<I_Path>();
            for (I_ThinExtByRefPartConcept extPart : refsetHelper.<I_ThinExtByRefPartConcept> getAllCurrentRefsetExtensions(
                pathRefsetId, pathConceptId)) {
                result.add(get(extPart.getC1id()));
            }
            return result;

        } catch (Exception e) {
            throw new TerminologyException("Unable to retrieve all paths.", e);
        }
    }

    protected List<I_Position> getPathOrigins(int nid) throws TerminologyException {
        try {
            ArrayList<I_Position> result = new ArrayList<I_Position>();
            for (I_ThinExtByRefPartConceptInt extPart : refsetHelper.<I_ThinExtByRefPartConceptInt> getAllCurrentRefsetExtensions(
                pathOriginRefsetId, nid)) {
                result.add(new Position(extPart.getIntValue(), get(extPart.getC1id())));
            }
            return result;

        } catch (Exception e) {
            throw new TerminologyException("Unable to retrieve path origins.", e);
        }
    }

    /**
     * Add or update a path and all its origin positions
     * NOTE it will not automatically remove origins. This must be done
     * explicitly with {@link #removeOrigin(I_Path, I_Position)}.
     */
    public void write(final I_Path path) throws TerminologyException {
        try {
            // write path

            refsetHelper.newRefsetExtension(pathRefsetId, pathConceptId, I_ThinExtByRefPartConcept.class,
                new BeanPropertyMap().with(ThinExtByRefPartProperty.CONCEPT_ONE, path.getConceptId()));

            cachePut(path.getConceptId(), path);

            // write position

            for (I_Position origin : path.getOrigins()) {
                writeOrigin(path, origin);
            }

            logger.info("Wrote path : " + path);

            autoCommit();

        } catch (Exception e) {
            throw new TerminologyException("Unable to write path: " + path, e);
        }
    }

    /**
     * Set an origin on a path
     */
    public void writeOrigin(final I_Path path, final I_Position origin) throws TerminologyException {

        try {
            if (refsetHelper.hasCurrentRefsetExtension(pathOriginRefsetId, path.getConceptId(),
                new BeanPropertyMap().with(ThinExtByRefPartProperty.CONCEPT_ONE, origin.getPath().getConceptId()).with(
                    ThinExtByRefPartProperty.INTEGER_VALUE, origin.getVersion()))) {
                // Skip already exists
                return;
            }

            // Retire any positions that may exist that just have a different
            // version (time) point
            refsetHelper.retireRefsetExtension(pathOriginRefsetId, path.getConceptId(), new BeanPropertyMap().with(
                ThinExtByRefPartProperty.CONCEPT_ONE, origin.getPath().getConceptId()));

            // Create the new origin/position
            refsetHelper.newRefsetExtension(pathOriginRefsetId, path.getConceptId(),
                I_ThinExtByRefPartConceptInt.class, new BeanPropertyMap().with(ThinExtByRefPartProperty.CONCEPT_ONE,
                    origin.getPath().getConceptId()).with(ThinExtByRefPartProperty.INTEGER_VALUE, origin.getVersion()));

            logger.info("Wrote origin path : " + origin + " to path " + path);

            cachePut(path.getConceptId(), path);

            autoCommit();

        } catch (Exception e) {
            throw new TerminologyException("Unable to write path origin: " + origin + " to path " + path, e);
        }
    }

    public void removeOrigin(I_Path path, I_Position origin) throws TerminologyException {
        try {
            refsetHelper.retireRefsetExtension(pathOriginRefsetId, path.getConceptId(), new BeanPropertyMap().with(
                ThinExtByRefPartProperty.CONCEPT_ONE, origin.getPath().getConceptId()));

            logger.info("Removed origin path : " + origin + " from path " + path);

            cachePut(path.getConceptId(), path);

            autoCommit();

        } catch (Exception e) {
            throw new TerminologyException("Unable to remove path origin: " + origin + " from path " + path, e);
        }
    }

    private I_Path cacheGet(int nid) {
        synchronized (pathCache) {
            return pathCache.get(nid);
        }
    }

    private boolean cacheContainsPath(int nid) {
        synchronized (pathCache) {
            return pathCache.containsKey(nid);
        }
    }

    private void cachePut(int nid, I_Path path) {
        synchronized (pathCache) {
            pathCache.put(nid, path);
        }
    }

    protected void autoCommit() throws Exception {
        if (autoCommit) {
            LocalVersionedTerminology.get().commit();
        }
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }
}
