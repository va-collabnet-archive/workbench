package org.ihtsdo.concept.component.refsetmember.cidLong;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
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
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

public class CidLongRevision extends RefsetRevision<CidLongRevision, CidLongMember>
        implements I_ExtendByRefPartCidLong<CidLongRevision>, RefexNidLongAnalogBI<CidLongRevision> {
   private int  c1Nid;
   private long longValue;

   //~--- constructors --------------------------------------------------------

   public CidLongRevision() {
      super();
   }

   protected CidLongRevision(int statusAtPositionNid, CidLongMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid     = primoridalMember.getC1Nid();
      longValue = primoridalMember.getLongValue();
   }

   public CidLongRevision(TkRefexUuidLongRevision eVersion, CidLongMember member) {
      super(eVersion, member);
      c1Nid     = Bdb.uuidToNid(eVersion.getUuid1());
      longValue = eVersion.getLong1();
   }

   public CidLongRevision(TupleInput input, CidLongMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid     = input.readInt();
      longValue = input.readLong();
   }

   protected CidLongRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, 
                             CidLongMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid     = primoridalMember.getC1Nid();
      longValue = primoridalMember.getLongValue();
   }

   protected CidLongRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid,
           CidLongRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid     = another.c1Nid;
      longValue = another.longValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getNid1());
      rcs.with(RefexProperty.LONG1, getLong1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidLongRevision.class.isAssignableFrom(obj.getClass())) {
         CidLongRevision another = (CidLongRevision) obj;

         return (this.c1Nid == another.c1Nid) && (longValue == another.longValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public CidLongRevision makeAnalog() {
      return new CidLongRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public CidLongRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      CidLongRevision newR = new CidLongRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidLongRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

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

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" c1Nid: ");
      ConceptComponent.addNidToBuffer(buf, c1Nid);
      buf.append(" longValue:").append(this.longValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeLong(longValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getC1id() {
      return c1Nid;
   }

   @Override
   public int getNid1() {
      return c1Nid;
   }

   @Override
   public long getLong1() {
      return this.longValue;
   }

   @Override
   public long getLongValue() {
      return longValue;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidLongMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_LONG;
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
   public CidLongMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (CidLongMember.Version) ((CidLongMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<CidLongMember.Version> getVersions() {
      return ((CidLongMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<CidLongRevision>> getVersions(ViewCoordinate c) {
      return ((CidLongMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

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
   public void setLong1(long l) throws PropertyVetoException {
      this.longValue = l;
      modified();
   }

   @Override
   public void setLongValue(long longValue) {
      this.longValue = longValue;
      modified();
   }
}
