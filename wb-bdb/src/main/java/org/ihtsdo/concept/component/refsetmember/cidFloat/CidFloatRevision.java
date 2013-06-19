package org.ihtsdo.concept.component.refsetmember.cidFloat;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
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
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

public class CidFloatRevision extends RefsetRevision<CidFloatRevision, CidFloatMember>
        implements RefexNidFloatAnalogBI<CidFloatRevision> {
   private int   c1Nid;
   private float floatValue;

   //~--- constructors --------------------------------------------------------

   public CidFloatRevision() {
      super();
   }

   public CidFloatRevision(int statusAtPositionNid, CidFloatMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid      = primoridalMember.getC1Nid();
      floatValue = primoridalMember.getFloatValue();
   }

   public CidFloatRevision(TkRefexUuidFloatRevision eVersion, CidFloatMember member) {
      super(eVersion, member);
      c1Nid      = Bdb.uuidToNid(eVersion.getUuid1());
      floatValue = eVersion.getFloat1();
   }

   public CidFloatRevision(TupleInput input, CidFloatMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid      = input.readInt();
      floatValue = input.readFloat();
   }

   public CidFloatRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, 
                           CidFloatMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid      = primoridalMember.getC1Nid();
      floatValue = primoridalMember.getFloatValue();
   }

   protected CidFloatRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid,
                              CidFloatRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid      = another.c1Nid;
      floatValue = another.floatValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getNid1());
      rcs.with(RefexProperty.FLOAT1, getFloat1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidFloatRevision.class.isAssignableFrom(obj.getClass())) {
         CidFloatRevision another = (CidFloatRevision) obj;

         return (this.c1Nid == another.c1Nid) && (this.floatValue == another.floatValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public CidFloatRevision makeAnalog() {
      return new CidFloatRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }

   @Override
   public CidFloatRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      CidFloatRevision newR = new CidFloatRevision(statusNid, time, authorNid, moduleNid, pathNid,this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidFloatRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

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
      buf.append(" floatValue:" + this.floatValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeFloat(floatValue);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   public int getNid1() {
      return c1Nid;
   }

   @Override
   public float getFloat1() {
      return this.floatValue;
   }

   public float getFloatValue() {
      return floatValue;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidFloatMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

    @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_FLOAT;
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
   public CidFloatMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (CidFloatMember.Version) ((CidFloatMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<CidFloatMember.Version> getVersions() {
      return ((CidFloatMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<CidFloatRevision>> getVersions(ViewCoordinate c) {
      return ((CidFloatMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
      modified();
   }

   @Override
   public void setNid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setFloat1(float f) throws PropertyVetoException {
      this.floatValue = f;
      modified();
   }

   public void setFloatValue(float floatValue) {
      this.floatValue = floatValue;
      modified();
   }
}
