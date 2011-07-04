package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;

import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.ConceptVersion;
import org.ihtsdo.concept.ContradictionIdentifier;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.db.bdb.computer.kindof.IsaCache;
import org.ihtsdo.db.bdb.computer.kindof.TypeCache;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.KindOfCacheBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.db.DbDependency;
import org.ihtsdo.tk.db.EccsDependency;

public class BdbTerminologyStore implements TerminologyStoreDI {

    private static ViewCoordinate metadataVC = null;

    @Override
    public ViewCoordinate getMetadataVC() throws IOException {
        if (metadataVC == null) {
            PathBI viewPath = new Path(getNidForUuids(
                    ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()),
                    null);
            PositionBI viewPosition = new Position(Long.MAX_VALUE, viewPath);
            PositionSetBI positionSet = new PositionSetReadOnly(viewPosition);

            NidSet allowedStatusNids = new NidSet();
            allowedStatusNids.add(getNidForUuids(
                    ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
            allowedStatusNids.add(getNidForUuids(
                    ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));

            NidSetBI isaTypeNids = new NidSet();
            isaTypeNids.add(getNidForUuids(
                    ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));


            ContradictionManagerBI contradictionManager =
                    new IdentifyAllConflictStrategy();

            int languageNid = getNidForUuids(
                    ArchitectonicAuxiliary.Concept.EN_US.getUids());
            int classifierNid = getNidForUuids(
                    ArchitectonicAuxiliary.Concept.SNOROCKET.getUids());

            metadataVC = new ViewCoordinate(Precedence.TIME,
                    positionSet,
                    allowedStatusNids,
                    isaTypeNids,
                    contradictionManager,
                    languageNid,
                    classifierNid,
                    RelAssertionType.STATED,
                    null, ViewCoordinate.LANGUAGE_SORT.TYPE_BEFORE_LANG);
        }
        return metadataVC;
    }

    @Override
    public PathBI getPath(int pathNid) throws IOException {
        return BdbPathManager.get().get(pathNid);
    }

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
        this.commit(concept.getChronicle());
    }

    @Override
    public void addUncommitted(ConceptVersionBI cv) throws IOException {
        addUncommitted(cv.getChronicle());
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

    @Override
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
        BdbProperty[] keysToCheck = new BdbProperty[]{
            BdbProperty.LAST_CHANGE_SET_WRITTEN,
            BdbProperty.LAST_CHANGE_SET_READ};

        List<DbDependency> latestDependencies = new ArrayList<DbDependency>(2);
        for (BdbProperty prop : keysToCheck) {
            String value = Bdb.getProperty(prop.toString());
            if (value != null) {
                String changeSetName = value;
                String changeSetSize = Bdb.getProperty(changeSetName);
                latestDependencies.add(new EccsDependency(changeSetName, changeSetSize));
            }
        }
        return latestDependencies;
    }

    @Override
    public boolean satisfiesDependencies(Collection<DbDependency> dependencies) {
        if (dependencies != null) {
            try {
                for (DbDependency d : dependencies) {
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
        assert memberUUID != null;
        return Bdb.hasUuid(memberUUID);
    }

    @Override
    public void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception {
        Bdb.getConceptDb().iterateConceptDataInParallel(processor);
    }

    @Override
    public void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception {
        Bdb.getConceptDb().iterateConceptDataInSequence(processor);
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
                ConceptFetcherBI fcfc) throws Exception {
            if (cNids.isMember(cNid)) {
                Concept c = (Concept) fcfc.fetch();
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
                ConceptFetcherBI fcfc) throws Exception {
            if (cNids.isMember(cNid)) {
                Concept c = (Concept) fcfc.fetch();
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
    public TerminologyConstructorBI getTerminologyConstructor(EditCoordinate ec, ViewCoordinate vc) {
        return new BdbTermConstructor(ec, vc);
    }

    @Override
    public NidBitSetBI getAllConceptNids() throws IOException {
        return Bdb.getConceptDb().getReadOnlyConceptIdSet();
    }

    @Override
    public Set<PositionBI> getPositionSet(Set<Integer> sapNids)
            throws IOException {
        HashSet<PositionBI> positions = new HashSet<PositionBI>(sapNids.size());
        for (int sap : sapNids) {
            try {
                if (sap >= 0) {
                    positions.add(Bdb.getSapDb().getPosition(sap));
                }
            } catch (PathNotExistsException ex) {
                throw new IOException(ex);
            } catch (TerminologyException ex) {
                throw new IOException(ex);
            }
        }
        return positions;
    }

    @Override
    public Set<PathBI> getPathSetFromSapSet(Set<Integer> sapNids)
            throws IOException {
        HashSet<PathBI> paths = new HashSet<PathBI>(sapNids.size());
        for (int sap : sapNids) {
            try {
                PathBI path = Bdb.getSapDb().getPosition(sap).getPath();
                paths.add(path);
                addOrigins(paths, path.getOrigins());
            } catch (PathNotExistsException ex) {
                throw new IOException(ex);
            } catch (TerminologyException ex) {
                throw new IOException(ex);
            }
        }
        return paths;
    }

    private void addOrigins(Set<PathBI> paths, Collection<? extends PositionBI> origins) {
        if (origins == null) {
            return;
        }
        for (PositionBI o : origins) {
            paths.add(o.getPath());
            addOrigins(paths, o.getPath().getOrigins());
        }
    }

    @Override
    public Set<PathBI> getPathSetFromPositionSet(Set<PositionBI> positions)
            throws IOException {
        HashSet<PathBI> paths = new HashSet<PathBI>(positions.size());
        for (PositionBI position : positions) {
            paths.add(position.getPath());
            //addOrigins(paths, position.getPath().getInheritedOrigins());
        }
        return paths;
    }

    @Override
    public NidBitSetBI getEmptyNidSet() throws IOException {
        return Bdb.getConceptDb().getEmptyIdSet();
    }

    @Override
    public ContradictionIdentifierBI getConflictIdentifier(ViewCoordinate vc, boolean useCase) {
		return new ContradictionIdentifier(vc, useCase);
    }
    
    @Override
    public boolean hasUncommittedChanges(){
    	if(Terms.get().getUncommitted().size() > 0){
    		return true;
    	}
    	return false;
    }

    @Override
    public Collection<? extends ConceptChronicleBI> getUncommittedConcepts() {
        return BdbCommitManager.getUncommitted();
    }
    
    
}
