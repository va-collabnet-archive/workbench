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
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

public class ThinExtByRefTuple implements I_ThinExtByRefTuple {
    I_ThinExtByRefPart part;

    public ArrayIntList getPartComponentNids() {
        return part.getPartComponentNids();
    }

    I_ThinExtByRefVersioned core;

    public ThinExtByRefTuple(I_ThinExtByRefVersioned core, I_ThinExtByRefPart part) {
        super();
        this.part = part;
        this.core = core;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getPathId()
     */
    public int getPathId() {
        return part.getPathId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getStatus()
     */
    @Deprecated
    public int getStatus() {
        return part.getStatusId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getVersion()
     */
    public int getVersion() {
        return part.getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#setPathId(int)
     */
    public void setPathId(int pathId) {
        part.setPathId(pathId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#setStatus(int)
     */
    @Deprecated
    public void setStatus(int idStatus) {
        part.setStatusId(idStatus);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#setVersion(int)
     */
    public void setVersion(int version) {
        part.setVersion(version);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefTuple#addVersion(org.dwfa.vodb.types
     * .ThinExtByRefPart)
     */
    public void addVersion(I_ThinExtByRefPart part) {
        core.addVersion(part);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getComponentId()
     */
    public int getComponentId() {
        return core.getComponentId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getMemberId()
     */
    public int getMemberId() {
        return core.getMemberId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getRefsetId()
     */
    public int getRefsetId() {
        return core.getRefsetId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getTypeId()
     */
    public int getTypeId() {
        return core.getTypeId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getVersions()
     */
    public List<? extends I_ThinExtByRefPart> getVersions() {
        return core.getVersions();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getCore()
     */
    public I_ThinExtByRefVersioned getCore() {
        return core;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getPart()
     */
    public I_ThinExtByRefPart getPart() {
        return part;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        ThinExtByRefTuple other = (ThinExtByRefTuple) obj;
        return part.equals(other.getPart()) && core.equals(other.getCore());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { part.hashCode(), core.hashCode() });

    }

    @Override
    public String toString() {
        return "ThinExtByRefVersioned refsetId: " + core.getRefsetId() + " memberId: " + core.getMemberId()
            + " componentId: " + core.getComponentId() + " typeId: " + core.getTypeId() + " version: " + part;
    }

    public I_ThinExtByRefPart duplicate() {
        return part.duplicate();
    }

    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
        return part.getUniversalPart();
    }

    public int compareTo(I_ThinExtByRefPart o) {
        return part.compareTo(o);
    }

    public I_AmTermComponent getFixedPart() {
        return core;
    }

    public int getStatusId() {
        return part.getStatusId();
    }

    public void setStatusId(int statusId) {
        part.setStatusId(statusId);
    }

    @Deprecated
    public I_ThinExtByRefPart duplicatePart() {
        return duplicate();
    }

    public int getFixedPartId() {
        return core.getNid();
    }

    public I_ThinExtByRefPart makePromotionPart(I_Path promotionPath) {
        return part.makePromotionPart(promotionPath);
    }

    public boolean promote(I_Path promotionPath) {
        I_ThinExtByRefPart promotionPart = part.makePromotionPart(promotionPath);
        addVersion(promotionPart);
        return true;
    }

    public long getTime() {
        return part.getTime();
    }

    public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        return part.makeAnalog(statusNid, pathNid, time);
    }
}
