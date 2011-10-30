package org.ihtsdo.concept.component;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.vodb.types.Position;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.concept.Concept;

public abstract class Revision<V extends Revision<V, C>, C extends ConceptComponent<V, C>>
        implements I_AmPart<V>, I_HandleFutureStatusAtPositionSetup, AnalogBI {
   public static SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

   //~--- fields --------------------------------------------------------------

   public C   primordialComponent;
   public int sapNid;

   //~--- constructors --------------------------------------------------------

   public Revision() {
      super();
   }

   public Revision(int statusAtPositionNid, C primordialComponent) {
      super();
      assert primordialComponent != null;
      assert statusAtPositionNid != 0;
      this.sapNid              = statusAtPositionNid;
      this.primordialComponent = primordialComponent;
      primordialComponent.clearVersions();
      assert primordialComponent != null;
      assert statusAtPositionNid != Integer.MAX_VALUE;
      this.primordialComponent.getEnclosingConcept().modified();
   }

   public Revision(TupleInput input, C conceptComponent) {
      this(input.readInt(), conceptComponent);
      conceptComponent.clearVersions();
      assert sapNid != 0;
   }

   public Revision(int statusNid, int authorNid, int pathNid, long time, C primordialComponent) {
      this.sapNid = Bdb.getSapDb().getSapNid(statusNid, authorNid, pathNid, time);
      assert sapNid != 0;
      this.primordialComponent = primordialComponent;
      primordialComponent.clearVersions();
      assert primordialComponent != null;
      assert sapNid != Integer.MAX_VALUE;
      this.primordialComponent.getEnclosingConcept().modified();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addAnnotation(@SuppressWarnings("rawtypes") RefexChronicleBI annotation)
           throws IOException {
      return primordialComponent.addAnnotation(annotation);
   }

   abstract protected void addComponentNids(Set<Integer> allNids);

   @Override
   public boolean addLongId(Long longId, int authorityNid, int statusNid, EditCoordinate ec, long time) {
      return primordialComponent.addLongId(longId, authorityNid, statusNid, ec, time);
   }

   protected String assertionString() {
      try {
         return Ts.get().getConcept(primordialComponent.enclosingConceptNid).toLongString();
      } catch (IOException ex) {
         Logger.getLogger(ConceptComponent.class.getName()).log(Level.SEVERE, null, ex);
      }

      return toString();
   }

   @SuppressWarnings("unchecked")
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (Revision.class.isAssignableFrom(obj.getClass())) {
         Revision<V, C> another = (Revision<V, C>) obj;

         if (this.sapNid == another.sapNid) {
            return true;
         }
      }

      return false;
   }

   @Override
   public final int hashCode() {
      return Hashcode.compute(primordialComponent.nid);
   }

   public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
      return primordialComponent.makeAdjudicationAnalogs(ec, vc);
   }

   /**
    * 1. Analog, an object, concept or situation which in some way
    *    resembles a different situation
    * 2. Analogy, in language, a comparison between concepts
    * @param statusNid
    * @param pathNid
    * @param time
    * @return
    */
   @Override
   public abstract V makeAnalog(int statusNid, int pathNid, long time);

   @Override
   public abstract V makeAnalog(int statusNid, int authorNid, int pathNid, long time);

   protected void modified() {
      if (primordialComponent != null) {
         primordialComponent.modified();
      }
   }

   public final boolean readyToWrite() {
      assert primordialComponent != null : assertionString();
      assert sapNid != Integer.MAX_VALUE : assertionString();
      assert(sapNid > 0) || (sapNid == -1);

      return true;
   }

   public abstract boolean readyToWriteRevision();

   @Override
   public boolean sapIsInRange(int min, int max) {
      return (sapNid >= min) && (sapNid <= max);
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(" sap:");
      buf.append(sapNid);

      try {
         buf.append(" status:");
         ConceptComponent.addNidToBuffer(buf, getStatusNid());
         buf.append(" author:");
         ConceptComponent.addNidToBuffer(buf, getAuthorNid());
         buf.append(" path:");
         ConceptComponent.addNidToBuffer(buf, getPathNid());
         buf.append(" tm: ");
         buf.append(TimeUtil.formatDate(getTime()));
         buf.append(" ");
         buf.append(getTime());
      } catch (Throwable e) {
         buf.append(" !!! Invalid sapNid. Cannot compute path, time, status. !!! ");
         buf.append(e.getLocalizedMessage());
      }

      buf.append(" };");

      return buf.toString();
   }

   @Override
   public abstract String toUserString();

   @Override
   public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContraditionException {
      return toUserString();
   }

   /**
    * Test method to check to see if two objects are equal in all respects.
    * @param another
    * @return either a zero length String, or a String containing a
    * description of the validation failures.
    * @throws IOException
    */
   public String validate(Revision<?, ?> another) throws IOException {
      assert another != null;

      StringBuilder buf = new StringBuilder();

      if (this.sapNid != another.sapNid) {
         buf.append("\t\tRevision.sapNid not equal: \n\t\t\tthis.sapNid = ").append(this.sapNid).append(
             "\n\t\t\tanother.sapNid = ").append(another.sapNid).append("\n");
      }

      if (!this.primordialComponent.equals(another.primordialComponent)) {
         buf.append(
             "\t\tRevision.primordialComponent not equal: " + "\n\t\t\tthis.primordialComponent = ").append(
             this.primordialComponent).append("\n\t\t\tanother.primordialComponent = ").append(
             another.primordialComponent).append("\n");
      }

      return buf.toString();
   }

   protected abstract void writeFieldsToBdb(TupleOutput output);

   public final void writePartToBdb(TupleOutput output) {
      output.writeInt(sapNid);
      writeFieldsToBdb(output);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<? extends IdBI> getAdditionalIds() {
      return primordialComponent.getAdditionalIds();
   }

   @Override
   public Collection<? extends IdBI> getAllIds() {
      return primordialComponent.getAllIds();
   }

   @Override
   public Set<Integer> getAllNidsForVersion() throws IOException {
      HashSet<Integer> allNids = new HashSet<Integer>();

      allNids.add(primordialComponent.nid);
      allNids.add(getStatusNid());
      allNids.add(getAuthorNid());
      allNids.add(getPathNid());
      addComponentNids(allNids);

      return allNids;
   }

   public Set<Integer> getAllSapNids() throws IOException {
      return primordialComponent.getAllSapNids();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
      return primordialComponent.getAnnotations();
   }

   @Override
   public int getAuthorNid() {
      return Bdb.getSapDb().getAuthorNid(sapNid);
   }

   @Override
   public ComponentChroncileBI getChronicle() {
      return (ComponentChroncileBI) primordialComponent;
   }

   @Override
   public int getConceptNid() {
      return primordialComponent.enclosingConceptNid;
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentAnnotationMembers(ViewCoordinate xyz)
           throws IOException {
      return primordialComponent.getCurrentAnnotationMembers(xyz);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentAnnotationMembers(ViewCoordinate xyz,
           int refexNid)
           throws IOException {
      return primordialComponent.getCurrentAnnotationMembers(xyz, refexNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz)
           throws IOException {
      return getCurrentAnnotationMembers(xyz);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz, int refexNid)
           throws IOException {
      return getCurrentAnnotationMembers(xyz, refexNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefexMembers(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      return primordialComponent.getCurrentRefexMembers(xyz, refsetNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz) throws IOException {
      return primordialComponent.getCurrentRefexes(xyz);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      return primordialComponent.getCurrentRefexes(xyz, refsetNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getInactiveRefexes(ViewCoordinate xyz) throws IOException {
      return getChronicle().getInactiveRefexes(xyz);
   }

   @Override
   public final int getNid() {
      return primordialComponent.getNid();
   }

   @Override
   public final ArrayIntList getPartComponentNids() {
      ArrayIntList resultList = getVariableVersionNids();

      resultList.add(getPathNid());
      resultList.add(getStatusNid());

      return resultList;
   }

   @Override
   @Deprecated
   public int getPathId() {
      return Bdb.getSapDb().getPathNid(sapNid);
   }

   @Override
   public int getPathNid() {
      return Bdb.getSapDb().getPathNid(sapNid);
   }

   @Override
   public PositionBI getPosition() throws IOException {
      return new Position(getTime(), Ts.get().getPath(getPathNid()));
   }

   public Set<PositionBI> getPositions() throws IOException {
      return primordialComponent.getPositions();
   }

   @Override
   public UUID getPrimUuid() {
      return primordialComponent.getPrimUuid();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
      return primordialComponent.getRefexMembers(refsetNid);
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
      return primordialComponent.getRefexes();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
      return primordialComponent.getRefexes(refsetNid);
   }

   @Override
   public int getSapNid() {
      return sapNid;
   }

   public final int getStatusAtPositionNid() {
      return sapNid;
   }

   @Override
   @Deprecated
   public int getStatusId() {
      return Bdb.getSapDb().getStatusNid(sapNid);
   }

   @Override
   public int getStatusNid() {
      return Bdb.getSapDb().getStatusNid(sapNid);
   }

   @Override
   public long getTime() {
      return Bdb.getSapDb().getTime(sapNid);
   }

   @Deprecated
   public final Set<TimePathId> getTimePathSet() {
      return primordialComponent.getTimePathSet();
   }

   @Override
   public final List<UUID> getUUIDs() {
      return primordialComponent.getUUIDs();
   }

   public abstract ArrayIntList getVariableVersionNids();

   @Override
   @Deprecated
   public int getVersion() {
      return Bdb.getSapDb().getVersion(sapNid);
   }

   public final C getVersioned() {
      return primordialComponent;
   }

   @Override
   public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      return primordialComponent.hasCurrentAnnotationMember(xyz, refsetNid);
   }

   @Override
   public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      return primordialComponent.hasCurrentRefexMember(xyz, refsetNid);
   }

   @Override
   public boolean isActive(NidSetBI allowedStatusNids) {
      return allowedStatusNids.contains(getStatusNid());
   }

   @Override
   public boolean isActive(ViewCoordinate vc) {
      return isActive(vc.getAllowedStatusNids());
   }

   @Override
   public boolean isBaselineGeneration() {
      return sapNid <= Bdb.getSapDb().getReadOnlyMax();
   }

   @Override
   public boolean isSetup() {
      return sapNid != Integer.MAX_VALUE;
   }

   public boolean isUncommitted() {
      return getTime() == Long.MAX_VALUE;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setAuthorNid(int authorNid) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Cannot change status if time != Long.MAX_VALUE; "
                 + "Use makeAnalog instead.");
      }

      if (authorNid != getPathNid()) {
         this.sapNid = Bdb.getSapNid(getStatusNid(), authorNid, getPathNid(), Long.MAX_VALUE);
         modified();
      }
   }

   @Override
   public final void setNid(int nid) throws PropertyVetoException {
      throw new PropertyVetoException("nid", null);
   }

   @Override
   @Deprecated
   public final void setPathId(int pathId) {
      setPathNid(pathId);
   }

   @Override
   public final void setPathNid(int pathId) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Cannot change status if time != Long.MAX_VALUE; "
                 + "Use makeAnalog instead.");
      }

      this.sapNid = Bdb.getSapNid(getStatusNid(), Terms.get().getAuthorNid(), pathId, Long.MAX_VALUE);
   }

   public void setStatusAtPosition(int statusNid, int authorNid, int pathNid, long time) {
      this.sapNid = Bdb.getSapDb().getSapNid(statusNid, authorNid, pathNid, time);
      modified();
   }

   @Override
   public void setStatusAtPositionNid(int sapNid) {
      assert sapNid != 0;
      this.sapNid = sapNid;
      modified();
   }

   @Override
   @Deprecated
   public final void setStatusId(int statusNid) {
      setStatusNid(statusNid);
      modified();
   }

   @Override
   public final void setStatusNid(int statusNid) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Cannot change status if time != Long.MAX_VALUE; "
                 + "Use makeAnalog instead.");
      }

      try {
         this.sapNid = Bdb.getSapNid(statusNid, Terms.get().getAuthorNid(), getPathNid(), Long.MAX_VALUE);
      } catch (Exception e) {
         throw new RuntimeException();
      }

      modified();
   }

   @Override
   public final void setTime(long time) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Cannot change status if time != Long.MAX_VALUE; "
                 + "Use makeAnalog instead.");
      }

      if (time != getTime()) {
         try {
            this.sapNid = Bdb.getSapNid(getStatusNid(), Terms.get().getAuthorNid(), getPathNid(), time);
         } catch (Exception e) {
            throw new RuntimeException();
         }

         modified();
      }
   }
   
   public Concept getEnclosingConcept() {
        return primordialComponent.getEnclosingConcept();
   }

}
