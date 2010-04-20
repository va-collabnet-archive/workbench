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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.TerminologyHelper;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.I_Manage;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.cid.CidMember;
import org.ihtsdo.concept.component.refsetmember.cidInt.CidIntMember;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.bdb.computer.refset.RefsetHelper;
import org.ihtsdo.etypes.EConcept;

/**
 * Path management.
 * 
 * Defines methods for obtaining and modifying paths. Paths are now
 * stored/defined in reference sets (extension by reference).
 * 
 * This implementation avoids the use of the redundant Path store and instead
 * marshals to to the Extension store (indirectly).
 * 
 */
public class BdbPathManager implements I_Manage<I_Path> {

	protected Path editPath;
	
	ConcurrentHashMap<Integer, Path> pathMap;
	
	private class RefsetHelperGetter {
		ConcurrentHashMap<I_ConfigAceFrame, RefsetHelper>  helperMap = 
			new ConcurrentHashMap<I_ConfigAceFrame, RefsetHelper>(7);
		
		private RefsetHelper get(I_ConfigAceFrame frameConfig) {
			assert frameConfig != null: "frameConfig cannot be null";
			RefsetHelper helper = helperMap.get(frameConfig);
			if (helper == null) {
				Set<Entry<I_ConfigAceFrame, RefsetHelper>> entries = helperMap.entrySet();
				while (entries.size() >= 6) {
					Entry<I_ConfigAceFrame, RefsetHelper> looser = null;
					for (Entry<I_ConfigAceFrame, RefsetHelper> entry: entries) {
						if (looser == null) {
							looser = entry;
						} else {
							if (entry.getValue().getLastAccess() < looser.getValue().getLastAccess()) {
								looser = entry;
							}
						}
					}
					helperMap.remove(looser.getKey());
					entries = helperMap.entrySet();
				}
				helper = new RefsetHelper(frameConfig);
				helperMap.put(frameConfig, helper);
			}
			return helper; 
		}
	}

	private static Logger logger = Logger.getLogger(BdbPathManager.class
			.getName());

	private RefsetHelperGetter helperGetter = new RefsetHelperGetter();
	
	public BdbPathManager() throws TerminologyException {
		try {
			editPath = new Path(ReferenceConcepts.TERM_AUXILIARY_PATH.getNid(), null);
			setupPathMap();
		} catch (Exception e) {
			throw new TerminologyException(
					"Unable to initialise path management.", e);
		}
	}

	public boolean exists(int cNid) throws TerminologyException, IOException {
	    if (pathMap.containsKey(cNid)) {
	        return true;
	    }
	    return getFromDisk(cNid) != null;
	}

	public I_Path get(int nid) throws PathNotExistsException,
			TerminologyException, IOException {
		if (exists(nid)) {
			return pathMap.get(nid);
		} else {
		    I_Path p = getFromDisk(nid);
		    if (p != null) {
		        return p;
		    }
		}
        throw new PathNotExistsException("Path not found: "
            + TerminologyHelper.conceptToString(nid));
	}
	
	@SuppressWarnings("unchecked")
	public Set<Integer> getPathNids() throws TerminologyException {
		try {
			HashSet<Integer> result = new HashSet<Integer>();
			Concept pathRefsetConcept = Bdb.getConceptDb().getConcept(
					ReferenceConcepts.REFSET_PATHS.getNid());

			for (RefsetMember extPart : pathRefsetConcept.getExtensions()) {
			    I_ExtendByRefPartCid conceptExtension = (I_ExtendByRefPartCid) extPart;
				result.add(conceptExtension.getC1id());
			}
			return result;

		} catch (Exception e) {
			throw new TerminologyException("Unable to retrieve all paths.", e);
		}
	}

	public Set<I_Path> getAll() throws TerminologyException {
	    return new HashSet<I_Path>(pathMap.values());
	}
	
	@SuppressWarnings("unchecked")
    private void setupPathMap() throws IOException {
        if (pathMap == null) {
            pathMap = new ConcurrentHashMap<Integer, Path>();

            try {
                Concept pathRefsetConcept = Bdb.getConceptDb().getConcept(
                        ReferenceConcepts.REFSET_PATHS.getNid());

                for (RefsetMember extPart : pathRefsetConcept.getExtensions()) {
                    CidMember conceptExtension = (CidMember) extPart;
                    int pathId = conceptExtension.getC1Nid();
                    pathMap.put(pathId, new Path(pathId, getPathOriginsFromDb(pathId)));
                }


            } catch (Exception e) {
                throw new IOException("Unable to retrieve all paths.", e);
            }
        }
	}
	
	@SuppressWarnings("unchecked")
    private Path getFromDisk(int cNid) throws IOException {
        try {
            Concept pathRefsetConcept = Bdb.getConceptDb().getConcept(
                    ReferenceConcepts.REFSET_PATHS.getNid());

            for (RefsetMember extPart : pathRefsetConcept.getExtensions()) {
                CidMember conceptExtension = (CidMember) extPart;
                int pathId = conceptExtension.getC1Nid();
                if (pathId == cNid) {
                    pathMap.put(pathId, new Path(pathId, getPathOriginsFromDb(pathId)));
                    return pathMap.get(cNid);
                }
            }

        } catch (Exception e) {
            throw new IOException("Unable to retrieve all paths.", e);
        }
        return null;
	}
	
	public List<I_Position> getAllPathOrigins(int nid) throws TerminologyException, IOException {
        Path p = pathMap.get(nid);
        if (p == null) {
            p = getFromDisk(nid);
        }
        return new ArrayList<I_Position>(p.getInheritedOrigins());
	}

    
    public List<I_Position> getPathOrigins(int nid) throws TerminologyException {
        try {
            Path p = pathMap.get(nid);
            return p.getOrigins();
        } catch (Exception e) {
            throw new TerminologyException("Unable to retrieve path children.", e);
        }
    }

	
	
	@SuppressWarnings("unchecked")
	private List<I_Position> getPathOriginsFromDb(int nid) throws TerminologyException {
		return getPathOriginsWithDepth(nid, 0);
	}

    private List<I_Position> getPathOriginsWithDepth(int nid, int depth) throws TerminologyException {
        try {
			ArrayList<I_Position> result = new ArrayList<I_Position>();
			Concept pathConcept = Bdb.getConceptDb().getConcept(nid);
			for (RefsetMember extPart : pathConcept
					.getConceptExtensions(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid())) {
				assert extPart != null : "No concept extension for: "
						+ pathConcept.getNid();
				CidIntMember conceptExtension = (CidIntMember) extPart;
				if (conceptExtension.getC1Nid() == nid) {
					AceLog.getAppLog().severe(
							"Self-referencing origin in path: "
									+ pathConcept.getDescriptions().iterator().next().getFirstTuple());
				} else {
				    if (pathMap.containsKey(conceptExtension.getC1Nid())) {
	                    result.add(new Position(conceptExtension.getIntValue(),
	                        pathMap.get(conceptExtension.getC1Nid())));
				    } else {
			            if (depth > 20) {
			                AceLog.getAppLog().alertAndLogException(new Exception(
			                    "Depth limit exceeded. Path concept: \n" +
			                    pathConcept.toLongString() + 
			                    "\n\n extensionPart: \n\n" + 
			                    extPart.toString()));
			            }
	                    result.add(new Position(conceptExtension.getIntValue(),
                            new Path(conceptExtension.getC1Nid(),  
                                getPathOriginsWithDepth(conceptExtension.getC1Nid(), depth + 1))));
				    }
				}
			}
			return result;
		} catch (Exception e) {
			throw new TerminologyException("Unable to retrieve path origins.", e);
		}
    }

	/**
	 * Add or update a path and all its origin positions NOTE it will not
	 * automatically remove origins. This must be done explicitly with
	 * {@link #removeOrigin(I_Path, I_Position)}.
	 */
	public void write(final I_Path path, I_ConfigAceFrame config)
			throws TerminologyException {
		try {
			// write path

			RefsetPropertyMap propMap = new RefsetPropertyMap().with(
					RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, path
							.getConceptId());
			helperGetter.get(config).newRefsetExtension(ReferenceConcepts.REFSET_PATHS.getNid(), ReferenceConcepts.PATH.getNid(),
					EConcept.REFSET_TYPES.CID, propMap, config);
			BdbCommitManager.addUncommittedNoChecks(Concept.get(ReferenceConcepts.REFSET_PATHS.getNid()));

			// write position

			for (I_Position origin : path.getOrigins()) {
				writeOrigin(path, origin, config);
			}
			pathMap.put(path.getConceptId(), (Path) path);
			logger.info("Wrote path : " + path);
		} catch (Exception e) {
			throw new TerminologyException("Unable to write path: " + path, e);
		}
	}

	/**
	 * Set an origin on a path
	 */
	public void writeOrigin(final I_Path path, final I_Position origin, I_ConfigAceFrame config) 
		throws TerminologyException {
		RefsetHelper refsetHelper = helperGetter.get(config);
		try {
			RefsetPropertyMap propMap = new RefsetPropertyMap().with(
					RefsetPropertyMap.REFSET_PROPERTY.CID_ONE,
					origin.getPath().getConceptId()).with(
					RefsetPropertyMap.REFSET_PROPERTY.INTEGER_VALUE,
					origin.getVersion());
			if (refsetHelper.hasCurrentRefsetExtension(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid(), path
					.getConceptId(), propMap)) {
				// Skip already exists
				return;
			}

			// Retire any positions that may exist that just have a different
			// version (time) point

			propMap = new RefsetPropertyMap().with(
					RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, origin
							.getPath().getConceptId());
			refsetHelper.retireRefsetExtension(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid(), path
					.getConceptId(), propMap);

			propMap = new RefsetPropertyMap().with(
					RefsetPropertyMap.REFSET_PROPERTY.CID_ONE,
					origin.getPath().getConceptId()).with(
					RefsetPropertyMap.REFSET_PROPERTY.INTEGER_VALUE,
					origin.getVersion());
			// Create the new origin/position
			refsetHelper.newRefsetExtension(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid(), path
					.getConceptId(), EConcept.REFSET_TYPES.CID_INT, propMap,
					config);
			BdbCommitManager.addUncommittedNoChecks(Concept.get(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid()));
            pathMap.put(path.getConceptId(), (Path) path);
			logger.info("Wrote origin path : " + origin + " to path " + path);
		} catch (Exception e) {
			throw new TerminologyException("Unable to write path origin: "
					+ origin + " to path " + path, e);
		}
	}

	public void removeOrigin(I_Path path, I_Position origin, I_ConfigAceFrame config)
			throws TerminologyException {
		try {
			RefsetHelper refsetHelper = helperGetter.get(config);
			refsetHelper.retireRefsetExtension(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid(), path
					.getConceptId(), new RefsetPropertyMap().with(
					RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, origin
							.getPath().getConceptId()));
			path.getOrigins().remove(origin);
            pathMap.put(path.getConceptId(), (Path) path);

			logger.info("Removed origin path : " + origin + " from path "
					+ path);
		} catch (Exception e) {
			throw new TerminologyException("Unable to remove path origin: "
					+ origin + " from path " + path, e);
		}
	}

}
