package org.ihtsdo.db.bdb;

//~--- non-JDK imports --------------------------------------------------------
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.dwfa.vodb.types.IntList;
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
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;
import org.ihtsdo.db.bdb.computer.kindof.TypeCache;
import org.ihtsdo.db.change.ChangeNotifier;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContradictionException;
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
import org.ihtsdo.tk.api.TermChangeListener;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.IsaCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.db.DbDependency;
import org.ihtsdo.tk.db.EccsDependency;

//~--- JDK imports ------------------------------------------------------------

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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class BdbTerminologyStore implements TerminologyStoreDI {

    private static ViewCoordinate metadataVC = null;
    private static EditCoordinate metadataEC = null;
    private static boolean isReleaseFormatSetup = false;
    private static int releaseFormat = 0;

    //~--- methods -------------------------------------------------------------
    @Override
    public void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer) {
        ChangeSetWriterHandler.addWriter(key, writer);
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
    public void suspendChangeNotifications() {
        ChangeNotifier.suspendNotifications();
    }

    @Override
    public void resumeChangeNotifications() {
        ChangeNotifier.resumeNotifications();
    }

    @Override
    public void addTermChangeListener(TermChangeListener cl) {
        ChangeNotifier.addTermChangeListener(cl);
    }

    @Override
    public void addUncommitted(ConceptChronicleBI concept) throws IOException {
        BdbCommitManager.addUncommitted(concept);
    }

    @Override
    public void addUncommitted(ConceptVersionBI cv) throws IOException {
        addUncommitted(cv.getChronicle());
    }
    
    @Override
    public void forget(RelationshipVersionBI rel) throws IOException{
        BdbCommitManager.forget(rel);
    }
    
    @Override
    public void forget(DescriptionVersionBI desc) throws IOException{
        BdbCommitManager.forget(desc);
    }
    
    @Override
    public void forget(RefexChronicleBI extension) throws IOException{
        BdbCommitManager.forget(extension);
    }
    
    @Override
    public void forget(ConAttrVersionBI attr) throws IOException{
        BdbCommitManager.forget(attr);
    }
    
    @Override
    public void forget(ConceptChronicleBI concept) throws IOException{
        BdbCommitManager.forget(concept);
    }

    @Override
    public void cancel() {
        BdbCommitManager.cancel();
    }

    @Override
    public void cancel(ConceptChronicleBI concept) {
        throw new UnsupportedOperationException();
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
    public void commit(ConceptChronicleBI cc) throws IOException {
        BdbCommitManager.commit((Concept) cc, ChangeSetPolicy.MUTABLE_ONLY,
                ChangeSetWriterThreading.SINGLE_THREAD);
    }

    @Override
    public void commit(ConceptVersionBI concept) throws IOException {
        this.commit(concept.getChronicle());
    }

    @Override
    public ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName,
            File changeSetTempFileName, ChangeSetGenerationPolicy policy) {
        return new EConceptChangeSetWriter(changeSetFileName, changeSetTempFileName, policy, true);
    }

    @Override
    public void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception {
        Bdb.getConceptDb().iterateConceptDataInParallel(processor);
    }

    @Override
    public void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception {
        Bdb.getConceptDb().iterateConceptDataInSequence(processor);
    }
    
    @Override
    public void iterateSapDataInSequence(ProcessSapDataBI processor) throws Exception {
        Bdb.getSapDb().iterateSapDataInSequence(processor);
    }

    
   @Override
   public Position newPosition(PathBI path, long time) throws IOException {
      return new Position(time, path);
   }

    @Override
    public void removeChangeSetGenerator(String key) {
        ChangeSetWriterHandler.removeWriter(key);
    }

    @Override
    public void removeTermChangeListener(TermChangeListener cl) {
        ChangeNotifier.removeTermChangeListener(cl);
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
    public boolean usesRf2Metadata() throws IOException {
        if (isReleaseFormatSetup == false) {
            UUID snomedRootUuid = SNOMED.Concept.ROOT.getUids().iterator().next();
            int snomedRootNid = Bdb.uuidToNid(snomedRootUuid);
            Concept cb = Bdb.getConcept(snomedRootNid);
            int rootStatusNid = cb.getConAttrs().getVersions().iterator().next().getStatusNid();
            int rf1CurrentNid = Bdb.uuidToNid(SnomedMetadataRf1.CURRENT_RF1.getUuids());
            int rf2ActiveValueNid = Bdb.uuidToNid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());

            if (rootStatusNid == rf1CurrentNid) {
                releaseFormat = 1;
                isReleaseFormatSetup = true;
            } else if (rootStatusNid == rf2ActiveValueNid) {
                releaseFormat = 2;
                isReleaseFormatSetup = true;
            } else {
                throw new IOException("usesRf2Metadata current/active status did not match Rf1 or Rf2.");
            }
        }

        if (releaseFormat == 2) {
            return true;
        } else {
            return false;
        }
    }

    public int uuidsToNid(Collection<UUID> uuids) throws IOException {
        return Bdb.uuidsToNid(uuids);
    }

    public int uuidsToNid(UUID... uuids) throws IOException {
        return Bdb.uuidToNid(uuids);
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public NidBitSetBI getAllConceptNids() throws IOException {
        return Bdb.getConceptDb().getReadOnlyConceptIdSet();
    }

    @Override
    public KindOfCacheBI getCache(ViewCoordinate coordinate) throws Exception {
        TypeCache c = new IsaCache(Bdb.getConceptDb().getConceptNidSet());

        c.setup(coordinate);
        c.getLatch().await();

        return c;
    }

    @Override
    public ComponentChroncileBI<?> getComponent(Collection<UUID> uuids) throws IOException {
        return getComponent(Bdb.uuidsToNid(uuids));
    }

    @Override
    public ComponentChroncileBI<?> getComponent(ComponentContainerBI cc) throws IOException {
        return getComponent(cc.getNid());
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
    public ComponentVersionBI getComponentVersion(ViewCoordinate c, Collection<UUID> uuids)
            throws IOException, ContradictionException {
        return getComponentVersion(c, Bdb.uuidsToNid(uuids));
    }

    @Override
    public ComponentVersionBI getComponentVersion(ViewCoordinate coordinate, int nid)
            throws IOException, ContradictionException {
        ComponentBI component = getComponent(nid);

        if (Concept.class.isAssignableFrom(component.getClass())) {
            return new ConceptVersion((Concept) component, coordinate);
        }

        return ((ComponentChroncileBI<?>) component).getVersion(coordinate);
    }

    @Override
    public ComponentVersionBI getComponentVersion(ViewCoordinate c, UUID... uuids)
            throws IOException, ContradictionException {
        return getComponentVersion(c, Bdb.uuidToNid(uuids));
    }

    @Override
    public ConceptChronicleBI getConcept(Collection<UUID> uuids) throws IOException {
        return getConcept(Bdb.uuidsToNid(uuids));
    }

    @Override
    public ConceptChronicleBI getConcept(ConceptContainerBI cc) throws IOException {
        return getConcept(cc.getCnid());
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
    public ConceptChronicleBI getConceptForNid(int nid) throws IOException {
        return getConcept(getConceptNidForNid(nid));
    }

    @Override
    public int getConceptNidForNid(int nid) throws IOException {
        return Bdb.getConceptNid(nid);
    }

    @Override
    public ConceptVersionBI getConceptVersion(ViewCoordinate c, Collection<UUID> uuids) throws IOException {
        return getConceptVersion(c, Bdb.uuidsToNid(uuids));
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
    public Map<Integer, ConceptVersionBI> getConceptVersions(ViewCoordinate c, NidBitSetBI cNids)
            throws IOException {
        ConceptVersionGetter processor = new ConceptVersionGetter(cNids, c);

        try {
            Bdb.getConceptDb().iterateConceptDataInParallel(processor);
        } catch (Exception e) {
            throw new IOException(e);
        }

        return Collections.unmodifiableMap(new HashMap<Integer, ConceptVersionBI>(processor.conceptMap));
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

    @Override
    public ContradictionIdentifierBI getConflictIdentifier(ViewCoordinate vc, boolean useCase) {
        return new ContradictionIdentifier(vc, useCase);
    }

    @Override
    public NidBitSetBI getEmptyNidSet() throws IOException {
        return Bdb.getConceptDb().getEmptyIdSet();
    }

    @Override
    public Collection<DbDependency> getLatestChangeSetDependencies() throws IOException {
        BdbProperty[] keysToCheck = new BdbProperty[]{BdbProperty.LAST_CHANGE_SET_WRITTEN,
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
    public ViewCoordinate getMetadataVC() throws IOException {
        if (metadataVC == null) {
            PathBI viewPath =
                    new Path(getNidForUuids(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()), null);
            PositionBI viewPosition = new Position(Long.MAX_VALUE, viewPath);
            PositionSetBI positionSet = new PositionSetReadOnly(viewPosition);
            NidSet allowedStatusNids = new NidSet();

            allowedStatusNids.add(getNidForUuids(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
            allowedStatusNids.add(getNidForUuids(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
            allowedStatusNids.add(SnomedMetadataRfx.getSTATUS_CURRENT_NID());

            NidSetBI isaTypeNids = new NidSet();

            isaTypeNids.add(getNidForUuids(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));

            ContradictionManagerBI contradictionManager = new IdentifyAllConflictStrategy();
            int languageNid =
                    getNidForUuids(ArchitectonicAuxiliary.Concept.EN_US.getUids());
            int classifierNid =
                    getNidForUuids(ArchitectonicAuxiliary.Concept.SNOROCKET.getUids());

            metadataVC = new ViewCoordinate(Precedence.PATH, positionSet, allowedStatusNids, isaTypeNids,
                    contradictionManager, languageNid, classifierNid,
                    RelAssertionType.INFERRED_THEN_STATED, null,
                    ViewCoordinate.LANGUAGE_SORT.TYPE_BEFORE_LANG);
        }

        return metadataVC;
    }
    
    @Override
    public EditCoordinate getMetadataEC() throws IOException {
        if (metadataEC == null) {
            /*
             * public EditCoordinate(int authorNid, NidSetBI editPaths) {
		super();
		assert editPaths != null;
		assert authorNid != Integer.MIN_VALUE;
		this.authorNid = authorNid;
		this.editPaths = editPaths.getSetValues();
	}
             */
            
            int authorNid = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.USER.getUids());
            int editPathNid = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
            metadataEC = new EditCoordinate(authorNid, editPathNid);
        }

        return metadataEC;
    }

    @Override
    public int getNidForUuids(Collection<UUID> uuids) throws IOException {
        return Bdb.uuidsToNid(uuids);
    }

    @Override
    public int getNidForUuids(UUID... uuids) throws IOException {
        return Bdb.uuidToNid(uuids);
    }

    @Override
    public PathBI getPath(int pathNid) throws IOException {
        return BdbPathManager.get().get(pathNid);
    }

    @Override
    public List<? extends PathBI> getPathChildren(int nid) {
        return BdbPathManager.get().getPathChildren(nid);
    }

    @Override
    public Set<PathBI> getPathSetFromPositionSet(Set<PositionBI> positions) throws IOException {
        HashSet<PathBI> paths = new HashSet<PathBI>(positions.size());

        for (PositionBI position : positions) {
            paths.add(position.getPath());

            // addOrigins(paths, position.getPath().getInheritedOrigins());
        }

        return paths;
    }

    @Override
    public Set<PathBI> getPathSetFromSapSet(Set<Integer> sapNids) throws IOException {
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

    @Override
    public Set<PositionBI> getPositionSet(Set<Integer> sapNids) throws IOException {
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
    public int[] getPossibleChildren(int parentNid, ViewCoordinate vc) throws IOException {
        if (vc.getIsaCoordinates().size() == 1) {
            IsaCoordinate isaCoordinate = vc.getIsaCoordinates().iterator().next();
            IsaCache cache = KindOfComputer.getIsaCacheMap().get(isaCoordinate);

            if ((cache != null) && cache.isReady()) {
                int[] allPossibleNids = Bdb.xref.getDestRelOrigins(parentNid, vc.getIsaTypeNids());
                IntList viewPossibleNids = new IntList();

                for (int childNid : allPossibleNids) {
                    try {
                        if (cache.isKindOf(childNid, parentNid)) {
                            viewPossibleNids.add(childNid);
                        }
                    } catch (Exception ex) {
                        throw new IOException(ex);
                    }
                }

                return viewPossibleNids.getListArray();
            }
        }

        return Bdb.xref.getDestRelOrigins(parentNid, vc.getIsaTypeNids());
    }

    @Override
    public int getReadOnlyMaxSap() {
        return Bdb.getSapDb().getReadOnlyMax();
    }

    @Override
    public long getSequence() {
        return Bdb.gVersion.incrementAndGet();
    }

    @Override
    public TerminologySnapshotDI getSnapshot(ViewCoordinate c) {
        return new BdbTerminologySnapshot(this, c);
    }

    @Override
    public TerminologyBuilderBI getTerminologyBuilder(EditCoordinate ec, ViewCoordinate vc) {
        return new BdbTermBuilder(ec, vc);
    }

    @Override
    public Collection<? extends ConceptChronicleBI> getUncommittedConcepts() {
        return BdbCommitManager.getUncommitted();
    }

    @Override
    public List<UUID> getUuidsForNid(int nid) throws IOException {
        return Bdb.getUuidsToNidMap().getUuidsForNid(nid);
    }

    @Override
    public UUID getUuidPrimordialForNid(int nid) throws IOException {
        ComponentChroncileBI<?> c = getComponent(nid);
        if (c != null) {
            return c.getPrimUuid();
        }
        return UUID.fromString("00000000-0000-0000-C000-000000000046");
    }

    @Override
    public boolean hasPath(int nid) throws IOException {
        return BdbPathManager.get().hasPath(nid);
    }

    @Override
    public boolean hasUncommittedChanges() {
        if (Terms.get().getUncommitted().size() > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean hasUuid(UUID memberUUID) {
        assert memberUUID != null;

        return Bdb.hasUuid(memberUUID);
    }

    @Override
    public void addVetoablePropertyChangeListener(CONCEPT_EVENT pce, VetoableChangeListener l) {
        GlobalPropertyChange.addVetoableChangeListener(pce, l);
    } 

    @Override
    public void addPropertyChangeListener(CONCEPT_EVENT pce, PropertyChangeListener l) {
        GlobalPropertyChange.addPropertyChangeListener(pce, l);
    }

    //~--- inner classes -------------------------------------------------------
    private class ConceptGetter implements I_ProcessUnfetchedConceptData {

        Map<Integer, ConceptChronicleBI> conceptMap = new ConcurrentHashMap<Integer, ConceptChronicleBI>();
        NidBitSetBI cNids;

        //~--- constructors -----------------------------------------------------
        public ConceptGetter(NidBitSetBI cNids) {
            super();
            this.cNids = cNids;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public boolean continueWork() {
            return true;
        }

        @Override
        public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
            if (cNids.isMember(cNid)) {
                Concept c = (Concept) fcfc.fetch();

                conceptMap.put(cNid, c);
            }
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public NidBitSetBI getNidSet() throws IOException {
            return cNids;
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
            // TODO Auto-generated method stub
        }
    }

    private class ConceptVersionGetter implements I_ProcessUnfetchedConceptData {

        Map<Integer, ConceptVersionBI> conceptMap = new ConcurrentHashMap<Integer, ConceptVersionBI>();
        NidBitSetBI cNids;
        ViewCoordinate coordinate;

        //~--- constructors -----------------------------------------------------
        public ConceptVersionGetter(NidBitSetBI cNids, ViewCoordinate c) {
            super();
            this.cNids = cNids;
            this.coordinate = c;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public boolean continueWork() {
            return true;
        }

        @Override
        public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception {
            if (cNids.isMember(cNid)) {
                Concept c = (Concept) fcfc.fetch();

                conceptMap.put(cNid, new ConceptVersion(c, coordinate));
            }
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public NidBitSetBI getNidSet() throws IOException {
            return cNids;
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
            // TODO Auto-generated method stub
        }
    }

    @Override
    public int getAuthorNidForSapNid(int sapNid) {
        return Bdb.getAuthorNidForSapNid(sapNid);
}

    @Override
    public int getPathNidForSapNid(int sapNid) {
         return Bdb.getPathNidForSapNid(sapNid);
   }

    @Override
    public int getStatusNidForSapNid(int sapNid) {
         return Bdb.getStatusNidForSapNid(sapNid);
   }

    @Override
    public long getTimeForSapNid(int sapNid) {
         return Bdb.getTimeForSapNid(sapNid);
   }

    @Override
    public void writeDirect(ConceptChronicleBI cc) throws IOException {
        BdbCommitManager.writeDirect(cc);
    }

    @Override
    public void clearInferredIsaCache() {
        KindOfComputer.clearIsaCache();
    }

    @Override
    public void addInferredParents(ViewCoordinate vc, IsaCoordinate isac, int cnid, int[] parentNids) throws IOException {
       KindOfComputer.addToIsaCache(vc, isac, cnid, parentNids);
    }
    
    @Override
    public void setIsaCacheAsComplete(IsaCoordinate isac) throws IOException {
    	KindOfComputer.setIsaCacheAsComplete(isac);
    }
    
    @Override
   public void touchComponent(int nid) {
      ChangeNotifier.touchComponent(nid);
   }
   
    @Override
   public void touchComponentAlert(int nid) {
      ChangeNotifier.touchComponentAlert(nid);
   }
   
    @Override
   public void touchComponentTemplate(int nid) {
      ChangeNotifier.touchComponentTemplate(nid);
   }

    @Override
   public void touchComponents(Collection<Integer> cNidSet) {
      ChangeNotifier.touchComponents(cNidSet);
   }

    @Override
   public void touchRefexRC(int nid) {
      ChangeNotifier.touchRefexRC(nid);
   }
   
    @Override
   public void touchRelOrigin(int nid) {
      ChangeNotifier.touchRelOrigin(nid);
   }

    @Override
   public void touchRelTarget(int nid) {
      ChangeNotifier.touchRelTarget(nid);
   }
   
}