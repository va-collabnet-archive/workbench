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
import org.ihtsdo.etypes.ERefsetCidRevision;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;

public class CidMember extends RefsetMember<CidRevision, CidMember>
        implements I_ExtendByRefPartCid<CidRevision>, RefexNidAnalogBI<CidRevision> {
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

   public CidMember(TkRefexUuidMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      c1Nid = Bdb.uuidToNid(refsetMember.getUuid1());

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<CidRevision, CidMember>(primordialSapNid);

         for (TkRefexUuidRevision eVersion : refsetMember.getRevisionList()) {
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
      rcs.with(RefexProperty.CNID1, getNid1());
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
      CidRevision newR = new CidRevision(getStatusNid(), getTime(),
              getAuthorNid(), getModuleNid(), getPathNid(), this);

      return newR;
   }
   
   @Override
   public CidRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      CidRevision newR = new CidRevision(statusNid, time,
              authorNid, moduleNid, pathNid, this);
      addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidRevision> makePromotionPart(PathBI promotionPath, int authorNid) {
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
        if(RefexNidVersionBI.class.isAssignableFrom(another.getClass())){
            RefexNidVersionBI cv = (RefexNidVersionBI) another;
            return (this.c1Nid == cv.getNid1());
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
   public int getNid1() {
      return c1Nid;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID;
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

      return Collections.unmodifiableList((List<Version>) versions);
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
   public void setNid1(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends RefsetMember<CidRevision, CidMember>.Version
           implements I_ExtendByRefVersion<CidRevision>, I_ExtendByRefPartCid<CidRevision>,
                      RefexNidAnalogBI<CidRevision> {
      private Version(RefexNidAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(RefexVersionBI o) {
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
         return getCv().getNid1();
      }

      @Override
      public int getNid1() {
         return getCv().getNid1();
      }

      RefexNidAnalogBI getCv() {
         return (RefexNidAnalogBI) cv;
      }

      @Override
      public TkRefexUuidMember getERefsetMember() throws IOException {
         return new TkRefexUuidMember(this, RevisionHandling.EXCLUDE_REVISIONS);
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
         getCv().setNid1(c1id);
      }

      @Override
      public void setNid1(int c1id) throws PropertyVetoException {
         getCv().setNid1(c1id);
      }
   }
}
