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

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;

public class ThinConTuple implements I_ConceptAttributeTuple {
    I_ConceptAttributeVersioned core;

    I_ConceptAttributePart part;

    public ArrayIntList getPartComponentNids() {
        return part.getPartComponentNids();
    }

    public void setConceptStatus(int conceptStatus) {
        part.setStatusId(conceptStatus);
    }

    transient Integer hash;

    public ThinConTuple(I_ConceptAttributeVersioned core, I_ConceptAttributePart part) {
        super();
        this.core = core;
        this.part = part;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getConId()
     */
    public int getConId() {
        return core.getConId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getConceptStatus()
     */
    @Deprecated
    public int getConceptStatus() {
        return part.getConceptStatus();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.ace.api.I_AmPart#setStatusId(int)
     */
    public void setStatusId(int statusId) {
        part.setStatusId(statusId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getPathId()
     */
    public int getPathId() {
        return part.getPathId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getVersion()
     */
    public int getVersion() {
        return part.getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeTuple#hasNewData(org.dwfa.vodb.
     * types.ThinConPart)
     */
    public boolean hasNewData(I_ConceptAttributePart another) {
        return part.hasNewData(another);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#isDefined()
     */
    public boolean isDefined() {
        return part.isDefined();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeTuple#setStatusId(java.lang.Integer
     * )
     */
    public void setStatusId(Integer statusId) {
        part.setStatusId(statusId);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#setDefined(boolean)
     */
    public void setDefined(boolean defined) {
        part.setDefined(defined);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#getConVersioned()
     */
    public I_ConceptAttributeVersioned getConVersioned() {
        return core;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeTuple#duplicatePart()
     */
    @Deprecated
    public I_ConceptAttributePart duplicatePart() {
        return part.duplicate();
    }

    @Override
    public boolean equals(Object obj) {
        ThinConTuple another = (ThinConTuple) obj;
        return core.equals(another.core) && part.equals(another.part);
    }

    @Override
    public int hashCode() {
        if (hash == null) {
            hash = HashFunction.hashCode(new int[] { core.hashCode(), part.hashCode() });
        }
        return hash;
    }

    public String toString() {
        return "ThinConTuple id: " + getConId() + " status: " + getConceptStatus() + " defined: " + isDefined()
            + " path: " + getPathId() + " version: " + getVersion();
    }

    public I_ConceptAttributePart getPart() {
        return part;
    }

    public int getStatusId() {
        return getConceptStatus();
    }

    public void setPathId(int pathId) {
        part.setPathId(pathId);
    }

    public void setVersion(int version) {
        part.setVersion(version);
    }

    public I_AmTermComponent getFixedPart() {
        return core;
    }

    public I_ConceptAttributePart duplicate() {
        return part.duplicate();
    }

    public int getFixedPartId() {
        return core.getConId();
    }

    public long getTime() {
        return part.getTime();
    }

    public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        return part.makeAnalog(statusNid, pathNid, time);
    }
}
