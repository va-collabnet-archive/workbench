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
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.table.TupleAdder;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinIdVersioned implements I_IdVersioned {
    public static final int SNOMED_CT_T3_PREFIX = 1;
    private int nativeId;
    private List<I_IdPart> versions;

    public ThinIdVersioned(int nativeId, int count) {
        super();
        this.nativeId = nativeId;
        this.versions = new ArrayList<I_IdPart>(count);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdVersioned#getNativeId()
     */
    public int getNativeId() {
        return nativeId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdVersioned#setNativeId(int)
     */
    public void setNativeId(int nativeId) {
        this.nativeId = nativeId;
    }

    public int getTermComponentId() {
        return nativeId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdVersioned#getVersions()
     */
    public List<I_IdPart> getVersions() {
        return versions;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdVersioned#getUIDs()
     */
    public List<UUID> getUIDs() {
        List<UUID> uids = new ArrayList<UUID>(versions.size());
        for (I_IdPart p : versions) {
            if (UUID.class.isAssignableFrom(p.getSourceId().getClass())) {
                uids.add((UUID) p.getSourceId());
            }
        }
        return uids;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_IdVersioned#addVersion(org.dwfa.vodb.types.I_IdPart
     * )
     */
    public boolean addVersion(I_IdPart srcId) {
        assert versions.contains(srcId) == false;
        return versions.add(srcId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_IdVersioned#hasVersion(org.dwfa.vodb.types.I_IdPart
     * )
     */
    public boolean hasVersion(I_IdPart newPart) {
        return versions.contains(newPart);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdVersioned#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("NativeId: ");
        buf.append(nativeId);
        buf.append(" parts: ");
        buf.append(versions.size());
        buf.append("\n  ");
        for (I_IdPart p : versions) {
            buf.append(p);
            buf.append("\n  ");
        }
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdVersioned#getTimePathSet()
     */
    public Set<TimePathId> getTimePathSet() {
        Set<TimePathId> tpSet = new HashSet<TimePathId>();
        for (I_IdPart p : versions) {
            tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
        }
        return tpSet;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdVersioned#getTuples()
     */
    public List<I_IdTuple> getTuples() {
        List<I_IdTuple> tuples = new ArrayList<I_IdTuple>();
        for (I_IdPart p : versions) {
            tuples.add(new ThinIdTuple(this, p));
        }
        return tuples;
    }

    private static Collection<UUID> getUids(int id) throws IOException, TerminologyException {
        return LocalFixedTerminology.getStore().getUids(id);
    }

    public UniversalAceIdentification getUniversal() throws IOException, TerminologyException {
        UniversalAceIdentification universal = new UniversalAceIdentification(this.versions.size());
        for (I_IdPart part : versions) {
            UniversalAceIdentificationPart universalPart = new UniversalAceIdentificationPart();
            universalPart.setIdStatus(getUids(part.getStatusId()));
            universalPart.setPathId(getUids(part.getPathId()));
            universalPart.setSource(getUids(part.getSource()));
            universalPart.setSourceId(part.getSourceId());
            universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
            universal.addVersion(universalPart);
        }
        return universal;
    }

    @Override
    public boolean equals(Object obj) {
        if (ThinIdVersioned.class.isAssignableFrom(obj.getClass())) {
            ThinIdVersioned another = (ThinIdVersioned) obj;
            if (this.nativeId != another.nativeId) {
                return false;
            }
            if (this.versions.size() != another.versions.size()) {
                return false;
            }
            for (I_IdPart part : versions) {
                if (another.versions.contains(part) == false) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return nativeId;
    }

    public int getNid() {
        return nativeId;
    }

    private class IdTupleAdder extends TupleAdder<I_IdTuple, ThinIdVersioned> {

        @Override
        public I_IdTuple makeTuple(I_AmPart part, ThinIdVersioned core) {
            return new ThinIdTuple(core, (I_IdPart) part);
        }
    }

    IdTupleAdder adder = new IdTupleAdder();

    public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions, List<I_IdTuple> matchingTuples) {
        adder.addTuples(allowedStatus, positions, matchingTuples, true, versions, this);
    }

    public boolean promote(I_Position viewPosition, Set<I_Path> pomotionPaths, I_IntSet allowedStatus) {
        int viewPathId = viewPosition.getPath().getConceptId();
        Set<I_Position> positions = new HashSet<I_Position>();
        positions.add(viewPosition);
        List<I_IdTuple> matchingTuples = new ArrayList<I_IdTuple>();
        addTuples(allowedStatus, positions, matchingTuples);
        if (matchingTuples.size() == 0) {
            matchingTuples.add(new ThinIdTuple(this, versions.get(0)));
        }
        boolean promotedAnything = false;
        for (I_Path promotionPath : pomotionPaths) {
            for (I_IdTuple it : matchingTuples) {
                if (it.getPathId() == viewPathId) {
                    I_IdPart promotionPart = it.getPart().duplicate();
                    promotionPart.setVersion(Integer.MAX_VALUE);
                    promotionPart.setPathId(promotionPath.getConceptId());
                    if (versions.contains(promotionPart) == false) {
                        it.getIdVersioned().addVersion(promotionPart);
                        promotedAnything = true;
                    }
                }
            }
        }
        return promotedAnything;
    }
}
