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
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinImageTuple implements I_ImageTuple {
    private I_ImageVersioned core;
    private I_ImagePart part;

    public ArrayIntList getPartComponentNids() {
        return part.getPartComponentNids();
    }

    public ThinImageTuple(I_ImageVersioned core, I_ImagePart part) {
        super();
        this.core = core;
        this.part = part;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageTuple#getImage()
     */
    public byte[] getImage() {
        return core.getImage();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageTuple#getImageId()
     */
    public int getImageId() {
        return core.getImageId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageTuple#getPathId()
     */
    public int getPathId() {
        return part.getPathId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageTuple#getStatusId()
     */
    public int getStatusId() {
        return part.getStatusId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageTuple#getVersion()
     */
    public int getVersion() {
        return part.getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageTuple#getTextDescription()
     */
    public String getTextDescription() {
        return part.getTextDescription();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageTuple#getTypeId()
     */
    public int getTypeId() {
        return part.getTypeId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageTuple#getFormat()
     */
    public String getFormat() {
        return core.getFormat();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageTuple#getConceptId()
     */
    public int getConceptId() {
        return core.getConceptId();
    }

    @Deprecated
    public I_ImagePart duplicatePart() {
        return new ThinImagePart(this.part);
    }

    public I_ImageVersioned getVersioned() {
        return core;
    }

    public I_ImagePart getPart() {
        return part;
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

    public I_ImagePart duplicate() {
        return duplicatePart();
    }

    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        // TODO
    }

    public void setTypeId(int type) {
        part.setTypeId(type);
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
        I_AmPart newPart = duplicate();
        newPart.setStatusId(statusNid);
        newPart.setPathId(pathNid);
        newPart.setVersion(ThinVersionHelper.convert(time));
        return newPart;
    }
}
