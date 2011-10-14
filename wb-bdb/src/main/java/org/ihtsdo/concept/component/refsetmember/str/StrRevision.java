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
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

public class StrRevision extends RefsetRevision<StrRevision, StrMember>
        implements I_ExtendByRefPartStr<StrRevision>, RefexStrAnalogBI<StrRevision> {
   private String stringValue;

   //~--- constructors --------------------------------------------------------

   public StrRevision() {
      super();
   }

   public StrRevision(int statusAtPositionNid, StrMember another) {
      super(statusAtPositionNid, another);
      stringValue = another.getStringValue();
   }

   public StrRevision(TkRefsetStrRevision eVersion, StrMember primoridalMember) {
      super(eVersion, primoridalMember);
      this.stringValue = eVersion.getStringValue();
   }

   public StrRevision(TupleInput input, StrMember primoridalMember) {
      super(input, primoridalMember);
      stringValue = input.readString();
   }

   public StrRevision(int statusNid, int pathNid, long time, StrMember another) {
      super(statusNid, pathNid, time, another);
      stringValue = another.getStringValue();
   }

   protected StrRevision(int statusNid, int pathNid, long time, StrRevision another) {
      super(statusNid, pathNid, time, another.primordialComponent);
      stringValue = another.stringValue;
   }

   public StrRevision(int statusNid, int authorNid, int pathNid, long time, StrMember another) {
      super(statusNid, authorNid, pathNid, time, another);
      stringValue = another.getStringValue();
   }

   protected StrRevision(int statusNid, int authorNid, int pathNid, long time, StrRevision another) {
      super(statusNid, authorNid, pathNid, time, another.primordialComponent);
      stringValue = another.stringValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.STRING1, getStr1());
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
      return new StrRevision(getStatusNid(), getPathNid(), getTime(), this);
   }

   @Override
   public StrRevision makeAnalog(int statusNid, int pathNid, long time) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);

         return this;
      }

      StrRevision newR = new StrRevision(statusNid, pathNid, time, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public StrRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);

         return this;
      }

      StrRevision newR = new StrRevision(statusNid, authorNid, pathNid, time, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<StrRevision> makePromotionPart(PathBI promotionPath) {

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
   public String getStr1() {
      return stringValue;
   }

   @Override
   public String getStringValue() {
      return stringValue;
   }

   @Override
   public TkRefsetAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContraditionException, IOException {
      return new TkRefsetStrMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.STR;
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
   public StrMember.Version getVersion(ViewCoordinate c) throws ContraditionException {
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
   public void setStr1(String str) throws PropertyVetoException {
      this.stringValue = str;
      modified();
   }

   public void setStringValue(String stringValue) {
      this.stringValue = stringValue;
      modified();
   }
}
