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
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinDescTuple implements I_DescriptionTuple {
    I_DescriptionVersioned fixedPart;
    I_DescriptionPart part;

    public ArrayIntList getPartComponentNids() {
        return part.getPartComponentNids();
    }

    transient Integer hash;

    public ThinDescTuple(I_DescriptionVersioned fixedPart, I_DescriptionPart variablePart) {
        super();
        this.fixedPart = fixedPart;
        this.part = variablePart;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getPathId()
     */
    public int getPathId() {
        return part.getPathId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getInitialCaseSignificant()
     */
    public boolean getInitialCaseSignificant() {
        return part.getInitialCaseSignificant();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getLang()
     */
    public String getLang() {
        return part.getLang();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getStatusId()
     */
    public int getStatusId() {
        return part.getStatusId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getText()
     */
    public String getText() {
        return part.getText();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getTypeId()
     */
    public int getTypeId() {
        return part.getTypeId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getVersion()
     */
    public int getVersion() {
        return part.getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getConceptId()
     */
    public int getConceptId() {
        return fixedPart.getConceptId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getDescId()
     */
    public int getDescId() {
        return fixedPart.getDescId();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_DescriptionTuple#setInitialCaseSignificant(boolean)
     */
    public void setInitialCaseSignificant(boolean capStatus) {
        part.setInitialCaseSignificant(capStatus);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#setLang(java.lang.String)
     */
    public void setLang(String lang) {
        part.setLang(lang);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#setPathId(int)
     */
    public void setPathId(int pathId) {
        part.setPathId(pathId);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#setStatusId(int)
     */
    public void setStatusId(int status) {
        part.setStatusId(status);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#setText(java.lang.String)
     */
    public void setText(String text) {
        part.setText(text);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#setTypeId(int)
     */
    public void setTypeId(int typeInt) {
        part.setTypeId(typeInt);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#setVersion(int)
     */
    public void setVersion(int version) {
        part.setVersion(version);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#duplicatePart()
     */
    @Deprecated
    public I_DescriptionPart duplicatePart() {
        return part.duplicate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionTuple#getDescVersioned()
     */
    public I_DescriptionVersioned getDescVersioned() {
        return fixedPart;
    }

    @Override
    public boolean equals(Object obj) {
        ThinDescTuple another = (ThinDescTuple) obj;
        return fixedPart.equals(another.fixedPart) && part.equals(another.part);
    }

    @Override
    public int hashCode() {
        if (hash == null) {
            hash = HashFunction.hashCode(new int[] { fixedPart.hashCode(), part.hashCode() });
        }
        return hash;
    }

    public I_DescriptionPart getPart() {
        return part;
    }

    public I_DescriptionVersioned getFixedPart() {
        return fixedPart;
    }

    public I_DescriptionPart duplicate() {
        return duplicatePart();
    }

    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        // TODO
    }

    public int getFixedPartId() {
        return fixedPart.getTermComponentId();
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
        I_DescriptionPart newPart = duplicate();
        newPart.setStatusId(statusNid);
        newPart.setPathId(pathNid);
        newPart.setVersion(ThinVersionHelper.convert(time));
        return newPart;
    }

}
