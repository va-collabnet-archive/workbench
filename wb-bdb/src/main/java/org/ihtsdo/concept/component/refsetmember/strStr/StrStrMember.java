package org.ihtsdo.concept.component.refsetmember.strStr;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.beans.PropertyVetoException;
import java.io.IOException;

import java.util.*;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStrStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.str.*;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetStrRevision;
import org.ihtsdo.etypes.ERefsetStrStrRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.api.refex.type_string_string.RefexStringStringAnalogBI;
import org.ihtsdo.tk.api.refex.type_string_string.RefexStringStringVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_string_string.TkRefsetStrStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string_string.TkRefsetStrStrRevision;
import org.ihtsdo.tk.hash.Hashcode;

public class StrStrMember extends RefsetMember<StrStrRevision, StrStrMember>
        implements I_ExtendByRefPartStrStr<StrStrRevision>, RefexStringStringAnalogBI<StrStrRevision> {
   private static VersionComputer<RefsetMember<StrStrRevision, StrStrMember>.Version> computer =
      new VersionComputer<RefsetMember<StrStrRevision, StrStrMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private String stringValue1;
   private String stringValue2;

   //~--- constructors --------------------------------------------------------

   public StrStrMember() {
      super();
   }

   public StrStrMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public StrStrMember(TkRefsetStrStrMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      stringValue1 = refsetMember.getString1();
      stringValue2 = refsetMember.getString2();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<StrStrRevision, StrStrMember>(primordialSapNid);

         for (TkRefsetStrStrRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new StrStrRevision(eVersion, this));
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
   public StrStrRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (StrStrMember.class.isAssignableFrom(obj.getClass())) {
         StrStrMember another = (StrStrMember) obj;

         return this.nid == another.nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { this.nid });
   }

   @Override
   public StrStrRevision makeAnalog() {
      StrStrRevision newR = new StrStrRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);

      return newR;
   }

   @Override
   public StrStrRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      StrStrRevision newR = new StrStrRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<StrStrRevision, StrStrMember> obj) {
      if (StrStrMember.class.isAssignableFrom(obj.getClass())) {
         StrStrMember another = (StrStrMember) obj;

         return this.stringValue1.equals(another.stringValue1);
      }

      return false;
   }
   
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexStringStringVersionBI.class.isAssignableFrom(another.getClass())){
            RefexStringStringVersionBI sv = (RefexStringStringVersionBI) another;
            if(this.stringValue1.equals(sv.getString1()) && this.stringValue2.equals(sv.getString2())){
                return true;
            }
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      stringValue1 = input.readString();
      stringValue2 = input.readString();
   }

   @Override
   protected final StrStrRevision readMemberRevision(TupleInput input) {
      return new StrStrRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      assert stringValue1 != null;
      assert stringValue2 != null;

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
      buf.append(" stringValue1: '").append(this.stringValue1).append("' ");
      buf.append(" stringValue2: '").append(this.stringValue2).append("' ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeString(stringValue1);
      output.writeString(stringValue2);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getString1() {
      return stringValue1;
   }

   @Override
   public String getString1Value() {
      return stringValue1;
   }
   
   @Override
   public String getString2() {
      return stringValue2;
   }

   @Override
   public String getString2Value() {
      return stringValue2;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefsetStrStrMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.STR_STR;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.STR_STR.getTypeNid();
   }

   @Override
   protected ArrayIntList getVariableVersionNids() {
      return new ArrayIntList(2);
   }

   @Override
   protected VersionComputer<RefsetMember<StrStrRevision, StrStrMember>.Version> getVersionComputer() {
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
            for (StrStrRevision r : revisions) {
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
      this.stringValue1 = str;
      modified();
   }

   @Override
   public void setString1Value(String stringValue) {
      this.stringValue1 = stringValue;
      modified();
   }

   @Override
   public void setString2(String str) throws PropertyVetoException {
      this.stringValue2 = str;
      modified();
   }

   @Override
   public void setString2Value(String stringValue) {
      this.stringValue2 = stringValue;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<StrStrRevision, StrStrMember>.Version
           implements I_ExtendByRefVersion<StrStrRevision>, I_ExtendByRefPartStrStr<StrStrRevision>,
                      RefexStringStringAnalogBI<StrStrRevision> {
      private Version(RefexStringStringAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(RefexVersionBI o) {
         if (I_ExtendByRefPartStrStr.class.isAssignableFrom(o.getClass())) {
            I_ExtendByRefPartStrStr<StrStrRevision> another = (I_ExtendByRefPartStrStr<StrStrRevision>) o;

            if (!this.getString1Value().equals(another.getString1Value())) {
               return this.getString1Value().compareTo(another.getString1Value());
            }
            
            if (!this.getString2Value().equals(another.getString2Value())) {
               return this.getString2Value().compareTo(another.getString2Value());
            }
         }

         return super.compareTo(o);
      }

      @Override
      public I_ExtendByRefPartStrStr<StrStrRevision> duplicate() {
         return (I_ExtendByRefPartStrStr<StrStrRevision>) super.duplicate();
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { getString1Value().hashCode(), getString2Value().hashCode() });
      }

      //~--- get methods ------------------------------------------------------

      RefexStringStringAnalogBI getCv() {
         return (RefexStringStringAnalogBI) cv;
      }

      @Override
      public TkRefsetStrStrMember getERefsetMember() throws IOException {
         return new TkRefsetStrStrMember(this, RevisionHandling.EXCLUDE_REVISIONS);
      }

      @Override
      public ERefsetStrStrRevision getERefsetRevision() throws IOException {
         return new ERefsetStrStrRevision(this);
      }

      @Override
      public String getString1() {
         return getCv().getString1();
      }

      @Override
      public String getString1Value() {
         return getCv().getString1();
      }
      
      @Override
      public String getString2() {
         return getCv().getString2();
      }

      @Override
      public String getString2Value() {
         return getCv().getString2();
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
      
      @Override
      public void setString2(String str) throws PropertyVetoException {
         getCv().setString2(str);
      }

      @Override
      public void setString2Value(String stringValue) throws PropertyVetoException {
         getCv().setString2(stringValue);
      }
   }
}
