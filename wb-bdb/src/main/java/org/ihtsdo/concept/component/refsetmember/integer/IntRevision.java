package org.ihtsdo.concept.component.refsetmember.integer;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
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
import org.ihtsdo.tk.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;

public class IntRevision extends RefsetRevision<IntRevision, IntMember>
        implements I_ExtendByRefPartInt<IntRevision>, RefexIntAnalogBI<IntRevision> {
   private int intValue;

   //~--- constructors --------------------------------------------------------

   public IntRevision() {
      super();
   }

   public IntRevision(int statusAtPositionNid, IntMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      intValue = primoridalMember.getIntValue();
   }

   public IntRevision(TkRefexIntRevision eVersion, IntMember member) {
      super(eVersion, member);
      this.intValue = eVersion.getInt1();
   }

   public IntRevision(TupleInput input, IntMember primoridalMember) {
      super(input, primoridalMember);
      intValue = input.readInt();
   }

   public IntRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, IntMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid,primoridalMember);
      intValue = primoridalMember.getIntValue();
   }

   protected IntRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, IntRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      intValue = another.intValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.INTEGER1, this.intValue);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (IntRevision.class.isAssignableFrom(obj.getClass())) {
         IntRevision another = (IntRevision) obj;

         return (intValue == another.intValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public IntRevision makeAnalog() {
      return new IntRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }

   @Override
   public IntRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      IntRevision newR = new IntRevision(statusNid, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<IntRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
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
      buf.append(" intValue:").append(this.intValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeInt(intValue);
   }

   //~--- get methods ---------------------------------------------------------

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
      return new TkRefexIntMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.INT;
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
   public IntMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (IntMember.Version) ((IntMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<IntMember.Version> getVersions() {
      return ((IntMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<IntRevision>> getVersions(ViewCoordinate c) {
      return ((IntMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setInt1(int l) throws PropertyVetoException {
      this.intValue = l;
      modified();
   }

   @Override
   public void setIntValue(int value) {
      this.intValue = value;
      modified();
   }
}
