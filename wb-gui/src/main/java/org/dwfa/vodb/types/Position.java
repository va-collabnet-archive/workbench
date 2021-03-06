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

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.tk.hash.Hashcode;

public class Position implements I_Position, Serializable {
  private static final int dataVersion = 1;

   /**
    *
    */
   private static final long serialVersionUID = 1L;
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == 1) {
          time = in.readLong();
          path = (PathBI) in.readObject();
      }
      else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeLong(time);
      out.writeObject(path);
   }


   static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

   //~--- fields --------------------------------------------------------------

   private PathBI path;
   private long   time;

   //~--- constructors --------------------------------------------------------

   public Position(int version, PathBI path) {
      super();

      if (path == null) {
         throw new IllegalArgumentException("path cannot be null");
      }

      this.time = Terms.get().convertToThickVersion(version);
      this.path = path;
   }

   public Position(long time, PathBI path) {
      super();

      if (path == null) {
         throw new IllegalArgumentException("path cannot be null");
      }

      this.time = time;
      this.path = path;
   }

   //~--- methods -------------------------------------------------------------

   /**
    *
    * @see
    * org.dwfa.vodb.types.I_Position#checkAntecedentOrEqualToOrigins(java.util
    * .List)
    */
   @Override
   public boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> origins) {
      for (PositionBI origin : origins) {
         if (path.getConceptNid() == origin.getPath().getConceptNid()) {
            return time <= origin.getTime();
         } else if (checkAntecedentOrEqualToOrigins(origin.getPath().getOrigins())) {
            return true;
         }
      }

      return false;
   }

   private boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> origins, int testVersion,
           int testPathId) {
      for (PositionBI origin : origins) {
         if (testPathId == origin.getPath().getConceptNid()) {
            return origin.getVersion() <= testVersion;
         } else if (checkAntecedentOrEqualToOrigins(origin.getPath().getOrigins(), testVersion, testPathId)) {
            return true;
         }
      }

      return false;
   }

   private boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> origins, long testTime,
           int testPathId) {
      for (PositionBI origin : origins) {
         if (testPathId == origin.getPath().getConceptNid()) {
            return origin.getTime() <= testTime;
         } else if (checkAntecedentOrEqualToOrigins(origin.getPath().getOrigins(), testTime, testPathId)) {
            return true;
         }
      }

      return false;
   }

   private boolean checkSubsequentOrEqualToOrigins(Collection<? extends PositionBI> origins, int testVersion,
           int testPathId) {
      for (PositionBI origin : origins) {
         if (testPathId == origin.getPath().getConceptNid()) {
            return origin.getVersion() >= testVersion;
         } else if (checkSubsequentOrEqualToOrigins(origin.getPath().getOrigins(), testVersion, testPathId)) {
            return true;
         }
      }

      return false;
   }

   private boolean checkSubsequentOrEqualToOrigins(Collection<? extends PositionBI> origins, long testTime,
           int testPathId) {
      for (PositionBI origin : origins) {
         if (testPathId == origin.getPath().getConceptNid()) {
            return origin.getTime() >= testTime;
         } else if (checkSubsequentOrEqualToOrigins(origin.getPath().getOrigins(), testTime, testPathId)) {
            return true;
         }
      }

      return false;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.dwfa.vodb.types.I_Position#equals(org.dwfa.vodb.types.Position)
    */
    public boolean equals(PositionBI another) {
        return ((time == another.getTime()) && (path.getConceptNid() == another.getPath().getConceptNid()));
    }

    @Override
    public boolean equals(Object obj) {
        if (PositionBI.class.isAssignableFrom(obj.getClass())) {
            return equals((PositionBI) obj);
        }

        return false;
    }

    @Override
    public boolean equals(long time, int pathId) {
        return ((this.time == time) && (path.getConceptNid() == pathId));
    }

    @Override
    public int hashCode() {
        return Hashcode.computeLong(time, path.getConceptNid());
    }

   /**
    *
    * @see org.dwfa.vodb.types.I_Position#equals(int, int)
    */
   @Override
   public boolean equals(int version, int pathId) {
      return ((this.time == Terms.get().convertToThickVersion(version)) && (path.getConceptNid() == pathId));
   }

   @SuppressWarnings("unchecked")
   public static PositionBI readPosition(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int version = in.readInt();
      int pathConceptId;

      try {
         List<UUID> pathIdList = (List<UUID>) in.readObject();

         if (Terms.get().hasId(pathIdList)) {
            pathConceptId = Terms.get().uuidToNative(pathIdList);
         } else {
            pathConceptId = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.localize().getNid();
         }
      } catch (TerminologyException e) {
         throw new IOException(e);
      }

      int              size    = in.readInt();
      List<PositionBI> origins = new ArrayList<PositionBI>(size);

      for (int i = 0; i < size; i++) {
         origins.add(readPosition(in));
      }

      Path p = new Path(pathConceptId, origins);

      return new Position(version, p);
   }

   public static Set<PositionBI> readPositionSet(ObjectInputStream in)
           throws IOException, ClassNotFoundException {
      int             size      = in.readInt();
      Set<PositionBI> positions = Collections.synchronizedSet(new HashSet<PositionBI>(size));

      for (int i = 0; i < size; i++) {
         try {
            PositionBI       position    = readPosition(in);
            I_GetConceptData pathConcept = Terms.get().getConcept(position.getPath().getConceptNid());
            PathBI           path        = Terms.get().getPath(pathConcept.getUids());

            positions.add(Terms.get().newPosition(path, position.getTime()));
         } catch (NullPointerException npe) {
            AceLog.getAppLog().severe("readPositionSet position not found");
         } catch (IOException ex) {
            if ((ex.getCause() != null)
                    && NoMappingException.class.isAssignableFrom(ex.getCause().getClass())) {
               AceLog.getAppLog().alertAndLogException(ex.getCause());
            } else {
               throw ex;
            }
         } catch (TerminologyException ex) {
            if ((ex.getCause() != null)
                    && NoMappingException.class.isAssignableFrom(ex.getCause().getClass())) {
               AceLog.getAppLog().alertAndLogException(ex.getCause());
            } else {
               throw new IOException(ex);
            }
         }
      }

      return positions;
   }

   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      try {
         I_GetConceptData cb = Terms.get().getConcept(path.getConceptNid());

         buff.append(cb.getInitialText());
      } catch (IOException e) {
         buff.append(e.getMessage());
         AceLog.getAppLog().alertAndLogException(e);
      } catch (TerminologyException e) {
         buff.append(e.getMessage());
         AceLog.getAppLog().alertAndLogException(e);
      }

      buff.append(": ");

      if (time == Long.MAX_VALUE) {
         buff.append("Latest");
      } else if (time == Long.MIN_VALUE) {
         buff.append("BOT");
      } else {
         Date positionDate = new Date(time);

         buff.append(dateFormatter.format(positionDate));
      }

      return buff.toString();
   }

   public static void writePosition(ObjectOutputStream out, PositionBI p) throws IOException {
      out.writeInt(p.getVersion());

      if (Terms.get().getId(p.getPath().getConceptNid()) != null) {
         out.writeObject(Terms.get().nativeToUuid(p.getPath().getConceptNid()));
      } else {
         out.writeObject(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
      }

      out.writeInt(p.getPath().getOrigins().size());

      for (PositionBI origin : p.getPath().getOrigins()) {
         writePosition(out, origin);
      }
   }

   public static void writePositionSet(ObjectOutputStream out, Set<PositionBI> viewPositions)
           throws IOException {
      out.writeInt(viewPositions.size());

      for (PositionBI p : viewPositions) {
         writePosition(out, p);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<? extends PositionBI> getAllOrigins() {
      return path.getOrigins();
   }

   /*
    * (non-Javadoc)
    *
    * @see org.dwfa.vodb.types.I_Position#getDepth(int)
    */
   public int getDepth(int pathId) {
      int depth = 0;

      if (pathId == path.getConceptNid()) {
         return depth;
      }

      List<PositionBI> depthOrigins = new ArrayList<PositionBI>(path.getOrigins());

      while (depthOrigins.size() > 0) {
         depth++;

         for (PositionBI o : depthOrigins) {
            if (o.getPath().getConceptNid() == pathId) {
               return depth;
            }
         }

         List<PositionBI> newOrigins = new ArrayList<PositionBI>();

         for (PositionBI p : depthOrigins) {
            newOrigins.addAll(p.getPath().getOrigins());
         }

         depthOrigins = newOrigins;
      }

      return Integer.MAX_VALUE;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.dwfa.vodb.types.I_Position#getPath()
    */
   @Override
   public PathBI getPath() {
      return path;
   }

   public int getPositionId() {
      throw new UnsupportedOperationException();
   }

   @Override
   public long getTime() {
      return time;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.dwfa.vodb.types.I_Position#getVersion()
    */
   @Override
   public int getVersion() {
      return Terms.get().convertToThinVersion(time);
   }

   /*
    * (non-Javadoc)
    *
    * @see
    * org.dwfa.vodb.types.I_Position#isAntecedentOrEqualTo(org.dwfa.vodb.types
    * .Position)
    */
   @Override
   public boolean isAntecedentOrEqualTo(PositionBI another) {
      if (equals(another)) {
         return true;
      }

      if (path.getConceptNid() == another.getPath().getConceptNid()) {
         return time <= another.getTime();
      }

      return checkAntecedentOrEqualToOrigins(another.getPath().getOrigins());
   }

   /*
    * (non-Javadoc)
    *
    * @see org.dwfa.vodb.types.I_Position#isAntecedentOrEqualTo(int, int)
    */
   @Override
   public boolean isAntecedentOrEqualTo(int version, int pathId) {
      if (equals(version, pathId)) {
         return true;
      }

      if (path.getConceptNid() == pathId) {
         return this.time <= Terms.get().convertToThickVersion(version);
      }

      return checkAntecedentOrEqualToOrigins(path.getOrigins(), version, pathId);
   }

   @Override
   public boolean isAntecedentOrEqualTo(long time, int pathId) {
      if (equals(time, pathId)) {
         return true;
      }

      if (path.getConceptNid() == pathId) {
         return this.time <= time;
      }

      return checkAntecedentOrEqualToOrigins(path.getOrigins(), time, pathId);
   }

   /*
    * (non-Javadoc)
    *
    * @see
    * org.dwfa.vodb.types.I_Position#isSubsequentOrEqualTo(org.dwfa.vodb.types
    * .I_Position)
    */
   @Override
   public boolean isSubsequentOrEqualTo(PositionBI another) {
      return another.isAntecedentOrEqualTo(this);
   }

   /*
    * (non-Javadoc)
    *
    * @see org.dwfa.vodb.types.I_Position#isSubsequentOrEqualTo(int, int)
    */
   @Override
   public boolean isSubsequentOrEqualTo(int version, int pathId) {
      if (equals(version, pathId)) {
         return true;
      }

      if (path.getConceptNid() == pathId) {
         return this.time >= Terms.get().convertToThickVersion(version);
      }

      return checkSubsequentOrEqualToOrigins(path.getOrigins(), version, pathId);
   }

   @Override
   public boolean isSubsequentOrEqualTo(long time, int pathId) {
      if (equals(time, pathId)) {
         return true;
      }

      if (path.getConceptNid() == pathId) {
         return this.time >= time;
      }

      return checkSubsequentOrEqualToOrigins(path.getOrigins(), time, pathId);
   }
}
