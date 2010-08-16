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

import org.dwfa.ace.api.DescriptionHasNoVersionsException;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.table.TupleAdder;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedDesc;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThinDescVersioned implements I_DescriptionVersioned {
    private int descId;

    private int conceptId;

    private List<I_DescriptionPart> versions;
    private DescriptionPartSorter descriptionPartSorter;

    public ThinDescVersioned(int descId, int conceptId, int count) {
        super();
        this.descId = descId;
        this.conceptId = conceptId;
        this.versions = new ArrayList<I_DescriptionPart>(count);
        descriptionPartSorter = new DescriptionPartSorter(new DescriptionPartComparator());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_DescriptionVersioned#addVersion(org.dwfa.vodb.types
     * .I_DescriptionPart)
     */
    public boolean addVersion(I_DescriptionPart newPart) {
        return versions.add(newPart);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#getVersions()
     */
    public List<I_DescriptionPart> getVersions() {
        return versions;
    }

    public List<I_DescriptionPart> getVersions(boolean returnConflictResolvedLatestState) throws TerminologyException,
            IOException {

        List<I_DescriptionPart> returnList = new ArrayList<I_DescriptionPart>(versions);

        if (returnConflictResolvedLatestState) {
            I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
            returnList = config.getConflictResolutionStrategy().resolveParts(returnList);
        }

        return returnList;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#versionCount()
     */
    public int versionCount() {
        return versions.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_DescriptionVersioned#matches(java.util.regex.Pattern
     * )
     */
    public boolean matches(Pattern p) {
        String lastText = null;
        for (I_DescriptionPart desc : versions) {
            if (desc.getText() != lastText) {
                lastText = desc.getText();
                Matcher m = p.matcher(lastText);
                if (m.find()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("ThinDescVersioned: desc: ");
        buff.append(descId);
        buff.append(" ConceptId: ");
        buff.append(conceptId);
        buff.append("\n");
        for (I_DescriptionPart desl : versions) {
            buff.append("     ");
            buff.append(desl.toString());
            buff.append("\n");
        }

        return buff.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#getConceptId()
     */
    public int getConceptId() {
        return conceptId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#getDescId()
     */
    public int getDescId() {
        return descId;
    }

    public int getTermComponentId() {
        return descId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#getTuples()
     */
    public List<I_DescriptionTuple> getTuples() {
        List<I_DescriptionTuple> tuples = new ArrayList<I_DescriptionTuple>();
        for (I_DescriptionPart p : getVersions()) {
            tuples.add(new ThinDescTuple(this, p));
        }

        return tuples;
    }

    public List<I_DescriptionTuple> getTuples(boolean returnConflictResolvedLatestState) throws TerminologyException,
            IOException {
        List<I_DescriptionTuple> tuples = new ArrayList<I_DescriptionTuple>();
        for (I_DescriptionPart p : getVersions(returnConflictResolvedLatestState)) {
            tuples.add(new ThinDescTuple(this, p));
        }

        return tuples;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#getFirstTuple()
     */
    public I_DescriptionTuple getFirstTuple() {
        return new ThinDescTuple(this, versions.get(0));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#getLastTuple()
     */
    public I_DescriptionTuple getLastTuple() {
        if (versions == null || versions.isEmpty()) {
            throw new DescriptionHasNoVersionsException();
        }

        return new ThinDescTuple(this, descriptionPartSorter.sort(versions));
    }

    private class DescTupleAdder extends TupleAdder<I_DescriptionTuple, ThinDescVersioned> {

        @Override
        public I_DescriptionTuple makeTuple(I_AmPart part, ThinDescVersioned core) {
            return new ThinDescTuple(core, (I_DescriptionPart) part);
        }

    }

    DescTupleAdder adder = new DescTupleAdder();

    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            List<I_DescriptionTuple> matchingTuples, boolean addUncommitted) {
        adder.addTuples(allowedStatus, allowedTypes, positions, matchingTuples, addUncommitted, versions, this);
    }

    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positionSet,
            List<I_DescriptionTuple> matchingTuples, boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException {

        List<I_DescriptionTuple> tuples = new ArrayList<I_DescriptionTuple>();

        addTuples(null, allowedTypes, positionSet, tuples, addUncommitted);

        if (returnConflictResolvedLatestState) {
            I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
            I_ManageConflict conflictResolutionStrategy;
            if (config == null) {
                conflictResolutionStrategy = new IdentifyAllConflictStrategy();
            } else {
                conflictResolutionStrategy = config.getConflictResolutionStrategy();
            }

            tuples = conflictResolutionStrategy.resolveTuples(tuples);
        }

        if (allowedStatus != null) {
            for (I_DescriptionTuple descTuple : tuples) {
                // filter by allowed status
                if (allowedStatus.contains(descTuple.getStatusId())) {
                    matchingTuples.add(descTuple);
                }
            }
        } else {
            matchingTuples.addAll(tuples);
        }
    }

    public void addTuples(I_IntSet allowedTypes, List<I_DescriptionTuple> matchingTuples, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {

        I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();

        addTuples(config.getAllowedStatus(), allowedTypes, config.getViewPositionSet(), matchingTuples, addUncommitted,
            returnConflictResolvedLatestState);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_DescriptionVersioned#convertIds(org.dwfa.vodb.jar
     * .I_MapNativeToNative)
     */
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        conceptId = jarToDbNativeMap.get(conceptId);
        descId = jarToDbNativeMap.get(descId);
        for (I_DescriptionPart p : versions) {
            p.convertIds(jarToDbNativeMap);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_DescriptionVersioned#merge(org.dwfa.vodb.types.
     * ThinDescVersioned)
     */
    public boolean merge(I_DescriptionVersioned jarDesc) {
        HashSet<I_DescriptionPart> versionSet = new HashSet<I_DescriptionPart>(versions);
        boolean changed = false;
        for (I_DescriptionPart jarPart : jarDesc.getVersions()) {
            if (!versionSet.contains(jarPart)) {
                changed = true;
                versions.add(jarPart);
            }
        }
        return changed;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#getTimePathSet()
     */
    public Set<TimePathId> getTimePathSet() {
        Set<TimePathId> tpSet = new HashSet<TimePathId>();
        for (I_DescriptionPart p : versions) {
            tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
        }
        return tpSet;
    }

    @Override
    public boolean equals(Object obj) {
        ThinDescVersioned another = (ThinDescVersioned) obj;
        return descId == another.descId;
    }

    @Override
    public int hashCode() {
        return descId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#toLocalFixedDesc()
     */
    public I_DescribeConceptLocally toLocalFixedDesc() {
        I_DescriptionPart part = versions.get(versions.size() - 1);
        return new LocalFixedDesc(descId, part.getStatusId(), conceptId, part.getInitialCaseSignificant(),
            part.getTypeId(), part.getText(), part.getLang());
    }

    private static Collection<UUID> getUids(int id) throws IOException, TerminologyException {
        return LocalFixedTerminology.getStore().getUids(id);
    }

    public UniversalAceDescription getUniversal() throws IOException, TerminologyException {
        UniversalAceDescription universal = new UniversalAceDescription(getUids(descId), getUids(conceptId),
            this.versionCount());
        for (I_DescriptionPart part : versions) {
            UniversalAceDescriptionPart universalPart = new UniversalAceDescriptionPart();
            universalPart.setInitialCaseSignificant(part.getInitialCaseSignificant());
            universalPart.setLang(part.getLang());
            universalPart.setPathId(getUids(part.getPathId()));
            universalPart.setStatusId(getUids(part.getStatusId()));
            universalPart.setText(part.getText());
            universalPart.setTypeId(getUids(part.getTypeId()));
            universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
            universal.addVersion(universalPart);
        }
        return universal;
    }

    public int getNid() {
        return descId;
    }

    public boolean promote(I_Position viewPosition, Set<I_Path> pomotionPaths, I_IntSet allowedStatus) {
        int viewPathId = viewPosition.getPath().getConceptId();
        Set<I_Position> positions = new HashSet<I_Position>();
        positions.add(viewPosition);
        List<I_DescriptionTuple> matchingTuples = new ArrayList<I_DescriptionTuple>();
        addTuples(allowedStatus, null, positions, matchingTuples, false);
        boolean promotedAnything = false;
        for (I_Path promotionPath : pomotionPaths) {
            for (I_DescriptionTuple dt : matchingTuples) {
                if (dt.getPathId() == viewPathId) {
                    I_DescriptionPart promotionPart = dt.getPart().duplicate();
                    promotionPart.setVersion(Integer.MAX_VALUE);
                    promotionPart.setPathId(promotionPath.getConceptId());
                    dt.getDescVersioned().addVersion(promotionPart);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
    }
}
