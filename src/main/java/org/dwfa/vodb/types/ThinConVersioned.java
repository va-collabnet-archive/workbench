package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinConVersioned implements I_ConceptAttributeVersioned {
   private int conId;

   private List<I_ConceptAttributePart> versions;

   public ThinConVersioned(int conId, int count) {
      super();
      this.conId = conId;
      this.versions = new ArrayList<I_ConceptAttributePart>(count);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#addVersion(org.dwfa.vodb.types.ThinConPart)
    */
   public boolean addVersion(I_ConceptAttributePart part) {
      int index = versions.size() - 1;
      if (index == -1) {
         return versions.add(part);
      } else if ((index >= 0) && (versions.get(index).hasNewData(part))) {
         return versions.add(part);
      }
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getVersions()
    */
   public List<I_ConceptAttributePart> getVersions() {
      return versions;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#versionCount()
    */
   public int versionCount() {
      return versions.size();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getConId()
    */
   public int getConId() {
      return conId;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTuples()
    */
   public List<I_ConceptAttributeTuple> getTuples() {
      List<I_ConceptAttributeTuple> tuples = new ArrayList<I_ConceptAttributeTuple>();
      for (I_ConceptAttributePart p : versions) {
         tuples.add(new ThinConTuple(this, p));
      }
      return tuples;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
    */
   public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
      conId = jarToDbNativeMap.get(conId);
      for (I_ConceptAttributePart part : versions) {
         ((I_ConceptAttributeVersioned) part).convertIds(jarToDbNativeMap);
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#merge(org.dwfa.vodb.types.ThinConVersioned)
    */
   public boolean merge(I_ConceptAttributeVersioned jarCon) {
      HashSet<I_ConceptAttributePart> versionSet = new HashSet<I_ConceptAttributePart>(versions);
      boolean changed = false;
      for (I_ConceptAttributePart jarPart : jarCon.getVersions()) {
         if (!versionSet.contains(jarPart)) {
            changed = true;
            versions.add((ThinConPart) jarPart);
         }
      }
      return changed;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTimePathSet()
    */
   public Set<TimePathId> getTimePathSet() {
      Set<TimePathId> tpSet = new HashSet<TimePathId>();
      for (I_ConceptAttributePart p : versions) {
         tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
      }
      return tpSet;
   }

   public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<I_ConceptAttributeTuple> returnTuples) {
      addTuples(allowedStatus, positions, returnTuples, true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#addTuples(org.dwfa.ace.IntSet,
    *      java.util.Set, java.util.List)
    */
   public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<I_ConceptAttributeTuple> returnTuples,
         boolean addUncommitted) {
      Set<ThinConPart> uncommittedParts = new HashSet<ThinConPart>();
      if (positions == null) {
         List<ThinConPart> addedParts = new ArrayList<ThinConPart>();
         Set<ThinConPart> rejectedParts = new HashSet<ThinConPart>();
         for (I_ConceptAttributePart part : versions) {
            if (part.getVersion() == Integer.MAX_VALUE) {
               uncommittedParts.add((ThinConPart) part);
            } else {
               if ((allowedStatus != null) && (!allowedStatus.contains(part.getConceptStatus()))) {
                  rejectedParts.add((ThinConPart) part);
                  continue;
               }
               addedParts.add((ThinConPart) part);
            }
         }
         for (I_ConceptAttributePart part : addedParts) {
            boolean addPart = true;
            for (I_ConceptAttributePart reject : rejectedParts) {
               if ((part.getVersion() <= reject.getVersion()) && (part.getPathId() == reject.getPathId())) {
                  addPart = false;
                  continue;
               }
            }
            if (addPart) {
               returnTuples.add(new ThinConTuple(this, part));
            }
         }
      } else {

         Set<ThinConPart> addedParts = new HashSet<ThinConPart>();
         for (I_Position position : positions) {
            Set<ThinConPart> rejectedParts = new HashSet<ThinConPart>();
            ThinConTuple possible = null;
            for (I_ConceptAttributePart part : versions) {
               if (part.getVersion() == Integer.MAX_VALUE) {
                  uncommittedParts.add((ThinConPart) part);
                  continue;
               } else if ((allowedStatus != null) && (!allowedStatus.contains(part.getConceptStatus()))) {
                  if (possible != null) {
                     I_Path matchingPartPath = position.getPath().getMatchingPath(possible.getPathId());
                     if (matchingPartPath != null) {
                        I_Position rejectedStatusPosition = new Position(part.getVersion(), matchingPartPath);
                        I_Path possiblePath = position.getPath().getMatchingPath(possible.getPathId());
                        I_Position possibleStatusPosition = new Position(possible.getVersion(), possiblePath);
                        if (position.isSubsequentOrEqualTo(rejectedStatusPosition)) {
                           if (rejectedStatusPosition.isSubsequentOrEqualTo(possibleStatusPosition)) {
                              possible = null;
                           }
                        }
                     }
                  }
                  rejectedParts.add((ThinConPart) part);
                  continue;
               }
               if (position.isSubsequentOrEqualTo(part.getVersion(), part.getPathId())) {
                  if (possible == null) {
                     if (!addedParts.contains(part)) {
                        possible = new ThinConTuple(this, part);
                        addedParts.add((ThinConPart) part);
                     }
                  } else {
                     if (possible.getPathId() == part.getPathId()) {
                        if (part.getVersion() > possible.getVersion()) {
                           if (!addedParts.contains(part)) {
                              possible = new ThinConTuple(this, part);
                              addedParts.add((ThinConPart) part);
                           }
                        }
                     } else {
                        if (position.getDepth(part.getPathId()) < position.getDepth(possible.getPathId())) {
                           if (!addedParts.contains(part)) {
                              possible = new ThinConTuple(this, part);
                              addedParts.add((ThinConPart) part);
                           }
                        }
                     }
                  }
               }

            }
            if (possible != null) {
               I_Path possiblePath = position.getPath().getMatchingPath(possible.getPathId());
               I_Position possibleStatusPosition = new Position(possible.getVersion(), possiblePath);
               boolean addPart = true;
               for (I_ConceptAttributePart reject : rejectedParts) {
                  int version = reject.getVersion();
                  I_Path matchingPath = position.getPath().getMatchingPath(reject.getPathId());
                  if (matchingPath != null) {
                     I_Position rejectedStatusPosition = new Position(version, matchingPath);
                     if ((rejectedStatusPosition.isSubsequentOrEqualTo(possibleStatusPosition))
                           && (position.isSubsequentOrEqualTo(rejectedStatusPosition))) {
                        addPart = false;
                        continue;
                     }
                  }
               }
               if (addPart) {
                  returnTuples.add(possible);
               }
            }
         }
      }
      if (addUncommitted) {
         for (I_ConceptAttributePart p : uncommittedParts) {
            returnTuples.add(new ThinConTuple(this, p));
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getLocalFixedConcept()
    */
   public I_ConceptualizeLocally getLocalFixedConcept() {
	  boolean isDefined = versions.get(versions.size() - 1).isDefined();
	  boolean isPrimitive = ! isDefined;
      return LocalFixedConcept.get(conId, isPrimitive);
   }

   @Override
   public boolean equals(Object obj) {
      ThinConVersioned another = (ThinConVersioned) obj;
      return conId == another.conId;
   }

   @Override
   public int hashCode() {
      return conId;
   }

   private static Collection<UUID> getUids(int id) throws IOException, TerminologyException {
      return LocalFixedTerminology.getStore().getUids(id);
   }

   public UniversalAceConceptAttributes getUniversal() throws IOException, TerminologyException {
      UniversalAceConceptAttributes conceptAttributes = new UniversalAceConceptAttributes(getUids(conId), this
            .versionCount());
      for (I_ConceptAttributePart part : versions) {
         UniversalAceConceptAttributesPart universalPart = new UniversalAceConceptAttributesPart();
         universalPart.setConceptStatus(getUids(part.getConceptStatus()));
         universalPart.setDefined(part.isDefined());
         universalPart.setPathId(getUids(part.getPathId()));
         universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
         conceptAttributes.addVersion(universalPart);
      }
      return conceptAttributes;
   }

   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("NativeId: ");
      buf.append(conId);
      buf.append(" parts: ");
      buf.append(versions.size());
      buf.append("\n  ");
      for (I_ConceptAttributePart p : versions) {
         buf.append(p);
         buf.append("\n  ");
      }
      return buf.toString();
   }

}
