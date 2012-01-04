package org.ihtsdo.concept.component.refsetmember.cidCid;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidCidMember;
import org.ihtsdo.etypes.ERefsetCidCidRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcid.TkRefsetCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcid.TkRefsetCidCidRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidVersionBI;

public class CidCidMember extends RefsetMember<CidCidRevision, CidCidMember>
        implements I_ExtendByRefPartCidCid<CidCidRevision>, RefexCnidCnidAnalogBI<CidCidRevision> {
   private static VersionComputer<RefsetMember<CidCidRevision, CidCidMember>.Version> computer =
      new VersionComputer<RefsetMember<CidCidRevision, CidCidMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private int c1Nid;
   private int c2Nid;

   //~--- constructors --------------------------------------------------------

   public CidCidMember() {
      super();
   }

   public CidCidMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public CidCidMember(TkRefsetCidCidMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());
      c2Nid = Bdb.uuidToNid(refsetMember.getC2Uuid());

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<CidCidRevision, CidCidMember>(primordialSapNid);

         for (TkRefsetCidCidRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new CidCidRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
      allNids.add(c2Nid);
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getCnid1());
      rcs.with(RefexProperty.CNID2, getCnid2());
   }

   @Override
   public CidCidMember duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidCidMember.class.isAssignableFrom(obj.getClass())) {
         CidCidMember another = (CidCidMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid) && (this.nid == another.nid)
                && (this.referencedComponentNid == another.referencedComponentNid);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid, c2Nid });
   }

   @Override
   public CidCidRevision makeAnalog() {
      return new CidCidRevision(getStatusNid(), getPathNid(), getTime(), this);
   }

   @Override
   public CidCidRevision makeAnalog(int statusNid, int pathNid, long time) {
      CidCidRevision newR = new CidCidRevision(statusNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   public CidCidRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      CidCidRevision newR = new CidCidRevision(statusNid, authorNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<CidCidRevision, CidCidMember> obj) {
      if (CidCidMember.class.isAssignableFrom(obj.getClass())) {
         CidCidMember another = (CidCidMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid);
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexCnidCnidVersionBI.class.isAssignableFrom(another.getClass())){
            RefexCnidCnidVersionBI cv = (RefexCnidCnidVersionBI) another;
            return (this.c1Nid == cv.getCnid1()) && (this.c2Nid == cv.getCnid2());
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      c1Nid = input.readInt();
      c2Nid = input.readInt();
   }

   @Override
   protected final CidCidRevision readMemberRevision(TupleInput input) {
      return new CidCidRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
      assert c1Nid != Integer.MAX_VALUE;
      assert c2Nid != Integer.MAX_VALUE;

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
      buf.append(" c1Nid: ");
      addNidToBuffer(buf, c1Nid);
      buf.append(" c2Nid: ");
      addNidToBuffer(buf, c2Nid);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeInt(c2Nid);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   public int getC1id() {
      return getC1Nid();
   }

   public int getC2Nid() {
      return c2Nid;
   }

   @Override
   public int getC2id() {
      return getC2Nid();
   }

   @Override
   public int getCnid1() {
      return c1Nid;
   }

   @Override
   public int getCnid2() {
      return c2Nid;
   }

   @Override
   public TkRefsetAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefsetCidCidMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.CID_CID;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.CID_CID.getTypeNid();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(4);

      variableNids.add(getC1id());
      variableNids.add(getC2id());

      return variableNids;
   }

   @Override
   protected VersionComputer<RefsetMember<CidCidRevision, CidCidMember>.Version> getVersionComputer() {
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
            for (CidCidRevision r : revisions) {
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

   public void setC2Nid(int c2Nid) {
      this.c2Nid = c2Nid;
      modified();
   }

   @Override
   public void setC2id(int c2id) {
      setC2Nid(c2id);
   }

   @Override
   public void setCnid1(int cnid1) throws PropertyVetoException {
      this.c1Nid = cnid1;
      modified();
   }

   @Override
   public void setCnid2(int cnid2) throws PropertyVetoException {
      this.c2Nid = cnid2;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<CidCidRevision, CidCidMember>.Version
           implements I_ExtendByRefVersion<CidCidRevision>, I_ExtendByRefPartCidCid<CidCidRevision>,
                      RefexCnidCnidAnalogBI<CidCidRevision> {
      private Version(RefexCnidCnidAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(RefexVersionBI o) {
         if (I_ExtendByRefPartCidCid.class.isAssignableFrom(o.getClass())) {
            I_ExtendByRefPartCidCid<CidCidRevision> another = (I_ExtendByRefPartCidCid<CidCidRevision>) o;

            if (this.getC1id() != another.getC1id()) {
               return this.getC1id() - another.getC1id();
            }

            if (this.getC2id() != another.getC2id()) {
               return this.getC2id() - another.getC2id();
            }
         }

         return super.compareTo(o);
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { getC1Nid(), getC2Nid() });
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public int getC1id() {
         return getCv().getCnid1();
      }

      @Override
      public int getC2id() {
         return getCv().getCnid2();
      }

      @Override
      public int getCnid1() {
         return getCv().getCnid1();
      }

      @Override
      public int getCnid2() {
         return getCv().getCnid2();
      }

      RefexCnidCnidAnalogBI getCv() {
         return (RefexCnidCnidAnalogBI) cv;
      }

      @Override
      public ERefsetCidCidMember getERefsetMember() throws IOException {
         return new ERefsetCidCidMember(this);
      }

      @Override
      public ERefsetCidCidRevision getERefsetRevision() throws IOException {
         return new ERefsetCidCidRevision(this);
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setC1id(int c1id) throws PropertyVetoException {
         getCv().setCnid1(c1id);
      }

      @Override
      public void setC2id(int c2id) throws PropertyVetoException {
         getCv().setCnid2(c2id);
      }

      @Override
      public void setCnid1(int cnid1) throws PropertyVetoException {
         getCv().setCnid1(cnid1);
      }

      @Override
      public void setCnid2(int cnid2) throws PropertyVetoException {
         getCv().setCnid2(cnid2);
      }
   }
}
