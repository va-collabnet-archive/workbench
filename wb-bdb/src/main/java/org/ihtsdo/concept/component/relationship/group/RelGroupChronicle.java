package org.ihtsdo.concept.component.relationship.group;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.util.id.Type5UuidFactory;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RelGroupChronicle implements RelationshipGroupChronicleBI {
   private int                                 conceptNid;
   private int                                 nid;
   private int                                 relGroup;
   private Collection<RelationshipChronicleBI> rels;
   private UUID                                uuid;

   //~--- constructors --------------------------------------------------------

   public RelGroupChronicle(Concept c, int relGroup, Collection<RelationshipChronicleBI> rels)
           throws IOException {
      super();
      this.relGroup   = relGroup;
      this.conceptNid = c.getNid();

      try {
         uuid = Type5UuidFactory.get(Type5UuidFactory.REL_GROUP_NAMESPACE,
                                     c.getPrimUuid().toString() + relGroup);
      } catch (NoSuchAlgorithmException e) {
         throw new IOException(e);
      }

      nid = Bdb.uuidToNid(uuid);
      Bdb.getNidCNidMap().setCNidForNid(conceptNid, nid);
      this.rels = rels;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addAnnotation(RefexChronicleBI<?> annotation) {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public String toUserString() {
      StringBuilder buff = new StringBuilder();

      buff.append("Group: ");

      for (RelationshipChronicleBI rc : rels) {
         buff.append(rc.toUserString());
         buff.append(";");
      }

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<? extends IdBI> getAdditionalIds() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends IdBI> getAllIds() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Set<Integer> getAllStampNids() throws IOException {
      HashSet<Integer> sapNids = new HashSet<Integer>();

      for (RelationshipChronicleBI r : rels) {
         sapNids.addAll(r.getAllStampNids());
      }

      return sapNids;
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public int getConceptNid() {
      return conceptNid;
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
           throws IOException {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate xyz, int refexNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz, int refexNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate xyz) throws IOException {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate xyz) throws IOException {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public int getNid() {
      return nid;
   }

   @Override
   public Set<PositionBI> getPositions() throws IOException {
      Set<PositionBI> positions = new HashSet<PositionBI>();

      for (RelationshipChronicleBI rc : rels) {
         positions.addAll(rc.getPositions());
      }

      return positions;
   }

   @Override
   public UUID getPrimUuid() {
      if (uuid == null) {
         return UUID.fromString("00000000-0000-0000-C000-000000000046");
      }

      return uuid;
   }

   @Override
   public RelationshipGroupVersionBI getPrimordialVersion() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public int getRelationshipGroupNumber() {
      return relGroup;
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRelationships() {
      return rels;
   }

   @Override
   public List<UUID> getUUIDs() {
      return Arrays.asList(new UUID[] { uuid });
   }

   @Override
   public RelationshipGroupVersionBI getVersion(ViewCoordinate c) throws ContradictionException {
      return new RelGroupVersion(this, c);
   }

   @Override
   public Collection<? extends RelationshipGroupVersionBI> getVersions() {
      return Arrays.asList(new RelationshipGroupVersionBI[] { new RelGroupVersion(this, null) });
   }

   @Override
   public Collection<? extends RelationshipGroupVersionBI> getVersions(ViewCoordinate c) {
      return Arrays.asList(new RelationshipGroupVersionBI[] { new RelGroupVersion(this, c) });
   }

   @Override
   public boolean hasAnnotationMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean hasRefexMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean isUncommitted() {
      return false;
   }

    @Override
    public Concept getEnclosingConcept() {
        return (Concept) rels.iterator().next().getEnclosingConcept();
    }
}
