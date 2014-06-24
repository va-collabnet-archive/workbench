package org.ihtsdo.concept.component.refsetmember.cidInt;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

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
import org.ihtsdo.tk.api.refex.type_nid_int.RefexNidIntAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

public class CidIntRevision extends RefsetRevision<CidIntRevision, CidIntMember>
        implements I_ExtendByRefPartCidInt<CidIntRevision>, RefexNidIntAnalogBI<CidIntRevision> {
   private int c1Nid;
   private int intValue;

   //~--- constructors --------------------------------------------------------

   public CidIntRevision() {
      super();
   }

   protected CidIntRevision(int statusAtPositionNid, CidIntMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      intValue = primoridalMember.getIntValue();
   }

   public CidIntRevision(TkRefexUuidIntRevision eVersion, CidIntMember member) {
      super(eVersion, member);
      c1Nid    = Bdb.uuidToNid(eVersion.getUuid1());
      intValue = eVersion.getInt1();
   }

   public CidIntRevision(TupleInput input, CidIntMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid    = input.readInt();
      intValue = input.readInt();
   }

   protected CidIntRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, 
                            CidIntMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      intValue = primoridalMember.getIntValue();
   }

   protected CidIntRevision(int statusNid, long time, int authorNid, int moduleNid,
           int pathNid, CidIntRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid    = another.c1Nid;
      intValue = another.intValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getNid1());
      rcs.with(RefexProperty.INTEGER1, getInt1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidIntRevision.class.isAssignableFrom(obj.getClass())) {
         CidIntRevision another = (CidIntRevision) obj;

         return (this.c1Nid == another.c1Nid) && (this.intValue == another.intValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public CidIntRevision makeAnalog() {
      return new CidIntRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }

   @Override
   public CidIntRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      CidIntRevision newR = new CidIntRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidIntRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

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
      ConceptComponent.addNidToBuffer(buf, c1Nid);
      buf.append(" intValue:" + this.intValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeInt(intValue);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   public int getC1id() {
      return c1Nid;
   }

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

   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_INT;
   }

   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(3);

      variableNids.add(getC1Nid());

      return variableNids;
   }

   @Override
   public CidIntMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (CidIntMember.Version) ((CidIntMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<CidIntMember.Version> getVersions() {
      return ((CidIntMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<CidIntRevision>> getVersions(ViewCoordinate c) {
      return ((CidIntMember) primordialComponent).getVersions(c);
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

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
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
      modified();
   }
}
