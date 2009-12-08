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
package org.dwfa.ace.task.cmrscs;

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
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;

public class CmrscsReader implements I_ReadChangeSet {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private File changeSetFile;
    private I_Count counter;
    private boolean initialized = false;
    private DataInputStream dis;
    private Long nextCommit;

    public CmrscsReader(File changeSetFile) {
        super();
        this.changeSetFile = changeSetFile;
    }

    public List<I_ValidateChangeSetChanges> getValidators() {
        return new ArrayList<I_ValidateChangeSetChanges>();
    }

    public int availableBytes() throws FileNotFoundException, IOException, ClassNotFoundException {
        lazyInit();
        if (dis != null) {
            return dis.available();
        }
        return 0;
    }

    public long nextCommitTime() throws IOException, ClassNotFoundException {
        lazyInit();
        if (nextCommit == null) {
            nextCommit = dis.readLong();
        }
        return nextCommit;
    }

    public void read() throws IOException, ClassNotFoundException {
        readUntil(Long.MAX_VALUE);
    }

    @SuppressWarnings("unchecked")
    public void readUntil(long endTime) throws IOException, ClassNotFoundException {
        HashSet<TimePathId> timePathValues = new HashSet<TimePathId>();
        UUID endUid = new UUID(0, 0);
        try {
            int unspecifiedUuidNid = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid();
            int conceptExt = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid();
            while (nextCommitTime() < endTime) {
                int count = 0;
                if (counter != null) {
                    counter.increment();
                }
                UUID pathUid = readUuid(dis);
                int pathNid = getVodb().uuidToNative(pathUid);
                I_Path path = getVodb().getPath(new UUID[] { pathUid });
                UUID refsetUid = readUuid(dis);
                int refsetNid = getVodb().uuidToNative(refsetUid);
                UUID memberUid = readUuid(dis);
                int version = getVodb().convertToThinVersion(nextCommit);
                timePathValues.add(new TimePathId(version, path.getConceptId()));
                while (memberUid.equals(endUid) == false) {
                    count++;
                    UUID componentUid = readUuid(dis);
                    UUID statusUid = readUuid(dis);
                    UUID conceptValueUid = readUuid(dis);

                    I_ThinExtByRefVersioned ebr;
                    I_ThinExtByRefPartConcept newPart = getVodb().newConceptExtensionPart();
                    newPart.setPathId(pathNid);
                    newPart.setStatus(getVodb().uuidToNative(statusUid));
                    newPart.setConceptId(getVodb().uuidToNative(conceptValueUid));
                    newPart.setVersion(version);
                    int memberNid = getVodb().uuidToNativeWithGeneration(memberUid, unspecifiedUuidNid, path, version);
                    if (getVodb().hasExtension(memberNid)) {
                        ebr = getVodb().getExtension(memberNid);
                        I_ThinExtByRefPartConcept lastPart = (I_ThinExtByRefPartConcept) ebr.getVersions().get(
                            ebr.getVersions().size() - 1);
                        ebr.getVersions().clear();
                        ebr.addVersion(lastPart);
                        ebr.addVersion(newPart);
                    } else {
                        ebr = getVodb().getDirectInterface().newExtensionBypassCommit(refsetNid, memberNid,
                            getVodb().uuidToNative(componentUid), conceptExt);
                        ((List<I_ThinExtByRefPartConcept>) ebr.getVersions()).add(newPart);

                    }
                    try {
                        if (memberUid.equals(UUID.fromString("1e3f44ed-bbe2-4148-bbe5-5240cd408b26"))) {
                            AceLog.getEditLog().severe("Encountered the problem ext member...");
                        }
                        getVodb().getDirectInterface().writeExt(ebr);
                    } catch (IOException e) {
                        AceLog.getEditLog().severe("Exception writing extension: " + ebr);
                        AceLog.getEditLog().severe(
                            "memberUid: " + memberUid + "\ncomponentUid: " + componentUid + "\npathUid: " + pathUid
                                + "\nrefsetUid: " + refsetUid + "\nstatusUid: " + statusUid + "\nconceptValueUid: "
                                + conceptValueUid + "\ndate: " + new Date(nextCommit));
                        AceLog.getEditLog().alertAndLogException(e);
                        throw e;
                    }
                    memberUid = readUuid(dis);
                }
                AceLog.getEditLog().info("End of commit set. Processed " + count + " items");
                nextCommit = dis.readLong();
            }
        } catch (EOFException ex) {
            dis.close();
            AceLog.getEditLog().info("End of change set: " + changeSetFile);
            nextCommit = Long.MAX_VALUE;
            getVodb().setProperty(changeSetFile.getName(), Long.toString(changeSetFile.length()));
        } catch (TerminologyException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("Committing time branches: " + timePathValues);
        }
        for (TimePathId timePath : timePathValues) {
            getVodb().getDirectInterface().writeTimePath(timePath);
        }
    }

    public void setChangeSetFile(File changeSetFile) {
        this.changeSetFile = changeSetFile;
    }

    public void setCounter(I_Count counter) {
        this.counter = counter;
    }

    private UUID readUuid(DataInputStream dis) throws IOException {
        return new UUID(dis.readLong(), dis.readLong());

    }

    @SuppressWarnings("unchecked")
    private void lazyInit() throws FileNotFoundException, IOException, ClassNotFoundException {
        if (initialized == false) {
            String lastImportSize = getVodb().getProperty(changeSetFile.getName());
            if (lastImportSize != null) {
                long lastSize = Long.parseLong(lastImportSize);
                if (lastSize == changeSetFile.length()) {
                    AceLog.getAppLog().finer(
                        "Change set already fully read: " + FileIO.getNormalizedRelativePath(changeSetFile));
                    // already imported, set to nothing to do...
                    nextCommit = Long.MAX_VALUE;
                    initialized = true;
                } else {
                    AceLog.getAppLog().finer(
                        "Change set previously encountered, but different length. Old length was " + lastSize
                            + " new length is " + changeSetFile.length() + ": "
                            + FileIO.getNormalizedRelativePath(changeSetFile));
                }
            } else {
                AceLog.getAppLog().finer(
                    "Change set never previously encountered: " + FileIO.getNormalizedRelativePath(changeSetFile));
            }

            if (initialized == false) {
                FileInputStream fis = new FileInputStream(changeSetFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                dis = new DataInputStream(bis);
                initialized = true;
                nextCommit = dis.readLong();
            }
        }
    }

    private I_TermFactory tf = null;

    private I_TermFactory getVodb() {
        if (tf == null) {
            tf = LocalVersionedTerminology.get();
        }
        return tf;
    }

    public File getChangeSetFile() {
        return changeSetFile;
    }

}
