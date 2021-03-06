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

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TerminologyHelper;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.vodb.I_Manage;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.cid.CidMember;
import org.ihtsdo.concept.component.refsetmember.cidInt.CidIntMember;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.concept.component.refsetmember.str.StrMember;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

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
public class BdbPathManager implements I_Manage<PathBI> {
   private static final Logger   logger = Logger.getLogger(BdbPathManager.class.getName());
   private static Lock           l      = new ReentrantLock();
   private static BdbPathManager singleton;

   //~--- fields --------------------------------------------------------------

   protected Path                   editPath;
   ConcurrentHashMap<Integer, Path> pathMap;
   private Concept                  pathRefsetConcept;
   private Concept                  refsetPathOriginsConcept;

   //~--- constructors --------------------------------------------------------

   private BdbPathManager() throws IOException {
      try {
         editPath = new Path(ReferenceConcepts.TERM_AUXILIARY_PATH.getNid(), null);
         setupPathMap();
      } catch (Exception e) {
         throw new IOException("Unable to initialise path management.", e);
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean exists(int cNid) throws IOException {
      if (pathMap.containsKey(cNid)) {
         return true;
      }

      return getFromDisk(cNid) != null;
   }

   public boolean existsFast(int cNid) throws IOException {
      if (pathMap.containsKey(cNid)) {
         return true;
      }

      return false;
   }

   public synchronized void resetPathMap() throws IOException {
      pathMap = null;
      setupPathMap();
   }

   @SuppressWarnings("unchecked")
   private void setupPathMap() throws IOException {
      if (pathMap == null) {
         pathMap = new ConcurrentHashMap<Integer, Path>();

         try {
            getPathRefsetConcept();

            for (RefsetMember extPart : getPathRefsetConcept().getExtensions()) {
               CidMember conceptExtension = (CidMember) extPart;
               int       pathId           = conceptExtension.getC1Nid();

               pathMap.put(pathId, new Path(pathId, getPathOriginsFromDb(pathId)));
            }
         } catch (Exception e) {
            throw new IOException("Unable to retrieve all paths.", e);
         }
      }
   }

   /**
    * Add or update a path and all its origin positions NOTE it will not
    * automatically remove origins. This must be done explicitly with {@link #removeOrigin(PathBI, I_Position)}.
    */
   @Override
   public void write(final PathBI path, I_ConfigAceFrame config) throws IOException {
      try {
          // write path
         RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                 ReferenceConcepts.PATH.getNid(),
                 ReferenceConcepts.REFSET_PATHS.getNid());
         refexBp.put(RefexCAB.RefexProperty.CNID1, path.getConceptNid());
         refexBp.setMemberUuid(refexBp.computeMemberContentUuid());
         TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
         RefexChronicleBI<?> member = builder.construct(refexBp);
         Concept pathRefConcept = getPathRefsetConcept();

         BdbCommitManager.addUncommittedNoChecks(pathRefConcept);

         // write position

         for (PositionBI origin : path.getOrigins()) {
            writeOrigin(path, origin, config);
         }

         pathMap.put(path.getConceptNid(), (Path) path);
         logger.log(Level.INFO, "Wrote path : {0}", path);
      } catch (Exception e) {
         throw new IOException("Unable to write path: " + path, e);
      }
   }

   /**
    * Set an origin on a path
    */
   public void writeOrigin(final PathBI path, final PositionBI origin, I_ConfigAceFrame config)
           throws IOException {
      assert path.getOrigins().contains(origin) : "Must add origin: " + origin + " before writing: " + path;

      try {
          TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
          ConceptVersionBI refsetConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), ReferenceConcepts.REFSET_PATH_ORIGINS.getNid());
          // Retire any positions that may exist that just have a different
          // version (time) point
          ConceptVersionBI pathConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), path.getConceptNid());
          Collection<? extends RefexVersionBI<?>> refexMembersActive = pathConcept.getRefexMembersActive(config.getViewCoordinate(), refsetConcept.getConceptNid());
          for(RefexVersionBI member : refexMembersActive){
                  RefexCAB retireBp = member.makeBlueprint(config.getViewCoordinate());
                  retireBp.setRetired();
                  retireBp.setComponentUuidNoRecompute(member.getPrimUuid());
                  builder.construct(retireBp);
          }
         // Create the new origin/position
         RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_INT,
                 path.getConceptNid(),
                 refsetConcept.getNid());
         memberBp.put(RefexCAB.RefexProperty.CNID1, origin.getPath().getConceptNid());
         memberBp.put(RefexCAB.RefexProperty.INTEGER1, origin.getVersion());
         memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
         RefexChronicleBI<?> member = builder.construct(memberBp);

         Concept pathOriginRefConcept = getRefsetPathOriginsConcept();

         BdbCommitManager.addUncommittedNoChecks(pathOriginRefConcept);
         pathMap.put(path.getConceptNid(), (Path) path);
      } catch (Exception e) {
         throw new IOException("Unable to write path origin: " + origin + " to path " + path, e);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public static BdbPathManager get() {
      if (singleton == null) {
         l.lock();

         try {
            if (singleton == null) {
               try {
                  BdbPathManager mgr = new BdbPathManager();

                  singleton = mgr;
               } catch (IOException ex) {
                  throw new RuntimeException(ex);
               }
            }
         } finally {
            l.unlock();
         }
      }

      return singleton;
   }

   public static void reset() {
      if (singleton != null) {
         l.lock();
         try {
         singleton = null;
         } finally {
            l.unlock();
         }
      }
   }

   @Override
   public PathBI get(int nid) throws IOException {
      if (exists(nid)) {
         return pathMap.get(nid);
      } else {
         PathBI p = getFromDisk(nid);

         if (p != null) {
            return p;
         }
      }

      AceLog.getAppLog().alertAndLogException(new PathNotExistsException("Path not found: "
              + TerminologyHelper.conceptToString(nid) + " uuid: "
              + Bdb.getUuidsToNidMap().getUuidsForNid(nid)));
      pathMap.put(
          nid,
          pathMap.get(
             Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids())));

      return pathMap.get(nid);
   }

   @Override
   public Set<PathBI> getAll() {
      return new HashSet<PathBI>(pathMap.values());
   }

   public List<PositionBI> getAllPathOrigins(int nid) throws IOException {
      Path p = pathMap.get(nid);

      if (p == null) {
         p = getFromDisk(nid);
      }

      return new ArrayList<PositionBI>(p.getInheritedOrigins());
   }

   @SuppressWarnings("unchecked")
   private Path getFromDisk(int cNid) throws IOException {
      try {
          for (RefsetMember extPart : getPathRefsetConcept().getExtensions()) {
              if (!StrMember.class.isAssignableFrom(extPart.getClass())) {
                  CidMember conceptExtension = (CidMember) extPart;
                  int pathId = conceptExtension.getC1Nid();
                  if (pathId == cNid) {
                      pathMap.put(pathId, new Path(pathId, getPathOriginsFromDb(pathId)));
                      return pathMap.get(cNid);
                  }
              }
          }
      } catch (Exception e) {
         throw new IOException("Unable to retrieve all paths.", e);
      }

      return null;
   }

   public List<Path> getPathChildren(int nid) {
      List<Path> children = new ArrayList<Path>();

      for (Path p : pathMap.values()) {
         if (p.getOrigins() != null) {
            for (PositionBI origin : p.getOrigins()) {
               if (origin.getPath().getConceptNid() == nid) {
                  children.add(p);
               }
            }
         }
      }

      return children;
   }

   @SuppressWarnings("unchecked")
   public Set<Integer> getPathNids() throws IOException {
      try {
         HashSet<Integer> result = new HashSet<Integer>();

         for (RefsetMember extPart : getPathRefsetConcept().getExtensions()) {
            I_ExtendByRefPartCid conceptExtension = (I_ExtendByRefPartCid) extPart;

            result.add(conceptExtension.getC1id());
         }

         return result;
      } catch (Exception e) {
         throw new IOException("Unable to retrieve all paths.", e);
      }
   }

   public Collection<? extends PositionBI> getPathOrigins(int nid) throws IOException {
      try {
         Path p = pathMap.get(nid);

         return p.getOrigins();
      } catch (Exception e) {
         throw new IOException("Unable to retrieve path children.", e);
      }
   }

   private List<I_Position> getPathOriginsFromDb(int nid) throws IOException {
      return getPathOriginsWithDepth(nid, 0);
   }

   private List<I_Position> getPathOriginsWithDepth(int nid, int depth) throws IOException {
      try {
         ArrayList<I_Position> result      = new ArrayList<I_Position>();
         Concept               pathConcept = Bdb.getConceptDb().getConcept(nid);

         for (I_ExtendByRef extPart :
                 Terms.get().getRefsetExtensionsForComponent(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid(),
                    pathConcept.getNid())) {
            if (extPart == null) {
               AceLog.getAppLog().alertAndLogException(new Exception("Null path origins for: "
                       + pathConcept.toLongString() + "\n\nin refset: \n\n"
                       + getRefsetPathOriginsConcept().toLongString()));
            } else {
               CidIntMember conceptExtension = (CidIntMember) extPart;

               if (conceptExtension.getC1Nid() == nid) {
                  AceLog.getAppLog().severe(
                      "Self-referencing origin in path: "
                      + pathConcept.getDescs().iterator().next().getFirstTuple());
               } else {
                  if (pathMap.containsKey(conceptExtension.getC1Nid())) {
                     result.add(new Position(conceptExtension.getIntValue(),
                                             pathMap.get(conceptExtension.getC1Nid())));
                  } else {
                     if (depth > 40) {
                        AceLog.getAppLog().alertAndLogException(
                            new Exception(
                                "\n\n****************************************\nDepth limit exceeded. Path concept: \n"
                                + pathConcept.toLongString() + "\n\n extensionPart: \n\n"
                                + extPart.toString() + "\n\n origin refset: \n\n"
                                + Concept.get(extPart.getRefsetId()).toLongString()
                                + "\n-------------------------------------------\n\n"));
                     } else {
                        result.add(new Position(conceptExtension.getIntValue(),
                                                new Path(conceptExtension.getC1Nid(),
                                                         getPathOriginsWithDepth(conceptExtension.getC1Nid(),
                                                            depth + 1))));
                     }
                  }
               }
            }
         }

         return result;
      } catch (Exception e) {
         throw new IOException("Unable to retrieve path origins.", e);
      }
   }

   private Concept getPathRefsetConcept() throws IOException {
      if (pathRefsetConcept == null) {
         pathRefsetConcept = Bdb.getConceptDb().getConcept(ReferenceConcepts.REFSET_PATHS.getNid());
      }

      return pathRefsetConcept;
   }

   private Concept getRefsetPathOriginsConcept() throws IOException {
      if (this.refsetPathOriginsConcept == null) {
         this.refsetPathOriginsConcept = Concept.get(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid());
      }

      return refsetPathOriginsConcept;
   }

   public boolean hasPath(int nid) throws IOException {
      if (exists(nid)) {
         return true;
      } else {
         PathBI p = getFromDisk(nid);

         if (p != null) {
            return true;
         }
      }

      return false;
   }
}
