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
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.util.HashFunction;

public class ThinRelTuple implements I_RelTuple {

    I_RelVersioned fixedPart;
    I_RelPart part;

    public ArrayIntList getPartComponentNids() {
        return part.getPartComponentNids();
    }

    transient Integer hash;

    public ThinRelTuple(I_RelVersioned fixedPart, I_RelPart variablePart) {
        super();
        this.fixedPart = fixedPart;
        this.part = variablePart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getC1Id()
     */
    public int getC1Id() {
        return fixedPart.getC1Id();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getC2Id()
     */
    public int getC2Id() {
        return fixedPart.getC2Id();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getRelId()
     */
    public int getRelId() {
        return fixedPart.getRelId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getPathId()
     */
    public int getPathId() {
        return part.getPathId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getCharacteristicId()
     */
    public int getCharacteristicId() {
        return part.getCharacteristicId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getGroup()
     */
    public int getGroup() {
        return part.getGroup();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getRefinabilityId()
     */
    public int getRefinabilityId() {
        return part.getRefinabilityId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getTypeId()
     */
    @Deprecated
    public int getTypeId() {
        return part.getTypeId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getStatusId()
     */
    public int getStatusId() {
        return part.getStatusId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getVersion()
     */
    public int getVersion() {
        return part.getVersion();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#setTypeId(java.lang.Integer)
     */
    @Deprecated
    public void setTypeId(int typeId) {
        part.setTypeId(typeId);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#setStatusId(java.lang.Integer)
     */
    public void setStatusId(int statusId) {
        part.setStatusId(statusId);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_RelTuple#setCharacteristicId(java.lang.Integer)
     */
    public void setCharacteristicId(int characteristicId) {
        part.setCharacteristicId(characteristicId);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#setRefinabilityId(java.lang.Integer)
     */
    public void setRefinabilityId(int refinabilityId) {
        part.setRefinabilityId(refinabilityId);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#setGroup(java.lang.Integer)
     */
    public void setGroup(int group) {
        part.setGroup(group);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#duplicate()
     */
    @Deprecated
    public I_RelPart duplicate() {
        return part.duplicate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getRelVersioned()
     */
    public I_RelVersioned getRelVersioned() {
        return fixedPart;
    }

    @Override
    public boolean equals(Object obj) {
        ThinRelTuple another = (ThinRelTuple) obj;
        return fixedPart.equals(another.fixedPart) && part.equals(another.part);
    }

    @Override
    public int hashCode() {
        if (hash == null) {
            hash = HashFunction.hashCode(new int[] { fixedPart.hashCode(), part.hashCode() });
        }
        return hash;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelTuple#getFixedPart()
     */
    public I_RelVersioned getFixedPart() {
        return fixedPart;
    }

    public I_RelPart getMutablePart() {
        return part;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("ThinRelTuple: relId: ");
        buff.append(getRelId());
        buff.append(" c1id: ");
        buff.append(getC1Id());
        buff.append(" c2id: ");
        buff.append(getC2Id());
        buff.append(" ");
        buff.append(part.toString());
        return buff.toString();
    }

    public void setPathId(int pathId) {
        part.setPathId(pathId);
    }

    public void setVersion(int version) {
        part.setVersion(version);
    }

    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        // TODO
    }

    public int getNid() {
        return fixedPart.getNid();
    }

    public int getPositionId() {
        throw new UnsupportedOperationException();
    }

    public void setPositionId(int pid) {
        throw new UnsupportedOperationException();
    }

	public long getTime() {
		return part.getTime();
	}

	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		return part.makeAnalog(statusNid, pathNid, time);
	}

}
