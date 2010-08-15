package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.concept.ConceptVersion;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public class BdbTerminologySnapshot implements TerminologySnapshotDI {
	
	private BdbTerminologyStore store;
	private Coordinate c;

	
	public BdbTerminologySnapshot(BdbTerminologyStore store,
			Coordinate coordinate) {
		super();
		this.store = store;
		this.c = coordinate;
	}

	@Override
	public ComponentVersionBI getComponentVersion(int nid) throws IOException, ContraditionException {
		return store.getComponentVersion(c, nid);
	}

	@Override
	public ComponentVersionBI getComponentVersion(UUID... uuids) throws IOException, ContraditionException {
		return store.getComponentVersion(c, uuids);
	}

	@Override
	public ComponentVersionBI getComponentVersion(Collection<UUID> uuids) throws IOException, ContraditionException {
		return store.getComponentVersion(c, uuids);
	}

	@Override
	public ConceptVersionBI getConceptVersion(int cNid) throws IOException {
		return new ConceptVersion(Bdb.getConcept(cNid), c);
	}

	@Override
	public ConceptVersionBI getConceptVersion(UUID... uuids) throws IOException {
		return new ConceptVersion(Bdb.getConcept(Bdb.uuidToNid(uuids)), c);
	}

	@Override
	public ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException {
		return new ConceptVersion(Bdb.getConcept(Bdb.uuidsToNid(uuids)), c);
	}

	@Override
	public void addUncommitted(ConceptVersionBI cv) throws IOException {
		BdbCommitManager.addUncommitted(cv.getConceptChronicle());
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
		commit(cv.getConceptChronicle());
	}

	@Override
	public void cancel(ConceptChronicleBI cc) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void commit(ConceptChronicleBI cc) throws IOException {
		throw new UnsupportedOperationException();
	}

}
