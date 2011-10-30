package org.ihtsdo.concept.component.relationship.group;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.vodb.types.Position;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RelGroupVersion implements RelGroupVersionBI {
   private long                time = Long.MIN_VALUE;
   private int                 authorNid;
   private ViewCoordinate      coordinate;
   private int                 pathNid;
   private RelGroupChronicleBI rg;
   private int                 statusNid;

   //~--- constructors --------------------------------------------------------

   public RelGroupVersion(RelGroupChronicleBI rg, ViewCoordinate coordinate) {
      assert rg != null;
      assert coordinate != null;
      this.rg         = rg;
      this.coordinate = new ViewCoordinate(coordinate);
      setupLatest();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
      return rg.addAnnotation(annotation);
   }

   @Override
   public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
      return rg.makeAdjudicationAnalogs(ec, vc);
   }

   @Override
   public boolean sapIsInRange(int min, int max) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   private void setupLatest() {
      time = Long.MIN_VALUE;

      for (RelationshipVersionBI relV : getAllCurrentRelVersions()) {
         if (relV.getTime() > time) {
            time      = relV.getTime();
            authorNid = relV.getAuthorNid();
            pathNid   = relV.getPathNid();
            statusNid = relV.getStatusNid();
         }
      }

      if (time == Long.MIN_VALUE) {
         for (RelationshipChronicleBI rel : getRels()) {
            for (RelationshipVersionBI relV : rel.getVersions(coordinate)) {
               if (relV.getTime() > time) {
                  time      = relV.getTime();
                  authorNid = relV.getAuthorNid();
                  pathNid   = relV.getPathNid();
                  statusNid = relV.getStatusNid();
               }
            }
         }
      }
   }

   @Override
   public String toUserString() {
      StringBuilder buff = new StringBuilder();

      buff.append("Group: ");

      for (RelationshipChronicleBI rel : getRels()) {
         buff.append(rel.toUserString());
         buff.append("; ");
      }

      return buff.toString();
   }

   @Override
   public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContraditionException {
      return toUserString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<? extends IdBI> getAdditionalIds() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getAllCurrentRelVersions() {
      ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();

      for (RelationshipChronicleBI relc : rg.getRels()) {
         if (coordinate != null) {
            for (RelationshipVersionBI rv : relc.getVersions(coordinate)) {
               if ((rv.getGroup() == rg.getRelGroup())
                       && coordinate.getAllowedStatusNids().contains(rv.getStatusNid())) {
                  results.add(rv);
               }
            }
         } else {
            for (RelationshipVersionBI rv : relc.getVersions()) {
               if (rv.getGroup() == rg.getRelGroup()) {
                  results.add(rv);
               }
            }
         }
      }

      return results;
   }

   @Override
   public Collection<? extends IdBI> getAllIds() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Set<Integer> getAllNidsForVersion() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getAllRels() throws ContraditionException {
      ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();

      for (RelationshipChronicleBI relc : rg.getRels()) {
         if (coordinate != null) {
            RelationshipVersionBI rv = relc.getVersion(coordinate.getVcWithAllStatusValues());

            if (rv != null) {
               if (rv.getGroup() == rg.getRelGroup()) {
                  results.add(rv);
               }
            }
         } else {
            for (RelationshipVersionBI rv : relc.getVersions()) {
               if (rv.getGroup() == rg.getRelGroup()) {
                  results.add(rv);
               }
            }
         }
      }

      return results;
   }

   @Override
   public Set<Integer> getAllSapNids() throws IOException {
      return rg.getAllSapNids();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException {
      return rg.getAnnotations();
   }

   @Override
   public int getAuthorNid() {
      return authorNid;
   }

   @Override
   public ComponentChroncileBI getChronicle() {
      return rg;
   }

   @Override
   public int getConceptNid() {
      return rg.getConceptNid();
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentAnnotationMembers(ViewCoordinate xyz)
           throws IOException {
      return rg.getCurrentAnnotationMembers(xyz);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentAnnotationMembers(ViewCoordinate xyz,
           int refexNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz, int refexNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefexMembers(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      return rg.getCurrentRefexMembers(xyz, refsetNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz) throws IOException {
      return rg.getCurrentRefexes(xyz);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getCurrentRels() throws ContraditionException {
      ArrayList<RelationshipVersionBI> results = new ArrayList<RelationshipVersionBI>();

      for (RelationshipChronicleBI relc : rg.getRels()) {
         if (coordinate != null) {
            RelationshipVersionBI rv = relc.getVersion(coordinate);

            if (rv != null) {
               if ((rv.getGroup() == rg.getRelGroup())
                       && coordinate.getAllowedStatusNids().contains(rv.getStatusNid())) {
                  results.add(rv);
               }
            }
         } else {
            for (RelationshipVersionBI rv : relc.getVersions()) {
               if (rv.getGroup() == rg.getRelGroup()) {
                  results.add(rv);
               }
            }
         }
      }

      return results;
   }

   @Override
   public ConceptChronicleBI getEnclosingConcept() {
      return rg.getEnclosingConcept();
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getInactiveRefexes(ViewCoordinate xyz) throws IOException {
      return rg.getInactiveRefexes(xyz);
   }

   @Override
   public int getNid() {
      return rg.getNid();
   }

   @Override
   public int getPathNid() {
      return pathNid;
   }

   @Override
   public PositionBI getPosition() throws IOException {
      return new Position(getTime(), Ts.get().getPath(getPathNid()));
   }

   @Override
   public Set<PositionBI> getPositions() throws IOException {
      return rg.getPositions();
   }

   @Override
   public UUID getPrimUuid() {
      return rg.getPrimUuid();
   }

   @Override
   public RelGroupVersionBI getPrimordialVersion() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
      return rg.getRefexes();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
      return rg.getRefexes(refsetNid);
   }

   @Override
   public int getRelGroup() {
      return rg.getRelGroup();
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRels() {
      return rg.getRels();
   }

   @Override
   public int getSapNid() {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getStatusNid() {
      return statusNid;
   }

   @Override
   public long getTime() {
      return time;
   }

   @Override
   public List<UUID> getUUIDs() {
      return rg.getUUIDs();
   }

   @Override
   public RelGroupVersionBI getVersion(ViewCoordinate c) throws ContraditionException {
      return rg.getVersion(c);
   }

   @Override
   public List<RelGroupVersion> getVersions() {
      return Arrays.asList(new RelGroupVersion[] { new RelGroupVersion(this, null) });
   }

   @Override
   public Collection<? extends RelGroupVersionBI> getVersions(ViewCoordinate c) {
      return Arrays.asList(new RelGroupVersionBI[] { new RelGroupVersion(this, c) });
   }

   @Override
   public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isActive() throws IOException {
      return isActive(coordinate);
   }

   @Override
   public boolean isActive(NidSetBI allowedStatusNids) {
      return allowedStatusNids.contains(statusNid);
   }

   @Override
   public boolean isActive(ViewCoordinate vc) throws IOException {
      return vc.getAllowedStatusNids().contains(statusNid);
   }

   @Override
   public boolean isBaselineGeneration() {
      for (RelationshipChronicleBI rc : getRels()) {
         for (RelationshipVersionBI rv : rc.getVersions()) {
            if (!rv.isBaselineGeneration()) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public boolean isUncommitted() {
      return false;
   }
}
