package org.ihtsdo.concept.component.refsetmember.cidCidStr;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_string.RefexNidNidStringAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_string.TkRefexUuidUuidStringMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_string.TkRefexUuidUuidStringRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

public class CidCidStrRevision extends RefsetRevision<CidCidStrRevision, CidCidStrMember>
        implements I_ExtendByRefPartCidCidString<CidCidStrRevision>,
                   RefexNidNidStringAnalogBI<CidCidStrRevision> {
   private int    c1Nid;
   private int    c2Nid;
   private String strValue;

   //~--- constructors --------------------------------------------------------

   public CidCidStrRevision() {
      super();
   }

   public CidCidStrRevision(int statusAtPositionNid, CidCidStrMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      c2Nid    = primoridalMember.getC2Nid();
      strValue = primoridalMember.getString1Value();
   }

   public CidCidStrRevision(TkRefexUuidUuidStringRevision eVersion, CidCidStrMember member) {
      super(eVersion, member);
      c1Nid    = Bdb.uuidToNid(eVersion.getUuid1());
      c2Nid    = Bdb.uuidToNid(eVersion.getUuid2());
      strValue = eVersion.getString1();
   }

   public CidCidStrRevision(TupleInput input, CidCidStrMember primoridalMember) {
      super(input, primoridalMember);
      c1Nid    = input.readInt();
      c2Nid    = input.readInt();
      strValue = input.readString();
   }

   public CidCidStrRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid,
                            CidCidStrMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      c1Nid    = primoridalMember.getC1Nid();
      c2Nid    = primoridalMember.getC2Nid();
      strValue = primoridalMember.getString1Value();
   }

   protected CidCidStrRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, 
                               CidCidStrRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      c1Nid    = another.c1Nid;
      c2Nid    = another.c2Nid;
      strValue = another.strValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {
      allNids.add(c1Nid);
      allNids.add(c2Nid);
   }

   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.CNID1, getNid1());
      rcs.with(RefexProperty.CNID2, getNid2());
      rcs.with(RefexProperty.STRING1, getString1());
   }

   @Override
   public CidCidStrRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (CidCidStrRevision.class.isAssignableFrom(obj.getClass())) {
         CidCidStrRevision another = (CidCidStrRevision) obj;

         return (this.c1Nid == another.c1Nid) && (this.c2Nid == another.c2Nid)
                && this.strValue.equals(another.strValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public CidCidStrRevision makeAnalog() {
      return new CidCidStrRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }

   @Override
   public CidCidStrRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      CidCidStrRevision newR = new CidCidStrRevision(statusNid, time, authorNid, moduleNid, pathNid,this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<CidCidStrRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert c1Nid != Integer.MAX_VALUE;
      assert c2Nid != Integer.MAX_VALUE;
      assert strValue != null;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName() + ":{");
      buf.append(" c1Nid:" + this.c1Nid);
      buf.append(" c2Nid:" + this.c2Nid);
      buf.append(" strValue:" + "'" + this.strValue + "'");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(c1Nid);
      output.writeInt(c2Nid);
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
   public String getString1() {
      return this.strValue;
   }

   public String getStrValue() {
      return strValue;
   }

   @Override
   public String getString1Value() {
      return strValue;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexUuidUuidStringMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.CID_CID_STR;
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
   public CidCidStrMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (CidCidStrMember.Version) ((CidCidStrMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<CidCidStrMember.Version> getVersions() {
      return ((CidCidStrMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<CidCidStrRevision>> getVersions(ViewCoordinate c) {
      return ((CidCidStrMember) primordialComponent).getVersions(c);
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

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.strValue = str;
      modified();
   }

   public void setStrValue(String strValue) {
      this.strValue = strValue;
      modified();
   }

   @Override
   public void setString1Value(String value) {
      this.strValue = value;
      modified();
   }
}
