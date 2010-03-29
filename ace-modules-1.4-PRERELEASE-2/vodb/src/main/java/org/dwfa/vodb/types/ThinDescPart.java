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

public class ThinDescPart implements I_DescriptionPart {

    private int pathId;
    private int version;
    private int statusId;
    private String text;
    private boolean initialCaseSignificant;
    private int typeId;
    private String lang;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(3);
        partComponentNids.add(pathId);
        partComponentNids.add(statusId);
        partComponentNids.add(typeId);
        return partComponentNids;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_DescriptionPart#hasNewData(org.dwfa.vodb.types.
     * ThinDescPart)
     */
    public boolean hasNewData(I_DescriptionPart another) {
        return ((this.pathId != another.getPathId()) || (this.statusId != another.getStatusId()) || ((this.text.equals(another.getText()) == false)
            || (this.initialCaseSignificant != another.getInitialCaseSignificant())
            || (this.typeId != another.getTypeId()) || ((this.lang.equals(another.getLang()) == false))));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getPathId()
     */
    public int getPathId() {
        return pathId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setPathId(int)
     */
    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getInitialCaseSignificant()
     */
    public boolean getInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_DescriptionPart#setInitialCaseSignificant(boolean)
     */
    public void setInitialCaseSignificant(boolean capStatus) {
        this.initialCaseSignificant = capStatus;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getLang()
     */
    public String getLang() {
        return lang;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setLang(java.lang.String)
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getStatusId()
     */
    public int getStatusId() {
        return statusId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setStatusId(int)
     */
    public void setStatusId(int status) {
        this.statusId = status;
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
        return typeId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setTypeId(int)
     */
    public void setTypeId(int typeInt) {
        this.typeId = typeInt;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#getVersion()
     */
    public int getVersion() {
        return version;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_DescriptionPart#setVersion(int)
     */
    public void setVersion(int version) {
        this.version = version;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(" statusId: ");
        buff.append(nidToString(statusId));
        buff.append(" text: ");
        buff.append(text);
        buff.append(" typeId: ");
        buff.append(nidToString(typeId));
        buff.append(" init case sig: ");
        buff.append(initialCaseSignificant);
        buff.append(" lang: ");
        buff.append(lang);
        buff.append(" pathId: ");
        buff.append(nidToString(pathId));
        buff.append(" version: ");
        buff.append(version);
        buff.append(" (");
        buff.append(new Date(ThinVersionHelper.convert(version)));
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
        pathId = jarToDbNativeMap.get(pathId);
        statusId = jarToDbNativeMap.get(statusId);
        typeId = jarToDbNativeMap.get(typeId);
    }

    @Override
    public boolean equals(Object obj) {
        I_DescriptionPart another = (I_DescriptionPart) obj;
        return ((initialCaseSignificant == another.getInitialCaseSignificant()) && (lang.equals(another.getLang()))
            && (pathId == another.getPathId()) && (text.equals(another.getText())) && (typeId == another.getTypeId()) && (version == another.getVersion()));
    }

    @Override
    public int hashCode() {
        int bhash = 0;
        if (initialCaseSignificant) {
            bhash = 1;
        }
        return HashFunction.hashCode(new int[] { bhash, lang.hashCode(), pathId, statusId, text.hashCode(), typeId,
                                                version });
    }

    public ThinDescPart duplicate() {
        ThinDescPart newPart = new ThinDescPart();
        newPart.pathId = pathId;
        newPart.version = version;
        newPart.statusId = statusId;
        newPart.text = text;
        newPart.initialCaseSignificant = initialCaseSignificant;
        newPart.typeId = typeId;
        newPart.lang = lang;
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
        return ThinVersionHelper.convert(version);
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
