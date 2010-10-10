package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.ConceptVersion;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.db.DbDependency;
import org.ihtsdo.tk.db.EccsDependency;

public class BdbTerminologyStore implements TerminologyStoreDI {

	@Override
	public ComponentBI getComponent(int nid) throws IOException {
		return (ComponentBI) Bdb.getComponent(nid);
	}

	@Override
	public ComponentBI getComponent(UUID... uuids) throws IOException {
		return getComponent(Bdb.uuidToNid(uuids));
	}

	@Override
	public ComponentBI getComponent(Collection<UUID> uuids) throws IOException {
		return getComponent(Bdb.uuidsToNid(uuids));
	}

	@Override
	public ComponentVersionBI getComponentVersion(Coordinate coordinate, int nid) throws IOException, ContraditionException {
		ComponentBI component = getComponent(nid);
		if (Concept.class.isAssignableFrom(component.getClass())) {
			return new ConceptVersion((Concept) component, coordinate);
		}
		return ((ComponentChroncileBI<?>) component).getVersion(coordinate);
	}

	@Override
	public ComponentVersionBI getComponentVersion(Coordinate c, UUID... uuids) throws IOException, ContraditionException {
		return getComponentVersion(c, Bdb.uuidToNid(uuids));
	}

	@Override
	public ComponentVersionBI getComponentVersion(Coordinate c, Collection<UUID> uuids) throws IOException, ContraditionException {
		return getComponentVersion(c, Bdb.uuidsToNid(uuids));
	}

	@Override
	public ConceptVersionBI getConceptVersion(Coordinate c, int cNid) throws IOException {
		return new ConceptVersion(Bdb.getConcept(cNid), c);
	}

	@Override
	public ConceptVersionBI getConceptVersion(Coordinate c, UUID... uuids) throws IOException {
		return getConceptVersion(c, Bdb.uuidToNid(uuids));
	}

	@Override
	public ConceptVersionBI getConceptVersion(Coordinate c, Collection<UUID> uuids) throws IOException {
		return getConceptVersion(c, Bdb.uuidsToNid(uuids));
	}

	@Override
	public TerminologySnapshotDI getSnapshot(Coordinate c) {
		return new BdbTerminologySnapshot(this, c);
	}

	@Override
	public void addUncommitted(ConceptChronicleBI concept) throws IOException {
		BdbCommitManager.addUncommitted(concept);
	}

	@Override
	public void cancel() {
		BdbCommitManager.cancel();
	}

	@Override
	public void cancel(ConceptVersionBI concept) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void commit() throws IOException {
		BdbCommitManager.commit();
	}

	@Override
	public void commit(ConceptVersionBI concept) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addUncommitted(ConceptVersionBI cv) throws IOException {
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
	public ConceptChronicleBI getConcept(int cNid) throws IOException {
		return Bdb.getConcept(cNid);
	}

	@Override
	public ConceptChronicleBI getConcept(UUID... uuids) throws IOException {
		return getConcept(Bdb.uuidToNid(uuids));
	}

	@Override
	public ConceptChronicleBI getConcept(Collection<UUID> uuids)
			throws IOException {
		return getConcept(Bdb.uuidsToNid(uuids));
	}

	@Override
	public int uuidsToNid(UUID... uuids) throws IOException {
		return Bdb.uuidToNid(uuids);
	}

	@Override
	public int uuidsToNid(Collection<UUID> uuids) throws IOException {
		return Bdb.uuidsToNid(uuids);
	}

	@Override
	public List<UUID> getUuidsForNid(int nid) {
		return Bdb.getUuidsToNidMap().getUuidsForNid(nid);
	}

	@Override
	public void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer) {
		ChangeSetWriterHandler.addWriter(key, writer);
    }
	
	@Override
	public void removeChangeSetGenerator(String key) {
		ChangeSetWriterHandler.removeWriter(key);
    }

    public ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName, 
			File changeSetTempFileName, 
			ChangeSetGenerationPolicy policy) {
    	return new EConceptChangeSetWriter(
    			changeSetFileName,
    			changeSetTempFileName,
    			policy, true);  	
    }

	@Override
	public Collection<DbDependency> getLatestChangeSetDependencies() throws IOException {
		BdbProperty[] keysToCheck = new BdbProperty[] { 
				BdbProperty.LAST_CHANGE_SET_WRITTEN, 
				BdbProperty.LAST_CHANGE_SET_READ};
		
		List<DbDependency> latestDependencies = new ArrayList<DbDependency>(2);
		for (BdbProperty prop: keysToCheck) {
			String value = Bdb.getProperty(prop.toString());
			if (value != null) {
				String changeSetName = value;
				String changeSetSize = Bdb.getProperty(changeSetName);
				latestDependencies.add(new EccsDependency(changeSetName, changeSetSize));
			}
		}
		return latestDependencies;
	}

    public boolean satisfiesDependencies(Collection<DbDependency> dependencies) throws IOException {
    	if (dependencies != null) {
        	for (DbDependency d: dependencies) {
        		String value = Bdb.getProperty(d.getKey());
        		if (d.satisfactoryValue(value) == false) {
        			return false;
        		}
        	}
    	}
    	return true;
    }

}
