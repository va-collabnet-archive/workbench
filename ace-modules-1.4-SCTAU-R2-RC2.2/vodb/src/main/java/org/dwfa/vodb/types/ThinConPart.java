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

import java.util.Date;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinConPart implements I_ConceptAttributePart {

    private int pathId;
    private int version;
    private int conceptStatus;
    private boolean defined;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(2);
        partComponentNids.add(pathId);
        partComponentNids.add(conceptStatus);
        return partComponentNids;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#getPathId()
     */
    public int getPathId() {
        return pathId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#setPathId(int)
     */
    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#getConceptStatus()
     */
    @Deprecated
    public int getConceptStatus() {
        return conceptStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#setConceptStatus(int)
     */
    @Deprecated
    public void setConceptStatus(int conceptStatus) {
        this.conceptStatus = conceptStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#isDefined()
     */
    public boolean isDefined() {
        return defined;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#setDefined(boolean)
     */
    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#getVersion()
     */
    public int getVersion() {
        return version;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#setVersion(int)
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributePart#hasNewData(org.dwfa.vodb.types
     * .ThinConPart)
     */
    public boolean hasNewData(I_ConceptAttributePart another) {
        return ((this.defined != another.isDefined()) || (this.pathId != another.getPathId()) || (this.conceptStatus != another.getStatusId()));
    }

    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        pathId = jarToDbNativeMap.get(pathId);
        conceptStatus = jarToDbNativeMap.get(conceptStatus);
    }

    @Override
    public boolean equals(Object obj) {
        ThinConPart another = (ThinConPart) obj;
        return ((pathId == another.pathId) && (version == another.version) && (conceptStatus == another.conceptStatus) && (defined == another.defined));
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { pathId, version, conceptStatus });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#duplicate()
     */
    public I_ConceptAttributePart duplicate() {
        ThinConPart newPart = new ThinConPart();
        newPart.setConceptStatus(conceptStatus);
        newPart.setDefined(defined);
        newPart.setPathId(pathId);
        newPart.setVersion(version);
        return newPart;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": defined " + defined + " status nid: " + conceptStatus + " pathId "
            + pathId + " version " + version + " (" + new Date(ThinVersionHelper.convert(version)) + ")";
    }

    public int getStatusId() {
        return getConceptStatus();
    }

    public void setStatusId(int statusId) {
        setConceptStatus(statusId);
    }

    public int getPositionId() {
        throw new UnsupportedOperationException();
    }

    public void setPositionId(int pid) {
        throw new UnsupportedOperationException();
    }

	@Override
	public long getTime() {
		return ThinVersionHelper.convert(version);
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		I_ConceptAttributePart newPart = duplicate();
		newPart.setStatusId(statusNid);
		newPart.setPathId(pathNid);
		newPart.setVersion(ThinVersionHelper.convert(time));
		return newPart;
	}
}
