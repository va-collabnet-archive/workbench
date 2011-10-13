package org.ihtsdo.concept.component.refsetmember.cid;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.refsetmember.cid.CidMember.Version;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;

public class CidRevision extends RefsetRevision<CidRevision, CidMember>
        implements I_ExtendByRefPartCid<CidRevision>, RefexCnidAnalogBI<CidRevision> {
   private int c1Nid;

   //~--- constructors --------------------------------------------------------

   public CidRevision() {
      super();
   }

   protected CidRevision(int statusAtPositionNid, CidMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
   }

   public CidRevision(TkRefsetCidRevision eVersion, CidMember member) {
      super(eVersion, member);
      c1Nid = Bdb.uuidToNid(eVersion.getC1Uuid());
   }

   public CidRevision(TupleInput input, CidMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid = input.readInt();
   }

   protected CidRevision(int statusNid, int pathNid, long time, CidMember primoridalMember) {
      super(statusNid, pathNid, time, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
   }

   protected CidRevision(int statusNid, int pathNid, long time, CidRevision another) {
      super(statusNid, pathNid, time, another.primordialComponent);
      c1Nid = another.c1Nid;
   }

   protected CidRevision(int statusNid, int authorNid, int pathNid, long time, CidMember primoridalMember) {
      super(statusNid, authorNid, pathNid, time, primoridalMember);
      c1Nid = primoridalMember.getC1Nid();
   }

   protected CidRevision(int statusNid, int authorNid, int pathNid, long time, CidRevision another) {
      super(statusNid, authorNid, pathNid, time, another.primordialComponent);
      c1Nid = another.c1Nid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getCnid1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidRevision.class.isAssignableFrom(obj.getClass())) {
         CidRevision another = (CidRevision) obj;

         if (this.c1Nid == another.c1Nid) {
            return super.equals(obj);
         }
      }

      return false;
   }

   @Override
   public CidRevision makeAnalog() {
      return new CidRevision(getStatusNid(), getPathNid(), getTime(), this);
   }

   @Override
   public CidRevision makeAnalog(int statusNid, int pathNid, long time) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);

         return this;
      }

      CidRevision newR = new CidRevision(statusNid, pathNid, time, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public CidRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);

         return this;
      }

      CidRevision newR = new CidRevision(statusNid, authorNid, pathNid, time, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidRevision> makePromotionPart(PathBI promotionPath) {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
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

      buf.append(this.getClass().getSimpleName() + ":{");
      buf.append(" c1Nid: ");
      ConceptComponent.addNidToBuffer(buf, this.c1Nid);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getC1id() {
      return c1Nid;
   }

   public int getCnid1() {
      return c1Nid;
   }

   @Override
   public TkRefsetAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContraditionException, IOException {
      return new TkRefsetCidMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.CID;
   }

   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(3);

      variableNids.add(getC1id());

      return variableNids;
   }

   @Override
   public CidMember.Version getVersion(ViewCoordinate c) throws ContraditionException {
      return (Version) ((CidMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<CidMember.Version> getVersions() {
      return ((CidMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<CidRevision>> getVersions(ViewCoordinate c) {
      return ((CidMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setC1id(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   public void setCnid1(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }
}
