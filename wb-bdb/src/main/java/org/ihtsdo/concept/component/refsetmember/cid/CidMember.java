package org.ihtsdo.concept.component.refsetmember.cid;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.etypes.ERefsetCidMember;
import org.ihtsdo.etypes.ERefsetCidRevision;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;

public class CidMember extends RefsetMember<CidRevision, CidMember>
        implements I_ExtendByRefPartCid<CidRevision>, RefexCnidAnalogBI<CidRevision> {
   private static VersionComputer<RefsetMember<CidRevision, CidMember>.Version> computer =
      new VersionComputer<RefsetMember<CidRevision, CidMember>.Version>();

   //~--- fields --------------------------------------------------------------

   private int c1Nid;

   //~--- constructors --------------------------------------------------------

   public CidMember() {
      super();
   }

   public CidMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public CidMember(TkRefsetCidMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid = Bdb.uuidToNid(refsetMember.getC1Uuid());

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<CidRevision, CidMember>(primordialSapNid);

         for (TkRefsetCidRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new CidRevision(eVersion, this));
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
      rcs.with(RefexProperty.CNID1, getCnid1());
   }

   @Override
   public I_ExtendByRefPart<CidRevision> duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidMember.class.isAssignableFrom(obj.getClass())) {
         CidMember another = (CidMember) obj;

         return (this.c1Nid == another.c1Nid) && (this.nid == another.nid)
                && (this.referencedComponentNid == another.referencedComponentNid);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { c1Nid });
   }

   @Override
   public CidRevision makeAnalog() {
      CidRevision newR = new CidRevision(getStatusNid(), getPathNid(), getTime(), this);

      return newR;
   }

   @Override
   public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
      CidRevision newR = new CidRevision(statusNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   public CidRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      CidRevision newR = new CidRevision(statusNid, authorNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidRevision> makePromotionPart(PathBI promotionPath) {
      throw new UnsupportedOperationException();
   }

   @Override
   protected boolean refexFieldsEqual(ConceptComponent<CidRevision, CidMember> obj) {
      if (CidMember.class.isAssignableFrom(obj.getClass())) {
         CidMember another = (CidMember) obj;

         return this.c1Nid == another.c1Nid;
      }

      return false;
   }
   
   @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexCnidVersionBI.class.isAssignableFrom(another.getClass())){
            RefexCnidVersionBI cv = (RefexCnidVersionBI) another;
            return (this.c1Nid == cv.getCnid1());
        }
        return false;
    }

   @Override
   protected void readMemberFields(TupleInput input) {
      c1Nid = input.readInt();
   }

   @Override
   protected final CidRevision readMemberRevision(TupleInput input) {
      return new CidRevision(input, this);
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
      addNidToBuffer(buf, c1Nid);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
      ComponentVersionBI c1Component = snapshot.getComponentVersion(c1Nid);

      return super.toUserString(snapshot) + " c1: " + c1Component.toUserString(snapshot);
   }

   @Override
   protected void writeMember(TupleOutput output) {
      output.writeInt(c1Nid);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   @Deprecated
   public int getC1id() {
      return getC1Nid();
   }

   @Override
   public int getCnid1() {
      return c1Nid;
   }

   @Override
   public TkRefsetAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefsetCidMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.CID;
   }

   @Override
   @Deprecated
   public int getTypeId() {
      return REFSET_TYPES.CID.getTypeNid();
   }

   @Override
   protected ArrayIntList getVariableVersionNids() {

      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected VersionComputer<RefsetMember<CidRevision, CidMember>.Version> getVersionComputer() {
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
            for (CidRevision cr : revisions) {
               if (cr.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(cr));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   @Deprecated
   public void setC1id(int c1id) {
      setC1Nid(c1id);
   }

   @Override
   public void setCnid1(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<CidRevision, CidMember>.Version
           implements I_ExtendByRefVersion<CidRevision>, I_ExtendByRefPartCid<CidRevision>,
                      RefexCnidAnalogBI<CidRevision> {
      private Version(RefexCnidAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(I_ExtendByRefPart<CidRevision> o) {
         if (I_ExtendByRefPartCid.class.isAssignableFrom(o.getClass())) {
            I_ExtendByRefPartCid<CidRevision> another = (I_ExtendByRefPartCid<CidRevision>) o;

            if (this.getC1id() != another.getC1id()) {
               return this.getC1id() - another.getC1id();
            }
         }

         return super.compareTo(o);
      }

      @Override
      public int hashCodeOfParts() {
         return Hashcode.compute(new int[] { getC1Nid() });
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public int getC1id() {
         return getCv().getCnid1();
      }

      @Override
      public int getCnid1() {
         return getCv().getCnid1();
      }

      RefexCnidAnalogBI getCv() {
         return (RefexCnidAnalogBI) cv;
      }

      @Override
      public TkRefsetCidMember getERefsetMember() throws IOException {
         return new ERefsetCidMember(this, CidMember.this);
      }

      @Override
      public ERefsetCidRevision getERefsetRevision() throws IOException {
         return new ERefsetCidRevision(this);
      }

      @Override
      public ArrayIntList getVariableVersionNids() {
         ArrayIntList variableNids = new ArrayIntList(3);

         variableNids.add(getC1id());

         return variableNids;
      }

      //~--- set methods ------------------------------------------------------

      @Override
      @Deprecated
      public void setC1id(int c1id) throws PropertyVetoException {
         getCv().setCnid1(c1id);
      }

      @Override
      public void setCnid1(int c1id) throws PropertyVetoException {
         getCv().setCnid1(c1id);
      }
   }
}
