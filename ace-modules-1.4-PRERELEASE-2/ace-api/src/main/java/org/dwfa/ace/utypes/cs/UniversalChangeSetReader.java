/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.utypes.cs;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalIdList;

public class UniversalChangeSetReader implements I_ReadChangeSet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private File changeSetFile;

    private I_Count counter;

    private ObjectInputStream ois;

    private int count = 0;

    private int conceptCount = 0;

    private int pathCount = 0;

    private int refsetMemberCount = 0;

    private int idListCount = 0;

    private int unvalidated = 0;

    private boolean initialized = false;

    private Long nextCommit;

    private transient List<I_ValidateChangeSetChanges> validators = new ArrayList<I_ValidateChangeSetChanges>();

    private List<I_ProcessUniversalChangeSets> processors;

    public UniversalChangeSetReader(List<I_ProcessUniversalChangeSets> processors, File changeSetFile) {
        super();
        this.processors = processors;
        this.changeSetFile = changeSetFile;
    }

    public UniversalChangeSetReader(I_ProcessUniversalChangeSets processor, File changeSetFile) {
        super();
        this.processors = new ArrayList<I_ProcessUniversalChangeSets>();
        this.processors.add(processor);
        this.changeSetFile = changeSetFile;
    }

    public long nextCommitTime() throws IOException, ClassNotFoundException {
        lazyInit();
        if (nextCommit == null) {
            nextCommit = ois.readLong();
        }
        return nextCommit;
    }

    public void readUntil(long endTime) throws IOException, ClassNotFoundException {
        if (AceLog.getEditLog().isLoggable(Level.INFO)) {
            AceLog.getEditLog().info(
                "Reading from log " + changeSetFile.getName() + " until " + new Date(endTime).toString());
        }
        while ((nextCommitTime() <= endTime) && (nextCommitTime() != Long.MAX_VALUE)) {
            try {
                Object obj = ois.readObject();
                count++;
                if (counter != null) {
                    counter.increment();
                }
                boolean validated = true;
                if (validated) {
                    if (UniversalAceBean.class.isAssignableFrom(obj.getClass())) {
                        conceptCount++;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Read UniversalAceBean... " + obj);
                        }
                        for (I_ProcessUniversalChangeSets processor : processors) {
                            processor.processUniversalAceBean((UniversalAceBean) obj, nextCommit);
                        }
                    } else if (UniversalIdList.class.isAssignableFrom(obj.getClass())) {
                        idListCount++;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Read UniversalIdList... " + obj);
                        }
                        for (I_ProcessUniversalChangeSets processor : processors) {
                            processor.processIdList((UniversalIdList) obj, nextCommit);
                        }
                    } else if (UniversalAcePath.class.isAssignableFrom(obj.getClass())) {
                        pathCount++;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Read UniversalAcePath... " + obj);
                        }
                        for (I_ProcessUniversalChangeSets processor : processors) {
                            processor.processAcePath((UniversalAcePath) obj, nextCommit);
                        }
                    } else if (UniversalAceExtByRefBean.class.isAssignableFrom(obj.getClass())) {
                        refsetMemberCount++;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Read UniversalAceExtByRefBean... " + obj);
                        }
                        for (I_ProcessUniversalChangeSets processor : processors) {
                            processor.processAceEbr((UniversalAceExtByRefBean) obj, nextCommit);
                        }
                    } else {
                        throw new IOException("Can't handle class: " + obj.getClass().getName());
                    }
                } else {
                    unvalidated++;
                }
                nextCommit = ois.readLong();
            } catch (EOFException ex) {
                ois.close();
                AceLog.getEditLog().info(
                    "\n  +++++----------------\n End of change set: " + changeSetFile.getName()
                        + "\n  +++++---------------\n");
                nextCommit = Long.MAX_VALUE;
            }
        }

        AceLog.getAppLog().info(
            "Change set " + changeSetFile.getName() + " contains " + count + " change objects. "
                + "\n unvalidated objects: " + unvalidated + "\n imported Concepts: " + conceptCount + " paths: "
                + pathCount + " refset members: " + refsetMemberCount + " idListCount:" + idListCount);

    }

    public void read() throws IOException, ClassNotFoundException {
        readUntil(Long.MAX_VALUE);
    }

    @SuppressWarnings("unchecked")
    private void lazyInit() throws FileNotFoundException, IOException, ClassNotFoundException {

        if (initialized == false) {
            FileInputStream fis = new FileInputStream(changeSetFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
            @SuppressWarnings("unused")
            Class<I_ReadChangeSet> readerClass = (Class<I_ReadChangeSet>) ois.readObject();
            initialized = true;
        }
    }

    public File getChangeSetFile() {
        return changeSetFile;
    }

    public void setCounter(I_Count counter) {
        this.counter = counter;
    }

    public List<I_ValidateChangeSetChanges> getValidators() {
        return validators;
    }

    public int availableBytes() throws FileNotFoundException, IOException, ClassNotFoundException {
        lazyInit();
        if (ois != null) {
            return ois.available();
        }
        return 0;
    }

    public void setChangeSetFile(File changeSetFile) {
        // TODO Auto-generated method stub

    }

}
