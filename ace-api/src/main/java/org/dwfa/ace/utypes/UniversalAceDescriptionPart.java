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
package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceDescriptionPart implements Serializable, I_VersionComponent {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private Collection<UUID> pathId;
    private long time;
    private Collection<UUID> statusId;
    private String text;
    private boolean initialCaseSignificant;
    private Collection<UUID> typeId;
    private String lang;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " text: " + text + " initialCaseSignificant: "
            + initialCaseSignificant + " typeId: " + typeId + " lang: " + lang + " status: " + statusId + " path: "
            + pathId + " time: " + time;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_DescriptionPart#getPathId()
     */
    public Collection<UUID> getPathId() {
        return pathId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_DescriptionPart#setPathId(int)
     */
    public void setPathId(Collection<UUID> pathId) {
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
    public Collection<UUID> getStatusId() {
        return statusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_DescriptionPart#setStatusId(int)
     */
    public void setStatusId(Collection<UUID> status) {
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
    public Collection<UUID> getTypeId() {
        return typeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_DescriptionPart#setTypeId(int)
     */
    public void setTypeId(Collection<UUID> typeInt) {
        this.typeId = typeInt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_DescriptionPart#getVersion()
     */
    public long getTime() {
        return time;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_DescriptionPart#setVersion(int)
     */
    public void setTime(long version) {
        this.time = version;
    }

}
