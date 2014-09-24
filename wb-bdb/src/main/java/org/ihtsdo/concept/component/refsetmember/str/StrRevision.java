package org.ihtsdo.concept.component.refsetmember.str;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

public class StrRevision extends RefsetRevision<StrRevision, StrMember>
        implements I_ExtendByRefPartStr<StrRevision>, RefexStringAnalogBI<StrRevision> {
   private String stringValue;

   //~--- constructors --------------------------------------------------------

   public StrRevision() {
      super();
   }

   public StrRevision(int statusAtPositionNid, StrMember another) {
      super(statusAtPositionNid, another);
      stringValue = another.getString1Value();
   }

   public StrRevision(TkRefsetStrRevision eVersion, StrMember primoridalMember) {
      super(eVersion, primoridalMember);
      this.stringValue = eVersion.getString1();
   }

   public StrRevision(TupleInput input, StrMember primoridalMember) {
      super(input, primoridalMember);
      stringValue = input.readString();
   }

   public StrRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, StrMember another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another);
      stringValue = another.getString1Value();
   }

   protected StrRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, StrRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      stringValue = another.stringValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.STRING1, getString1());
   }

   @Override
   public StrRevision duplicate() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (StrRevision.class.isAssignableFrom(obj.getClass())) {
         StrRevision another = (StrRevision) obj;

         return stringValue.equals(another.stringValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public StrRevision makeAnalog() {
      return new StrRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public StrRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      StrRevision newR = new StrRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<StrRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      assert stringValue != null;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(" stringValue:" + "'").append(this.stringValue).append("' ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeString(stringValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getString1() {
      return stringValue;
   }

   @Override
   public String getString1Value() {
      return stringValue;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefsetStrMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.STR;
   }

   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      return new ArrayIntList(2);
   }

   @Override
   public StrMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (StrMember.Version) ((StrMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<StrMember.Version> getVersions() {
      return ((StrMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<StrRevision>> getVersions(ViewCoordinate c) {
      return ((StrMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setString1(String str) throws PropertyVetoException {
      this.stringValue = str;
      modified();
   }

   public void setString1Value(String stringValue) {
      this.stringValue = stringValue;
      modified();
   }
}
