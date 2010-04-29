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
package org.dwfa.ace.cs;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.I_AmChangeSetObject;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.ace.utypes.UniversalIdList;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinExtByRefPart;
import org.dwfa.vodb.types.ThinExtByRefVersioned;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;
import org.dwfa.vodb.types.ThinImagePart;
import org.dwfa.vodb.types.ThinImageVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.je.DatabaseException;

public class BinaryChangeSetReader implements I_ReadChangeSet {

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

    private VodbEnv vodb;

    private transient List<I_ValidateChangeSetChanges> validators = new ArrayList<I_ValidateChangeSetChanges>();

    public BinaryChangeSetReader() {
        super();
    }

    public long nextCommitTime() throws IOException, ClassNotFoundException {
        lazyInit();
        ACE.commitSequence++;
        if (nextCommit == null) {
            try {
                nextCommit = ois.readLong();
            } catch (EOFException e) {
                AceLog.getAppLog().info("No next commit time for file: " + changeSetFile);
                nextCommit = Long.MAX_VALUE;
            }
        }
        return nextCommit;
    }

    public void readUntil(long endTime) throws IOException, ClassNotFoundException {
        HashSet<TimePathId> values = new HashSet<TimePathId>();
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
                for (I_ValidateChangeSetChanges v : getValidators()) {
                    if (v.validateChange((I_AmChangeSetObject) obj, getVodb()) == false) {
                        validated = false;
                        AceLog.getEditLog().fine("Failed validator: " + v);
                        break;
                    }
                }
                if (validated) {
                    if (UniversalAceBean.class.isAssignableFrom(obj.getClass())) {
                        conceptCount++;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Read UniversalAceBean... " + obj);
                        }
                        ACE.addImported(commitAceBean((UniversalAceBean) obj, nextCommit, values));
                    } else if (UniversalIdList.class.isAssignableFrom(obj.getClass())) {
                        idListCount++;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Read UniversalIdList... " + obj);
                        }
                        commitIdList((UniversalIdList) obj, nextCommit, values);
                    } else if (UniversalAcePath.class.isAssignableFrom(obj.getClass())) {
                        pathCount++;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Read UniversalAcePath... " + obj);
                        }
                        commitAcePath((UniversalAcePath) obj, nextCommit);
                    } else if (UniversalAceExtByRefBean.class.isAssignableFrom(obj.getClass())) {
                        refsetMemberCount++;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Read UniversalAceExtByRefBean... " + obj);
                        }
                        commitAceEbr((UniversalAceExtByRefBean) obj, nextCommit, values);
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
                getVodb().setProperty(changeSetFile.getName(), Long.toString(changeSetFile.length()));
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        try {
            if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                AceLog.getEditLog().fine("Committing time branches: " + values);
            }
            getVodb().addPositions(values);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
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
        if (!initialized) {
            FileInputStream fis = new FileInputStream(changeSetFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
        }

        String lastImportSize = getVodb().getProperty(changeSetFile.getName());
        if (lastImportSize != null && !initialized) {
            long lastSize = Long.parseLong(lastImportSize);
            if (lastSize == changeSetFile.length()) {
                AceLog.getAppLog().finer(
                    "Change set already fully read: " + FileIO.getNormalizedRelativePath(changeSetFile));
                // already imported, set to nothing to do...
                nextCommit = Long.MAX_VALUE;
                initialized = true;
            } else {
                AceLog.getAppLog().finer(
                    "Change set already read: " + lastSize + " Skipping read bytes " + FileIO.getNormalizedRelativePath(changeSetFile));
                ois.skipBytes((int) lastSize);
            }
        }
        if (initialized == false) {
            boolean validated = true;
            for (I_ValidateChangeSetChanges v : getValidators()) {
                try {
                    if (v.validateFile(changeSetFile, getVodb()) == false) {
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
                Class<I_ReadChangeSet> readerClass = (Class<I_ReadChangeSet>) ois.readObject();
                if (BinaryChangeSetReader.class.isAssignableFrom(readerClass)) {
                    AceLog.getEditLog().fine(
                        "\n--------------------------\nNow initializing change set with BinaryChangeSetReader: "
                            + changeSetFile.getName() + "\n--------------------------\n ");
                } else {
                    AceLog.getAppLog().warning(
                        "ReaderClass " + readerClass.getName() + " is not assignable from BinaryChangeSetReader...");
                }
            } else {
                nextCommit = Long.MAX_VALUE;
            }
            initialized = true;
        }
    }

    private void commitAcePath(UniversalAcePath path, long time) throws DatabaseException, TerminologyException,
            IOException {
        AceLog.getEditLog().fine("Importing new universal path: \n" + path);
        List<I_Position> origins = null;
        try {
            I_Path newPath = getVodb().getPath(getNid(path.getPathId()));
            AceLog.getEditLog().fine("Importing path that already exists: \n" + path + "\n\n" + newPath);
        } catch (PathNotExistsException e) {
            if (path.getOrigins() != null) {
                origins = new ArrayList<I_Position>(path.getOrigins().size());
                for (UniversalAcePosition pos : path.getOrigins()) {
                    I_Path thinPath = getVodb().getPath(getNid(pos.getPathId()));
                    origins.add(new Position(ThinVersionHelper.convert(pos.getTime()), thinPath));
                }
            }
            Path newPath = new Path(getNid(path.getPathId()), origins);
            AceLog.getEditLog().fine("writing new path: \n" + newPath);
            getVodb().writePath(newPath);
        }

    }

    private ConceptBean commitAceBean(UniversalAceBean bean, long time, Set<TimePathId> values) throws IOException,
            ClassNotFoundException {
        try {
            if (time == Long.MAX_VALUE) {
                throw new IOException("commit time = Long.MAX_VALUE");
            }
            // Do all the committing...
            commitUncommittedIds(time, bean, values);
            commitUncommittedConceptAttributes(time, bean, values);
            commitUncommittedDescriptions(time, bean, values);
            commitUncommittedRelationships(time, bean, values);
            commitUncommittedImages(time, bean, values);

            commitConceptAttributeChanges(time, bean, values);
            commitDescriptionChanges(time, bean, values);
            commitRelationshipChanges(time, bean, values);
            commitImageChanges(time, bean, values);

            ConceptBean localBean = ConceptBean.get(bean.getId().getUIDs());
            localBean.flush();
            return localBean;
        } catch (Exception e) {
            AceLog.getEditLog().severe(
                "Error committing bean in change set: " + changeSetFile + "\nUniversalAceBean:  \n" + bean);
            throw new ToIoException(e);
        }
    }

    private void commitAceEbr(UniversalAceExtByRefBean bean, long time, Set<TimePathId> values) throws IOException,
            ClassNotFoundException {
        try {
            if (time == Long.MAX_VALUE) {
                throw new IOException("commit time = Long.MAX_VALUE");
            }
            if (getVodb() == null) {
                throw new IOException("getVodb() returns null");
            }
            // Do all the committing...
            Collection<UUID> memberUid = bean.getMemberUid();
            I_IdVersioned id = getVodb().getId(memberUid);
            int memberId = Integer.MIN_VALUE;
            if (id == null) {
                I_Path path = getVodb().getPath(
                    getVodb().uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()));
                memberId = getVodb().uuidToNativeWithGeneration(memberUid, Integer.MAX_VALUE, path,
                    ThinVersionHelper.convert(time));
            } else {
                memberId = id.getNativeId();
            }

            I_ThinExtByRefVersioned extension = null;
            if (getVodb().hasExtension(memberId)) {
                extension = getVodb().getExtension(memberId);
            } else {
                int refsetId = getVodb().getId(bean.getRefsetUid()).getNativeId();
                I_IdVersioned componentUuid = getVodb().getId(bean.getComponentUid());

                if (componentUuid == null) {
                    AceLog.getAppLog().severe(
                        changeSetFile.getName() + " Error importing extension... Component with id does not exist: "
                            + bean.getComponentUid());
                } else {
                    int componentId = componentUuid.getNativeId();
                    int typeId = getVodb().getId(bean.getTypeUid()).getNativeId();
                    int partCount = bean.getVersions().size();
                    extension = new ThinExtByRefVersioned(refsetId, memberId, componentId, typeId, partCount);
                }
            }
            if (extension != null) {
                boolean changed = false;
                for (UniversalAceExtByRefPart part : bean.getVersions()) {
                    if (part.getTime() == Long.MAX_VALUE) {
                        ThinExtByRefPart newPart = ThinExtByRefVersioned.makePart(part);
                        newPart.setVersion(ThinVersionHelper.convert(time));
                        if (!extension.getVersions().contains(newPart)) {
                            changed = true;
                            extension.addVersion(newPart);
                            values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                        }
                    }
                }
                if (changed) {
                    getVodb().writeExt(extension);
                    AceLog.getEditLog().fine("Importing changed extension: \n" + extension);
                }
            }
        } catch (TerminologyException e) {
            throw new ToIoException(e);
        }
    }

    private void commitIdList(UniversalIdList list, long time, Set<TimePathId> values) throws IOException,
            ClassNotFoundException {
        if (time == Long.MAX_VALUE) {
            throw new IOException("commit time = Long.MAX_VALUE");
        }
        // Do all the commiting...
        boolean firstException = true;
        for (UniversalAceIdentification id : list.getUncommittedIds()) {
            try {
                AceLog.getEditLog().fine("commitUncommittedIds: " + id);
                ThinIdVersioned tid = null;
                I_Path path;
                for (UniversalAceIdentificationPart part : id.getVersions()) {
                    try {
                        path = getVodb().getPath(getVodb().uuidToNative(part.getPathId()));
                    } catch (TerminologyException ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                        path = getVodb().newPath(null, getVodb().getConcept(part.getPathId()));
                    }

                    if (tid == null) {
                        try {
                            int nid = getNid(id.getUIDs());
                            AceLog.getEditLog().fine("Uncommitted id already exists: \n" + id);
                            tid = (ThinIdVersioned) getVodb().getId(nid);
                            AceLog.getEditLog().fine("found ThinIdVersioned: " + tid);
                        } catch (NoMappingException ex) {
                            /*
                             * Generate on the ARCHITECTONIC_BRANCH for now, it
                             * will get overwritten when the id is written to
                             * the database, with the proper branch and version
                             * values.
                             */
                            int nid = getVodb().uuidToNativeWithGeneration(id.getUIDs(), Integer.MAX_VALUE, path,
                                ThinVersionHelper.convert(time));
                            tid = new ThinIdVersioned(nid, id.getVersions().size());
                            AceLog.getEditLog().fine("created ThinIdVersioned: " + tid);
                        }
                    }
                    ThinIdPart newPart = new ThinIdPart();
                    newPart.setStatusId(getNid(part.getIdStatus()));
                    newPart.setPathId(getVodb().uuidToNative(part.getPathId()));
                    newPart.setSource(getNid(part.getSource()));
                    newPart.setSourceId(part.getSourceId());
                    if (part.getTime() == Long.MAX_VALUE) {
                        newPart.setVersion(ThinVersionHelper.convert(time));
                    } else {
                        newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
                    }
                    if (tid.getVersions().contains(newPart)) {
                        // Nothing changed...
                    } else {
                        values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                        tid.addVersion(newPart);
                        AceLog.getEditLog().fine(" add version: " + newPart);
                    }
                }
                /*
                 * The ARCHITECTONIC_BRANCH will be overridden here with the
                 * proper branch and version values here...
                 */
                getVodb().writeId(tid);
            } catch (TerminologyException e) {
                if (firstException) {
                    AceLog.getEditLog().alertAndLog(
                        Level.SEVERE,
                        "Exception. Ignoring component, and continuing import. Examine log for future exceptions. "
                            + id, e);
                    firstException = false;
                } else {
                    AceLog.getEditLog().log(
                        Level.SEVERE,
                        "Exception. Ignoring component, and continuing import. Examine log for future exceptions. "
                            + id, e);
                }
            } catch (Exception e) {
                if (firstException) {
                    AceLog.getEditLog()
                        .alertAndLog(
                            Level.SEVERE,
                            "Exception. Ignoring component, and continuing import. Examine log for future exceptions."
                                + id, e);
                    firstException = false;
                } else {
                    AceLog.getEditLog()
                        .log(
                            Level.SEVERE,
                            "Exception. Ignoring component, and continuing import. Examine log for future exceptions."
                                + id, e);
                }
            }

        }
    }

    private int getNid(Collection<UUID> uids) throws TerminologyException, IOException {
        return getVodb().uuidToNative(uids);
    }

    private void commitConceptAttributeChanges(long time, UniversalAceBean bean, Set<TimePathId> values)
            throws DatabaseException, TerminologyException, IOException {
        if (bean.getConceptAttributes() != null) {
            try {
                UniversalAceConceptAttributes attributes = bean.getConceptAttributes();
                ThinConVersioned thinAttributes = (ThinConVersioned) getVodb().getConceptAttributes(
                    getNid(attributes.getConId()));
                boolean changed = false;
                for (UniversalAceConceptAttributesPart part : attributes.getVersions()) {
                    if (part.getTime() == Long.MAX_VALUE) {
                        ThinConPart newPart = new ThinConPart();
                        newPart.setPathId(getNid(part.getPathId()));
                        newPart.setStatusId(getNid(part.getConceptStatus()));
                        newPart.setDefined(part.isDefined());
                        newPart.setVersion(ThinVersionHelper.convert(time));
                        if (!thinAttributes.getVersions().contains(newPart)) {
                            changed = true;
                            thinAttributes.addVersion(newPart);
                            values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                        }
                    }
                }
                if (changed) {
                    getVodb().writeConceptAttributes(thinAttributes);
                    AceLog.getEditLog().fine("Importing changed attributes: \n" + thinAttributes);
                }
            } catch (TerminologyException e) {
                AceLog.getEditLog().alertAndLog(
                    Level.SEVERE,
                    "TerminologyException. Ignoring component, and continuing import." + bean.getConceptAttributes()
                        + " " + changeSetFile.getCanonicalPath(), e);
            }
        }
    }

    private void commitUncommittedConceptAttributes(long time, UniversalAceBean bean, Set<TimePathId> values)
            throws DatabaseException, TerminologyException, IOException {
        if (bean.getUncommittedConceptAttributes() != null) {
            UniversalAceConceptAttributes attributes = bean.getUncommittedConceptAttributes();
            ThinConVersioned thinAttributes = new ThinConVersioned(getNid(attributes.getConId()),
                attributes.versionCount());
            for (UniversalAceConceptAttributesPart part : attributes.getVersions()) {
                ThinConPart newPart = new ThinConPart();
                newPart.setPathId(getNid(part.getPathId()));
                newPart.setStatusId(getNid(part.getConceptStatus()));
                newPart.setDefined(part.isDefined());
                if (part.getTime() == Long.MAX_VALUE) {
                    newPart.setVersion(ThinVersionHelper.convert(time));
                } else {
                    newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
                }
                values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                thinAttributes.addVersion(newPart);
            }
            try {
                ThinConVersioned oldVersioned = (ThinConVersioned) getVodb().getConceptAttributes(
                    thinAttributes.getConId());
                if (oldVersioned != null) {
                    oldVersioned.merge(thinAttributes);
                    AceLog.getEditLog().fine(
                        "Merging attributes with existing (should have been null): \n" + thinAttributes + "\n\n"
                            + oldVersioned);
                }
            } catch (IOException e) {
                if (ToIoException.class.isAssignableFrom(e.getClass())) {
                    // expected exception if this is a new concept...
                } else
                    throw e;
            }
            getVodb().writeConceptAttributes(thinAttributes);
            AceLog.getEditLog().fine("Importing attributes: \n" + thinAttributes);
        }
    }

    private void commitImageChanges(long time, UniversalAceBean bean, Set<TimePathId> values) throws DatabaseException,
            TerminologyException, IOException {
        for (UniversalAceImage image : bean.getImages()) {
            try {
                ThinImageVersioned thinImage = (ThinImageVersioned) getVodb().getImage(getNid(image.getImageId()));
                boolean changed = false;
                for (UniversalAceImagePart part : image.getVersions()) {
                    if (part.getTime() == Long.MAX_VALUE) {
                        ThinImagePart newPart = new ThinImagePart();
                        newPart.setPathId(getNid(part.getPathId()));
                        newPart.setStatusId(getNid(part.getStatusId()));
                        newPart.setTextDescription(part.getTextDescription());
                        newPart.setTypeId(getNid(part.getTypeId()));
                        newPart.setVersion(ThinVersionHelper.convert(time));
                        if (!thinImage.getVersions().contains(newPart)) {
                            changed = true;
                            thinImage.addVersion(newPart);
                            values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                        }
                    }
                }
                if (changed) {
                    getVodb().writeImage(thinImage);
                    AceLog.getEditLog().fine("Importing image changes: \n" + thinImage);
                }
            } catch (DatabaseException e) {
                AceLog.getEditLog().alertAndLog(
                    Level.SEVERE,
                    "Database exception. Ignoring component, and continuing import." + image + " "
                        + changeSetFile.getCanonicalPath(), e);
            } catch (TerminologyException e) {
                AceLog.getEditLog().alertAndLog(
                    Level.SEVERE,
                    "TerminologyException. Ignoring component, and continuing import." + image + " "
                        + changeSetFile.getCanonicalPath(), e);
            }
        }
    }

    private void commitUncommittedImages(long time, UniversalAceBean bean, Set<TimePathId> values)
            throws DatabaseException, TerminologyException, IOException {
        for (UniversalAceImage image : bean.getUncommittedImages()) {
            ThinImageVersioned thinImage = new ThinImageVersioned(getNid(image.getImageId()), image.getImage(),
                new ArrayList<I_ImagePart>(), image.getFormat(), getNid(image.getConceptId()));
            for (UniversalAceImagePart part : image.getVersions()) {
                ThinImagePart newPart = new ThinImagePart();
                newPart.setPathId(getNid(part.getPathId()));
                newPart.setStatusId(getNid(part.getStatusId()));
                newPart.setTextDescription(part.getTextDescription());
                newPart.setTypeId(getNid(part.getTypeId()));
                if (part.getTime() == Long.MAX_VALUE) {
                    newPart.setVersion(ThinVersionHelper.convert(time));
                } else {
                    newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
                }
                values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                thinImage.addVersion(newPart);
            }
            try {
                ThinImageVersioned oldVersioned = (ThinImageVersioned) getVodb().getImage(thinImage.getImageId());
                if (oldVersioned != null) {
                    oldVersioned.merge(thinImage);
                    AceLog.getEditLog().fine(
                        "Merging image with existing (should have been null): \n" + thinImage + "\n\n" + oldVersioned);
                }
            } catch (DatabaseException e) {
                // expected exception...
            }
            getVodb().writeImage(thinImage);
            AceLog.getEditLog().fine("Importing image: \n" + thinImage);
        }
    }

    private void commitUncommittedRelationships(long time, UniversalAceBean bean, Set<TimePathId> values)
            throws DatabaseException, TerminologyException, IOException {
        for (UniversalAceRelationship rel : bean.getUncommittedSourceRels()) {
            try {
                ThinRelVersioned thinRel = new ThinRelVersioned(getNid(rel.getRelId()), getNid(rel.getC1Id()),
                    getNid(rel.getC2Id()), rel.versionCount());
                for (UniversalAceRelationshipPart part : rel.getVersions()) {
                    ThinRelPart newPart = new ThinRelPart();
                    newPart.setCharacteristicId(getNid(part.getCharacteristicId()));
                    newPart.setGroup(part.getGroup());
                    newPart.setPathId(getNid(part.getPathId()));
                    newPart.setRefinabilityId(getNid(part.getRefinabilityId()));
                    newPart.setTypeId(getNid(part.getRelTypeId()));
                    newPart.setStatusId(getNid(part.getStatusId()));
                    newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
                    if (part.getTime() == Long.MAX_VALUE) {
                        newPart.setVersion(ThinVersionHelper.convert(time));
                    } else {
                        newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
                    }

                    if (!thinRel.getVersions().contains(newPart)) {
                        values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                        thinRel.addVersion(newPart);
                    }
                }

                ThinRelVersioned oldVersioned = null;
                try {
                    oldVersioned = (ThinRelVersioned) getVodb().getRel(thinRel.getRelId(), thinRel.getC1Id());
                } catch (DatabaseException ignorNoRelationshipsFound) {
                }

                //If there is an existing relationship, add the new version if the details are different.
                if (oldVersioned != null) {
                    if (oldVersioned.merge(thinRel)) {
                        getVodb().writeRel(thinRel);
                        AceLog.getAppLog().info(
                            "Merging rel with existing (should have been null): \n" + thinRel + "\n\n" + oldVersioned);
                    } else {
                        AceLog.getAppLog().severe(
                            "Ignoring as existing relationship with the details \n" + thinRel + "\n\n" + oldVersioned);
                    }
                } else {
                    getVodb().writeRel(thinRel);
                }

                if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                    AceLog.getEditLog().fine("Importing rel: \n" + thinRel);
                    List<I_RelVersioned> destRels = getVodb().getDestRels(thinRel.getC2Id());
                    if (destRels.contains(thinRel)) {
                        AceLog.getEditLog().fine("found in dest rels.");
                    } else {
                        AceLog.getEditLog().severe("NOT found in dest rels: " + destRels);
                    }
                }
            } catch (NoMappingException e) {
                AceLog.getEditLog().alertAndLog(
                    Level.SEVERE,
                    "Mapping exception. Ignoring component, and continuing import.\n" + rel + " "
                        + changeSetFile.getCanonicalPath(), e);
            }

        }
    }

    private void commitRelationshipChanges(long time, UniversalAceBean bean, Set<TimePathId> values)
            throws DatabaseException, TerminologyException, IOException {
        for (UniversalAceRelationship rel : bean.getSourceRels()) {
            try {
                ThinRelVersioned thinRel = (ThinRelVersioned) getVodb().getRel(getNid(rel.getRelId()),
                    getNid(rel.getC1Id()));
                boolean changed = false;
                if (thinRel == null) {
                    changed = true;
                    thinRel = new ThinRelVersioned(getNid(rel.getRelId()), getNid(rel.getC1Id()),
                        getNid(rel.getC2Id()), rel.getVersions().size());
                    for (UniversalAceRelationshipPart part : rel.getVersions()) {
                        ThinRelPart newPart = new ThinRelPart();
                        newPart.setCharacteristicId(getNid(part.getCharacteristicId()));
                        newPart.setGroup(part.getGroup());
                        newPart.setPathId(getNid(part.getPathId()));
                        newPart.setRefinabilityId(getNid(part.getRefinabilityId()));
                        newPart.setTypeId(getNid(part.getRelTypeId()));
                        newPart.setStatusId(getNid(part.getStatusId()));
                        if (part.getTime() == Long.MAX_VALUE) {
                            newPart.setVersion(ThinVersionHelper.convert(time));
                        } else {
                            newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
                        }
                        if (!thinRel.getVersions().contains(newPart)) {
                            changed = true;
                            thinRel.addVersion(newPart);
                            values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                        }
                    }

                    AceLog.getEditLog().warning(
                        "Committed relationship missing from database: \n\n" + rel
                            + "\n\nAdding missing relationship and continuing.");

                } else {
                    for (UniversalAceRelationshipPart part : rel.getVersions()) {
                        if (part.getTime() == Long.MAX_VALUE) {
                            ThinRelPart newPart = new ThinRelPart();
                            newPart.setCharacteristicId(getNid(part.getCharacteristicId()));
                            newPart.setGroup(part.getGroup());
                            newPart.setPathId(getNid(part.getPathId()));
                            newPart.setRefinabilityId(getNid(part.getRefinabilityId()));
                            newPart.setTypeId(getNid(part.getRelTypeId()));
                            newPart.setStatusId(getNid(part.getStatusId()));
                            newPart.setVersion(ThinVersionHelper.convert(time));
                            if (!thinRel.getVersions().contains(newPart)) {
                                changed = true;
                                thinRel.addVersion(newPart);
                                values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                            }
                        }
                    }
                }
                if (changed) {
                    getVodb().writeRel(thinRel);
                    AceLog.getEditLog().fine("Importing rel: \n" + thinRel);
                }
            } catch (DatabaseException e) {
                AceLog.getEditLog().alertAndLog(
                    Level.SEVERE,
                    "Database exception. Ignoring component, and continuing import." + rel + " "
                        + changeSetFile.getCanonicalPath(), e);
            } catch (TerminologyException e) {
                AceLog.getEditLog().alertAndLog(
                    Level.SEVERE,
                    "TerminologyException. Ignoring component, and continuing import." + rel + " "
                        + changeSetFile.getCanonicalPath(), e);
            }
        }
    }

    private void commitDescriptionChanges(long time, UniversalAceBean bean, Set<TimePathId> values)
            throws DatabaseException, TerminologyException, IOException {
        for (UniversalAceDescription desc : bean.getDescriptions()) {
            if (desc.getVersions().size() == 0) {
                AceLog.getAppLog().warning("Description has no parts: " + desc);
            } else {
                try {
                    try {

                        if (getVodb().hasDescription(getNid(desc.getDescId()), getNid(desc.getConceptId()))) {

                            ThinDescVersioned thinDesc = (ThinDescVersioned) getVodb().getDescription(
                                getNid(desc.getDescId()), getNid(desc.getConceptId()));
                            boolean changed = false;
                            for (UniversalAceDescriptionPart part : desc.getVersions()) {
                                if (part.getTime() == Long.MAX_VALUE) {
                                    ThinDescPart newPart = new ThinDescPart();
                                    newPart.setInitialCaseSignificant(part.getInitialCaseSignificant());
                                    newPart.setLang(part.getLang());
                                    newPart.setPathId(getNid(part.getPathId()));
                                    newPart.setStatusId(getNid(part.getStatusId()));
                                    newPart.setText(part.getText());
                                    newPart.setTypeId(getNid(part.getTypeId()));
                                    newPart.setVersion(ThinVersionHelper.convert(time));
                                    if (!thinDesc.getVersions().contains(newPart)) {
                                        changed = true;
                                        thinDesc.addVersion(newPart);
                                        values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                                    }
                                }
                            }
                            if (changed) {
                                getVodb().writeDescription(thinDesc);
                                AceLog.getEditLog().fine("Importing desc changes: \n" + thinDesc);
                            }

                        } else {
                            commitNewDescription(time, values, desc);
                            AceLog.getEditLog().warning(
                                "Committed description missing from database: \n\n" + desc
                                    + "\n\nAdding missing description and continuing.");
                        }
                    } catch (IOException e) {
                        AceLog.getAppLog().info("Description: " + desc.getDescId() + "\n      " + desc + "\n");
                        AceLog.getAppLog().info("Concept: " + desc.getConceptId());
                        AceLog.getAppLog().info("UniversalAceBean: " + bean);
                        AceLog.getAppLog().alertAndLog(Level.SEVERE, "Processing " + desc, e);
                        throw e;
                    }
                } catch (TerminologyException e) {
                    AceLog.getEditLog().alertAndLog(
                        Level.SEVERE,
                        "TerminologyException. Ignoring component, and continuing import." + desc + " "
                            + changeSetFile.getCanonicalPath(), e);
                }
            }
        }
    }

    private void commitUncommittedDescriptions(long time, UniversalAceBean bean, Set<TimePathId> values)
            throws DatabaseException, TerminologyException, IOException {
        for (UniversalAceDescription desc : bean.getUncommittedDescriptions()) {
            commitNewDescription(time, values, desc);
        }
    }

    private void commitNewDescription(long time, Set<TimePathId> values, UniversalAceDescription desc)
            throws TerminologyException, IOException {
        ThinDescVersioned thinDesc = new ThinDescVersioned(getNid(desc.getDescId()), getNid(desc.getConceptId()),
            desc.getVersions().size());
        for (UniversalAceDescriptionPart part : desc.getVersions()) {
            ThinDescPart newPart = new ThinDescPart();
            newPart.setInitialCaseSignificant(part.getInitialCaseSignificant());
            newPart.setLang(part.getLang());
            newPart.setPathId(getNid(part.getPathId()));
            newPart.setStatusId(getNid(part.getStatusId()));
            newPart.setText(part.getText());
            newPart.setTypeId(getNid(part.getTypeId()));
            newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
            if (part.getTime() == Long.MAX_VALUE) {
                newPart.setVersion(ThinVersionHelper.convert(time));
            } else {
                newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
            }
            values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
            thinDesc.addVersion(newPart);
        }
        try {
            ThinDescVersioned oldDescVersioned = (ThinDescVersioned) getVodb().getDescription(thinDesc.getDescId(),
                thinDesc.getConceptId());
            if (oldDescVersioned != null) {
                oldDescVersioned.merge(thinDesc);
                AceLog.getEditLog().fine(
                    "Merging desc with existing (should have been null): \n" + thinDesc + "\n\n" + oldDescVersioned);
            }
        } catch (IOException e) {
            // expected exception...
        }
        getVodb().writeDescription(thinDesc);
        AceLog.getEditLog().fine("Importing desc: \n" + thinDesc);
    }

    private void commitUncommittedIds(long time, UniversalAceBean bean, Set<TimePathId> values)
            throws DatabaseException, TerminologyException, IOException {
        for (UniversalAceIdentification id : bean.getUncommittedIds()) {
            AceLog.getEditLog().fine("commitUncommittedIds: " + id);
            ThinIdVersioned tid = null;
            for (UniversalAceIdentificationPart part : id.getVersions()) {
                I_Path path = getVodb().getPath(getVodb().uuidToNative(part.getPathId()));
                if (tid == null) {
                    try {
                        int nid = getNid(id.getUIDs());
                        AceLog.getEditLog().fine("Uncommitted id already exists: \n" + id);
                        tid = (ThinIdVersioned) getVodb().getId(nid);
                        AceLog.getEditLog().fine("found ThinIdVersioned: " + tid);
                    } catch (NoMappingException ex) {
                        /*
                         * Generate on the ARCHITECTONIC_BRANCH for now, it will
                         * get overwritten when the id is written to the
                         * database, with the proper branch and version values.
                         */
                        int nid = getVodb().uuidToNativeWithGeneration(id.getUIDs(), Integer.MAX_VALUE, path,
                            ThinVersionHelper.convert(time));
                        tid = new ThinIdVersioned(nid, id.getVersions().size());
                        AceLog.getEditLog().fine("created ThinIdVersioned: " + tid);
                    }
                }
                ThinIdPart newPart = new ThinIdPart();
                newPart.setStatusId(getNid(part.getIdStatus()));
                newPart.setPathId(getVodb().uuidToNative(part.getPathId()));
                newPart.setSource(getNid(part.getSource()));
                newPart.setSourceId(part.getSourceId());
                if (part.getTime() == Long.MAX_VALUE) {
                    newPart.setVersion(ThinVersionHelper.convert(time));
                } else {
                    newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
                }
                if (tid.getVersions().contains(newPart)) {
                    // already there
                } else {
                    values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
                    tid.addVersion(newPart);
                    AceLog.getEditLog().fine(" add version: " + newPart);
                }
            }
            /*
             * The ARCHITECTONIC_BRANCH will be overridden here with the proper
             * branch and version values here...
             */
            getVodb().writeId(tid);
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

    public VodbEnv getVodb() {
        if (vodb == null) {
            vodb = AceConfig.getVodb();
        }
        return vodb;
    }

    public void setVodb(VodbEnv vodb) {
        this.vodb = vodb;
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

}
