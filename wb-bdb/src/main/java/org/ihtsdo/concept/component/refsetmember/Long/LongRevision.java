package org.ihtsdo.concept.component.refsetmember.Long;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.refsetmember.Long.LongMember.Version;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.type_long.TkRefexLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_long.TkRefexLongRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class LongRevision extends RefsetRevision<LongRevision, LongMember>
        implements I_ExtendByRefPartLong<LongRevision>, RefexLongAnalogBI<LongRevision> {
   private long longValue;

   //~--- constructors --------------------------------------------------------

   public LongRevision() {
      super();
   }

   public LongRevision(int statusAtPositionNid, LongMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      longValue = primoridalMember.getLongValue();
   }

   public LongRevision(TkRefexLongRevision eVersion, LongMember member) {
      super(eVersion, member);
      this.longValue = eVersion.getLong1();
   }

   public LongRevision(TupleInput input, LongMember primoridalMember) {
      super(input, primoridalMember);
      longValue = input.readLong();
   }

   public LongRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, LongMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      longValue = primoridalMember.getLongValue();
   }

   protected LongRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, LongRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      longValue = another.longValue;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      // ;
   }

   @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexProperty.LONG1, getLong1());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (LongRevision.class.isAssignableFrom(obj.getClass())) {
         LongRevision another = (LongRevision) obj;

         return (this.longValue == another.longValue) && super.equals(obj);
      }

      return false;
   }

   @Override
   public LongRevision makeAnalog() {
      return new LongRevision(getStatusNid(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);
   }
   
   @Override
   public LongRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
       if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }
      LongRevision newR = new LongRevision(statusNid, time, authorNid,
              moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<LongRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

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
      buf.append(" longValue:").append(this.longValue);
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {
      output.writeLong(longValue);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public long getLong1() {
      return longValue;
   }

   @Override
   public long getLongValue() {
      return longValue;
   }

   @Override
   public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContradictionException, IOException {
      return new TkRefexLongMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   @Override
   protected TK_REFEX_TYPE getTkRefsetType() {
      return TK_REFEX_TYPE.LONG;
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
   public LongMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (Version) ((LongMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<LongMember.Version> getVersions() {
      return ((LongMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<LongRevision>> getVersions(ViewCoordinate c) {
      return ((LongMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLong1(long l) throws PropertyVetoException {
      this.longValue = l;
      modified();
   }

   public void setLongValue(long longValue) {
      this.longValue = longValue;
      modified();
   }
}
