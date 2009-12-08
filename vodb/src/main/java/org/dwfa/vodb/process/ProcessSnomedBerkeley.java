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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.activity.UpperInfoOnlyConsoleMonitor;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;
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

/**
 * Goal which touches a timestamp file.
 * 
 * @goal snomed-berkley
 * 
 * @phase generate-resources
 */
public class ProcessSnomedBerkeley extends ProcessSnomed {

    /*
     * DESCRIPTIONID DESCRIPTIONSTATUS CONCEPTID TERM INITIALCAPITALSTATUS
     * DESCRIPTIONTYPE LANGUAGECODE
     * 220309016 0 138875005 SNOMED CT Concept 1 1 en
     */
    // public static final long SNOMED_ROOT_CONCEPTID = 138875005L;
    // public static final long SNOMED_ROOT_DESCID = 220309016L;
    // public static final long SNOMED_ISA_REL = 116680003L;

    Map<Long, Integer> idMap = new HashMap<Long, Integer>();

    // Add a hash map
    public synchronized int getIntId(long snomedId, Date version) throws Exception {
        if (idMap.containsKey(snomedId)) {
            return idMap.get(snomedId);
        }
        int thinVers = ThinVersionHelper.convert(version.getTime());
        UUID snomedUid = Type3UuidFactory.fromSNOMED(snomedId);
        // We have not encountered the SNOMED ID yet, so generate a new one.

        // See if the UUID generated a duplicate...
        // Check to be sure the UUID generated from nameUUID is unique.
        I_IdVersioned dup = vodb.getId(snomedUid);
        if (dup != null) {
            boolean error = true;
            if (error) {
                StringBuffer buf = new StringBuffer();
                buf.append("Severe error: ");
                buf.append(snomedId);
                buf.append(" generates a duplicate type 3 UUID. ");
                buf.append(dup.toString());
                throw new Exception(buf.toString());
            }
        }
        // No duplicate, so generate id...

        int newId = vodb.uuidToNativeWithGeneration(snomedUid, snomedType3UuidSource, snomedPath, thinVers);
        idMap.put(snomedId, newId);
        I_IdVersioned idv = new ThinIdVersioned(newId, 2);
        // add
        ThinIdPart idPart = new ThinIdPart();
        idPart.setStatusId(currentId);
        idPart.setPathId(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()));
        idPart.setSource(snomedType3UuidSource);
        idPart.setSourceId(snomedUid);
        idPart.setVersion(thinVers);
        idv.addVersion(idPart);

        idPart = new ThinIdPart();
        idPart.setStatusId(currentId);
        idPart.setPathId(snomedPath.getConceptId());
        idPart.setSource(snomedIntIdSource);
        idPart.setSourceId(new Long(snomedId));
        idPart.setVersion(thinVers);
        idv.addVersion(idPart);
        vodb.writeId(idv);
        return newId;
    }

    private VodbEnv vodb;
    // private Map<UUID, Integer> uuidToIntMap;
    private Stopwatch timer;
    private I_ShowActivity monitor;
    private I_Path snomedPath;
    private int snomedType3UuidSource;
    private int snomedIntIdSource;
    private int currentId;

    public ProcessSnomedBerkeley(VodbEnv vodb, int constantDate) throws DatabaseException, TerminologyException,
            IOException {
        super(constantDate);
        this.vodb = vodb;
        // this.uuidToIntMap = constantToIntMap;
        this.timer = new Stopwatch();
        this.timer.start();
        monitor = new UpperInfoOnlyConsoleMonitor();
        snomedPath = new Path(getSnomedCorePathId(), new ArrayList<I_Position>());
        snomedType3UuidSource = vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_T3_UUID.getUids());
        snomedIntIdSource = vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
        currentId = vodb.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
        uuidToNative = vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids());
    }

    public void cleanupSNOMED(I_IntSet relsToIgnore) throws Exception {
        // Update the history records for the relationships...
        printElapsedTime();
        vodb.cleanupSNOMED(relsToIgnore, getReleaseDates());
        monitor.setProgressInfoUpper("Starting populate positions.");
        vodb.populatePositions();
        printElapsedTime();
        monitor.setProgressInfoUpper("Starting makeLuceneIndex().");
        vodb.createLuceneDescriptionIndex();
        printElapsedTime();
        monitor.setProgressInfoUpper("Starting cleanup.");
        vodb.close();
        monitor.setProgressInfoUpper("Close complete.");
        printElapsedTime();

    }

    private void printElapsedTime() {
        Date end = new Date();
        long elapsed = timer.getElapsedTime();
        elapsed = elapsed / 1000;
        AceLog.getAppLog().info("Elapsed sec: " + elapsed);
        elapsed = elapsed / 60;
        AceLog.getAppLog().info("Elapsed min: " + elapsed);
        AceLog.getAppLog().info(end.toString());
    }

    public void writeConcept(CountDownLatch latch, Date releaseDate, Object conceptKey, Object conceptStatus,
            boolean defChar, Object ignoredPath) throws Exception {
        ThinConPart con = new ThinConPart();
        con.setPathId(uuidToNative);
        con.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));

        con.setStatusId(getNativeStatus((Integer) conceptStatus));
        con.setDefined(defChar);

        int conId = getIntId((Long) conceptKey, releaseDate);

        I_ConceptAttributeVersioned vcon = null;
        synchronized (vodb) {
            if (vodb.hasConcept(conId)) {
                vcon = vodb.getConceptAttributes(conId);
            }
            if (vcon == null) {
                vcon = new ThinConVersioned(getIntId((Long) conceptKey, releaseDate), 1);
            }
            if (vcon.addVersion(con)) {
                vodb.writeConceptAttributes(vcon);
            }
        }
        latch.countDown();
    }

    private int getNativeStatus(Integer conceptStatus) throws IOException, TerminologyException {
        I_ConceptualizeUniversally status = ArchitectonicAuxiliary.getStatusFromId((Integer) conceptStatus);
        int statusNativeId = vodb.uuidToNative(status.getUids());
        return statusNativeId;
    }

    private int getNativeDescType(Integer typeId) throws IOException, TerminologyException {
        I_ConceptualizeUniversally type = ArchitectonicAuxiliary.getSnomedDescriptionType((Integer) typeId);
        int typeNativeId = vodb.uuidToNative(type.getUids());
        return typeNativeId;
    }

    private int getNativeCharacteristicType(Integer characteristicId) throws IOException, TerminologyException {
        I_ConceptualizeUniversally characteristic = ArchitectonicAuxiliary.getSnomedCharacteristicType((Integer) characteristicId);
        int characteristicNativeId;
        try {
            characteristicNativeId = vodb.uuidToNative(characteristic.getUids());
            return characteristicNativeId;
        } catch (NoMappingException e) {
            AceLog.getAppLog().severe("Can't find characteristic: " + characteristic);
            throw e;
        }
    }

    private int getNativeRefinability(Integer refinabilityId) throws IOException, TerminologyException {
        I_ConceptualizeUniversally refinability = ArchitectonicAuxiliary.getSnomedRefinabilityType((Integer) refinabilityId);
        int refinabilityNativeId = vodb.uuidToNative(refinability.getUids());
        return refinabilityNativeId;
    }

    private Integer snomedCorePathId;
    private int uuidToNative;

    private int getSnomedCorePathId() throws TerminologyException, IOException {
        if (snomedCorePathId == null) {
            snomedCorePathId = vodb.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids());
        }
        return snomedCorePathId;
    }

    public void writeDescription(CountDownLatch latch, Date releaseDate, Object descriptionId, Object status,
            Object conceptId, String text, boolean capStatus, Object typeInt, String lang, Object ignoredPath)
            throws Exception {

        text = new String(text.getBytes(), "UTF-8");
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            if (text.getBytes()[0] < 0) {
                AceLog.getAppLog().info("********\n" + text + "\n length: " + text.length());
                StringBuffer buff = new StringBuffer();
                for (byte b : text.getBytes()) {
                    buff.append((int) b);
                    buff.append(' ');
                }
                AceLog.getAppLog().fine("\n\nUTF 8:");
                for (byte b : text.getBytes("UTF-8")) {
                    buff.append((int) b);
                    buff.append(' ');
                }
                AceLog.getAppLog().fine("\n\nUTF 16:");
                for (char c : text.toCharArray()) {
                    buff.append(c);
                    buff.append(' ');
                }
                AceLog.getAppLog().fine(buff.toString());
            }
        }
        ThinDescPart desc = new ThinDescPart();
        desc.setPathId(getSnomedCorePathId());
        desc.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
        desc.setStatusId(getNativeStatus((Integer) status));
        desc.setInitialCaseSignificant(capStatus);
        desc.setLang(lang);
        desc.setText(text);
        desc.setTypeId(getNativeDescType((Integer) typeInt));

        int descId = getIntId((Long) descriptionId, releaseDate);
        int concId = getIntId((Long) conceptId, releaseDate);
        I_DescriptionVersioned vdesc;
        synchronized (vodb) {
            if (vodb.hasDescription(descId, concId)) {
                vdesc = vodb.getDescription(descId, concId);
            } else {
                vdesc = new ThinDescVersioned(descId, concId, 1);
            }
            if (vdesc.addVersion(desc)) {
                vodb.writeDescriptionNoLuceneUpdate(vdesc);
            }
        }

        latch.countDown();
    }

    public void writeRelationship(CountDownLatch latch, Date releaseDate, Object relID, Object statusIdIgnored,
            Object conceptOneID, Object relationshipTypeConceptID, Object conceptTwoID, Object characteristic,
            Object refinability, int group, Object ignoredPath) throws Exception {
        ThinRelPart rel = new ThinRelPart();
        rel.setPathId(getSnomedCorePathId());
        rel.setVersion(ThinVersionHelper.convert(releaseDate.getTime()));
        rel.setStatusId(getNativeStatus(0));
        rel.setCharacteristicId(getNativeCharacteristicType((Integer) characteristic));
        rel.setGroup(group);
        rel.setRefinabilityId(getNativeRefinability((Integer) refinability));
        rel.setTypeId(getIntId((Long) relationshipTypeConceptID, releaseDate));

        int relId = getIntId((Long) relID, releaseDate);
        int c1id = getIntId((Long) conceptOneID, releaseDate);
        I_RelVersioned vrel;
        synchronized (vodb) {
            if (vodb.hasRel(relId, c1id)) {
                vrel = vodb.getRel(relId, c1id);
            } else {
                vrel = new ThinRelVersioned(getIntId((Long) relID, releaseDate), c1id, getIntId((Long) conceptTwoID,
                    releaseDate), 1);
            }
            if (vrel.addVersionNoRedundancyCheck(rel)) {
                vodb.writeRel(vrel);
            }
        }

        latch.countDown();

    }

    @Override
    public Logger getLog() {
        return vodb.getLogger();
    }

    @Override
    public void iterateRelationships(MakeRelSet oldRelItr) throws Exception {
        vodb.iterateRelationships(oldRelItr);
    }

    @Override
    public void writeId(UUID primaryUuid, UUID sourceSystemUuid, Object sourceId, UUID statusUuid, Date statusDate,
            UUID pathUuid) {
        throw new UnsupportedOperationException();
    }

}
