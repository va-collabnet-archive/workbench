package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.ConceptVersion;
import org.ihtsdo.concept.I_FetchConceptFromCursor;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.db.bdb.computer.kindof.IsaCache;
import org.ihtsdo.db.bdb.computer.kindof.TypeCache;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.KindOfCacheBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.amend.TerminologyAmendmentBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.db.DbDependency;
import org.ihtsdo.tk.db.EccsDependency;

public class BdbTerminologyStore implements TerminologyStoreDI {

	@Override
	public ComponentChroncileBI<?> getComponent(int nid) throws IOException {
		return (ComponentChroncileBI<?>) Bdb.getComponent(nid);
	}

	@Override
	public ComponentChroncileBI<?> getComponent(UUID... uuids) throws IOException {
		return getComponent(Bdb.uuidToNid(uuids));
	}

	@Override
	public ComponentChroncileBI<?> getComponent(Collection<UUID> uuids) throws IOException {
		return getComponent(Bdb.uuidsToNid(uuids));
	}

	@Override
	public ComponentVersionBI getComponentVersion(ViewCoordinate coordinate, int nid) throws IOException, ContraditionException {
		ComponentBI component = getComponent(nid);
		if (Concept.class.isAssignableFrom(component.getClass())) {
			return new ConceptVersion((Concept) component, coordinate);
		}
		return ((ComponentChroncileBI<?>) component).getVersion(coordinate);
	}

	@Override
	public ComponentVersionBI getComponentVersion(ViewCoordinate c, UUID... uuids) throws IOException, ContraditionException {
		return getComponentVersion(c, Bdb.uuidToNid(uuids));
	}

	@Override
	public ComponentVersionBI getComponentVersion(ViewCoordinate c, Collection<UUID> uuids) throws IOException, ContraditionException {
		return getComponentVersion(c, Bdb.uuidsToNid(uuids));
	}

	@Override
	public ConceptVersionBI getConceptVersion(ViewCoordinate c, int cNid) throws IOException {
		return new ConceptVersion(Bdb.getConcept(cNid), c);
	}

	@Override
	public ConceptVersionBI getConceptVersion(ViewCoordinate c, UUID... uuids) throws IOException {
		return getConceptVersion(c, Bdb.uuidToNid(uuids));
	}

	@Override
	public ConceptVersionBI getConceptVersion(ViewCoordinate c, Collection<UUID> uuids) throws IOException {
		return getConceptVersion(c, Bdb.uuidsToNid(uuids));
	}

	@Override
	public TerminologySnapshotDI getSnapshot(ViewCoordinate c) {
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

	public int uuidsToNid(UUID... uuids) throws IOException {
		return Bdb.uuidToNid(uuids);
	}

	public int uuidsToNid(Collection<UUID> uuids) throws IOException {
		return Bdb.uuidsToNid(uuids);
	}

	@Override
	public int getNidForUuids(UUID... uuids) throws IOException {
		return Bdb.uuidToNid(uuids);
	}

	@Override
	public int getNidForUuids(Collection<UUID> uuids) throws IOException {
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

    public boolean satisfiesDependencies(Collection<DbDependency> dependencies) {
    	if (dependencies != null) {
        	try {
				for (DbDependency d: dependencies) {
					String value = Bdb.getProperty(d.getKey());
					if (d.satisfactoryValue(value) == false) {
						return false;
					}
				}
			} catch (Throwable e) {
				AceLog.getAppLog().alertAndLogException(e);
				return false;
			}
    	}
    	return true;
    }

    @Override
    public boolean hasUuid(UUID memberUUID) {
        return Bdb.hasUuid(memberUUID);
    }

    private class ConceptGetter implements I_ProcessUnfetchedConceptData {
    	NidBitSetBI cNids;
    	Map<Integer, ConceptChronicleBI> conceptMap = new ConcurrentHashMap<Integer, ConceptChronicleBI>();
    	
		public ConceptGetter(NidBitSetBI cNids) {
			super();
			this.cNids = cNids;
		}

		@Override
		public NidBitSetBI getNidSet() throws IOException {
			return cNids;
		}

		@Override
		public void processUnfetchedConceptData(int cNid,
				I_FetchConceptFromCursor fcfc) throws Exception {
			if (cNids.isMember(cNid)) {
				Concept c = fcfc.fetch();
				conceptMap.put(cNid, c);
			}
			
		}

		@Override
		public void setParallelConceptIterators(
				List<ParallelConceptIterator> pcis) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean continueWork() {
			return true;
		}
    	
    }
	@Override
    public Map<Integer, ConceptChronicleBI> getConcepts(NidBitSetBI cNids) throws IOException {
    	ConceptGetter processor = new ConceptGetter(cNids);
    	try {
			Bdb.getConceptDb().iterateConceptDataInParallel(processor);
		} catch (Exception e) {
			throw new IOException(e);
		}
		return Collections.unmodifiableMap(new HashMap<Integer, ConceptChronicleBI>(processor.conceptMap));
    }

    private class ConceptVersionGetter implements I_ProcessUnfetchedConceptData {
    	NidBitSetBI cNids;
    	Map<Integer, ConceptVersionBI> conceptMap = new ConcurrentHashMap<Integer, ConceptVersionBI>();
    	ViewCoordinate coordinate;
    	
		public ConceptVersionGetter(NidBitSetBI cNids, ViewCoordinate c) {
			super();
			this.cNids = cNids;
			this.coordinate = c;
		}

		@Override
		public NidBitSetBI getNidSet() throws IOException {
			return cNids;
		}

		@Override
		public void processUnfetchedConceptData(int cNid,
				I_FetchConceptFromCursor fcfc) throws Exception {
			if (cNids.isMember(cNid)) {
				Concept c = fcfc.fetch();
				conceptMap.put(cNid, new ConceptVersion(c, coordinate));
			}
			
		}

		@Override
		public void setParallelConceptIterators(
				List<ParallelConceptIterator> pcis) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean continueWork() {
			return true;
		}
    	
    }
	@Override
	public Map<Integer, ConceptVersionBI> getConceptVersions(ViewCoordinate c,
			NidBitSetBI cNids) throws IOException {
		ConceptVersionGetter processor = new ConceptVersionGetter(cNids, c);
    	try {
			Bdb.getConceptDb().iterateConceptDataInParallel(processor);
		} catch (Exception e) {
			throw new IOException(e);
		}
		return Collections.unmodifiableMap(new HashMap<Integer, ConceptVersionBI>(processor.conceptMap));
	}

	@Override
	public int getConceptNidForNid(int nid) throws IOException {
		return Bdb.getConceptNid(nid);
	}

	@Override
	public KindOfCacheBI getCache(ViewCoordinate coordinate) throws Exception {
		TypeCache c = new IsaCache(Bdb.getConceptDb().getConceptNidSet());
		c.setup(coordinate);
		c.getLatch().await();
		return c;
	}

	@Override
	public TerminologyAmendmentBI getAmender(EditCoordinate ec, ViewCoordinate vc) {
		return new BdbAmender(ec, vc);
	}

    
}
