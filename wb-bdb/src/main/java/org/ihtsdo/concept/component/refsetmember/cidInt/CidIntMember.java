package org.ihtsdo.concept.component.refsetmember.cidInt;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidIntRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_nid_int.RefexNidIntAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;

public class CidIntMember extends RefsetMember<CidIntRevision, CidIntMember>
        implements I_ExtendByRefPartCidInt<CidIntRevision>, RefexNidIntAnalogBI<CidIntRevision> {
   private static VersionComputer<RefsetMember<CidIntRevision, CidIntMember>.Version> computer =
      new VersionComputer<RefsetMember<CidIntRevision, CidIntMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private int c1Nid;
   private int intValue;

   //~--- constructors --------------------------------------------------------

   public CidIntMember() {
      super();
   }

   public CidIntMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public CidIntMember(TkRefexUuidIntMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid    = Bdb.uuidToNid(refsetMember.getUuid1());
      intValue = refsetMember.getInt1();

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<CidIntRevision, CidIntMember>(primordialSapNid);

         for (TkRefexUuidIntRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new CidIntRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getNid1());
      rcs.with(RefexProperty.INTEGER1, getInt1());
   }

   @Override
   public CidIntRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidIntMember.class.isAssignableFrom(obj.getClass())) {
         CidIntMember another = (CidIntMember) obj;

         if (super.equals(another)) {
            return (this.c1Nid == another.c1Nid) && (this.intValue == another.intValue);
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public CidIntRevision makeAnalog() {
      CidIntRevision newR = new CidIntRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);

      return newR;
   }

   @Override
   public CidIntRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      CidIntRevision newR = new CidIntRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<CidIntRevision, CidIntMember> obj) {
      if (CidIntMember.class.isAssignableFrom(obj.getClass())) {
         CidIntMember another = (CidIntMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.intValue == another.intValue);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexNidIntVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidIntVersionBI cv = (RefexNidIntVersionBI) another;
            return (this.c1Nid == cv.getNid1()) && (this.intValue == cv.getInt1());
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      c1Nid    = input.readInt();
      intValue = input.readInt();
   }

   @Override
   protected final CidIntRevision readMemberRevision(TupleInput input) {
      return new CidIntRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      assert c1Nid != Integer.MAX_VALUE;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append("c1Nid: ");
      ConceptComponent.addNidToBuffer(buf, c1Nid);
      buf.append(" intValue: ").append(this.intValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeInt(intValue);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   public int getC1id() {
      return getC1Nid();
   }

   @Override
   public int getNid1() {
      return c1Nid;
   }

   @Override
   public int getInt1() {
      return intValue;
   }

   @Override
   public int getIntValue() {
      return intValue;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidIntMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_INT;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.CID_INT.getTypeNid();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(3);

      variableNids.add(getC1Nid());

      return variableNids;
   }

   @Override
   protected VersionComputer<RefsetMember<CidIntRevision, CidIntMember>.Version> getVersionComputer() {
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
            for (CidIntRevision r : revisions) {
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

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setC1id(int c1id) {
      setC1Nid(c1id);
   }

   @Override
   public void setNid1(int cnid1) throws PropertyVetoException {
      this.c1Nid = cnid1;
      modified();
   }

   @Override
   public void setInt1(int l) throws PropertyVetoException {
      this.intValue = l;
      modified();
   }

   @Override
   public void setIntValue(int intValue) {
      this.intValue = intValue;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<CidIntRevision, CidIntMember>.Version
           implements I_ExtendByRefVersion<CidIntRevision>, I_ExtendByRefPartCidInt<CidIntRevision>,
                      RefexNidIntAnalogBI<CidIntRevision> {
      private Version(RefexNidIntAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(RefexVersionBI o) {
         if (I_ExtendByRefPartCidInt.class.isAssignableFrom(o.getClass())) {
            I_ExtendByRefPartCidInt<CidIntRevision> another = (I_ExtendByRefPartCidInt<CidIntRevision>) o;

            if (this.getC1id() != another.getC1id()) {
               return this.getC1id() - another.getC1id();
            }

            if (this.getIntValue() != another.getIntValue()) {
               return this.getIntValue() - another.getIntValue();
            }
         }

         return super.compareTo(o);
      }

      @Override
      public I_ExtendByRefPartCidInt<CidIntRevision> duplicate() {
         return (I_ExtendByRefPartCidInt<CidIntRevision>) super.duplicate();
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { getC1Nid(), getIntValue() });
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public int getC1id() {
         return getCv().getNid1();
      }

      @Override
      public int getNid1() {
         return getCv().getNid1();
      }

      RefexNidIntAnalogBI getCv() {
         return (RefexNidIntAnalogBI) cv;
      }

      @Override
      public TkRefexUuidIntMember getERefsetMember() throws IOException {
         return new TkRefexUuidIntMember(this, RevisionHandling.EXCLUDE_REVISIONS);
      }

      @Override
      public ERefsetCidIntRevision getERefsetRevision() throws IOException {
         return new ERefsetCidIntRevision(this);
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
      public void setC1id(int c1id) throws PropertyVetoException {
         getCv().setNid1(c1id);
      }

      @Override
      public void setNid1(int cnid1) throws PropertyVetoException {
         getCv().setNid1(cnid1);
      }

      @Override
      public void setInt1(int i) throws PropertyVetoException {
         getCv().setInt1(i);
      }

      @Override
      public void setIntValue(int intValue) throws PropertyVetoException {
         getCv().setInt1(intValue);
      }
   }
}
