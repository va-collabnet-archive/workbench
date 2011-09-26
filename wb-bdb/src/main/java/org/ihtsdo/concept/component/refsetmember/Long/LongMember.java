package org.ihtsdo.concept.component.refsetmember.Long;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetLongMember;
import org.ihtsdo.etypes.ERefsetLongRevision;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongRevision;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class LongMember extends RefsetMember<LongRevision, LongMember>
        implements I_ExtendByRefPartLong<LongRevision>, RefexLongAnalogBI<LongRevision> {
   private static VersionComputer<RefsetMember<LongRevision, LongMember>.Version> computer =
      new VersionComputer<RefsetMember<LongRevision, LongMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private long longValue;

   //~--- constructors --------------------------------------------------------

   public LongMember() {
      super();
   }

   public LongMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public LongMember(TkRefsetLongMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      longValue = refsetMember.getLongValue();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<LongRevision, LongMember>(primordialSapNid);

         for (TkRefsetLongRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new LongRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.LONG1, getLong1());
   }

   @Override
   public LongRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (LongMember.class.isAssignableFrom(obj.getClass())) {
         LongMember another = (LongMember) obj;

         return this.nid == another.nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { this.nid });
   }

   @Override
   public LongRevision makeAnalog() {
      LongRevision newR = new LongRevision(getStatusNid(), getPathNid(), getTime(), this);

      return newR;
   }

   @Override
   public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
      LongRevision newR = new LongRevision(statusNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   public LongRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      LongRevision newR = new LongRevision(statusNid, authorNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean membersEqual(ConceptComponent<LongRevision, LongMember> obj) {
      if (LongMember.class.isAssignableFrom(obj.getClass())) {
         LongMember another = (LongMember) obj;

         return this.longValue == another.longValue;
      }

      return false;
   }

   @Override
   protected void readMemberFields(TupleInput input) {
      longValue = input.readLong();
   }

   @Override
   protected final LongRevision readMemberRevision(TupleInput input) {
      return new LongRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      return true;
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName());
      buf.append(" longValue:").append(this.longValue);
      buf.append(" ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeLong(longValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public long getLong1() {
      return longValue;
   }

   @Override
   public long getLongValue() {
      return longValue;
   }

   @Override
   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.LONG;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.LONG.getTypeNid();
   }

   @Override
   protected ArrayIntList getVariableVersionNids() {
      return new ArrayIntList(2);
   }

   @Override
   protected VersionComputer<RefsetMember<LongRevision, LongMember>.Version> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<Version> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<Version> list = new ArrayList<Version>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new Version(this));
         }

         if (revisions != null) {
            for (LongRevision lr : revisions) {
               if (lr.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(lr));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLong1(long l) throws PropertyVetoException {
      this.longValue = l;
      modified();
   }

   @Override
   public void setLongValue(long longValue) {
      this.longValue = longValue;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<LongRevision, LongMember>.Version
           implements I_ExtendByRefVersion<LongRevision>, I_ExtendByRefPartLong<LongRevision>,
                      RefexLongAnalogBI<LongRevision> {
      private Version(RefexLongAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(I_ExtendByRefPart<LongRevision> o) {
         if (I_ExtendByRefPartLong.class.isAssignableFrom(o.getClass())) {
            I_ExtendByRefPartLong<LongRevision> another = (I_ExtendByRefPartLong<LongRevision>) o;

            if (this.getLongValue() != another.getLongValue()) {
               if (this.getLongValue() > another.getLongValue()) {
                  return 1;
               } else if (this.getLongValue() < another.getLongValue()) {
                  return -1;
               }
            }
         }

         return super.compareTo(o);
      }

      @Override
      public I_ExtendByRefPartLong<LongRevision> duplicate() {
         return (I_ExtendByRefPartLong<LongRevision>) super.duplicate();
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { new Long(getLongValue()).hashCode() });
      }

      //~--- get methods ------------------------------------------------------

      RefexLongAnalogBI getCv() {
         return (RefexLongAnalogBI) cv;
      }

      @Override
      public ERefsetLongMember getERefsetMember() throws TerminologyException, IOException {
         return new ERefsetLongMember(this);
      }

      @Override
      public ERefsetLongRevision getERefsetRevision() throws TerminologyException, IOException {
         return new ERefsetLongRevision(this);
      }

      @Override
      public long getLong1() {
         return getCv().getLong1();
      }

      @Override
      @Deprecated
      public long getLongValue() {
         return getCv().getLong1();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setLong1(long l) throws PropertyVetoException {
         getCv().setLong1(l);
      }

      @Override
      @Deprecated
      public void setLongValue(long value) throws PropertyVetoException {
         getCv().setLong1(value);
      }
   }
}
