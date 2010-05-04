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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.table.TupleAdder;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;

public class ThinConVersioned implements I_ConceptAttributeVersioned {
    private int conId;

    private List<I_ConceptAttributePart> versions;

    public ThinConVersioned(int conId, int count) {
        super();
        this.conId = conId;
        this.versions = new ArrayList<I_ConceptAttributePart>(count);
    }

    public int getNid() {
        return conId;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeVersioned#addVersion(org.dwfa.vodb
     * .types.ThinConPart)
     */
    public boolean addVersion(I_ConceptAttributePart part) {
        return versions.add(part);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getVersions()
     */
    public List<I_ConceptAttributePart> getVersions() {
        return versions;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#versionCount()
     */
    public int versionCount() {
        return versions.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getConId()
     */
    public int getConId() {
        return conId;
    }

    public int getTermComponentId() {
        return conId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTuples()
     */
    public List<I_ConceptAttributeTuple> getTuples() {
        List<I_ConceptAttributeTuple> tuples = new ArrayList<I_ConceptAttributeTuple>();
        for (I_ConceptAttributePart p : versions) {
            tuples.add(new ThinConTuple(this, p));
        }
        return tuples;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeVersioned#convertIds(org.dwfa.vodb
     * .jar.I_MapNativeToNative)
     */
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        conId = jarToDbNativeMap.get(conId);
        for (I_ConceptAttributePart part : versions) {
            ((I_ConceptAttributeVersioned) part).convertIds(jarToDbNativeMap);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeVersioned#merge(org.dwfa.vodb.types
     * .ThinConVersioned)
     */
    public boolean merge(I_ConceptAttributeVersioned jarCon) {
        HashSet<I_ConceptAttributePart> versionSet = new HashSet<I_ConceptAttributePart>(versions);
        boolean changed = false;
        for (I_ConceptAttributePart jarPart : jarCon.getVersions()) {
            if (!versionSet.contains(jarPart)) {
                changed = true;
                versions.add((ThinConPart) jarPart);
            }
        }
        return changed;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTimePathSet()
     */
    public Set<TimePathId> getTimePathSet() {
        Set<TimePathId> tpSet = new HashSet<TimePathId>();
        for (I_ConceptAttributePart p : versions) {
            tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
        }
        return tpSet;
    }

    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<I_ConceptAttributeTuple> returnTuples) {
        addTuples(allowedStatus, positions, returnTuples, true);
    }

    private class ConTupleAdder extends TupleAdder<I_ConceptAttributeTuple, ThinConVersioned> {

        @Override
        public I_ConceptAttributeTuple makeTuple(I_AmPart part, ThinConVersioned core) {
            return new ThinConTuple(core, (I_ConceptAttributePart) part);
        }

    }

    ConTupleAdder adder = new ConTupleAdder();

    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions,
            List<I_ConceptAttributeTuple> matchingTuples, boolean addUncommitted) {
        adder.addTuples(allowedStatus, null, positions, matchingTuples, addUncommitted, versions, this);
    }

    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positionSet,
            List<I_ConceptAttributeTuple> returnTuples, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {

        List<I_ConceptAttributeTuple> tuples = new ArrayList<I_ConceptAttributeTuple>();

        addTuples(allowedStatus, positionSet, tuples, addUncommitted);

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

        returnTuples.addAll(tuples);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeVersioned#getLocalFixedConcept()
     */
    public I_ConceptualizeLocally getLocalFixedConcept() {
        boolean isDefined = versions.get(versions.size() - 1).isDefined();
        boolean isPrimitive = !isDefined;
        return LocalFixedConcept.get(conId, isPrimitive);
    }

    @Override
    public boolean equals(Object obj) {
        ThinConVersioned another = (ThinConVersioned) obj;
        return conId == another.conId;
    }

    @Override
    public int hashCode() {
        return conId;
    }

    private static Collection<UUID> getUids(int id) throws IOException, TerminologyException {
        return LocalFixedTerminology.getStore().getUids(id);
    }

    public UniversalAceConceptAttributes getUniversal() throws IOException, TerminologyException {
        UniversalAceConceptAttributes conceptAttributes = new UniversalAceConceptAttributes(getUids(conId),
            this.versionCount());
        for (I_ConceptAttributePart part : versions) {
            UniversalAceConceptAttributesPart universalPart = new UniversalAceConceptAttributesPart();
            universalPart.setConceptStatus(getUids(part.getStatusId()));
            universalPart.setDefined(part.isDefined());
            universalPart.setPathId(getUids(part.getPathId()));
            universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
            conceptAttributes.addVersion(universalPart);
        }
        return conceptAttributes;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("NativeId: ");
        buf.append(conId);
        buf.append(" parts: ");
        buf.append(versions.size());
        buf.append("\n  ");
        for (I_ConceptAttributePart p : versions) {
            buf.append(p);
            buf.append("\n  ");
        }
        return buf.toString();
    }

    public void setConId(int conId) {
        if (this.conId == Integer.MIN_VALUE) {
            this.conId = conId;
        } else {
            throw new RuntimeException("Cannot change the conId once set");
        }
    }

    public List<I_ConceptAttributeTuple> getTuples(I_IntSet allowedStatus, Set<I_Position> viewPositionSet) {
        List<I_ConceptAttributeTuple> returnList = new ArrayList<I_ConceptAttributeTuple>();

        addTuples(allowedStatus, viewPositionSet, returnList);

        return returnList;
    }

    public boolean promote(I_Position viewPosition, Set<I_Path> promotionPaths, I_IntSet allowedStatus) {
        int viewPathId = viewPosition.getPath().getConceptId();
        Set<I_Position> viewPositionSet = new HashSet<I_Position>();
        viewPositionSet.add(viewPosition);
        boolean promotedAnything = false;
        for (I_Path promotionPath : promotionPaths) {
            for (I_ConceptAttributeTuple tuple : getTuples(allowedStatus, viewPositionSet)) {
                if (tuple.getPart().getPathId() == viewPathId) {
                    I_ConceptAttributePart promotionPart = tuple.getPart().duplicate();
                    promotionPart.setVersion(Integer.MAX_VALUE);
                    promotionPart.setPathId(promotionPath.getConceptId());
                    addVersion(promotionPart);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
    }
}
