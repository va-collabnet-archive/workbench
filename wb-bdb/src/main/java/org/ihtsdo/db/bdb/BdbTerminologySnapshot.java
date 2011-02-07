package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.concept.ConceptVersion;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.amend.TerminologyAmendmentBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class BdbTerminologySnapshot implements TerminologySnapshotDI {

	private BdbTerminologyStore store;

   private ViewCoordinate vc;


	public BdbTerminologySnapshot(BdbTerminologyStore store,
			ViewCoordinate coordinate) {
		super();
		this.store = store;
		this.vc = coordinate;
	}


   @Override
   public Set<PositionBI> getPositionSet(Set<Integer> sapNids)
           throws IOException {
      return store.getPositionSet(sapNids);
   }

   @Override
   public Set<PathBI> getPathSetFromSapSet(Set<Integer> sapNids)
           throws IOException {
      return store.getPathSetFromSapSet(sapNids);
   }

   @Override
   public Set<PathBI> getPathSetFromPositionSet(Set<PositionBI> positions)
           throws IOException {
      return store.getPathSetFromPositionSet(positions);
   }

   @Override
	public ComponentVersionBI getComponentVersion(int nid) throws IOException, ContraditionException {
		return store.getComponentVersion(vc, nid);
	}

	@Override
	public ComponentVersionBI getComponentVersion(UUID... uuids) throws IOException, ContraditionException {
		return store.getComponentVersion(vc, uuids);
	}

	@Override
	public ComponentVersionBI getComponentVersion(Collection<UUID> uuids) throws IOException, ContraditionException {
		return store.getComponentVersion(vc, uuids);
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
	public ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException {
		return new ConceptVersion(Bdb.getConcept(Bdb.uuidsToNid(uuids)), vc);
	}

	@Override
	public void addUncommitted(ConceptVersionBI cv) throws IOException {
		BdbCommitManager.addUncommitted(cv);
	}

	@Override
	public void addUncommitted(ConceptChronicleBI concept) throws IOException {
		BdbCommitManager.addUncommitted(concept);
	}

	@Override
	public void cancel() throws IOException {
		BdbCommitManager.cancel();
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
	public void commit(ConceptVersionBI cv) throws IOException {
		commit(cv);
	}

	@Override
	public void cancel(ConceptChronicleBI cc) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void commit(ConceptChronicleBI cc) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<Integer, ConceptVersionBI> getConceptVersions(NidBitSetBI cNids) throws IOException {
		return store.getConceptVersions(vc, cNids);
	}

	public TerminologyAmendmentBI getAmender(EditCoordinate ec) {
		return store.getAmender(ec, vc);
	}

	public void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer) {
		store.addChangeSetGenerator(key, writer);
	}

	public ChangeSetGeneratorBI createDtoChangeSetGenerator(
			File changeSetFileName, File changeSetTempFileName,
			ChangeSetGenerationPolicy policy) {
		return store.createDtoChangeSetGenerator(changeSetFileName,
				changeSetTempFileName, policy);
	}

	public void removeChangeSetGenerator(String key) {
		store.removeChangeSetGenerator(key);
	}

}
