package org.ihtsdo.cs.econcept;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.I_AmChangeSetObject;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbProperty;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.time.TimeUtil;

import com.sleepycat.je.DatabaseException;

public class EConceptChangeSetReader implements I_ReadChangeSet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private File changeSetFile;

    private File csreFile;
    private transient FileWriter csreOut;
    private File csrcFile;
    private transient FileWriter csrcOut;

    private I_Count counter;

    private DataInputStream dataStream;

    private int count = 0;

    private int conceptCount = 0;

    private int unvalidated = 0;

    private boolean initialized = false;

    public Long nextCommit;
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
        AceLog.getEditLog().info("readUntil called nextCommitTime() = "+nextCommitTime()+" endTime = "+endTime +" Long.MAX_VALUE = "+Long.MAX_VALUE);
        while ((nextCommitTime() <= endTime) && (nextCommitTime() != Long.MAX_VALUE)) {
      //  while ((nextCommitTime() <= endTime)) {
            try {
                EConcept eConcept = new EConcept(dataStream);
                
                if (csreOut != null) {
                    csreOut.append("\n*******************************\n");
                    csreOut.append(TimeUtil.formatDateForFile(nextCommitTime()));
                    csreOut.append("\n*******************************\n");
                    csreOut.append(eConcept.toString());
                }
                //AceLog.getEditLog().info("Reading change set entry: \n" + eConcept);
                count++;
                //AceLog.getEditLog().info("eConcept found count = "+count);
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
                if (changeSetFile.length() == 0) {
                    changeSetFile.delete();
                }
                AceLog.getEditLog().info(
                    "\n  +++++----------------\n End of change set: " + changeSetFile.getName()
                        + "\n  +++++---------------\n");
                nextCommit = Long.MAX_VALUE;
                Terms.get().setProperty(changeSetFile.getName(),
                    Long.toString(changeSetFile.length()));
                Terms.get().setProperty(BdbProperty.LAST_CHANGE_SET_READ.toString(),
                        changeSetFile.getName());
                if (csreOut != null) {
                    csreOut.flush();
                    csreOut.close();
                    if (csreFile.length() == 0) {
                        csreFile.delete();
                    }
                }
                if (csrcOut != null) {
                    csrcOut.flush();
                    csrcOut.close();
                    if (csrcFile.length() == 0) {
                        csrcFile.delete();
                    }
                }
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
            if (EConceptChangeSetWriter.writeDebugFiles) {
                csrcOut.append("\n*******************************\n");
                csrcOut.append(TimeUtil.formatDateForFile(time));
                csrcOut.append("\n********** before ***********\n");

                Concept before = Concept.get(Bdb.uuidToNid(eConcept.getPrimordialUuid()));
                csrcOut.append(before.toLongString());
                csrcOut.flush();
                Concept after = Concept.mergeAndWrite(eConcept);
                csrcOut.append("\n----------- after  -----------\n");
                csrcOut.append(after.toLongString());
                return after;
            } else {
                return Concept.mergeAndWrite(eConcept);
            }
        } catch (Exception e) {
            AceLog.getEditLog().severe(
                "Error committing bean in change set: " + changeSetFile + "\nUniversalAceBean:  \n" + eConcept);
            throw new ToIoException(e);
        }
    }

    private void lazyInit() throws FileNotFoundException, IOException, ClassNotFoundException {
        String lastImportSize = Terms.get().getProperty(changeSetFile.getName());
        if (lastImportSize != null) {
            long lastSize = Long.parseLong(lastImportSize);
            if (lastSize == changeSetFile.length()) {
                AceLog.getAppLog().finer(
                    "Change set already fully read: " + changeSetFile.getName());
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
                
                if (EConceptChangeSetWriter.writeDebugFiles) {
                    csreFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".csre");;
                    csreOut = new FileWriter(csreFile, true);
                    csrcFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".csrc");;
                    csrcOut = new FileWriter(csrcFile, true);
                }
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
