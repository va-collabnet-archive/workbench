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
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinIdPart implements I_IdPart {
    private int pathId;
    private int version;
    private int idStatus;
    private int source;
    private Object sourceId;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(3);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(source);
        return partComponentNids;
    }

    public ThinIdPart() {
        super();
    }

    public ThinIdPart(I_IdPart another) {
        super();
        this.pathId = another.getPathId();
        this.version = another.getVersion();
        this.idStatus = another.getStatusId();
        this.source = another.getSource();
        this.sourceId = another.getSourceId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#getPathId()
     */
    public int getPathId() {
        return pathId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#setPathId(int)
     */
    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#getIdStatus()
     */
    @Deprecated
    public int getIdStatus() {
        return idStatus;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#setIdStatus(int)
     */
    @Deprecated
    public void setIdStatus(int idStatus) {
        this.idStatus = idStatus;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#getSource()
     */
    public int getSource() {
        return source;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#setSource(int)
     */
    public void setSource(int source) {
        this.source = source;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#getSourceId()
     */
    public Object getSourceId() {
        return sourceId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#setSourceId(java.lang.Object)
     */
    public void setSourceId(Object sourceId) {
        this.sourceId = sourceId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#getVersion()
     */
    public int getVersion() {
        return version;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_IdPart#setVersion(int)
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_IdPart#hasNewData(org.dwfa.vodb.types.ThinIdPart)
     */
    public boolean hasNewData(I_IdPart another) {
        return ((this.pathId != another.getPathId()) || (this.idStatus != another.getStatusId())
            || (this.source != another.getSource()) || sourceId.equals(another.getSourceId()) == false);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Source: ");
        buf.append(source);
        buf.append(" SourceId: ");
        buf.append(sourceId);
        buf.append(" StatusId: ");
        buf.append(idStatus);
        buf.append(" pathId: ");
        buf.append(pathId);
        buf.append(" version: ");
        buf.append(version);
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        I_IdPart another = (I_IdPart) obj;
        return ((pathId == another.getPathId()) && (version == another.getVersion())
            && (idStatus == another.getStatusId()) && (source == another.getSource()) && (sourceId.equals(another.getSourceId())));
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { pathId, version, idStatus, source, sourceId.hashCode() });
    }

    public I_IdPart duplicate() {
        return new ThinIdPart(this);
    }

    public int getStatusId() {
        return idStatus;
    }

    public void setStatusId(int statusId) {
        this.idStatus = statusId;
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
