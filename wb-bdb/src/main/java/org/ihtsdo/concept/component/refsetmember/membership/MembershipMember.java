package org.ihtsdo.concept.component.refsetmember.membership;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetMember;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.tk.hash.Hashcode;

public class MembershipMember extends RefsetMember<MembershipRevision, MembershipMember> {
   private static VersionComputer<RefsetMember<MembershipRevision, MembershipMember>.Version> computer =
      new VersionComputer<RefsetMember<MembershipRevision, MembershipMember>.Version>();

   //~--- constructors --------------------------------------------------------

   public MembershipMember() {
      super();
   }

   public MembershipMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public MembershipMember(TkRefsetMember refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);

      if (refsetMember.getRevisionList() != null) {
         revisions = new RevisionSet<MembershipRevision, MembershipMember>(primordialSapNid);

         for (TkRefsetRevision eVersion : refsetMember.getRevisionList()) {
            revisions.add(new MembershipRevision(eVersion, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

    @Override
   protected void addSpecProperties(RefexCAB rcs) {

      // no fields to add...
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (MembershipMember.class.isAssignableFrom(obj.getClass())) {
         MembershipMember another = (MembershipMember) obj;

         return this.nid == another.nid;
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { this.nid });
   }

   @Override
   public MembershipRevision makeAnalog() {
      MembershipRevision newR = new MembershipRevision(getStatusNid(), getPathNid(), getTime(), this);

      return newR;
   }

   @Override
   public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
      MembershipRevision newR = new MembershipRevision(statusNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   public MembershipRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
      MembershipRevision newR = new MembershipRevision(statusNid, authorNid, pathNid, time, this);

      addRevision(newR);

      return newR;
   }

   @Override
   protected boolean membersEqual(ConceptComponent<MembershipRevision, MembershipMember> obj) {
      if (MembershipMember.class.isAssignableFrom(obj.getClass())) {
         return true;
      }

      return false;
   }

   @Override
   protected void readMemberFields(TupleInput input) {

      // nothing to read...
   }

   @Override
   protected final MembershipRevision readMemberRevision(TupleInput input) {
      return new MembershipRevision(input, this);
   }

   @Override
   public boolean readyToWriteRefsetMember() {
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
      buf.append(" }=> ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeMember(TupleOutput output) {

      // nothing to write
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   protected TK_REFSET_TYPE getTkRefsetType() {
      return TK_REFSET_TYPE.MEMBER;
   }

   @Override
   public int getTypeId() {
      return REFSET_TYPES.MEMBER.getTypeNid();
   }

   @Override
   protected ArrayIntList getVariableVersionNids() {
      return new ArrayIntList(2);
   }

   @Override
   protected VersionComputer<RefsetMember<MembershipRevision,
           MembershipMember>.Version> getVersionComputer() {
      return computer;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<Version> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<Version> list = new ArrayList<Version>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new Version(this));
         }

         if (revisions != null) {
            for (MembershipRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }
}
