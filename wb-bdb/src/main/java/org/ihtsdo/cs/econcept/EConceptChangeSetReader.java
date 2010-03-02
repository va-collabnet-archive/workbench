package org.ihtsdo.cs.econcept;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.I_AmChangeSetObject;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.ToIoException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.time.TimeUtil;

import com.sleepycat.je.DatabaseException;

public class EConceptChangeSetReader implements I_ReadChangeSet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private File changeSetFile;

    private I_Count counter;

    private DataInputStream dataStream;

    private int count = 0;

    private int conceptCount = 0;

    private int unvalidated = 0;

    private boolean initialized = false;

    private Long nextCommit;
    private String nextCommitStr;

    private transient List<I_ValidateChangeSetChanges> validators = new ArrayList<I_ValidateChangeSetChanges>();

    public EConceptChangeSetReader() {
        super();
    }

    public long nextCommitTime() throws IOException, ClassNotFoundException {
        lazyInit();
        if (nextCommit == null) {
            try {
                nextCommit = dataStream.readLong();
                assert nextCommit != Long.MAX_VALUE;
                nextCommitStr = TimeUtil.getFileDateFormat().format(new Date(nextCommit));
            } catch (EOFException e) {
                AceLog.getAppLog().info("No next commit time for file: " + changeSetFile);
                nextCommit = Long.MAX_VALUE;
                nextCommitStr = "end of time";
            }
        }
        return nextCommit;
    }

    public void readUntil(long endTime) throws IOException, ClassNotFoundException {
        HashSet<TimePathId> values = new HashSet<TimePathId>();
        if (AceLog.getEditLog().isLoggable(Level.INFO)) {
            AceLog.getEditLog().info(
                "Reading from log " + changeSetFile.getName() + " until " + 
                TimeUtil.getFileDateFormat().format(new Date(endTime)));
        }
        while ((nextCommitTime() <= endTime) && (nextCommitTime() != Long.MAX_VALUE)) {
            try {
                EConcept eConcept = new EConcept(dataStream);
                //AceLog.getEditLog().info("Reading change set entry: \n" + eConcept);
                count++;
                if (counter != null) {
                    counter.increment();
                }
                boolean validated = true;
                for (I_ValidateChangeSetChanges v : getValidators()) {
                    if (v.validateChange((I_AmChangeSetObject) eConcept, Terms.get()) == false) {
                        validated = false;
                        AceLog.getEditLog().fine("Failed validator: " + v);
                        break;
                    }
                }
                if (validated) {
                        conceptCount++;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Read eConcept... " + eConcept);
                        }
                        ACE.addImported(commitEConcept(eConcept, nextCommit, values));
                } else {
                    unvalidated++;
                }
                nextCommit = dataStream.readLong();
            } catch (EOFException ex) {
                dataStream.close();
                AceLog.getEditLog().info(
                    "\n  +++++----------------\n End of change set: " + changeSetFile.getName()
                        + "\n  +++++---------------\n");
                nextCommit = Long.MAX_VALUE;
                Terms.get().setProperty(FileIO.getNormalizedRelativePath(changeSetFile),
                    Long.toString(changeSetFile.length()));
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        try {
            if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                AceLog.getEditLog().fine("Committing time branches: " + values);
            }
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
        AceLog.getAppLog().info(
            "Change set " + changeSetFile.getName() + " contains " + count + " change objects. "
                + "\n unvalidated objects: " + unvalidated + "\n imported Concepts: " + conceptCount);

    }

    public void read() throws IOException, ClassNotFoundException {
        readUntil(Long.MAX_VALUE);
    }


    private Concept commitEConcept(EConcept eConcept, long time, Set<TimePathId> values) throws IOException,
            ClassNotFoundException {
        try {
            assert time != Long.MAX_VALUE;
            return Concept.mergeAndWrite(eConcept);
        } catch (Exception e) {
            AceLog.getEditLog().severe(
                "Error committing bean in change set: " + changeSetFile + "\nUniversalAceBean:  \n" + eConcept);
            throw new ToIoException(e);
        }
    }

    private void lazyInit() throws FileNotFoundException, IOException, ClassNotFoundException {
        String lastImportSize = Terms.get().getProperty(FileIO.getNormalizedRelativePath(changeSetFile));
        if (lastImportSize != null) {
            long lastSize = Long.parseLong(lastImportSize);
            if (lastSize == changeSetFile.length()) {
                AceLog.getAppLog().finer(
                    "Change set already fully read: " + FileIO.getNormalizedRelativePath(changeSetFile));
                // already imported, set to nothing to do...
                nextCommit = Long.MAX_VALUE;
                initialized = true;
            }
        }
        if (initialized == false) {
            boolean validated = true;
            for (I_ValidateChangeSetChanges v : getValidators()) {
                try {
                    if (v.validateFile(changeSetFile, Terms.get()) == false) {
                        validated = false;
                        AceLog.getEditLog().fine(
                            "Validation failed for: " + changeSetFile.getAbsolutePath() + " validator: " + v);
                        break;
                    }
                } catch (TerminologyException e) {
                    throw new ToIoException(e);
                }
            }
            if (validated) {
                FileInputStream fis = new FileInputStream(changeSetFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                dataStream = new DataInputStream(bis);
            } else {
                nextCommit = Long.MAX_VALUE;
                nextCommitStr = "end of time";
            }
            initialized = true;
        }
    }

    public File getChangeSetFile() {
        return changeSetFile;
    }

    public void setChangeSetFile(File changeSetFile) {
        this.changeSetFile = changeSetFile;
    }

    public void setCounter(I_Count counter) {
        this.counter = counter;
    }


    public List<I_ValidateChangeSetChanges> getValidators() {
        return validators;
    }

    public int availableBytes() throws FileNotFoundException, IOException, ClassNotFoundException {
        lazyInit();
        if (dataStream != null) {
            return dataStream.available();
        }
        return 0;
    }

}
