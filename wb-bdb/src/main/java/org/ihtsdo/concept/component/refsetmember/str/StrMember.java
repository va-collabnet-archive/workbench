package org.ihtsdo.concept.component.refsetmember.str;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetStrRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;

public class StrMember extends RefsetMember<StrRevision, StrMember>
        implements I_ExtendByRefPartStr<StrRevision>, RefexStringAnalogBI<StrRevision> {
   private static VersionComputer<RefsetMember<StrRevision, StrMember>.Version> computer =
      new VersionComputer<RefsetMember<StrRevision, StrMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private String stringValue;

   //~--- constructors --------------------------------------------------------

   public StrMember() {
      super();
   }

   public StrMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public StrMember(TkRefsetStrMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      stringValue = refsetMember.getString1();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<StrRevision, StrMember>(primordialSapNid);

         for (TkRefsetStrRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new StrRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.STRING1, getString1());
   }

   @Override
   public StrRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (StrMember.class.isAssignableFrom(obj.getClass())) {
         StrMember another = (StrMember) obj;

         return this.nid == another.nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { this.nid });
   }

   @Override
   public StrRevision makeAnalog() {
      StrRevision newR = new StrRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);

      return newR;
   }

   @Override
   public StrRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      StrRevision newR = new StrRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<StrRevision, StrMember> obj) {
      if (StrMember.class.isAssignableFrom(obj.getClass())) {
         StrMember another = (StrMember) obj;

         return this.stringValue.equals(another.stringValue);
      }

      return false;
   }
   
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexStringVersionBI.class.isAssignableFrom(another.getClass())){
            RefexStringVersionBI sv = (RefexStringVersionBI) another;
            return this.stringValue.equals(sv.getString1());
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      stringValue = input.readString();
   }

   @Override
   protected final StrRevision readMemberRevision(TupleInput input) {
      return new StrRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      assert stringValue != null;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(": ");
      buf.append(" stringValue: '").append(this.stringValue).append("' ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeString(stringValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getString1() {
      return stringValue;
   }

   @Override
   public String getString1Value() {
      return stringValue;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefsetStrMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.STR;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.STR.getTypeNid();
   }

   @Override
   protected ArrayIntList getVariableVersionNids() {
      return new ArrayIntList(2);
   }

   @Override
   protected VersionComputer<RefsetMember<StrRevision, StrMember>.Version> getVersionComputer() {
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
            for (StrRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return Collections.unmodifiableList((List<Version>) versions);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.stringValue = str;
      modified();
   }

   @Override
   public void setString1Value(String stringValue) {
      this.stringValue = stringValue;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<StrRevision, StrMember>.Version
           implements I_ExtendByRefVersion<StrRevision>, I_ExtendByRefPartStr<StrRevision>,
                      RefexStringAnalogBI<StrRevision> {
      private Version(RefexStringAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(RefexVersionBI o) {
         if (I_ExtendByRefPartStr.class.isAssignableFrom(o.getClass())) {
            I_ExtendByRefPartStr<StrRevision> another = (I_ExtendByRefPartStr<StrRevision>) o;

            if (!this.getString1Value().equals(another.getString1Value())) {
               return this.getString1Value().compareTo(another.getString1Value());
            }
         }

         return super.compareTo(o);
      }

      @Override
      public I_ExtendByRefPartStr<StrRevision> duplicate() {
         return (I_ExtendByRefPartStr<StrRevision>) super.duplicate();
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { getString1Value().hashCode() });
      }

      //~--- get methods ------------------------------------------------------

      RefexStringAnalogBI getCv() {
         return (RefexStringAnalogBI) cv;
      }

      @Override
      public TkRefsetStrMember getERefsetMember() throws IOException {
         return new TkRefsetStrMember(this, RevisionHandling.EXCLUDE_REVISIONS);
      }

      @Override
      public ERefsetStrRevision getERefsetRevision() throws IOException {
         return new ERefsetStrRevision(this);
      }

      @Override
      public String getString1() {
         return getCv().getString1();
      }

      @Override
      public String getString1Value() {
         return getCv().getString1();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setString1(String str) throws PropertyVetoException {
         getCv().setString1(str);
      }

      @Override
      public void setString1Value(String stringValue) throws PropertyVetoException {
         getCv().setString1(stringValue);
      }
   }
}
