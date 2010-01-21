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
package org.ihtsdo.db.bdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TerminologyHelper;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.I_Manage;
import org.dwfa.vodb.PathManager;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cid.CidMember;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt.CidIntMember;

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
public class BdbPathManager implements I_Manage<I_Path> {

    protected int pathRefsetId;
    protected int pathOriginRefsetId;
    protected int pathConceptId;

    protected boolean autoCommit = false;

    private Logger logger = Logger.getLogger(PathManager.class.getName());
    
    protected Path editPath;

    public BdbPathManager() throws TerminologyException {
        try {
            pathRefsetId = RefsetAuxiliary.Concept.REFSET_PATHS.localize().getNid();
            pathOriginRefsetId = RefsetAuxiliary.Concept.REFSET_PATH_ORIGINS.localize().getNid();
            pathConceptId = ArchitectonicAuxiliary.Concept.PATH.localize().getNid();

            int auxPathId = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.localize().getNid();
            editPath = new Path(auxPathId, null);

        } catch (Exception e) {
            throw new TerminologyException("Unable to initialise path management.", e);
        }
    }

    public boolean exists(int cNid) throws TerminologyException {
        try {
        	 return Terms.get().hasConcept(cNid);
        } catch (Exception e) {
            throw new TerminologyException("Unable to determine if path exists.", e);
        }
    }

    public I_Path get(int nid) throws PathNotExistsException, TerminologyException {
        if (exists(nid)) {
            return new Path(nid, getPathOrigins(nid));
        } else {
            throw new PathNotExistsException("Path not found: " + TerminologyHelper.conceptToString(nid));
        }
    }

    public Set<I_Path> getAll() throws TerminologyException {
        try {
            HashSet<I_Path> result = new HashSet<I_Path>();
            Concept pathRefsetConcept = Bdb.getConceptDb().getConcept(pathRefsetId);
            
            for (RefsetMember<?, ?> extPart : pathRefsetConcept.getExtensions()) {
            	CidMember conceptExtension = (CidMember) extPart;
                	result.add(get(conceptExtension.getC1Nid()));
            }
            return result;

        } catch (Exception e) {
            throw new TerminologyException("Unable to retrieve all paths.", e);
        }
    }

    protected List<I_Position> getPathOrigins(int nid) throws TerminologyException {
        try {
            ArrayList<I_Position> result = new ArrayList<I_Position>();
            Concept pathConcept = Bdb.getConceptDb().getConcept(nid);
            for (RefsetMember<?, ?> extPart : pathConcept.getConceptExtensions(pathOriginRefsetId)) {
            	CidIntMember conceptExtension = (CidIntMember) extPart;
                result.add(new Position(conceptExtension.getIntValue(), get(conceptExtension.getC1Nid())));
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
            throw new UnsupportedOperationException();

        } catch (Exception e) {
            throw new TerminologyException("Unable to write path: " + path, e);
        }
    }

    /**
     * Set an origin on a path
     */
    public void writeOrigin(final I_Path path, final I_Position origin) throws TerminologyException {

        try {
            throw new UnsupportedOperationException();
        } catch (Exception e) {
            throw new TerminologyException("Unable to write path origin: " + origin + " to path " + path, e);
        }
    }

    public void removeOrigin(I_Path path, I_Position origin) throws TerminologyException {
        try {
           throw new UnsupportedOperationException();
        } catch (Exception e) {
            throw new TerminologyException("Unable to remove path origin: " + origin + " from path " + path, e);
        }
    }

    protected void autoCommit() throws Exception {
        if (autoCommit) {
            Terms.get().commit();
        }
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }
}
