/**
 *
 */
package org.ihtsdo.concept;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.util.Collection;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.db.change.ChangeNotifier;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class ParallelConceptIterator implements Callable<Boolean>, I_FetchConceptFromCursor {

    private enum FETCH {
        ONE,
        TWO,
        THREE
    };

    private ProcessUnfetchedConceptDataBI processor;
    private int first;
    private int last;
    private int countToProcess;
    private int processedCount = 0;
    private Database readOnly;
    private Database mutable;
    private ParallelConceptIterator.FETCH fetchKind;
    private int currentCNid;
    private Cursor roCursor;
    private Cursor mutableCursor;
    private DatabaseEntry aKey;
    private DatabaseEntry roFoundData;
    private DatabaseEntry mutableFoundData;
    private Thread currentThread;
    private boolean stop = false;
    private ConceptBinder conceptBinder = new ConceptBinder(); 

    public ParallelConceptIterator(int first, int last, int count, ProcessUnfetchedConceptDataBI processor,
            Database readOnly, Database mutable) {
        super();
        this.first = first;
        this.last = last;
        this.countToProcess = count;
        this.processor = processor;
        this.readOnly = readOnly;
        this.mutable = mutable;
        aKey = new DatabaseEntry();
        aKey.setPartial(false);
        roFoundData = new DatabaseEntry();
        roFoundData.setPartial(false);
        mutableFoundData = new DatabaseEntry();
        mutableFoundData.setPartial(false);
        currentThread = Thread.currentThread();
        conceptBinder = new ConceptBinder();
    }

    @Override
    public void update(ConceptChronicleBI cc) throws Exception {
        Concept concept = (Concept) cc;
        int cNid = cc.getNid();
        if (concept.isUncommitted()) {
            BdbCommitManager.addUncommittedNid(cNid);
        }
        concept.modified();
        ChangeNotifier.touch(concept);

        synchronized (cc) {
            long writeVersion = Bdb.gVersion.incrementAndGet();
            if (writeVersion < concept.getDataVersion()) {
                Bdb.gVersion.set(concept.getDataVersion() + 1);
                writeVersion = Bdb.gVersion.incrementAndGet();
            }
            DatabaseEntry updateData = new DatabaseEntry();
            conceptBinder.objectToEntry(concept, updateData);
            concept.resetNidData();
            mutableCursor.putCurrent(updateData);
            concept.setLastWrite(writeVersion);
        }
        Collection<Integer> nids = concept.getAllNids();
        NidCNidMapBdb nidCidMap = Bdb.getNidCNidMap();
        for (int nid : nids) {
            assert nid != 0 : "nid is 0: " + nids;
            nidCidMap.setCNidForNid(cNid, nid);
        }
    }

    @Override
    public Concept fetch() throws Exception {
        switch (fetchKind) {
        case ONE:
            return fetchOne();
        case TWO:
            return fetchTwo();
        case THREE:
            return fetchThree();
        default:
            break;
        }
        return null;
    }
    
    @Override
    public ConceptVersion fetch(ViewCoordinate vc) throws Exception {
        Concept c = fetch();
        if (c != null) {
            return c.getVersion(vc);
        }
        return null;
    }

    private Concept fetchThree() throws IOException {
        Concept c = Concept.getIfInMap(currentCNid);
        if (c != null) {
            return c;
        }
        mutableCursor.getCurrent(aKey, mutableFoundData, LockMode.READ_UNCOMMITTED);
        return Concept.get(currentCNid, new byte[0], mutableFoundData.getData());
    }

    private Concept fetchTwo() throws IOException {
        Concept c = Concept.getIfInMap(currentCNid);
        if (c != null) {
            return c;
        }
        roCursor.getCurrent(aKey, roFoundData, LockMode.READ_UNCOMMITTED);
        return Concept.get(currentCNid, roFoundData.getData(), new byte[0]);
    }

    private Concept fetchOne() throws IOException {
        Concept c = Concept.getIfInMap(currentCNid);
        if (c != null) {
            return c;
        }
        roCursor.getCurrent(aKey, roFoundData, LockMode.READ_UNCOMMITTED);
        mutableCursor.getCurrent(aKey, mutableFoundData, LockMode.READ_UNCOMMITTED);
        return Concept.get(currentCNid, roFoundData.getData(), mutableFoundData.getData());
    }

    @Override
    public Boolean call() throws Exception {
        CursorConfig cursorConfig = new CursorConfig();
        cursorConfig.setReadUncommitted(true);
        roCursor = readOnly.openCursor(null, cursorConfig);
        mutableCursor = mutable.openCursor(null, cursorConfig);
        int roKey = first;
        int mutableKey = first;
        try {
            DatabaseEntry roFoundKey = new DatabaseEntry();
            IntegerBinding.intToEntry(roKey, roFoundKey);
            DatabaseEntry roFoundDataPartial = new DatabaseEntry();
            roFoundDataPartial.setPartial(true);
            roFoundDataPartial.setPartial(0, 0, true);

            DatabaseEntry mutableFoundKey = new DatabaseEntry();
            IntegerBinding.intToEntry(mutableKey, mutableFoundKey);
            DatabaseEntry mutableFoundDataPartial = new DatabaseEntry();
            mutableFoundDataPartial.setPartial(true);
            mutableFoundDataPartial.setPartial(0, 0, true);

            roKey = setupCursor(roCursor, roFoundKey, roFoundDataPartial);
            mutableKey = setupCursor(mutableCursor, mutableFoundKey, mutableFoundDataPartial);

            while ((roKey <= last || mutableKey <= last) && processor.continueWork()) {
                if (roKey == mutableKey) {
                    fetchKind = FETCH.ONE;
                    currentCNid = roKey;
                    processor.processUnfetchedConceptData(currentCNid, this);
                    processedCount++;
                    if (roKey < last) {
                        roKey = advanceCursor(roCursor, roFoundKey, roFoundDataPartial);
                        mutableKey = advanceCursor(mutableCursor, mutableFoundKey, mutableFoundDataPartial);
                    } else {
                        roKey = Integer.MAX_VALUE;
                        mutableKey = Integer.MAX_VALUE;
                    }
                } else if (roKey < mutableKey) {
                    fetchKind = FETCH.TWO;
                    currentCNid = roKey;
                    processor.processUnfetchedConceptData(currentCNid, this);
                    processedCount++;
                    if (roKey < last) {
                        roKey = advanceCursor(roCursor, roFoundKey, roFoundDataPartial);
                    } else {
                        roKey = Integer.MAX_VALUE;
                    }
                } else {
                    fetchKind = FETCH.THREE;
                    currentCNid = mutableKey;
                    processor.processUnfetchedConceptData(currentCNid, this);
                    processedCount++;
                    if (mutableKey < last) {
                        mutableKey = advanceCursor(mutableCursor, mutableFoundKey, mutableFoundDataPartial);
                    } else {
                        mutableKey = Integer.MAX_VALUE;
                    }
                }
            }
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine(
                    "Parallel concept iterator finished.\n" + " First: " + first + " last: " + last + " roKey: "
                        + roKey + " mutableKey: " + mutableKey + " processedCount: " + processedCount
                        + " countToProcess: " + countToProcess);
            }
            return true;
        } finally {
            roCursor.close();
            mutableCursor.close();
        }
    }

    private int advanceCursor(Cursor mutableCursor, DatabaseEntry mutableFoundKey, DatabaseEntry mutableFoundData) {
    	if (stop) {
    		return Integer.MAX_VALUE;
    	}
        int mutableKey;
        if (mutableCursor.getNext(mutableFoundKey, mutableFoundData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
            mutableKey = IntegerBinding.entryToInt(mutableFoundKey);
        } else {
            mutableKey = Integer.MAX_VALUE;
        }
        return mutableKey;
    }

    private int setupCursor(Cursor cursor, DatabaseEntry foundKey, DatabaseEntry foundData) {
        int cNid;
        if (cursor.getSearchKeyRange(foundKey, foundData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
            cNid = IntegerBinding.entryToInt(foundKey);
        } else {
            cNid = Integer.MAX_VALUE;
        }
        return cNid;
    }

    public Thread getCurrentThread() {
        return currentThread;
    }

    public void setCurrentThread(Thread currentThread) {
        this.currentThread = currentThread;
    }

    public void stop() {
    	this.stop = true;
    }
}