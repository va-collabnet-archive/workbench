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
import java.util.Date;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinDescPartWithCoreDelegate implements I_DescriptionPart {

    private String text;
    private ThinDescPartCore core;

    public ArrayIntList getPartComponentNids() {
        return core.getPartComponentNids();
    }

    public ThinDescPartWithCoreDelegate(String text, ThinDescPartCore core) {
        super();
        this.text = text;
        this.core = core;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_DescriptionPart#hasNewData(org.dwfa.vodb.types.
     * ThinDescPart)
     */
    public boolean hasNewData(I_DescriptionPart another) {
        return ((core.getPathId() != another.getPathId()) || (core.getStatusId() != another.getStatusId()) || ((this.text.equals(another.getText()) == false)
            || (core.getInitialCaseSignificant() != another.getInitialCaseSignificant())
            || (core.getTypeId() != another.getTypeId()) || ((core.getLang().equals(another.getLang()) == false))));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getPathId()
     */
    public int getPathId() {
        return core.getPathId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setPathId(int)
     */
    public void setPathId(int pathId) {
        throw new UnsupportedOperationException("duplicate, then perform set on duplicate object");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getInitialCaseSignificant()
     */
    public boolean getInitialCaseSignificant() {
        return core.getInitialCaseSignificant();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_DescriptionPart#setInitialCaseSignificant(boolean)
     */
    public void setInitialCaseSignificant(boolean capStatus) {
        throw new UnsupportedOperationException("duplicate, then perform set on duplicate object");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getLang()
     */
    public String getLang() {
        return core.getLang();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setLang(java.lang.String)
     */
    public void setLang(String lang) {
        throw new UnsupportedOperationException("duplicate, then perform set on duplicate object");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getStatusId()
     */
    public int getStatusId() {
        return core.getStatusId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setStatusId(int)
     */
    public void setStatusId(int status) {
        throw new UnsupportedOperationException("duplicate, then perform set on duplicate object");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getText()
     */
    public String getText() {
        return text;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setText(java.lang.String)
     */
    public void setText(String text) {
        this.text = text;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getTypeId()
     */
    public int getTypeId() {
        return core.getTypeId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setTypeId(int)
     */
    public void setTypeId(int typeInt) {
        throw new UnsupportedOperationException("duplicate, then perform set on duplicate object");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getVersion()
     */
    public int getVersion() {
        return core.getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setVersion(int)
     */
    public void setVersion(int version) {
        throw new UnsupportedOperationException("duplicate, then perform set on duplicate object");
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(" statusId: ");
        buff.append(nidToString(core.getStatusId()));
        buff.append(" text: ");
        buff.append(text);
        buff.append(" typeId: ");
        buff.append(nidToString(core.getTypeId()));
        buff.append(" init case sig: ");
        buff.append(core.getInitialCaseSignificant());
        buff.append(" lang: ");
        buff.append(core.getLang());
        buff.append(" pathId: ");
        buff.append(nidToString(core.getPathId()));
        buff.append(" version: ");
        buff.append(core.getVersion());
        buff.append(" (");
        buff.append(new Date(ThinVersionHelper.convert(core.getVersion())));
        buff.append(")");

        return buff.toString();
    }

    private String nidToString(int nid) {
        try {
            return ConceptBean.get(nid).getInitialText();
        } catch (IOException e) {
            return Integer.toString(nid);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.dwfa.vodb.types.I_DescriptionPart#convertIds(org.dwfa.vodb.jar.
     * I_MapNativeToNative)
     */
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        I_DescriptionPart another = (I_DescriptionPart) obj;
        return ((core.getInitialCaseSignificant() == another.getInitialCaseSignificant())
            && (core.getLang().equals(another.getLang())) && (core.getPathId() == another.getPathId())
            && (text.equals(another.getText())) && (core.getTypeId() == another.getTypeId()) && (core.getVersion() == another.getVersion()));
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { core.hashCode(), text.hashCode() });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#duplicate()
     */
    public ThinDescPart duplicate() {
        ThinDescPart newPart = new ThinDescPart();
        newPart.setPathId(core.getPathId());
        newPart.setVersion(core.getVersion());
        newPart.setStatusId(core.getStatusId());
        newPart.setText(text);
        newPart.setInitialCaseSignificant(core.getInitialCaseSignificant());
        newPart.setTypeId(core.getTypeId());
        newPart.setLang(core.getLang());
        return newPart;
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
        ThinDescPart newPart = duplicate();
        newPart.setStatusId(statusNid);
        newPart.setPathId(pathNid);
        newPart.setVersion(ThinVersionHelper.convert(time));
        return newPart;
    }

}
