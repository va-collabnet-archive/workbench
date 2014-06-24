package org.ihtsdo.concept.component.refsetmember.Boolean;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.refsetmember.Boolean.BooleanMember.Version;
import org.ihtsdo.etypes.ERefsetBooleanMember;
import org.ihtsdo.etypes.ERefsetBooleanRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.dwfa.ace.api.Terms;

public class BooleanRevision extends RefsetRevision<BooleanRevision, BooleanMember>
        implements I_ExtendByRefPartBoolean<BooleanRevision>, RefexBooleanAnalogBI<BooleanRevision> {
   private boolean booleanValue;

   //~--- constructors --------------------------------------------------------

   public BooleanRevision() {
      super();
   }

   protected BooleanRevision(int statusAtPositionNid, BooleanMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      this.booleanValue = primoridalMember.getBooleanValue();
   }

   public BooleanRevision(TkRefexBooleanRevision eVersion, BooleanMember booleanMember) {
      super(eVersion, booleanMember);
      this.booleanValue = eVersion.isBooleanValue();
   }

   public BooleanRevision(TupleInput input, BooleanMember primoridalMember) {
      super(input, primoridalMember);
      booleanValue = input.readBoolean();
   }

   protected BooleanRevision(int statusNid, long time, int authorNid, int moduleNid,
           int pathNid, BooleanMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      this.booleanValue = primoridalMember.getBooleanValue();
   }

   protected BooleanRevision(int statusNid, long time, int authorNid, int moduleNid,
           int pathNid, BooleanRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      this.booleanValue = another.getBooleanValue();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      // ;
   }

   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.BOOLEAN1, getBoolean1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (BooleanRevision.class.isAssignableFrom(obj.getClass())) {
         BooleanRevision another = (BooleanRevision) obj;

         return (this.booleanValue == another.booleanValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public BooleanRevision makeAnalog() {
      return new BooleanRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }
   
   @Override
   public BooleanRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      BooleanRevision newR = new BooleanRevision(statusNid, time, authorNid,
              moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<BooleanRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

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
      buf.append(" booleanValue:").append(this.booleanValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeBoolean(booleanValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public boolean getBoolean1() {
      return this.booleanValue;
   }

   public boolean getBooleanValue() {
      return booleanValue;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexBooleanMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.BOOLEAN;
   }

   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(2);

      return variableNids;
   }

   @Override
   public BooleanMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (Version) ((BooleanMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<BooleanMember.Version> getVersions() {
      return ((BooleanMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<BooleanRevision>> getVersions(ViewCoordinate c) {
      return ((BooleanMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setBoolean1(boolean l) throws PropertyVetoException {
      this.booleanValue = l;
      modified();
   }

   public void setBooleanValue(boolean booleanValue) {
      this.booleanValue = booleanValue;
      modified();
   }
}
