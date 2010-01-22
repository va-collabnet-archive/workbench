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
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinImagePart implements I_ImagePart {
    private int pathId;
    private int version;
    private int statusId;
    private String textDescription;
    private int typeId;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(3);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(typeId);
        return partComponentNids;
    }

    public ThinImagePart() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#getPathId()
     */
    public int getPathId() {
        return pathId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#getStatusId()
     */
    public int getStatusId() {
        return statusId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#getVersion()
     */
    public int getVersion() {
        return version;
    }

    public ThinImagePart(int pathId, int version, int status, String textDescription, int type) {
        super();
        this.pathId = pathId;
        this.version = version;
        this.statusId = status;
        this.textDescription = textDescription;
        this.typeId = type;
    }

    public ThinImagePart(I_ImagePart another) {
        super();
        this.pathId = another.getPathId();
        this.version = another.getVersion();
        this.statusId = another.getStatusId();
        this.textDescription = another.getTextDescription();
        this.typeId = another.getTypeId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#setPathId(int)
     */
    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#setStatusId(int)
     */
    public void setStatusId(int status) {
        this.statusId = status;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#setVersion(int)
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#getTextDescription()
     */
    public String getTextDescription() {
        return textDescription;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#setTextDescription(java.lang.String)
     */
    public void setTextDescription(String name) {
        this.textDescription = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#getTypeId()
     */
    public int getTypeId() {
        return typeId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
     */
    public void setTypeId(int type) {
        this.typeId = type;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     *
     *
     *
     *
     *
     * org.dwfa.vodb.types.I_ImagePart#hasNewData(org.dwfa.vodb.types.ThinImagePart
     * )
     */
    public boolean hasNewData(I_ImagePart another) {
        return ((this.pathId != another.getPathId()) || (this.statusId != another.getStatusId()) || ((this.textDescription.equals(another.getTextDescription()) == false) || (this.typeId != another.getTypeId())));
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.dwfa.vodb.types.I_ImagePart#convertIds(org.dwfa.vodb.jar.
     * I_MapNativeToNative)
     */
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        pathId = jarToDbNativeMap.get(pathId);
        statusId = jarToDbNativeMap.get(statusId);
        typeId = jarToDbNativeMap.get(typeId);
    }

    @Override
    public boolean equals(Object obj) {
        ThinImagePart another = (ThinImagePart) obj;
        return ((pathId == another.pathId) && (statusId == another.statusId)
            && (textDescription.equals(another.textDescription)) && (typeId == another.typeId) && (version == another.version));
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { pathId, statusId, textDescription.hashCode(), typeId, version });
    }

    public I_ImagePart duplicate() {
        return new ThinImagePart(this);
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
        I_AmPart newPart = duplicate();
        newPart.setStatusId(statusNid);
        newPart.setPathId(pathNid);
        newPart.setVersion(ThinVersionHelper.convert(time));
        return newPart;
    }
}
