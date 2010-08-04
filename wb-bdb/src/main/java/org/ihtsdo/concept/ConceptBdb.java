package org.ihtsdo.concept;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.IdentifierSetReadOnly;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.ComponentBdb;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.thread.NamedThreadFactory;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class ConceptBdb extends ComponentBdb {

    private IdentifierSet conceptIdSet;

    private static ThreadGroup conDbThreadGroup = new ThreadGroup("concept db threads");

    private static ExecutorService iteratorService =
            Executors.newCachedThreadPool(new NamedThreadFactory(conDbThreadGroup, "parallel iterator service"));

    private int processors = Runtime.getRuntime().availableProcessors();

    public ConceptBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv) throws IOException {
        super(readOnlyBdbEnv, mutableBdbEnv);
        getReadOnlyConceptIdSet();
    }

    @Override
    protected String getDbName() {
        return "conceptDb";
    }

    @Override
    protected void init() throws IOException {
        // nothing to do...
    }

    public List<UUID> getUuidsForConcept(int cNid) throws IOException {
        return getConcept(cNid).getUids();
    }

    public Concept getConcept(int cNid) throws IOException {
        assert cNid != Integer.MAX_VALUE;
        return Concept.get(cNid);
    }

    public Concept getWritableConcept(int cNid) throws IOException {
        return Concept.get(cNid);
    }

    public void writeConcept(Concept concept) throws IOException {
    	
    	AceLog.getAppLog().info("writeConcept concept = "+concept.toLongString());
    	
    	try {
    		throw new Exception("Write Concept Debug");
    	}
    	catch(Exception e) {
    		//System.out.println(e);
    		StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		String stacktrace = sw.toString();
    		AceLog.getAppLog().info("writeConcept Stack = "+stacktrace);
    		System.out.println(e);
    	}
    	
    	
        if (Bdb.watchList.containsKey(concept.getNid())) {
            AceLog.getAppLog().info("---!!! Writing to conceptBdb: " + concept.toLongString() + "\n---!!! ");
        }
        if (concept.isCanceled()) {
            return;
        }
        ConceptBinder binder = new ConceptBinder();
        DatabaseEntry key = new DatabaseEntry();
        int cNid = concept.getNid();
        conceptIdSet.setMember(cNid);
        IntegerBinding.intToEntry(cNid, key);
        DatabaseEntry value = new DatabaseEntry();
        synchronized (concept) {
            long writeVersion = Bdb.gVersion.incrementAndGet();
            if (writeVersion < concept.getDataVersion()) {
                Bdb.gVersion.set(concept.getDataVersion() + 1);
                writeVersion = Bdb.gVersion.incrementAndGet();
            }
            binder.objectToEntry(concept, value);
            concept.resetNidData();
            mutable.put(null, key, value);
            concept.setLastWrite(writeVersion);
        }
        Collection<Integer> nids = concept.getAllNids();
        NidCNidMapBdb nidCidMap = Bdb.getNidCNidMap();
        for (int nid : nids) {
            assert nid != 0 : "nid is 0: " + nids;
            nidCidMap.setCidForNid(cNid, nid);
        }
    }

    public int getCount() throws IOException {
        return (int) getReadOnlyConceptIdSet().cardinality();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.concept.I_ProcessAllConceptData#iterateConceptData(
     * org.ihtsdo.db.bdb.concept.ConceptData)
     */
    public void iterateConceptDataInSequence(I_ProcessConceptData processor) throws Exception {
        iterateConceptData(new FetchConceptAdaptor(processor), 1);
    }

    public void iterateConceptDataInParallel(I_ProcessConceptData processor) throws Exception {
        iterateConceptData(new FetchConceptAdaptor(processor), processors * 2);
    }

    public void iterateConceptDataInSequence(I_ProcessUnfetchedConceptData processor) throws Exception {
        iterateConceptData(processor, 1);
    }

    public void iterateConceptDataInParallel(I_ProcessUnfetchedConceptData processor) throws Exception {
        iterateConceptData(processor, processors * 2);
    }

    private void iterateConceptData(I_ProcessUnfetchedConceptData processor, int executors) throws IOException,
            InterruptedException, ExecutionException {
    	AceLog.getAppLog().info("Iterate in parallel. Executors: " + executors);
        IdentifierSet ids = (IdentifierSet) getReadOnlyConceptIdSet();
        int cardinality = ids.cardinality();
        int idsPerParallelConceptIterator = cardinality / executors;
    	AceLog.getAppLog().info("Iterate in parallel. idsPerParallelConceptIterator: " + idsPerParallelConceptIterator);
        I_IterateIds idsItr = ids.iterator();
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(executors + 1);
        List<ParallelConceptIterator> pcis = new ArrayList<ParallelConceptIterator>();
        int sum = 0;
        while (idsItr.next()) {
            int first = idsItr.nid();
            int last = first;
            int count = 1;
            while (idsItr.next()) {
                last = idsItr.nid();
                count++;
                if (count == idsPerParallelConceptIterator) {
                    break;
                }
            }
            sum = sum + count;
            ParallelConceptIterator pci = new ParallelConceptIterator(first, last, count, processor, readOnly, mutable);
        	AceLog.getAppLog().info("Iterate in parallel. first: " + first + 
        			" last: " + last +
        			" count: " + count);
            Future<Boolean> f = iteratorService.submit(pci);

            futures.add(f);
            pcis.add(pci);
        }

        processor.setParallelConceptIterators(pcis);

        for (Future<Boolean> f : futures) {
            f.get();
        }
    }

    private static class GetCNids implements Callable<IdentifierSet> {
        private Database db;

        public GetCNids(Database db) {
            super();
            this.db = db;
        }

        @Override
        public IdentifierSet call() throws Exception {
            int size = (int) db.count();
            IdentifierSet nidMap = new IdentifierSet(size + 2);
            CursorConfig cursorConfig = new CursorConfig();
            cursorConfig.setReadUncommitted(true);
            Cursor cursor = db.openCursor(null, cursorConfig);
            try {
                DatabaseEntry foundKey = new DatabaseEntry();
                DatabaseEntry foundData = new DatabaseEntry();
                foundData.setPartial(true);
                foundData.setPartial(0, 0, true);
                while (cursor.getNext(foundKey, foundData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
                    int cNid = IntegerBinding.entryToInt(foundKey);
                    nidMap.setMember(cNid);
                }
                cursor.close();
                return nidMap;
            } finally {
                cursor.close();
            }
        }
    }

    /**
     * 
     * @return a read-only bit set, with all concept identifiers set to true.
     * @throws IOException
     */
    public IdentifierSetReadOnly getReadOnlyConceptIdSet() throws IOException {
        if (conceptIdSet == null) {

            GetCNids readOnlyGetter = new GetCNids(readOnly);
            GetCNids mutableGetter = new GetCNids(mutable);
            try {
                IdentifierSet readOnlyMap = readOnlyGetter.call();
                IdentifierSet mutableMap = mutableGetter.call();
                readOnlyMap.or(mutableMap);
                conceptIdSet = new IdentifierSet(readOnlyMap);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return new IdentifierSetReadOnly(conceptIdSet);
    }

    /**
     * 
     * @return a mutable bit set, with all concept identifiers set to true.
     * @throws IOException
     */
    public I_RepresentIdSet getConceptIdSet() throws IOException {
        return new IdentifierSet(conceptIdSet);
    }

    public I_RepresentIdSet getEmptyIdSet() throws IOException {
        return new IdentifierSet(getReadOnlyConceptIdSet().totalBits());
    }

    public class ReferenceFinder implements I_ProcessConceptData {
        int memberNid;
        StringBuffer buff = new StringBuffer();

        public ReferenceFinder(int memberNid) {
            super();
            this.memberNid = memberNid;
        }

        public void processConceptData(Concept concept) throws Exception {
            if (concept.getAllNids().contains(memberNid)) {
                buff.append(concept.toLongString());
                return;
            }
            for (RefsetMember<?, ?> member : concept.getExtensions()) {
                if (member.getNid() == memberNid) {
                    buff.append(concept.toLongString());
                    return;
                }
            }
        }

        @Override
        public String toString() {
            if (buff.length() == 0) {
                buff.append("[no references found]");
            }
            return buff.toString();
        }

        @Override
        public boolean continueWork() {
            return true;
        }

    }

    public String findReferences(int memberNid) throws Exception {
        ReferenceFinder finder = new ReferenceFinder(memberNid);
        iterateConceptDataInParallel(finder);
        return finder.toString();
    }

    public void forget(Concept c) throws IOException {
        int cNid = c.getNid();
        try {
            DatabaseEntry key = new DatabaseEntry();
            IntegerBinding.intToEntry(cNid, key);
            // Perform the deletion. All records that use this key are
            // deleted.
            mutable.delete(null, key);
        } catch (Exception e) {
            throw new IOException(e);
        }
        conceptIdSet.setNotMember(cNid);
    }
}
