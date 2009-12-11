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
package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedDesc;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinDescVersioned implements I_DescriptionVersioned {
   private int descId;

   private int conceptId;

   private List<I_DescriptionPart> versions;

   public ThinDescVersioned(int descId, int conceptId, int count) {
      super();
      this.descId = descId;
      this.conceptId = conceptId;
      this.versions = new ArrayList<I_DescriptionPart>(count);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#addVersion(org.dwfa.vodb.types.I_DescriptionPart)
    */
   public boolean addVersion(I_DescriptionPart newPart) {
      int index = versions.size() - 1;
      if (index == -1) {
         return versions.add(newPart);
      } else if (index >= 0) {
         I_DescriptionPart prevDesc = versions.get(index);
         if (prevDesc.hasNewData(newPart)) {
            if (prevDesc.getText().equals(newPart.getText())) {
               newPart.setText(prevDesc.getText());
            }
            if (prevDesc.getLang().equals(newPart.getLang())) {
               newPart.setLang(prevDesc.getLang());
            }
            return versions.add(newPart);
         }
      }
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#getVersions()
    */
   public List<I_DescriptionPart> getVersions() {
      return versions;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#versionCount()
    */
   public int versionCount() {
      return versions.size();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#matches(java.util.regex.Pattern)
    */
   public boolean matches(Pattern p) {
      String lastText = null;
      for (I_DescriptionPart desc : versions) {
         if (desc.getText() != lastText) {
            lastText = desc.getText();
            Matcher m = p.matcher(lastText);
            if (m.find()) {
               return true;
            }
         }
      }
      return false;
   }

   public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append("ThinDescVersioned: desc: ");
      buff.append(descId);
      buff.append(" ConceptId: ");
      buff.append(conceptId);
      buff.append("\n");
      for (I_DescriptionPart desl : versions) {
         buff.append("     ");
         buff.append(desl.toString());
         buff.append("\n");
      }

      return buff.toString();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#getConceptId()
    */
   public int getConceptId() {
      return conceptId;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#getDescId()
    */
   public int getDescId() {
      return descId;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#getTuples()
    */
   public List<I_DescriptionTuple> getTuples() {
      List<I_DescriptionTuple> tuples = new ArrayList<I_DescriptionTuple>();
      for (I_DescriptionPart p : getVersions()) {
         tuples.add(new ThinDescTuple(this, p));
      }
      return tuples;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#getFirstTuple()
    */
   public I_DescriptionTuple getFirstTuple() {
      return new ThinDescTuple(this, versions.get(0));
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#getLastTuple()
    */
   public I_DescriptionTuple getLastTuple() {
      return new ThinDescTuple(this, versions.get(versions.size() - 1));
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#addTuples(org.dwfa.ace.IntSet,
    *      org.dwfa.ace.IntSet, java.util.Set, java.util.List)
    */
   public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
         List<I_DescriptionTuple> matchingTuples, boolean addUncommitted) {
      Set<I_DescriptionPart> uncommittedParts = new HashSet<I_DescriptionPart>();
      if (positions == null) {
         List<I_DescriptionPart> addedParts = new ArrayList<I_DescriptionPart>();
         Set<I_DescriptionPart> rejectedParts = new HashSet<I_DescriptionPart>();
         for (I_DescriptionPart part : versions) {
            if (part.getVersion() == Integer.MAX_VALUE) {
               uncommittedParts.add(part);
            } else {
               if ((allowedStatus != null) && (!allowedStatus.contains(part.getStatusId()))) {
                  rejectedParts.add(part);
                  continue;
               }
               if ((allowedTypes != null) && (!allowedTypes.contains(part.getTypeId()))) {
                  rejectedParts.add(part);
                  continue;
               }
               addedParts.add(part);
            }
         }
         for (I_DescriptionPart part : addedParts) {
            boolean addPart = true;
            for (I_DescriptionPart reject : rejectedParts) {
               if ((part.getVersion() <= reject.getVersion()) && (part.getPathId() == reject.getPathId())) {
                  addPart = false;
                  continue;
               }
            }
            if (addPart) {
               matchingTuples.add(new ThinDescTuple(this, part));
            }
         }
      } else {
         Set<I_DescriptionPart> addedParts = new HashSet<I_DescriptionPart>();
         for (I_Position position : positions) {
            Set<I_DescriptionPart> rejectedParts = new HashSet<I_DescriptionPart>();
            ThinDescTuple possible = null;
            for (I_DescriptionPart part : versions) {
               if (part.getVersion() == Integer.MAX_VALUE) {
                  uncommittedParts.add(part);
                  continue;
               } else if ((allowedStatus != null) && (!allowedStatus.contains(part.getStatusId()))) {
                  if (possible != null) {
                     Position rejectedStatusPosition = new Position(part.getVersion(), position.getPath()
                           .getMatchingPath(part.getPathId()));
                     I_Path possiblePath = position.getPath().getMatchingPath(possible.getPathId());
                     I_Position possibleStatusPosition = new Position(possible.getVersion(), possiblePath);
                     if (rejectedStatusPosition.getPath() != null
                           && rejectedStatusPosition.isSubsequentOrEqualTo(possibleStatusPosition)
                           && position.isSubsequentOrEqualTo(rejectedStatusPosition)) {
                        possible = null;
                     }
                  }
                  rejectedParts.add(part);
                  continue;
               }
               if ((allowedTypes != null) && (!allowedTypes.contains(part.getTypeId()))) {
                  rejectedParts.add(part);
                  continue;
               }
               if (position.isSubsequentOrEqualTo(part.getVersion(), part.getPathId())) {
                  if (possible == null) {
                     if (!addedParts.contains(part)) {
                        possible = new ThinDescTuple(this, part);
                        addedParts.add(part);
                     }
                  } else {
                     if (possible.getPathId() == part.getPathId()) {
                        if (part.getVersion() > possible.getVersion()) {
                           if (!addedParts.contains(part)) {
                              possible = new ThinDescTuple(this, part);
                              addedParts.add(part);
                           }
                        }
                     } else {
                        if (position.getDepth(part.getPathId()) < position.getDepth(possible.getPathId())) {
                           if (!addedParts.contains(part)) {
                              possible = new ThinDescTuple(this, part);
                              addedParts.add(part);
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
               for (I_DescriptionPart reject : rejectedParts) {
                  int version = reject.getVersion();
                  I_Path matchingPath = position.getPath().getMatchingPath(reject.getPathId());
                  if (matchingPath != null) {
                     Position rejectedStatusPosition = new Position(version, matchingPath);
                     if (rejectedStatusPosition.getPath() != null
                           && rejectedStatusPosition.isSubsequentOrEqualTo(possibleStatusPosition)
                           && position.isSubsequentOrEqualTo(rejectedStatusPosition)) {
                        addPart = false;
                        continue;
                     }
                  }
               }
               if (addPart) {
                  matchingTuples.add(possible);
               }
            }
         }
      }
      if (addUncommitted) {
         for (I_DescriptionPart p : uncommittedParts) {
            matchingTuples.add(new ThinDescTuple(this, p));
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
    */
   public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
      conceptId = jarToDbNativeMap.get(conceptId);
      descId = jarToDbNativeMap.get(descId);
      for (I_DescriptionPart p : versions) {
         p.convertIds(jarToDbNativeMap);
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#merge(org.dwfa.vodb.types.ThinDescVersioned)
    */
   public boolean merge(I_DescriptionVersioned jarDesc) {
      HashSet<I_DescriptionPart> versionSet = new HashSet<I_DescriptionPart>(versions);
      boolean changed = false;
      for (I_DescriptionPart jarPart : jarDesc.getVersions()) {
         if (!versionSet.contains(jarPart)) {
            changed = true;
            versions.add(jarPart);
         }
      }
      return changed;

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#getTimePathSet()
    */
   public Set<TimePathId> getTimePathSet() {
      Set<TimePathId> tpSet = new HashSet<TimePathId>();
      for (I_DescriptionPart p : versions) {
         tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
      }
      return tpSet;
   }

   @Override
   public boolean equals(Object obj) {
      ThinDescVersioned another = (ThinDescVersioned) obj;
      return descId == another.descId;
   }

   @Override
   public int hashCode() {
      return descId;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_DescriptionVersioned#toLocalFixedDesc()
    */
   public I_DescribeConceptLocally toLocalFixedDesc() {
      I_DescriptionPart part = versions.get(versions.size() - 1);
      return new LocalFixedDesc(descId, part.getStatusId(), conceptId, part.getInitialCaseSignificant(), part
            .getTypeId(), part.getText(), part.getLang());
   }

   private static Collection<UUID> getUids(int id) throws IOException, TerminologyException {
      return LocalFixedTerminology.getStore().getUids(id);
   }

   public UniversalAceDescription getUniversal() throws IOException, TerminologyException {
      UniversalAceDescription universal = new UniversalAceDescription(getUids(descId), getUids(conceptId), this
            .versionCount());
      for (I_DescriptionPart part : versions) {
         UniversalAceDescriptionPart universalPart = new UniversalAceDescriptionPart();
         universalPart.setInitialCaseSignificant(part.getInitialCaseSignificant());
         universalPart.setLang(part.getLang());
         universalPart.setPathId(getUids(part.getPathId()));
         universalPart.setStatusId(getUids(part.getStatusId()));
         universalPart.setText(part.getText());
         universalPart.setTypeId(getUids(part.getTypeId()));
         universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
         universal.addVersion(universalPart);
      }
      return universal;
   }

}
