package org.ihtsdo.concept.component.refsetmember.integer;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetIntMember;
import org.ihtsdo.etypes.ERefsetIntRevision;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntMember;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.tk.hash.Hashcode;

public class IntMember extends RefsetMember<IntRevision, IntMember>
        implements I_ExtendByRefPartInt<IntRevision>, RefexIntAnalogBI<IntRevision> {
   private static VersionComputer<RefsetMember<IntRevision, IntMember>.Version> computer =
      new VersionComputer<RefsetMember<IntRevision, IntMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private int intValue;

   //~--- constructors --------------------------------------------------------

   public IntMember() {
      super();
   }

   public IntMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public IntMember(TkRefsetIntMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      intValue = refsetMember.getIntValue();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<IntRevision, IntMember>(primordialSapNid);

         for (TkRefsetIntRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new IntRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

    @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.INTEGER1, this.intValue);
   }

   @Override
   public IntRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (IntMember.class.isAssignableFrom(obj.getClass())) {
         IntMember another = (IntMember) obj;

         return this.nid == another.nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { this.nid });
   }

   @Override
   public IntRevision makeAnalog() {
      IntRevision newR = new IntRevision(getStatusNid(), getPathNid(), getTime(), this);

      return newR;
   }

   @Override
   public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
      IntRevision newR = new IntRevision(statusNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   public IntRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      IntRevision newR = new IntRevision(statusNid, authorNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean membersEqual(ConceptComponent<IntRevision, IntMember> obj) {
      if (IntMember.class.isAssignableFrom(obj.getClass())) {
         IntMember another = (IntMember) obj;

         return this.intValue == another.intValue;
      }

      return false;
   }

   @Override
   protected void readMemberFields(TupleInput input) {
      intValue = input.readInt();
   }

   @Override
   protected final IntRevision readMemberRevision(TupleInput input) {
      return new IntRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(" ");
      buf.append(this.intValue);
      buf.append(" ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(intValue);
   }

   //~--- get methods ---------------------------------------------------------

    @Override
   public int getInt1() {
      return intValue;
   }

    @Override
   public int getIntValue() {
      return intValue;
   }

    @Override
   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.INT;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.INT.getTypeNid();
   }

   @Override
   protected ArrayIntList getVariableVersionNids() {
      return new ArrayIntList(2);
   }

    @Override
   protected VersionComputer<RefsetMember<IntRevision, IntMember>.Version> getVersionComputer() {
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
            for (RefexIntAnalogBI r: revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setInt1(int intValue) throws PropertyVetoException {
      this.intValue = intValue;
      modified();
   }

    @Override
   public void setIntValue(int intValue) {
      this.intValue = intValue;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<IntRevision, IntMember>.Version
           implements I_ExtendByRefVersion<IntRevision>, I_ExtendByRefPartInt<IntRevision>,
                      RefexIntAnalogBI<IntRevision> {
      private Version(RefexIntAnalogBI cv) {
         super(cv);
      }
      RefexIntAnalogBI getCv() {
          return (RefexIntAnalogBI) cv;
      }
      //~--- methods ----------------------------------------------------------

        @Override
      public int compareTo(I_ExtendByRefPart<IntRevision> o) {
         if (I_ExtendByRefPartInt.class.isAssignableFrom(o.getClass())) {
            I_ExtendByRefPartInt<IntRevision> another = (I_ExtendByRefPartInt<IntRevision>) o;

            if (this.getIntValue() != another.getIntValue()) {
               return this.getIntValue() - another.getIntValue();
            }
         }

         return super.compareTo(o);
      }

      @Override
      public I_ExtendByRefPartInt<IntRevision> duplicate() {
         return (I_ExtendByRefPartInt<IntRevision>) super.duplicate();
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { getIntValue() });
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public ERefsetIntMember getERefsetMember() throws TerminologyException, IOException {
         return new ERefsetIntMember(this);
      }

      @Override
      public ERefsetIntRevision getERefsetRevision() throws TerminologyException, IOException {
         return new ERefsetIntRevision(this);
      }

      @Override
      public int getInt1() {
         return getCv().getInt1();
      }

      @Override
      public int getIntValue() {
         return getCv().getInt1();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setInt1(int value) throws PropertyVetoException {
         getCv().setInt1(value);
      }

      @Override
      public void setIntValue(int value) throws PropertyVetoException {
         getCv().setInt1(value);
      }
   }
}
