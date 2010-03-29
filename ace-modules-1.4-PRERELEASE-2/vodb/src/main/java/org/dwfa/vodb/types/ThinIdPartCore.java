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

import org.dwfa.util.HashFunction;

public class ThinIdPartCore {
    private int pathId;
    private int version;
    private int idStatus;
    private int source;

    public ThinIdPartCore() {
        super();
    }

    public ThinIdPartCore(ThinIdPartCore another) {
        super();
        this.pathId = another.pathId;
        this.version = another.version;
        this.idStatus = another.idStatus;
        this.source = another.source;
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
    public int getIdStatus() {
        return idStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IdPart#setIdStatus(int)
     */
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
    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IdPart#setSourceId(java.lang.Object)
     */
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

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Source: ");
        buf.append(source);
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
        ThinIdPartCore another = (ThinIdPartCore) obj;
        return ((pathId == another.pathId) && (version == another.version) && (idStatus == another.idStatus) && (source == another.source));
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { pathId, version, idStatus, source });
    }

    public ThinIdPartCore duplicate() {
        return new ThinIdPartCore(this);
    }

}
