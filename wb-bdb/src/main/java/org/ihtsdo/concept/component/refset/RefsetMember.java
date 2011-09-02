package org.ihtsdo.concept.component.refset;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.db.util.NidPairForRefset;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class RefsetMember<R extends RefsetRevision<R, C>, C extends RefsetMember<R, C>>
        extends ConceptComponent<R, C> implements I_ExtendByRef, RefexChronicleBI<R>, RefexAnalogBI<R> {
   public int                        referencedComponentNid;
   public int                        refsetNid;
   protected List<? extends Version> versions;

   //~--- constructors --------------------------------------------------------

   public RefsetMember() {
      super();
      referencedComponentNid = Integer.MAX_VALUE;
      refsetNid              = Integer.MAX_VALUE;
   }

   public RefsetMember(int enclosingConceptNid, TupleInput input) throws IOException {
      super(enclosingConceptNid, input);
   }

   public RefsetMember(TkRefsetAbstractMember<?> refsetMember, int enclosingConceptNid) throws IOException {
      super(refsetMember, enclosingConceptNid);
      refsetNid              = Bdb.uuidToNid(refsetMember.refsetUuid);
      referencedComponentNid = Bdb.uuidToNid(refsetMember.getComponentUuid());
      primordialSapNid       = Bdb.getSapNid(refsetMember);
      assert primordialSapNid != Integer.MAX_VALUE;
      assert referencedComponentNid != Integer.MAX_VALUE;
      assert refsetNid != Integer.MAX_VALUE;
   }

   //~--- methods -------------------------------------------------------------

   protected abstract void addSpecProperties(RefexCAB rcs);

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   public void addTuples(List<I_ExtendByRefVersion> returnTuples, Precedence precedencePolicy,
                         I_ManageContradiction contradictionManager)
           throws TerminologyException, IOException {
      List<RefsetMember<R, C>.Version> versionsToAdd = new ArrayList<RefsetMember<R, C>.Version>();

      getVersionComputer().addSpecifiedVersions(Terms.get().getActiveAceFrameConfig().getAllowedStatus(),
              Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly(), versionsToAdd,
              (List<Version>) getVersions(), precedencePolicy, contradictionManager);
      returnTuples.addAll((Collection<? extends I_ExtendByRefVersion<R>>) versionsToAdd);
   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positions,
                         List<I_ExtendByRefVersion> returnTuples, Precedence precedencePolicy,
                         I_ManageContradiction contradictionManager)
           throws TerminologyException, IOException {
      List<RefsetMember<R, C>.Version> versionsToAdd = new ArrayList<RefsetMember<R, C>.Version>();

      getVersionComputer().addSpecifiedVersions(allowedStatus, positions, versionsToAdd,
              (List<Version>) getVersions(), precedencePolicy, contradictionManager);
      returnTuples.addAll((Collection<? extends I_ExtendByRefVersion>) versionsToAdd);
   }

   @SuppressWarnings("unchecked")
   @Override
   public void addVersion(@SuppressWarnings("rawtypes") I_ExtendByRefPart part) {
      versions = null;
      super.addRevision((R) part);
   }

   @Override
   public void clearVersions() {
      versions = null;
      clearAnnotationVersions();
   }

   public final int compareTo(I_ExtendByRefPart<R> o) {
      if (getNid() != o.getNid()) {
         return getNid() - o.getNid();
      }

      return this.getSapNid() - o.getSapNid();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (RefsetMember.class.isAssignableFrom(obj.getClass())) {
         RefsetMember<?, ?> another = (RefsetMember<?, ?>) obj;

         return this.referencedComponentNid == another.referencedComponentNid;
      }

      return false;
   }

   @Override
   public boolean fieldsEqual(ConceptComponent<R, C> obj) {
      if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
         RefsetMember<R, C> another = (RefsetMember<R, C>) obj;

         if (this.getTypeNid() != another.getTypeNid()) {
            return false;
         }

         if (membersEqual(obj)) {
            return conceptComponentFieldsEqual(another);
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { referencedComponentNid });
   }

   public abstract R makeAnalog();

   @SuppressWarnings("unchecked")
   public I_ExtendByRefPart<R> makePromotionPart(PathBI promotionPath) {
      return (I_ExtendByRefPart<R>) makeAnalog(getStatusNid(), promotionPath.getConceptNid(), Long.MAX_VALUE);
   }

   protected abstract boolean membersEqual(ConceptComponent<R, C> obj);

   @SuppressWarnings("unchecked")
   public RefsetMember<R, C> merge(RefsetMember<R, C> component) throws IOException {
      return (RefsetMember<R, C>) super.merge((C) component);
   }

   private void moveRefset(Concept from, Concept to) throws IOException {
      if (from.isAnnotationStyleRefex()) {
         if (from.isAnnotationIndex()) {
            from.getData().getMemberNids().remove(this.nid);
            from.modified();
            BdbCommitManager.addUncommittedNoChecks(from);
         }
      } else {
         from.getRefsetMembers().remove(this);
         from.getData().getMemberNids().remove(this.nid);
         from.modified();
         BdbCommitManager.addUncommittedNoChecks(from);
      }

      if (to.isAnnotationStyleRefex()) {
         if (from.isAnnotationStyleRefex()) {

            // nothing to move.
         } else {
            Bdb.getComponent(this.referencedComponentNid).addAnnotation(this);
            Bdb.getNidCNidMap().resetCidForNid(Bdb.getNidCNidMap().getCNid(this.referencedComponentNid), nid);
            this.enclosingConceptNid = Bdb.getNidCNidMap().getCNid(this.referencedComponentNid);
            to.modified();
            BdbCommitManager.addUncommitted(to);
         }
      } else {
         if (from.isAnnotationStyleRefex()) {
            ComponentBI component = Bdb.getComponent(this.referencedComponentNid);

            if (component instanceof ConceptComponent) {
               ((ConceptComponent) component).getAnnotationsMod().remove(this);
            } else if (component instanceof Concept) {
               ((ConceptComponent) ((Concept) component).getConAttrs()).getAnnotationsMod().remove(this);
            }
         }

         to.getRefsetMembers().add(this);
         Bdb.getNidCNidMap().resetCidForNid(to.getNid(), nid);
         this.enclosingConceptNid = to.getNid();
         to.modified();
         BdbCommitManager.addUncommitted(to);
      }
   }

   @Override
   public boolean promote(PositionBI viewPosition, PathSetReadOnly pomotionPaths, NidSetBI allowedStatus,
                          Precedence precedence)
           throws IOException, TerminologyException {
      int                 viewPathId     = viewPosition.getPath().getConceptNid();
      Collection<Version> matchingTuples = getVersionComputer().getSpecifiedVersions(allowedStatus,
                                              viewPosition, getVersions(), precedence, null);
      boolean promotedAnything = false;

      for (PathBI promotionPath : pomotionPaths) {
         for (Version v : matchingTuples) {
            if (v.getPathNid() == viewPathId) {
               RefsetRevision<?, ?> revision = v.makeAnalog(v.getStatusNid(), promotionPath.getConceptNid(),
                                                  Long.MAX_VALUE);

               addVersion(revision);
               promotedAnything = true;
            }
         }
      }

      return promotedAnything;
   }

   @Override
   public void readFromBdb(TupleInput input) {
      refsetNid              = input.readInt();
      referencedComponentNid = input.readInt();
      assert refsetNid != Integer.MAX_VALUE;
      assert referencedComponentNid != Integer.MAX_VALUE;
      readMemberFields(input);

      int additionalVersionCount = input.readShort();

      if (additionalVersionCount > 0) {
         if (revisions == null) {
            revisions = new RevisionSet<R, C>(primordialSapNid);
         }

         for (int i = 0; i < additionalVersionCount; i++) {
            R r = readMemberRevision(input);

            if ((r.sapNid != -1) && (r.getTime() != Long.MIN_VALUE)) {
               revisions.add(r);
            }
         }
      }
   }

   protected abstract void readMemberFields(TupleInput input);

   protected abstract R readMemberRevision(TupleInput input);

   @Override
   public final boolean readyToWriteComponent() {
      assert referencedComponentNid != Integer.MAX_VALUE : assertionString();
      assert referencedComponentNid != 0 : assertionString();
      assert refsetNid != Integer.MAX_VALUE : assertionString();
      assert refsetNid != 0 : assertionString();
      assert readyToWriteRefsetMember() : assertionString();

      return true;
   }

   public abstract boolean readyToWriteRefsetMember();

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(" refset:");
      addNidToBuffer(buf, refsetNid);
      buf.append(" type:");

      try {
         buf.append(REFSET_TYPES.nidToType(getTypeNid()));
      } catch (IOException e) {
         buf.append(e.getLocalizedMessage());
      }

      buf.append(" rcNid:");
      addNidToBuffer(buf, referencedComponentNid);
      buf.append(" ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public String toUserString() {
      return toString();
   }

   @Override
   public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContraditionException {
      ComponentVersionBI c1Component = snapshot.getConceptVersion(refsetNid);

      return "refex: " + c1Component.toUserString(snapshot);
   }

   /**
    * Test method to check to see if two objects are equal in all respects.
    * @param another
    * @return either a zero length String, or a String containing a description of the
    * validation failures.
    * @throws IOException
    */
   public String validate(RefsetMember<?, ?> another) throws IOException {
      assert another != null;

      StringBuilder buf = new StringBuilder();

      if (this.referencedComponentNid != another.referencedComponentNid) {
         buf.append(
             "\tRefsetMember.referencedComponentNid not equal: \n"
             + "\t\tthis.referencedComponentNid = ").append(this.referencedComponentNid).append(
                 "\n" + "\t\tanother.referencedComponentNid = ").append(
                 another.referencedComponentNid).append("\n");
      }

      // Compare the parents
      buf.append(super.validate(another));

      return buf.toString();
   }

   protected abstract void writeMember(TupleOutput output);

   @Override
   public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
      List<RefsetRevision<R, C>> additionalVersionsToWrite = new ArrayList<RefsetRevision<R, C>>();

      if (revisions != null) {
         for (RefsetRevision<R, C> p : revisions) {
            if ((p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid)
                    && (p.getTime() != Long.MIN_VALUE)) {
               additionalVersionsToWrite.add(p);
            }
         }
      }

      assert refsetNid != Integer.MAX_VALUE;
      assert referencedComponentNid != Integer.MAX_VALUE;
      output.writeInt(refsetNid);
      output.writeInt(referencedComponentNid);
      writeMember(output);
      output.writeShort(additionalVersionsToWrite.size());

      NidPairForRefset npr = NidPair.getRefsetNidMemberNidPair(refsetNid, nid);

      Bdb.addXrefPair(referencedComponentNid, npr);

      for (RefsetRevision<R, C> p : additionalVersionsToWrite) {
         p.writePartToBdb(output);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getCollectionNid() {
      return refsetNid;
   }

   @Override
   public int getComponentId() {
      return referencedComponentNid;
   }

   @Override
   public int getComponentNid() {
      return referencedComponentNid;
   }

   @Override
   public int getMemberId() {
      return nid;
   }

   @Override
   public RefsetMember<R, C> getMutablePart() {
      return this;
   }

   @Override
   public List<? extends I_ExtendByRefPart<R>> getMutableParts() {
      return getVersions();
   }

   @Override
   public RefsetMember getPrimordialVersion() {
      return RefsetMember.this;
   }

   @Override
   public int getReferencedComponentNid() {
      return referencedComponentNid;
   }

   @Override
   public RefexCAB getRefexEditSpec() throws IOException {
      RefexCAB rcs = new RefexCAB(getTkRefsetType(), getReferencedComponentNid(), getRefsetId(),
                                  getPrimUuid());

      addSpecProperties(rcs);

      return rcs;
   }

   @Override
   public int getRefsetId() {
      return refsetNid;
   }

   protected abstract TK_REFSET_TYPE getTkRefsetType();

   @Override
   public List<Version> getTuples() {
      return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
   }

   @SuppressWarnings("rawtypes")
   @Override
   public List<? extends I_ExtendByRefVersion> getTuples(I_ManageContradiction contradictionMgr)
           throws TerminologyException, IOException {

      // TODO Implement contradictionMgr part...
      return getVersions();
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   public List<? extends I_ExtendByRefVersion> getTuples(I_IntSet allowedStatus,
           PositionSetReadOnly positions, Precedence precedencePolicy,
           I_ManageContradiction contradictionManager)
           throws TerminologyException, IOException {
      List<RefsetMember<R, C>.Version> versionsToAdd = new ArrayList<RefsetMember<R, C>.Version>();

      getVersionComputer().addSpecifiedVersions(allowedStatus, positions, versionsToAdd,
              (List<Version>) getVersions(), precedencePolicy, contradictionManager);

      return versionsToAdd;
   }

   @SuppressWarnings("deprecation")
   @Override
   public int getTypeNid() {
      return getTypeId();
   }

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public RefsetMember<R, C>.Version getVersion(ViewCoordinate c) throws ContraditionException {
      List<RefsetMember<R, C>.Version> vForC = getVersions(c);

      if (vForC.isEmpty()) {
         return null;
      }

      if (vForC.size() > 1) {
         vForC = c.getContradictionManager().resolveVersions(vForC);
      }

      if (vForC.size() > 1) {
         throw new ContraditionException(vForC.toString());
      }

      return vForC.get(0);
   }

   protected abstract VersionComputer<RefsetMember<R, C>.Version> getVersionComputer();

   @SuppressWarnings("unchecked")
   @Override
   public List<? extends Version> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<Version> list = new ArrayList<Version>(count);

         list.add(new Version(this));

         if (revisions != null) {
            for (RefsetRevision rv : revisions) {
               list.add(new Version(rv));
            }
         }

         versions = list;
      }

      return (List<Version>) versions;
   }

   @Override
   public List<RefsetMember<R, C>.Version> getVersions(ViewCoordinate c) {
      List<RefsetMember<R, C>.Version> returnTuples = new ArrayList<RefsetMember<R, C>.Version>(2);

      getVersionComputer().addSpecifiedVersions(c.getAllowedStatusNids(), (NidSetBI) null,
              c.getPositionSet(), returnTuples, getVersions(), c.getPrecedence(),
              c.getContradictionManager());

      return returnTuples;
   }

   @Override
   public boolean hasExtensions() throws IOException {
      return getEnclosingConcept().hasExtensionsForComponent(nid);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setCollectionNid(int collectionNid) throws PropertyVetoException {
      if ((this.refsetNid == Integer.MAX_VALUE) || (this.refsetNid == collectionNid)
              || (getTime() == Long.MAX_VALUE)) {
         if (this.refsetNid != collectionNid) {
            if ((this.refsetNid != 0) && (this.nid != 0)) {
               NidPairForRefset oldNpr = NidPair.getRefsetNidMemberNidPair(this.refsetNid, this.nid);

               Bdb.forgetXrefPair(this.referencedComponentNid, oldNpr);
            }

            // new xref is added on the dbWrite.
            this.refsetNid = collectionNid;
            modified();
         }
      } else {
         throw new PropertyVetoException("Cannot change refset unless member is uncommitted...", null);
      }
   }

   @Override
   public void setReferencedComponentNid(int referencedComponentNid) {
      if (this.referencedComponentNid != referencedComponentNid) {
         if ((this.refsetNid != 0) && (this.nid != 0)) {
            NidPairForRefset oldNpr = NidPair.getRefsetNidMemberNidPair(this.refsetNid, this.nid);

            Bdb.forgetXrefPair(this.referencedComponentNid, oldNpr);
         }

         // new xref is added on the dbWrite.
         this.referencedComponentNid = referencedComponentNid;
         modified();
      }
   }

   @Override
   public void setRefsetId(int refsetNid) throws IOException {
      if (getTime() == Long.MAX_VALUE) {
         if (this.refsetNid != refsetNid) {
            if ((this.refsetNid != 0) && (this.nid != 0)) {
               NidPairForRefset oldNpr = NidPair.getRefsetNidMemberNidPair(this.refsetNid, this.nid);

               Bdb.forgetXrefPair(this.referencedComponentNid, oldNpr);
            }

            int     oldRefsetNid = this.refsetNid;
            Concept oldRefset    = Concept.get(oldRefsetNid);
            Concept newRefset    = Concept.get(refsetNid);

            this.refsetNid = refsetNid;
            moveRefset(oldRefset, newRefset);
            modified();
         }
      } else {
         throw new UnsupportedOperationException("Cannot change refset unless member is uncommitted...");
      }
   }

   @Override
   public void setTypeId(int typeId) {
      if (typeId != getTypeNid()) {
         throw new UnsupportedOperationException();
      }
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends ConceptComponent<R, C>.Version
           implements I_ExtendByRefVersion<R>, I_ExtendByRefPart<R>, RefexAnalogBI<R> {
      public Version(RefexAnalogBI<R> cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @SuppressWarnings("unchecked")
      @Override
      public void addVersion(I_ExtendByRefPart<R> part) {
         versions = null;
         RefsetMember.this.addRevision((R) part);
      }

      @Override
      public int compareTo(I_ExtendByRefPart<R> o) {
         if (this.getNid() != o.getNid()) {
            return this.getNid() - o.getNid();
         }

         return this.getSapNid() - o.getSapNid();
      }

      @Override
      public I_ExtendByRefPart<R> duplicate() {
         throw new UnsupportedOperationException();
      }

      @Override
      public int hashCodeOfParts() {
         return 0;
      }

      public R makeAnalog() {
         if (RefsetMember.this != cv) {}

         return (R) RefsetMember.this.makeAnalog();
      }

      @Override
      @Deprecated
      public RefsetRevision<?, ?> makeAnalog(int statusNid, int pathNid, long time) {
         return getCv().makeAnalog(statusNid, getAuthorNid(), pathNid, time);
      }

      @Override
      public R makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
         return getCv().makeAnalog(statusNid, authorNid, pathNid, time);
      }

      @Override
      public I_ExtendByRefPart<R> makePromotionPart(PathBI promotionPath) {
         throw new UnsupportedOperationException();
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public int getCollectionNid() {
         return refsetNid;
      }

      @Override
      public int getComponentId() {
         return referencedComponentNid;
      }

      @Override
      public I_ExtendByRef getCore() {
         return RefsetMember.this;
      }

      RefexAnalogBI<R> getCv() {
         return (RefexAnalogBI<R>) cv;
      }

      public TkRefsetAbstractMember<?> getERefsetMember() throws TerminologyException, IOException {
         throw new UnsupportedOperationException("subclass must override");
      }

      public TkRevision getERefsetRevision() throws TerminologyException, IOException {
         throw new UnsupportedOperationException("subclass must override");
      }

      @Override
      public int getMemberId() {
         return nid;
      }

      @SuppressWarnings("unchecked")
      @Override
      public I_ExtendByRefPart<R> getMutablePart() {
         return (I_ExtendByRefPart<R>) super.getMutablePart();
      }

      @Override
      public RefsetMember getPrimordialVersion() {
         return RefsetMember.this;
      }

      @Override
      public int getReferencedComponentNid() {
         return RefsetMember.this.getReferencedComponentNid();
      }

      @Override
      public RefexCAB getRefexEditSpec() throws IOException {
         return getCv().getRefexEditSpec();
      }

      @Override
      public int getRefsetId() {
         return refsetNid;
      }

      @Override
      @Deprecated
      public int getStatus() {
         return getCv().getSapNid();
      }

      @Override
      public int getTypeId() {
         return RefsetMember.this.getTypeNid();
      }

      public int getTypeNid() {
         return RefsetMember.this.getTypeNid();
      }

      @Override
      public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
         throw new UnsupportedOperationException();
      }

      @Override
      public ArrayIntList getVariableVersionNids() {
         if (RefsetMember.this != getCv()) {
            return ((RefsetRevision) getCv()).getVariableVersionNids();
         } else {
            return RefsetMember.this.getVariableVersionNids();
         }
      }

      @Override
      public RefsetMember<R, C>.Version getVersion(ViewCoordinate c) throws ContraditionException {
         return RefsetMember.this.getVersion(c);
      }

      @Override
      public List<? extends I_ExtendByRefPart<R>> getVersions() {
         return RefsetMember.this.getVersions();
      }

      @Override
      public Collection<RefsetMember<R, C>.Version> getVersions(ViewCoordinate c) {
         return RefsetMember.this.getVersions(c);
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setCollectionNid(int collectionNid) throws PropertyVetoException {
         RefsetMember.this.setCollectionNid(collectionNid);
      }

      @Override
      public void setReferencedComponentNid(int componentNid) throws PropertyVetoException {
         RefsetMember.this.setReferencedComponentNid(componentNid);
      }

      @Override
      @Deprecated
      public void setStatus(int idStatus) throws PropertyVetoException {
          getCv().setStatusNid(idStatus);
      }
   }
}
