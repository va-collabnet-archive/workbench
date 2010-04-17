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
public class BaseConceptDto {
    private boolean latest = false;
    private UUID pathId;
    private UUID status;
    private String statusCode;
    private Date dateTime;
    private boolean isActive = false;;
    private boolean islive = false;
    private NAMESPACE namespace;
    private TYPE type;

    /**
     * Bean constructor.
     */
    public BaseConceptDto() {

    }

    /**
     * @return the latest
     */
    public boolean isLatest() {
        return latest;
    }

    /**
     * @param latest the latest to set
     */
    public void setLatest(boolean latest) {
        this.latest = latest;
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
    public UUID getStatusId() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatusId(UUID status) {
        this.status = status;
    }

    /**
     * @return the statusCode
     */
    public final String getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public final void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
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
     * @return the islive
     */
    public final boolean isLive() {
        return islive;
    }

    /**
     * @param islive the islive to set
     */
    public final void setLive(boolean islive) {
        this.islive = islive;
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
