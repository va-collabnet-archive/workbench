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
package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.TupleAdder;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartBoolean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartInteger;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartLanguage;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartMeasurement;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartScopedLanguage;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.tapi.TerminologyRuntimeException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;

/**
 * @todo add version to vodb -> added as getProperty...
 * @todo add imported change set info to vodb, need to set theProperty...
 *
 * @todo have change sets automatically increment as size increases over a
 *       certain size. Added increment to change set file name format.
 * @todo add extension ability
 *
 * @author kec
 *
 */
public class ThinExtByRefVersioned implements I_ThinExtByRefVersioned {

    private int refsetId;

    private int memberId;

    private int componentId;

    private int typeId; // Use an enumeration when reading/writing, and convert
    // it to the corresponding concept nid...

    private List<I_ThinExtByRefPart> versions;

    public ThinExtByRefVersioned(int refsetId, int memberId, int componentId, int typeId) {
        this(refsetId, memberId, componentId, typeId, 1);
    }

    public ThinExtByRefVersioned(int refsetId, int memberId, int componentId, int typeId, int partCount) {
        super();
        this.refsetId = refsetId;
        this.memberId = memberId;
        this.componentId = componentId;
        this.typeId = typeId;
        this.versions = new ArrayList<I_ThinExtByRefPart>(partCount);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getMemberId()
     */
    public int getMemberId() {
        return memberId;
    }

    public int getTermComponentId() {
        return memberId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getComponentId()
     */
    public int getComponentId() {
        return componentId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getTypeId()
     */
    public int getTypeId() {
        return typeId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getVersions()
     */
    public List<? extends I_ThinExtByRefPart> getVersions() {
        return versions;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getRefsetId()
     */
    public int getRefsetId() {
        return refsetId;
    }

    @Override
    public boolean equals(Object obj) {
        ThinExtByRefVersioned another = (ThinExtByRefVersioned) obj;
        return ((refsetId == another.refsetId) && (memberId == another.memberId)
            && (componentId == another.componentId) && (typeId == another.typeId) && (versions.equals(another.versions)));
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { refsetId, memberId, componentId, typeId });
    }

    @Override
    public String toString() {
        try {
            StringBuffer buff = new StringBuffer();
            buff.append(this.getClass().getSimpleName());
            buff.append(" refset: ");
            buff.append(LocalVersionedTerminology.get().getConcept(refsetId).toString());
            buff.append(" memberId: ");
            buff.append(memberId);
            if (LocalVersionedTerminology.get().hasConcept(componentId)) {
                buff.append(" component: ");
                buff.append(LocalVersionedTerminology.get().getConcept(componentId).toString());
            } else {
                buff.append(" componentId: ");
                buff.append(componentId);
            }
            buff.append(" type: ");
            buff.append(LocalVersionedTerminology.get().getConcept(typeId).toString());
            buff.append(" versions: ");
            synchronized (versions) {
                buff.append(versions);
            }
            return buff.toString();
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return "ThinExtByRefVersioned refsetId: " + refsetId + " memberId: " + memberId + " componentId: "
            + componentId + " typeId: " + typeId + " versions: " + versions;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefVersioned#addVersion(org.dwfa.vodb.
     * types.ThinExtByRefPart)
     */
    public void addVersion(I_ThinExtByRefPart part) {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine(
                "Adding part: " + part + " to member: " + memberId + " for component: " + componentId);
        }
        versions.add(part);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#setRefsetId(int)
     */
    public void setRefsetId(int refsetId) {
        this.refsetId = refsetId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#setTypeId(int)
     */
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static ThinExtByRefPart makePart(UniversalAceExtByRefPart part) throws TerminologyException, IOException {

        VodbEnv vodb = AceConfig.getVodb();

        if (UniversalAceExtByRefPartBoolean.class.equals(part.getClass())) {
            ThinExtByRefPartBoolean thinPart = new ThinExtByRefPartBoolean();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartBoolean booleanPart = (UniversalAceExtByRefPartBoolean) part;
            thinPart.setValue(booleanPart.getBooleanValue());
            return thinPart;

        } else if (UniversalAceExtByRefPartConcept.class.equals(part.getClass())) {
            ThinExtByRefPartConcept thinPart = new ThinExtByRefPartConcept();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartConcept conceptPart = (UniversalAceExtByRefPartConcept) part;
            thinPart.setConceptId(vodb.uuidToNative(conceptPart.getConceptUid()));
            return thinPart;

        } else if (UniversalAceExtByRefPartInteger.class.equals(part.getClass())) {
            ThinExtByRefPartInteger thinPart = new ThinExtByRefPartInteger();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartInteger integerPart = (UniversalAceExtByRefPartInteger) part;
            thinPart.setIntValue(integerPart.getIntValue());
            return thinPart;

        } else if (UniversalAceExtByRefPartLanguage.class.equals(part.getClass())) {
            ThinExtByRefPartLanguage thinPart = new ThinExtByRefPartLanguage();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartLanguage languagePart = (UniversalAceExtByRefPartLanguage) part;
            thinPart.setAcceptabilityId(vodb.uuidToNative(languagePart.getAcceptabilityUids()));
            thinPart.setCorrectnessId(vodb.uuidToNative(languagePart.getCorrectnessUids()));
            thinPart.setDegreeOfSynonymyId(vodb.uuidToNative(languagePart.getDegreeOfSynonymyUids()));
            return thinPart;

        } else if (UniversalAceExtByRefPartMeasurement.class.equals(part.getClass())) {
            ThinExtByRefPartMeasurement thinPart = new ThinExtByRefPartMeasurement();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartMeasurement measurementPart = (UniversalAceExtByRefPartMeasurement) part;
            thinPart.setMeasurementValue(measurementPart.getMeasurementValue());
            thinPart.setUnitsOfMeasureId(vodb.uuidToNative(measurementPart.getUnitsOfMeasureUids()));
            return thinPart;

        } else if (UniversalAceExtByRefPartScopedLanguage.class.equals(part.getClass())) {
            ThinExtByRefPartLanguageScoped thinPart = new ThinExtByRefPartLanguageScoped();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartScopedLanguage scopedLanguagePart = (UniversalAceExtByRefPartScopedLanguage) part;
            thinPart.setAcceptabilityId(vodb.uuidToNative(scopedLanguagePart.getAcceptabilityUids()));
            thinPart.setCorrectnessId(vodb.uuidToNative(scopedLanguagePart.getCorrectnessUids()));
            thinPart.setDegreeOfSynonymyId(vodb.uuidToNative(scopedLanguagePart.getDegreeOfSynonymyUids()));

            thinPart.setPriority(scopedLanguagePart.getPriority());
            thinPart.setScopeId(vodb.uuidToNative(scopedLanguagePart.getScopeUids()));
            thinPart.setTagId(vodb.uuidToNative(scopedLanguagePart.getTagUids()));
            return thinPart;

        } else if (UniversalAceExtByRefPartString.class.equals(part.getClass())) {
            ThinExtByRefPartString thinPart = new ThinExtByRefPartString();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartString stringPart = (UniversalAceExtByRefPartString) part;
            thinPart.setStringValue(stringPart.getStringValue());
            return thinPart;

        } else if (UniversalAceExtByRefPartConceptString.class.equals(part.getClass())) {
            ThinExtByRefPartConceptString thinPart = new ThinExtByRefPartConceptString();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartConceptString conceptStringPart = (UniversalAceExtByRefPartConceptString) part;
            thinPart.setC1id(vodb.uuidToNative(conceptStringPart.getC1UuidCollection()));
            thinPart.setStr(conceptStringPart.getStr());
            return thinPart;

        } else if (UniversalAceExtByRefPartConceptInt.class.equals(part.getClass())) {
            ThinExtByRefPartConceptInt thinPart = new ThinExtByRefPartConceptInt();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartConceptInt conceptIntPart = (UniversalAceExtByRefPartConceptInt) part;
            thinPart.setC1id(vodb.uuidToNative(conceptIntPart.getConceptUid()));
            thinPart.setIntValue(conceptIntPart.getIntValue());
            return thinPart;

        } else if (UniversalAceExtByRefPartConceptConcept.class.equals(part.getClass())) {
            ThinExtByRefPartConceptConcept thinPart = new ThinExtByRefPartConceptConcept();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartConceptConcept ccPart = (UniversalAceExtByRefPartConceptConcept) part;
            thinPart.setC1id(vodb.uuidToNative(ccPart.getC1UuidCollection()));
            thinPart.setC2id(vodb.uuidToNative(ccPart.getC2UuidCollection()));
            return thinPart;

        } else if (UniversalAceExtByRefPartConceptConceptConcept.class.equals(part.getClass())) {
            ThinExtByRefPartConceptConceptConcept thinPart = new ThinExtByRefPartConceptConceptConcept();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartConceptConceptConcept cccPart = (UniversalAceExtByRefPartConceptConceptConcept) part;
            thinPart.setC1id(vodb.uuidToNative(cccPart.getC1UuidCollection()));
            thinPart.setC2id(vodb.uuidToNative(cccPart.getC2UuidCollection()));
            thinPart.setC3id(vodb.uuidToNative(cccPart.getC3UuidCollection()));
            return thinPart;

        } else if (UniversalAceExtByRefPartConceptConceptString.class.equals(part.getClass())) {
            ThinExtByRefPartConceptConceptString thinPart = new ThinExtByRefPartConceptConceptString();
            setStandardFields(part, vodb, thinPart);

            UniversalAceExtByRefPartConceptConceptString ccsPart = (UniversalAceExtByRefPartConceptConceptString) part;
            thinPart.setC1id(vodb.uuidToNative(ccsPart.getC1UuidCollection()));
            thinPart.setC2id(vodb.uuidToNative(ccsPart.getC2UuidCollection()));
            thinPart.setStringValue(ccsPart.getStr());
            return thinPart;

        } else {
            throw new UnsupportedOperationException("Can't handle UniversalAceExtByRefPart of type: " + part.getClass());
        }
    }

    private static void setStandardFields(UniversalAceExtByRefPart part, VodbEnv vodb, I_ThinExtByRefPart thinPart)
            throws TerminologyException, IOException {
        thinPart.setPathId(vodb.uuidToNative(part.getPathUid()));
        thinPart.setStatusId(vodb.uuidToNative(part.getStatusUid()));
        thinPart.setVersion(ThinVersionHelper.convert(part.getTime()));
    }

    private class ExtTupleAdder extends TupleAdder<I_ThinExtByRefTuple, ThinExtByRefVersioned> {

        @Override
        public I_ThinExtByRefTuple makeTuple(I_AmPart part, ThinExtByRefVersioned core) {
            return new ThinExtByRefTuple(core, (I_ThinExtByRefPart) part);
        }

    }

    ExtTupleAdder adder = new ExtTupleAdder();

    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<I_ThinExtByRefTuple> matchingTuples,
            boolean addUncommitted) {
        adder.addTuples(allowedStatus, null, positions, matchingTuples, addUncommitted, versions, this);
    }

    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<I_ThinExtByRefTuple> returnTuples,
            boolean addUncommitted, boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {

        List<I_ThinExtByRefTuple> tuples = new ArrayList<I_ThinExtByRefTuple>();

        if (returnConflictResolvedLatestState) {
            addTuples(null, positions, tuples, addUncommitted);
            I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
            I_ManageConflict conflictResolutionStrategy;
            if (config == null) {
                conflictResolutionStrategy = new IdentifyAllConflictStrategy();
            } else {
                conflictResolutionStrategy = config.getConflictResolutionStrategy();
            }

            tuples = conflictResolutionStrategy.resolveTuples(tuples);
            List<I_ThinExtByRefPart> versions = new ArrayList<I_ThinExtByRefPart>();
            for (I_ThinExtByRefTuple tuple : tuples) {
            	versions.add(tuple.getPart());
			}
            adder.addTuples(allowedStatus, null, positions, returnTuples, addUncommitted, versions, this);
        } else {
            addTuples(allowedStatus, positions, tuples, addUncommitted);
            returnTuples.addAll(tuples);
        }
    }

    public void addTuples(List<I_ThinExtByRefTuple> returnTuples, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {

        I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
        addTuples(config.getAllowedStatus(), config.getViewPositionSet(), returnTuples, addUncommitted,
            returnConflictResolvedLatestState);
    }

    public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus, Set<I_Position> positions, boolean addUncommitted) {
        List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
        addTuples(allowedStatus, positions, returnTuples, addUncommitted);
        return returnTuples;
    }

    public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus, Set<I_Position> positions,
            boolean addUncommitted, boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
        List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
        addTuples(allowedStatus, positions, returnTuples, addUncommitted, returnConflictResolvedLatestState);
        return returnTuples;
    }

    public List<I_ThinExtByRefTuple> getTuples(boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException {
        List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
        addTuples(returnTuples, addUncommitted, returnConflictResolvedLatestState);
        return returnTuples;
    }

    public int getNid() {
        return memberId;
    }

    public boolean promote(I_Position viewPosition, Set<I_Path> pomotionPaths, I_IntSet allowedStatus) {
        int viewPathId = viewPosition.getPath().getConceptId();
        Set<I_Position> positions = new HashSet<I_Position>();
        positions.add(viewPosition);
        List<I_ThinExtByRefTuple> matchingTuples = new ArrayList<I_ThinExtByRefTuple>();
        addTuples(allowedStatus, positions, matchingTuples, false);
        boolean promotedAnything = false;
        for (I_Path promotionPath : pomotionPaths) {
            for (I_ThinExtByRefTuple it : matchingTuples) {
                if (it.getPathId() == viewPathId) {
                    I_ThinExtByRefPart promotionPart = it.getPart().duplicate();
                    promotionPart.setVersion(Integer.MAX_VALUE);
                    promotionPart.setPathId(promotionPath.getConceptId());
                    it.addVersion(promotionPart);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
    }

    public I_ThinExtByRefPart getLatestVersion() {
        I_ThinExtByRefPart latestVersion = null;
        for (I_ThinExtByRefPart part : getVersions()) {
            if ((latestVersion == null) || (part.getVersion() >= latestVersion.getVersion())) {
                latestVersion = part;
            }
        }
        return latestVersion;
    }
}
