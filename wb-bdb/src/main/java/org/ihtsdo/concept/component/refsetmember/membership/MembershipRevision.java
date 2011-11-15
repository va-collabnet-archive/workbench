package org.ihtsdo.concept.component.refsetmember.membership;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetMember;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.type_member.RefexMemberAnalogBI;

public class MembershipRevision extends RefsetRevision<MembershipRevision, MembershipMember> 
    implements RefexMemberAnalogBI<MembershipRevision> {
   public MembershipRevision() {
      super();
   }

   public MembershipRevision(int statusAtPositionNid, MembershipMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
   }

   public MembershipRevision(TkRefsetRevision eVersion, MembershipMember member) {
      super(eVersion, member);
   }

   public MembershipRevision(TupleInput input, MembershipMember primoridalMember) {
      super(input, primoridalMember);
   }

   public MembershipRevision(int statusNid, int pathNid, long time, MembershipMember primoridalMember) {
      super(statusNid, pathNid, time, primoridalMember);
   }

   protected MembershipRevision(int statusNid, int pathNid, long time, MembershipRevision another) {
      super(statusNid, pathNid, time, another.primordialComponent);
   }

   public MembershipRevision(int statusNid, int authorNid, int pathNid, long time,
                             MembershipMember primoridalMember) {
      super(statusNid, authorNid, pathNid, time, primoridalMember);
   }

   protected MembershipRevision(int statusNid, int authorNid, int pathNid, long time,
                                MembershipRevision another) {
      super(statusNid, pathNid, time, another.primordialComponent);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

   protected void addSpecProperties(RefexCAB rcs) {

      // no fields to add...
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (MembershipRevision.class.isAssignableFrom(obj.getClass())) {
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public MembershipRevision makeAnalog() {
      return new MembershipRevision(getStatusNid(), getPathNid(), getTime(), this);
   }

   @Override
   public MembershipRevision makeAnalog(int statusNid, int pathNid, long time) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);

         return this;
      }

      MembershipRevision newR = new MembershipRevision(statusNid, pathNid, time, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public MembershipRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);

         return this;
      }

      MembershipRevision newR = new MembershipRevision(statusNid, authorNid, pathNid, time, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public RefsetRevision<MembershipRevision, MembershipMember> makePromotionPart(PathBI promotionPath) {

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
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeFieldsToBdb(TupleOutput output) {

      // nothing to write
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public TkRefsetAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContraditionException, IOException {
      return new TkRefsetMember(this, exclusionSet, conversionMap, 0, true, vc);
   }

   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.MEMBER;
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
   public MembershipMember.Version getVersion(ViewCoordinate c) throws ContraditionException {
      return (MembershipMember.Version) ((MembershipMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<MembershipMember.Version> getVersions() {
      return ((MembershipMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<MembershipRevision>> getVersions(ViewCoordinate c) {
      return ((MembershipMember) primordialComponent).getVersions(c);
   }
}
