package org.ihtsdo.concept.component.refsetmember.cidCidCid;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid.TkRefexUuidUuidUuidRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.dwfa.ace.api.Terms;

public class CidCidCidRevision extends RefsetRevision<CidCidCidRevision, CidCidCidMember>
        implements I_ExtendByRefPartCidCidCid<CidCidCidRevision>,
                   RefexNidNidNidAnalogBI<CidCidCidRevision> {
   private int c1Nid;
   private int c2Nid;
   private int c3Nid;

   //~--- constructors --------------------------------------------------------

   public CidCidCidRevision() {
      super();
   }

   public CidCidCidRevision(int statusAtPositionNid, CidCidCidMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
      c2Nid = primoridalMember.getC2Nid();
      c3Nid = primoridalMember.getC3Nid();
   }

   public CidCidCidRevision(TkRefexUuidUuidUuidRevision eVersion, CidCidCidMember member) {
      super(eVersion, member);
      c1Nid = Bdb.uuidToNid(eVersion.getUuid1());
      c2Nid = Bdb.uuidToNid(eVersion.getUuid2());
      c3Nid = Bdb.uuidToNid(eVersion.getUuid3());
   }

   public CidCidCidRevision(TupleInput input, CidCidCidMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid = input.readInt();
      c2Nid = input.readInt();
      c3Nid = input.readInt();
   }
   
   public CidCidCidRevision(int statusNid, long time, int authorNid, int moduleNid,
           int pathNid, CidCidCidMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
      c2Nid = primoridalMember.getC2Nid();
      c3Nid = primoridalMember.getC3Nid();
   }

   protected CidCidCidRevision(int statusNid, long time, int authorNid, int moduleNid,
           int pathNid, CidCidCidRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid = another.c1Nid;
      c2Nid = another.c2Nid;
      c3Nid = another.c3Nid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
      allNids.add(c2Nid);
      allNids.add(c3Nid);
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getNid1());
      rcs.with(RefexProperty.CNID2, getNid2());
      rcs.with(RefexProperty.CNID3, getNid3());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidCidCidRevision.class.isAssignableFrom(obj.getClass())) {
         CidCidCidRevision another = (CidCidCidRevision) obj;

         return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)
                && (this.c3Nid == another.c3Nid) && super.equals(obj);
      }

      return false;
   }

   @Override
   public CidCidCidRevision makeAnalog() {
      return new CidCidCidRevision(getStatusNid(), getTime(), 
              getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }
   
   @Override
   public CidCidCidRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      CidCidCidRevision newR = new CidCidCidRevision(statusNid, time,
              authorNid, moduleNid, pathNid,this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidCidCidRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert c1Nid != Integer.MAX_VALUE;
      assert c2Nid != Integer.MAX_VALUE;
      assert c3Nid != Integer.MAX_VALUE;

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
      ConceptComponent.addNidToBuffer(buf, c1Nid);
      buf.append(" c2Nid: ");
      ConceptComponent.addNidToBuffer(buf, c2Nid);
      buf.append(" c3Nid: ");
      ConceptComponent.addNidToBuffer(buf, c3Nid);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeInt(c2Nid);
      output.writeInt(c3Nid);
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

   public int getC3Nid() {
      return c3Nid;
   }

   @Override
   public int getC3id() {
      return c3Nid;
   }

   @Override
   public int getNid1() {
      return c1Nid;
   }

   @Override
   public int getNid2() {
      return c2Nid;
   }

   @Override
   public int getNid3() {
      return c3Nid;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidUuidUuidMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_CID_CID;
   }

   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(5);

      variableNids.add(getC1id());
      variableNids.add(getC2id());
      variableNids.add(getC3id());

      return variableNids;
   }

   @Override
   public CidCidCidMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (CidCidCidMember.Version) ((CidCidCidMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<CidCidCidMember.Version> getVersions() {
      return ((CidCidCidMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<CidCidCidRevision>> getVersions(ViewCoordinate c) {
      return ((CidCidCidMember) primordialComponent).getVersions(c);
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

   public void setC3Nid(int c3Nid) {
      this.c3Nid = c3Nid;
      modified();
   }

   @Override
   public void setC3id(int c3nid) {
      this.c3Nid = c3nid;
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

   @Override
   public void setNid3(int cnid) throws PropertyVetoException {
      this.c3Nid = cnid;
      modified();
   }
}
