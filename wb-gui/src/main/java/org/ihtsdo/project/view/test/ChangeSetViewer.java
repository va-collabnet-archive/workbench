/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.view.test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;

import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.helper.time.TimeHelper;

/**
 * The Class ChangeSetViewer.
 */
public class ChangeSetViewer {

    /**
     * Gets the change set file.
     *
     * @return the change set file
     */
    public File getChangeSetFile() {
        return changeSetFile;
    }

    /**
     * Sets the change set file.
     *
     * @param changeSetFile the new change set file
     */
    public void setChangeSetFile(File changeSetFile) {
        this.changeSetFile = changeSetFile;
    }
    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The change set file.
     */
    private File changeSetFile;
    /**
     * The csre file.
     */
    private File csreFile;
    /**
     * The csre out.
     */
    private transient FileWriter csreOut;
    /**
     * The csrc file.
     */
    private File csrcFile;
    /**
     * The csrc out.
     */
    private transient FileWriter csrcOut;
    /**
     * The counter.
     */
    private I_Count counter;
    /**
     * The data stream.
     */
    private DataInputStream dataStream;
    /**
     * The count.
     */
    private int count = 0;
    /**
     * The concept count.
     */
    private int conceptCount = 0;
    /**
     * The unvalidated.
     */
    private int unvalidated = 0;
    /**
     * The initialized.
     */
    private boolean initialized = false;
    /**
     * The next commit.
     */
    private Long nextCommit;
    /**
     * The next commit str.
     */
    private String nextCommitStr;
    /**
     * The no commit.
     */
    private boolean noCommit = false;

    /**
     * Checks if is no commit.
     *
     * @return true, if is no commit
     */
    public boolean isNoCommit() {
        return noCommit;
    }

    /**
     * Sets the no commit.
     *
     * @param noCommit the new no commit
     */
    public void setNoCommit(boolean noCommit) {
        this.noCommit = noCommit;
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        try {
            File dir = new File("/Users/alo/Desktop/changesets");
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".eccs")) {
                    ChangeSetViewer cv = new ChangeSetViewer();
                    cv.setChangeSetFile(file);
                    cv.read();
                }
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (ClassNotFoundException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    /**
     * Read.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public void read() throws IOException, ClassNotFoundException {
        readUntil(Long.MAX_VALUE);
    }

    /**
     * Read until.
     *
     * @param endTime the end time
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public void readUntil(long endTime) throws IOException, ClassNotFoundException {
        HashSet<TimePathId> values = new HashSet<TimePathId>();
        if (AceLog.getEditLog().isLoggable(Level.INFO)) {
            AceLog.getEditLog().info(
                    "Reading from log " + changeSetFile.getName() + " until "
                    + TimeHelper.getFileDateFormat().format(new Date(endTime)));
        }
        while ((nextCommitTime() <= endTime) && (nextCommitTime() != Long.MAX_VALUE)) {
            try {
                EConcept eConcept = new EConcept(dataStream);
                if (eConcept != null) {
                    int textIni = eConcept.toString().indexOf("[TkDescription:  text:");
                    if (textIni > 0) {
                        System.out.println(eConcept.toString().substring(textIni, textIni + 255));
                    }
                }
                if (csreOut != null) {
                    csreOut.append("\n*******************************\n");
                    csreOut.append(TimeHelper.formatDateForFile(nextCommitTime()));
                    csreOut.append("\n*******************************\n");
                    csreOut.append(eConcept.toString());
                }
                //AceLog.getEditLog().info("Reading change set entry: \n" + eConcept);
                count++;
                if (counter != null) {
                    counter.increment();
                }
                boolean validated = true;
                if (validated) {
                    conceptCount++;
                    if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                        AceLog.getEditLog().fine("Read eConcept... " + eConcept);
                    }
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
                //                Terms.get().setProperty(changeSetFile.getName(),
                //                    Long.toString(changeSetFile.length()));
                //                Terms.get().setProperty(BdbProperty.LAST_CHANGE_SET_READ.toString(),
                //                        changeSetFile.getName());
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
        } catch (Exception e) {
            throw new ToIoException(e);
        }
        AceLog.getAppLog().info(
                "Change set " + changeSetFile.getName() + " contains " + count + " change objects. "
                + "\n unvalidated objects: " + unvalidated + "\n imported concepts: " + conceptCount);

    }

    /**
     * Next commit time.
     *
     * @return the long
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public long nextCommitTime() throws IOException, ClassNotFoundException {
        lazyInit();
        if (nextCommit == null) {
            try {
                nextCommit = dataStream.readLong();
                assert nextCommit != Long.MAX_VALUE;
                nextCommitStr = TimeHelper.getFileDateFormat().format(new Date(nextCommit));
            } catch (EOFException e) {
                AceLog.getAppLog().info("No next commit time for file: " + changeSetFile);
                nextCommit = Long.MAX_VALUE;
                nextCommitStr = "end of time";
            }
        }
        return nextCommit;
    }

    /**
     * Lazy init.
     *
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private void lazyInit() throws FileNotFoundException, IOException, ClassNotFoundException {
        String lastImportSize = null;//Terms.get().getProperty(changeSetFile.getName());
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
            if (validated) {
                FileInputStream fis = new FileInputStream(changeSetFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                dataStream = new DataInputStream(bis);
//
//                if (EConceptChangeSetWriter.writeDebugFiles) {
//                    csreFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".csre");
//                    csreOut = new FileWriter(csreFile, true);
//                    csrcFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".csrc");
//                    csrcOut = new FileWriter(csrcFile, true);
//                }
            } else {
                nextCommit = Long.MAX_VALUE;
                nextCommitStr = "end of time";
            }
            initialized = true;
        }
    }
}
