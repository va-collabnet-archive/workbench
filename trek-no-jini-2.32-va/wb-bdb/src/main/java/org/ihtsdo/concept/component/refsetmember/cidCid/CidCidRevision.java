package org.ihtsdo.concept.component.refsetmember.cidCid;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.refsetmember.cidCid.CidCidMember.Version;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid.TkRefexUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid.TkRefsetUuidUuidRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.dwfa.ace.api.Terms;

public class CidCidRevision extends RefsetRevision<CidCidRevision, CidCidMember>
        implements I_ExtendByRefPartCidCid<CidCidRevision>, RefexNidNidAnalogBI<CidCidRevision> {
   private int c1Nid;
   private int c2Nid;

   //~--- constructors --------------------------------------------------------

   public CidCidRevision() {
      super();
   }

   public CidCidRevision(int statusAtPositionNid, CidCidMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
      c2Nid = primoridalMember.getC2Nid();
   }

   public CidCidRevision(TkRefsetUuidUuidRevision eVersion, CidCidMember member) {
      super(eVersion, member);
      c1Nid = Bdb.uuidToNid(eVersion.getUuid1());
      c2Nid = Bdb.uuidToNid(eVersion.getUuid2());
   }

   public CidCidRevision(TupleInput input, CidCidMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid = input.readInt();
      c2Nid = input.readInt();
   }

   public CidCidRevision(int statusNid, long time, int authorNid,
           int moduleNid, int pathNid, CidCidMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
      c2Nid = primoridalMember.getC2Nid();
   }

   protected CidCidRevision(int statusNid, long time, int authorNid,
           int moduleNid, int pathNid, CidCidRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid = another.c1Nid;
      c2Nid = another.c2Nid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
      allNids.add(c2Nid);
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getNid1());
      rcs.with(RefexProperty.CNID2, getNid2());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidCidRevision.class.isAssignableFrom(obj.getClass())) {
         CidCidRevision another = (CidCidRevision) obj;

         if ((this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)) {
            return super.equals(obj);
         }
      }

      return false;
   }

   @Override
   public CidCidRevision makeAnalog() {
      return new CidCidRevision(getStatusNid(), getTime(), getAuthorNid(),
              getModuleNid(), getPathNid(), this);
   }
   
   @Override
   public CidCidRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      CidCidRevision newR = new CidCidRevision(statusNid, time,
              authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidCidRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
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

      buf.append(this.getClass().getSimpleName() + ":{");
      buf.append(" c1Nid: ");
      ConceptComponent.addNidToBuffer(buf, c1Nid);
      buf.append(" c2Nid: ");
      ConceptComponent.addNidToBuffer(buf, c2Nid);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeInt(c2Nid);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   public int getC1id() {
      return c1Nid;
   }

   public int getC2Nid() {
      return c2Nid;
   }

   @Override
   public int getC2id() {
      return c2Nid;
   }

   public int getNid1() {
      return c1Nid;
   }

   public int getNid2() {
      return c2Nid;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidUuidMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_CID;
   }

   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(4);

      variableNids.add(getC1id());
      variableNids.add(getC2id());

      return variableNids;
   }

   @Override
   public CidCidMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (Version) ((CidCidMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<CidCidMember.Version> getVersions() {
      return ((CidCidMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<CidCidRevision>> getVersions(ViewCoordinate c) {
      return ((CidCidMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setC1id(int c1id) {
      this.c1Nid = c1id;
      modified();
   }

   public void setC2Nid(int c2Nid) {
      this.c2Nid = c2Nid;
      modified();
   }

   @Override
   public void setC2id(int c2id) {
      this.c2Nid = c2id;
      modified();
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setNid2(int cnid) throws PropertyVetoException {
      this.c2Nid = cnid;
      modified();
   }
}
