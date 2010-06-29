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
package org.dwfa.vodb.process;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.I_MapIds;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.je.DatabaseException;

public class ProcessAceFormatSourcesBerkeley extends ProcessAceFormatSources {
    /** Used to store the set of path int ids so that the paths can be create in the path store. */
    private HashSet<Integer> pathUuid = new HashSet<Integer>();

    public void flushIdBuffer() throws Exception {
        map.flushIdBuffer();
    }

    private static class MaxId {
        int max = Integer.MIN_VALUE;

        public synchronized void max(int x) {
            max = Math.max(max, x);
        }

        public synchronized int next() {
            max++;
            return max;
        }
    }

    private class MemoryIdMapper implements I_MapIds {

        ConcurrentHashMap<UUID, Integer> ids = new ConcurrentHashMap<UUID, Integer>();

        MaxId maxId = new MaxId();
        int encodingSource = PrimordialId.ACE_AUX_ENCODING_ID.getNativeId(Integer.MIN_VALUE);
        int encodingPathId = PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE);
        int currentStatusId = PrimordialId.CURRENT_ID.getNativeId(Integer.MIN_VALUE);

        private MemoryIdMapper() {
            super();
            for (PrimordialId pid : PrimordialId.values()) {
                pid.getUids();
                int nid = pid.getNativeId(Integer.MIN_VALUE);
                maxId.max(nid);
                for (UUID uuid : pid.getUids()) {
                    ids.put(uuid, nid);
                }
            }
        }

        public int getIntId(Collection<UUID> uids, I_Path idPath, int version) throws Exception {
            synchronized (ids) {
                Integer nid = null;
                boolean unmapped = false;
                for (UUID uuid : uids) {
                    if (ids.containsKey(uuid)) {
                        nid = ids.get(uuid);
                    } else {
                        unmapped = true;
                    }
                }
                if (unmapped) {
                    if (nid == null) {
                        nid = maxId.next();
                    }
                    for (UUID uuid : uids) {
                         ids.put(uuid, nid);
                    }
                }
                return nid;
            }
        }

         public int getIntId(UUID uid, I_Path idPath, int version) throws Exception {
             synchronized (ids) {
                if (ids.containsKey(uid)) {
                    return ids.get(uid);
                } else {
                    int nid = maxId.next();
                    ids.put(uid, nid);
                     return nid;
                }
             }
        }

        public void flushIdBuffer() throws Exception {
            synchronized (ids) {
                getLog().info("ID Count: " + ids.size());
                int count = 0;
                for (Entry<UUID, Integer> entry : ids.entrySet()) {
                    I_IdVersioned idv = vodb.getIdNullOk(entry.getValue());
                    if (idv == null) {
                        idv = new ThinIdVersioned(entry.getValue(), 1);
                        addUuidPart(entry.getKey(), idv);
                    }
                    vodb.writeId(idv);
                    count++;
                    if (count % 100000 == 0) {
                        getLog().info("processed " + count + " identifiers. ");
                    }
                }

                map = new BerkeleyIdMapper();
                this.ids.clear();
                getLog().info("Converted to Berkeley-based id mapper");
            }
        }

        private void addUuidPart(UUID uuid, I_IdVersioned idv)
                throws TerminologyException, IOException {
            ThinIdPart idPart = new ThinIdPart();
            idPart.setStatusId(currentStatusId);
            idPart.setPathId(encodingPathId);
            idPart.setSource(encodingSource);
            idPart.setSourceId(uuid);
            idPart.setVersion(Integer.MIN_VALUE);
            idv.addVersion(idPart);
        }

    }

    private class BerkeleyIdMapper implements I_MapIds {
        private WeakHashMap<UUID, Integer> ids = new WeakHashMap<UUID, Integer>();

        private WeakHashMap<Collection<UUID>, Integer> idsFromCollection = new WeakHashMap<Collection<UUID>, Integer>();

        int encodingSource = PrimordialId.ACE_AUX_ENCODING_ID.getNativeId(Integer.MIN_VALUE);

        public BerkeleyIdMapper() throws DatabaseException {
            super();
            vodb.logIdDbStats();
        }

        public int getIntId(Collection<UUID> uids, I_Path idPath, int version) throws Exception {
            synchronized (ids) {
                Integer nid = idsFromCollection.get(uids);
                if (nid != null) {
                    return nid;
                }
                if (vodb.hasId(uids)) {
                    nid = vodb.getId(uids).getNativeId();
                    idsFromCollection.put(uids, nid);
                    return nid;
                }

                Iterator<UUID> idsItr = uids.iterator();
                UUID firstId = idsItr.next();

                int newId = vodb.uuidToNativeWithGeneration(firstId, encodingSource, idPath, version);
                idsFromCollection.put(uids, newId);

                I_IdVersioned idv = new ThinIdVersioned(newId, 1);
                addUuidPart(idPath, version, firstId, idv);
                while (idsItr.hasNext()) {
                    addUuidPart(idPath, version, idsItr.next(), idv);
                }

                vodb.writeId(idv);
                return newId;
            }
        }

        public int getIntId(UUID uid, I_Path idPath, int version) throws Exception {
            synchronized (ids) {
                Integer nid = ids.get(uid);
                if (nid != null) {
                    return nid;
                }
                if (vodb.hasId(uid)) {
                    nid = vodb.getId(uid).getNativeId();
                    ids.put(uid, nid);
                    return nid;
                }

                int newId = vodb.uuidToNativeWithGeneration(uid, encodingSource, idPath, version);
                ids.put(uid, newId);

                I_IdVersioned idv = new ThinIdVersioned(newId, 1);

                addUuidPart(idPath, version, uid, idv);
                vodb.writeId(idv);

                return newId;
            }
        }

        private void addUuidPart(I_Path idPath, int version, UUID firstId, I_IdVersioned idv)
                throws TerminologyException, IOException {
            ThinIdPart idPart = new ThinIdPart();
            idPart.setStatusId(vodb.uuidToNativeWithGeneration(ArchitectonicAuxiliary.Concept.CURRENT.getUids(),
                                                               encodingSource, idPath, version));
            idPart.setPathId(vodb.uuidToNativeWithGeneration(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
                    .getUids(), encodingSource, idPath, version));
            idPart.setSource(encodingSource);
            idPart.setSourceId(firstId);
            idPart.setVersion(version);
            idv.addVersion(idPart);
        }

        public void flushIdBuffer() throws Exception {
            // Nothing to do...
        }
    }

    private VodbEnv vodb;

    protected static I_MapIds map;

    protected static I_Path aceAuxPath;

    public ProcessAceFormatSourcesBerkeley(VodbEnv vodb) throws DatabaseException, IOException {
        super();
        // map = new BerkeleyIdMapper();
        map = new MemoryIdMapper();
        this.vodb = vodb;
        Class<?> nativeIdClass = this.vodb.getNativeIdClass();
        if (nativeIdClass.equals(Integer.class)) {
            for (PrimordialId primId : PrimordialId.values()) {
                for (UUID uid : primId.getUids()) {
                    I_IdVersioned thinId = new ThinIdVersioned(primId.getNativeId(Integer.MIN_VALUE), 1);
                    ThinIdPart idPart = new ThinIdPart();
                    idPart.setStatusId(PrimordialId.CURRENT_ID.getNativeId(Integer.MIN_VALUE));
                    idPart.setPathId(PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE));
                    idPart.setSource(PrimordialId.ACE_AUX_ENCODING_ID.getNativeId(Integer.MIN_VALUE));
                    idPart.setSourceId(uid);
                    idPart.setVersion(Integer.MIN_VALUE);
                    thinId.addVersion(idPart);
                    vodb.writeId(thinId);
                }
            }
        } else {
            throw new UnsupportedOperationException("Long native id type is not currently supported. ");
        }
        aceAuxPath = new Path(PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE), new ArrayList<I_Position>());
    }

    @Override
    public Logger getLog() {
        return vodb.getLogger();
    }

    @Override
    public void cleanupSNOMED(I_IntSet relsToIgnore) throws Exception {
        //nothing to clean up since using ace formats.
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeConcept(CountDownLatch latch, Date releaseDate, Object conceptKey, Object conceptStatus, boolean defChar, Object pathId)
            throws Exception {
        int version = ThinVersionHelper.convert(releaseDate.getTime());
        ThinConPart con = new ThinConPart();
        con.setPathId(map.getIntId((Collection<UUID>) pathId, aceAuxPath, version));
        con.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
        con.setStatusId(map.getIntId((UUID) conceptStatus, aceAuxPath, version));
        con.setDefined(defChar);
        int conceptId = map.getIntId((UUID) conceptKey, aceAuxPath, version);
        I_ConceptAttributeVersioned vcon = null;
        synchronized (vodb) {
            if (vodb.hasConcept(conceptId)) {
                vcon = vodb.getConceptAttributes(conceptId);
            }
            if (vcon == null) {
                vcon = new ThinConVersioned(map.getIntId((UUID) conceptKey, aceAuxPath, version), 1);
            }
            if (vcon.addVersion(con)) {
                vodb.writeConceptAttributes(vcon);
            }
        }

        latch.countDown();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeDescription(CountDownLatch latch, Date releaseDate, Object descriptionId, Object status, Object conceptId, String text,
        boolean capStatus, Object typeInt, String lang, Object pathID) throws Exception {
        int version = ThinVersionHelper.convert(releaseDate.getTime());
        ThinDescPart desc = new ThinDescPart();
        desc.setPathId(map.getIntId((Collection<UUID>) pathID, aceAuxPath, version));
        desc.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
        desc.setStatusId(map.getIntId((UUID) status, aceAuxPath, ThinVersionHelper.convert(releaseDate.getTime())));
        desc.setInitialCaseSignificant(capStatus);
        desc.setLang(lang);
        desc.setText(text);
        desc.setTypeId(map.getIntId((UUID) typeInt, aceAuxPath, ThinVersionHelper.convert(releaseDate.getTime())));

        int descId = map.getIntId((UUID) descriptionId, aceAuxPath, version);
        int concId = map.getIntId((UUID) conceptId, aceAuxPath, version);

        I_DescriptionVersioned vdesc;
        synchronized (vodb) {
            if (vodb.hasDescription(descId, concId)) {
                vdesc = vodb.getDescription(descId, concId);
            } else {
                vdesc = new ThinDescVersioned(map.getIntId((UUID) descriptionId, aceAuxPath, version), map
                        .getIntId((UUID) conceptId, aceAuxPath, ThinVersionHelper.convert(releaseDate.getTime())), 1);
            }
            if (vdesc.addVersion(desc)) {
                vodb.writeDescriptionNoLuceneUpdate(vdesc);
            }
        }

        latch.countDown();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeRelationship(CountDownLatch latch, Date releaseDate, Object relID, Object statusId, Object conceptOneID,
        Object relationshipTypeConceptID, Object conceptTwoID, Object characteristic, Object refinability, int group,
        Object pathId) throws Exception {
        int version = ThinVersionHelper.convert(releaseDate.getTime());
        ThinRelPart part = new ThinRelPart();
        int c1id = map.getIntId((UUID) conceptOneID, aceAuxPath, version);
        int c2id = map.getIntId((UUID) conceptTwoID, aceAuxPath, version);
        if (c1id == c2id) {
            // log for now, throw exception later
            AceLog.getEditLog().log(
                                    Level.SEVERE,
                                    "*RECURSION* Rel points a concept to itself: " + relID + " c one id: "
                                            + conceptOneID + " c two id: " + conceptTwoID);
            throw new Exception("*RECURSION* Rel points a concept to itself: " + relID + " c one id: " + conceptOneID
                    + " c two id: " + conceptTwoID);

        }
        part.setPathId(map.getIntId((Collection<UUID>) pathId, aceAuxPath, version));
        part.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
        part.setStatusId(map.getIntId((Collection<UUID>) statusId, aceAuxPath, version));
        part.setCharacteristicId(map.getIntId((UUID) characteristic, aceAuxPath, version));
        part.setGroup(group);
        part.setRefinabilityId(map.getIntId((UUID) refinability, aceAuxPath, version));
        part.setTypeId(map.getIntId((UUID) relationshipTypeConceptID, aceAuxPath, version));

        int relId = map.getIntId((UUID) relID, aceAuxPath, version);
        I_RelVersioned vrel;
        synchronized (vodb) {
            if (vodb.hasRel(relId, c1id)) {
                vrel = vodb.getRel(relId, c1id);
                if ((vrel.getC1Id() != c1id) || (vrel.getC2Id() != c2id)) {
                    I_GetConceptData c1 = ConceptBean.get(c1id);
                    I_GetConceptData c2 = ConceptBean.get(c2id);
                    I_GetConceptData c3 = ConceptBean.get(vrel.getC2Id());

                    AceLog.getEditLog().log(Level.SEVERE,
                            "Duplicate rels with different c1 and c2:\n relId: " + relID + "\n c1: " + c1 + "\n c2: " + c2 + "\n c3: " + c3);

                    ThinRelPart retiredPart = part.duplicate();
                    retiredPart.setStatusId(map.getIntId(RETIRED_UUID, aceAuxPath, version));
                    if (vrel.addVersionNoRedundancyCheck(retiredPart)) {
                        vodb.writeRel(vrel);
                    }

                    vrel = new ThinRelVersioned(map.getIntId((UUID) relID, aceAuxPath, version),
                            map.getIntId((UUID) conceptOneID, aceAuxPath, version),
                            map.getIntId((UUID) conceptTwoID, aceAuxPath,version), 1);
                }
            } else {
                vrel = new ThinRelVersioned(map.getIntId((UUID) relID, aceAuxPath, version), map
                        .getIntId((UUID) conceptOneID, aceAuxPath, version), map.getIntId((UUID) conceptTwoID, aceAuxPath,
                                                                                          version), 1);
            }
            if (vrel.addVersionNoRedundancyCheck(part)) {
                vodb.writeRel(vrel);
            }
        }

        latch.countDown();
    }

    @Override
    public void writeId(UUID primaryUuid, UUID sourceSystemUuid, Object sourceId, UUID statusUuid, Date statusDate,
        UUID pathUuid) throws Exception {
        map.getIntId(Arrays.asList(new UUID[] { primaryUuid }), aceAuxPath, ThinVersionHelper.convert(statusDate
                .getTime()));

        ThinIdVersioned idv = ((VodbEnv) LocalVersionedTerminology.get()).getId(primaryUuid);
        ThinIdPart idPart = new ThinIdPart();
        idPart.setStatusId(vodb.uuidToNative(statusUuid));
        idPart.setPathId(vodb.uuidToNative(pathUuid));
        idPart.setSource(vodb.uuidToNative(sourceSystemUuid));
        idPart.setSourceId(sourceId);
        idPart.setVersion(ThinVersionHelper.convert(statusDate.getTime()));
        if (idv.getVersions().contains(idPart) == false) {
            idv.addVersion(idPart);
            vodb.writeId(idv);
        }

    }

    @Override public void execute(final File snomedDir) throws Exception {
        throw new UnsupportedOperationException("This functionality has not been implemented for Ace format files.");
    }

    /**
     * Write the new paths.
     *
     * @see org.dwfa.vodb.process.ProcessAceFormatSources#writeNewPaths()
     */
    public void writeNewPaths() throws Exception {
        for (UUID pathUuid : getPathUuids()) {
            Path path = new Path(map.getIntId(pathUuid, aceAuxPath, 0), null);
            path.commit(0, null);
        }
    }

    protected void readBooleanMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception {

        st.nextToken();
        boolean booleanValue = st.sval.toLowerCase().startsWith("t");
        int version = ThinVersionHelper.convert(statusDate.getTime());
        int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);

        // Now done with reading file, and getting member id. Could return
        // control back to caller here.

        ProcessMemberTaskBoolean.acquire(refsetUuid, statusUuid, componentUuid, pathUuid, version, memberId,
                                         booleanValue);
    }

    @Override
    protected void readConIntMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception {
        st.nextToken();
        UUID conceptUuid = (UUID) getId(st);
        st.nextToken();
        int intValue = Integer.parseInt(st.sval);
        int version = ThinVersionHelper.convert(statusDate.getTime());
        int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);

        // Now done with reading file, and getting member id. Could return
        // control back to caller here.

        ProcessMemberTaskConceptInt.acquire(refsetUuid, statusUuid, componentUuid, pathUuid, version, memberId,
                                            conceptUuid, intValue);

    }

    @Override
    protected void readConceptMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception {

        ProcessMemberTaskConcept.check();

        st.nextToken();
        UUID conceptUuid = (UUID) getId(st);
        int version = ThinVersionHelper.convert(statusDate.getTime());
        int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);

        // Now done with reading file, and getting member id. Could return
        // control back to caller here after launching new thread.

        ProcessMemberTaskConcept.acquire(refsetUuid, statusUuid, componentUuid, pathUuid, version, memberId,
                                         conceptUuid);

    }

    @Override
    protected void readMeasurementMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception {
        st.nextToken();
        double doubleVal = Double.parseDouble(st.sval);
        st.nextToken();
        UUID unitsOfMeasureUuid = (UUID) getId(st);

        int version = ThinVersionHelper.convert(statusDate.getTime());
        int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);

        // Now done with reading file, and getting member id. Could return
        // control back to caller here.

        ProcessMemberTaskMeasurement.acquire(refsetUuid, statusUuid, componentUuid, pathUuid, version, memberId,
                                             doubleVal, unitsOfMeasureUuid);

    }

    @Override
    protected void readIntegerMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception {
        st.nextToken();
        try {
            int intValue = Integer.parseInt(st.sval);
            int version = ThinVersionHelper.convert(statusDate.getTime());
            int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);

            // Now done with reading file, and getting member id. Could return
            // control back to caller here.

            ProcessMemberTaskInteger.acquire(refsetUuid, statusUuid, componentUuid, pathUuid, version, memberId,
                intValue);
        } catch (NumberFormatException ex) {
            AceLog.getAppLog().info(ex.toString() + " in file: " + readInfo);
        }

    }

    @Override
    protected void readLanguageMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception {
        st.nextToken();
        UUID acceptabilityUuid = (UUID) getId(st);

        st.nextToken();
        UUID correctnessUuid = (UUID) getId(st);

        st.nextToken();
        UUID synonymyUuid = (UUID) getId(st);

        int version = ThinVersionHelper.convert(statusDate.getTime());
        int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);

        // Now done with reading file, and getting member id. Could return
        // control back to caller here.

        ProcessMemberTaskLanguage.acquire(refsetUuid, statusUuid, componentUuid, pathUuid, version, memberId,
                                          acceptabilityUuid, correctnessUuid, synonymyUuid);
    }

    @Override
    protected void readStringMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception {
        st.nextToken();
        String strExt = st.sval;
        int version = ThinVersionHelper.convert(statusDate.getTime());
        int memberId = map.getIntId((UUID) memberUuid, aceAuxPath, version);

        // Now done with reading file, and getting member id. Could return
        // control back to caller here.

        ProcessMemberTaskString.acquire(refsetUuid, statusUuid, componentUuid, pathUuid, version, memberId, strExt);
    }

    @Override
    protected void finishRefsetRead(REFSET_FILE_TYPES refsetType, File refsetFile, CountDownLatch refsetLatch) throws Exception {
        switch (refsetType) {
        case BOOLEAN:
            ProcessMemberTaskBoolean.check();
            getLog().info("Waiting for refset latch for boolean refset: " + refsetLatch.getCount());
            refsetLatch.await();
            ProcessMemberTaskBoolean.check();
            break;
        case CONCEPT:
            ProcessMemberTaskConcept.check();
            getLog().info("Waiting for refset latch for concept refset: " + refsetLatch.getCount());
            refsetLatch.await();
            ProcessMemberTaskConcept.check();
            break;
        case CONINT:
            ProcessMemberTaskConceptInt.check();
            getLog().info("Waiting for refset latch for con-int refset: " + refsetLatch.getCount());
            refsetLatch.await();
            ProcessMemberTaskConceptInt.check();
            break;
        case INTEGER:
            ProcessMemberTaskInteger.check();
            getLog().info("Waiting for refset latch for integer refset: " + refsetLatch.getCount());
            refsetLatch.await();
            ProcessMemberTaskInteger.check();
            break;
        case LANGUAGE:
            ProcessMemberTaskLanguage.check();
            getLog().info("Waiting for refset latch for language refset: " + refsetLatch.getCount());
            refsetLatch.await();
            ProcessMemberTaskLanguage.check();
            break;
        case MEASUREMENT:
            ProcessMemberTaskMeasurement.check();
            getLog().info("Waiting for refset latch for measurement refset: " + refsetLatch.getCount());
            refsetLatch.await();
            ProcessMemberTaskMeasurement.check();
            break;
        case STRING:
            ProcessMemberTaskString.check();
            getLog().info("Waiting for refset latch for string refset: " + refsetLatch.getCount());
            refsetLatch.await();
            ProcessMemberTaskString.check();
            break;
        default:
            throw new IOException("Can't handle refset type: " + refsetType);
        }
    }

    @Override
    protected void startRefsetRead(REFSET_FILE_TYPES refsetType, File refsetfile) throws IOException {
        switch (refsetType) {
        case BOOLEAN:
            break;
        case CONCEPT:
            break;
        case CONINT:
            break;
        case INTEGER:
            break;
        case LANGUAGE:
            break;
        case MEASUREMENT:
            break;
        case STRING:
            break;
        default:
            throw new IOException("Can't handle refset type: " + refsetType);
        }
    }

}
