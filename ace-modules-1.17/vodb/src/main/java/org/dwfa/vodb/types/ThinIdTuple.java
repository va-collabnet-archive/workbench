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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinIdTuple implements I_IdTuple {
    I_IdVersioned core;
    I_IdPart part;

    public ArrayIntList getPartComponentNids() {
        return part.getPartComponentNids();
    }

    public ThinIdTuple(I_IdVersioned core, I_IdPart part) {
        super();
        this.core = core;
        this.part = part;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getNativeId()
     */
    public int getNativeId() {
        return core.getNativeId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getTimePathSet()
     */
    public Set<TimePathId> getTimePathSet() {
        return core.getTimePathSet();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getUIDs()
     */
    public List<UUID> getUIDs() {
        return core.getUIDs();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getVersions()
     */
    public List<I_IdPart> getVersions() {
        return core.getVersions();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_IdTuple#hasVersion(org.dwfa.vodb.types.I_IdPart)
     */
    public boolean hasVersion(I_IdPart newPart) {
        return core.hasVersion(newPart);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#setNativeId(int)
     */
    public void setNativeId(int nativeId) {
        core.setNativeId(nativeId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getIdStatus()
     */
    @Deprecated
    public int getIdStatus() {
        return part.getStatusId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getPathId()
     */
    public int getPathId() {
        return part.getPathId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getSource()
     */
    public int getSource() {
        return part.getSource();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getSourceId()
     */
    public Object getSourceId() {
        return part.getSourceId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getVersion()
     */
    public int getVersion() {
        return part.getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#getIdVersioned()
     */
    public I_IdVersioned getIdVersioned() {
        return core;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdTuple#duplicatePart()
     */
    @Deprecated
    public I_IdPart duplicatePart() {
        ThinIdPart newPart = new ThinIdPart();
        newPart.setPathId(getPathId());
        newPart.setVersion(getVersion());
        newPart.setIdStatus(getIdStatus());
        newPart.setSource(getSource());
        newPart.setSourceId(getSourceId());
        return newPart;
    }

    public I_IdPart getPart() {
        return part;
    }

    public int getStatusId() {
        return part.getStatusId();
    }

    public void setPathId(int pathId) {
        part.setPathId(pathId);
    }

    public void setStatusId(int idStatus) {
        part.setStatusId(idStatus);
    }

    public void setVersion(int version) {
        part.setVersion(version);
    }

    public I_AmTermComponent getFixedPart() {
        return core;
    }

    public I_IdPart duplicate() {
        return duplicatePart();
    }

    public int getFixedPartId() {
        return core.getNid();
    }

    public int getPositionId() {
        throw new UnsupportedOperationException();
    }

    public void setPositionId(int pid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTime() {
        return ThinVersionHelper.convert(getVersion());
    }

    @Override
    public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        I_IdPart newPart = duplicate();
        newPart.setStatusId(statusNid);
        newPart.setPathId(pathNid);
        newPart.setVersion(ThinVersionHelper.convert(time));
        return newPart;
    }
}
