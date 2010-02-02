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
package org.dwfa.dto;

import java.util.Date;
import java.util.UUID;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

/**
 * Concept details for exporting.
 */
public class ConceptDto {
    private UUID conceptId;
    private UUID pathId;
    private UUID status;
    private Date dateTime;
    private String fullySpecifiedName;
    private String ctv3Id;
    private String snomedId;
    private boolean isActive;
    private boolean isPrimative;
    private NAMESPACE namespace;
    private TYPE type;

    /**
     * Bean constructor.
     */
    public ConceptDto() {

    }

    /**
     * @return the conceptId
     */
    public UUID getConceptId() {
        return conceptId;
    }

    /**
     * @param conceptId the conceptId to set
     */
    public void setConceptId(UUID conceptId) {
        this.conceptId = conceptId;
    }

    /**
     * @return the pathId
     */
    public UUID getPathId() {
        return pathId;
    }

    /**
     * @param pathId the pathId to set
     */
    public void setPathId(UUID pathId) {
        this.pathId = pathId;
    }

    /**
     * @return the status
     */
    public UUID getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatusId(UUID status) {
        this.status = status;
    }

    /**
     * @return the dateTime
     */
    public Date getDateTime() {
        return dateTime;
    }

    /**
     * @param dateTime the dateTime to set
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * @return the fullySpecifiedName
     */
    public String getFullySpecifiedName() {
        return fullySpecifiedName;
    }

    /**
     * @param fullySpecifiedName the fullySpecifiedName to set
     */
    public void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
    }

    /**
     * @return the ctv3Id
     */
    public String getCtv3Id() {
        return ctv3Id;
    }

    /**
     * @param ctv3Id the ctv3Id to set
     */
    public void setCtv3Id(String ctv3Id) {
        this.ctv3Id = ctv3Id;
    }

    /**
     * @return the snomedId
     */
    public String getSnomedId() {
        return snomedId;
    }

    /**
     * @param snomedId the snomedId to set
     */
    public void setSnomedId(String snomedId) {
        this.snomedId = snomedId;
    }

    /**
     * @return the isActive
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * @param isActive the isActive to set
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * @return the isPrimative
     */
    public boolean isPrimative() {
        return isPrimative;
    }

    /**
     * @param isPrimative the isPrimative to set
     */
    public void setPrimative(boolean isPrimative) {
        this.isPrimative = isPrimative;
    }

    /**
     * @return the namespace
     */
    public NAMESPACE getNamespace() {
        return namespace;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(NAMESPACE namespace) {
        this.namespace = namespace;
    }

    /**
     * @return the type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(TYPE type) {
        this.type = type;
    }
}
