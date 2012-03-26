package org.ihtsdo.concept.component.refsetmember.cidStr;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
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
import org.ihtsdo.tk.api.refex.type_cnid_str.RefexCnidStrAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidstr.TkRefsetCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidstr.TkRefsetCidStrRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

public class CidStrRevision extends RefsetRevision<CidStrRevision, CidStrMember>
        implements I_ExtendByRefPartCidString<CidStrRevision>, RefexCnidStrAnalogBI<CidStrRevision> {
   private int    c1Nid;
   private String strValue;

   //~--- constructors --------------------------------------------------------

   public CidStrRevision() {
      super();
   }

   public CidStrRevision(int statusAtPositionNid, CidStrMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      strValue = primoridalMember.getStringValue();
   }

   public CidStrRevision(TkRefsetCidStrRevision eVersion, CidStrMember member) {
      super(eVersion, member);
      c1Nid    = Bdb.uuidToNid(eVersion.getC1Uuid());
      strValue = eVersion.getStrValue();
   }

   public CidStrRevision(TupleInput input, CidStrMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid    = input.readInt();
      strValue = input.readString();
   }

   public CidStrRevision(int statusNid, int pathNid, long time, CidStrMember primoridalMember) {
      super(statusNid, pathNid, time, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      strValue = primoridalMember.getStringValue();
   }

   protected CidStrRevision(int statusNid, int pathNid, long time, CidStrRevision another) {
      super(statusNid, pathNid, time, another.primordialComponent);
      c1Nid    = another.c1Nid;
      strValue = another.strValue;
   }

   public CidStrRevision(int statusNid, int authorNid, int pathNid, long time,
                         CidStrMember primoridalMember) {
      super(statusNid, authorNid, pathNid, time, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      strValue = primoridalMember.getStringValue();
   }

   protected CidStrRevision(int statusNid, int authorNid, int pathNid, long time, CidStrRevision another) {
      super(statusNid, authorNid, pathNid, time, another.primordialComponent);
      c1Nid    = another.c1Nid;
      strValue = another.strValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
   }

   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getCnid1());
      rcs.with(RefexProperty.STRING1, getStr1());
   }

   @Override
   public CidStrRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidStrRevision.class.isAssignableFrom(obj.getClass())) {
         CidStrRevision another = (CidStrRevision) obj;

         return (this.c1Nid == another.c1Nid) && this.strValue.equals(another.strValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public CidStrRevision makeAnalog() {
      return new CidStrRevision(getStatusNid(), getPathNid(), getTime(), this);
   }

   @Override
   public CidStrRevision makeAnalog(int statusNid, int pathNid, long time) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);

         return this;
      }

      CidStrRevision newR = new CidStrRevision(statusNid, pathNid, time, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public CidStrRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);

         return this;
      }

      CidStrRevision newR = new CidStrRevision(statusNid, authorNid, pathNid, time, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidStrRevision> makePromotionPart(PathBI promotionPath) {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert c1Nid != Integer.MAX_VALUE;
      assert strValue != null;

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
      buf.append(" strValue:" + "'").append(this.strValue).append("'");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeString(strValue);
   }

   //~--- get methods ---------------------------------------------------------

   public int getC1Nid() {
      return c1Nid;
   }

   @Override
   public int getC1id() {
      return c1Nid;
   }

   @Override
   public int getCnid1() {
      return c1Nid;
   }

   @Override
   public String getStr1() {
      return strValue;
   }

   @Override
   public String getStringValue() {
      return strValue;
   }

   @Override
   public TkRefsetAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefsetCidStrMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.CID_STR;
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
   public CidStrMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (CidStrMember.Version) ((CidStrMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<CidStrMember.Version> getVersions() {
      return ((CidStrMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<CidStrRevision>> getVersions(ViewCoordinate c) {
      return ((CidStrMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Nid(int c1Nid) {
      this.c1Nid = c1Nid;
   }

   @Override
   public void setC1id(int c1id) {
      this.c1Nid = c1id;
      modified();
   }

   @Override
   public void setCnid1(int cnid) throws PropertyVetoException {
      this.c1Nid = cnid;
      modified();
   }

   @Override
   public void setStr1(String str) throws PropertyVetoException {
      this.strValue = str;
      modified();
   }

   public void setStringValue(String strValue) {
      this.strValue = strValue;
      modified();
   }
}