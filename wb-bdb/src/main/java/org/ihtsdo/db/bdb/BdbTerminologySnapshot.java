package org.ihtsdo.db.bdb;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.concept.ConceptVersion;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.search.ScoredComponentReference;
import org.ihtsdo.tk.uuid.UuidFactory;

public class BdbTerminologySnapshot implements TerminologySnapshotDI {
   private BdbTerminologyStore store;

    private ViewCoordinate      vc;

   //~--- constructors --------------------------------------------------------

   public BdbTerminologySnapshot(BdbTerminologyStore store, ViewCoordinate coordinate) {
      super();
      this.store = store;
      this.vc    = coordinate;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer) {
      store.addChangeSetGenerator(key, writer);
   }

   @Override
   public void addUncommitted(ConceptChronicleBI concept) throws IOException {
      BdbCommitManager.addUncommitted(concept);
   }

   @Override
   public void addUncommitted(ConceptVersionBI cv) throws IOException {
      BdbCommitManager.addUncommitted(cv);
   }


   @Override
   public void addUncommittedNoChecks(ConceptChronicleBI concept) throws IOException {
      BdbCommitManager.addUncommittedNoChecks(concept);
   }

   @Override
   public void addUncommittedNoChecks(ConceptVersionBI cv) throws IOException {
      BdbCommitManager.addUncommittedNoChecks(cv);
   }

   @Override
   public void cancel() throws IOException {
      BdbCommitManager.cancel();
   }

   @Override
   public void cancel(ConceptChronicleBI cc) throws IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public void cancel(ConceptVersionBI concept) throws IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public void commit() throws IOException {
      BdbCommitManager.commit();
   }

   @Override
   public void commit(ConceptChronicleBI cc) throws IOException {
      throw new UnsupportedOperationException();
   }
   
   @Override
    public void commit(ConceptChronicleBI cc, ChangeSetPolicy changeSetPolicy) throws IOException {
        throw new UnsupportedOperationException();
    }

   @Override
   public void commit(ConceptVersionBI cv) throws IOException {
      commit(cv);
   }

   @Override
   public ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName,
           File changeSetTempFileName, ChangeSetGenerationPolicy policy) {
      return store.createDtoChangeSetGenerator(changeSetFileName, changeSetTempFileName, policy);
   }
   
   @Override
   public Position newPosition(PathBI path, long time) throws IOException {
        return store.newPosition(path, time);
   }

   @Override
   public void removeChangeSetGenerator(String key) {
      store.removeChangeSetGenerator(key);
   }

   //~--- get methods ---------------------------------------------------------


   @Override
   public TerminologyBuilderBI getBuilder(EditCoordinate ec) {
      return store.getTerminologyBuilder(ec, vc);
   }

   @Override
   public ComponentVersionBI getComponentVersion(Collection<UUID> uuids)
           throws IOException, ContradictionException {
      return store.getComponentVersion(vc, uuids);
   }

   @Override
   public ComponentVersionBI getComponentVersion(ComponentContainerBI cc)
           throws IOException, ContradictionException {
      return getComponentVersion(cc.getNid());
   }

   @Override
   public ComponentVersionBI getComponentVersion(int nid) throws IOException, ContradictionException {
      return store.getComponentVersion(vc, nid);
   }

   @Override
   public ComponentVersionBI getComponentVersion(UUID... uuids) throws IOException, ContradictionException {
      return store.getComponentVersion(vc, uuids);
   }

   @Override
   public ConceptVersionBI getConceptForNid(int nid) throws IOException {
      return store.getConceptVersion(vc, store.getConceptNidForNid(nid));
   }

   @Override
   public ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException {
      return new ConceptVersion(Bdb.getConcept(Bdb.uuidsToNid(uuids)), vc);
   }

   @Override
   public ConceptVersionBI getConceptVersion(ConceptContainerBI cc) throws IOException {
      return getConceptVersion(cc.getConceptNid());
   }

   @Override
   public ConceptVersionBI getConceptVersion(int cNid) throws IOException {
      return new ConceptVersion(Bdb.getConcept(cNid), vc);
   }

   @Override
   public ConceptVersionBI getConceptVersion(UUID... uuids) throws IOException {
      return new ConceptVersion(Bdb.getConcept(Bdb.uuidToNid(uuids)), vc);
   }

   @Override
   public Map<Integer, ConceptVersionBI> getConceptVersions(NidBitSetBI cNids) throws IOException {
      return store.getConceptVersions(vc, cNids);
   }

   @Override
   public PathBI getPath(int pathNid) throws IOException {
      return store.getPath(pathNid);
   }

   @Override
   public Set<PathBI> getPathSetFromPositionSet(Set<PositionBI> positions) throws IOException {
      return store.getPathSetFromPositionSet(positions);
   }

   @Override
   public Set<PathBI> getPathSetFromStampSet(Set<Integer> stampNids) throws IOException {
      return store.getPathSetFromStampSet(stampNids);
   }

   @Override
   public Set<PositionBI> getPositionSet(Set<Integer> sapNids) throws IOException {
      return store.getPositionSet(sapNids);
   }

   @Override
   public int[] getPossibleChildren(int cNid) throws IOException {
        try {
            return store.getPossibleChildren(cNid, vc);
        } catch (ContradictionException ex) {
            throw new IOException(ex);
        }
   }

   @Override
   public ViewCoordinate getViewCoordinate() {
      return vc;
   }

    @Override
    public void writeDirect(ConceptChronicleBI cc) throws IOException {
        store.writeDirect(cc);
    }
    
    @Override
    public int getConceptNidForNid(Integer nid) throws IOException{
        return store.getConceptNidForNid(nid);
    }
    
    @Override
    public boolean isKindOf(int childNid, int parentNid) throws IOException, ContradictionException {
        return store.isKindOf(childNid, parentNid, vc);
    }

    @Override
    public int getNidFromAlternateId(UUID authorityUuid, String altId) throws IOException {
        return store.getNidForUuids(UuidFactory.getUuidFromAlternateId(authorityUuid, altId));
    }

    @Override
    public Collection<ScoredComponentReference> doTextSearch(String query) throws IOException, ParseException {
        return store.doTextSearch(query);
    }
}
