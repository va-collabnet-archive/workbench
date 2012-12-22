/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project;

import org.dwfa.ace.api.I_GetConceptData;

/**
 * The Class ListItemBean.
 */
public class ListItemBean {

    /**
     * The source fsn.
     */
    private ContextualizedDescription sourceFsn;
    /**
     * The source prefered.
     */
    private ContextualizedDescription sourcePrefered;
    /**
     * The target prefered.
     */
    private ContextualizedDescription targetPrefered;
    /**
     * The status.
     */
    private I_GetConceptData status;

    /**
     * Gets the source fsn.
     *
     * @return the source fsn
     */
    public ContextualizedDescription getSourceFsn() {
        return sourceFsn;
    }

    /**
     * Sets the source fsn.
     *
     * @param sourceFsn the new source fsn
     */
    public void setSourceFsn(ContextualizedDescription sourceFsn) {
        this.sourceFsn = sourceFsn;
    }

    /**
     * Gets the source prefered.
     *
     * @return the source prefered
     */
    public ContextualizedDescription getSourcePrefered() {
        return sourcePrefered;
    }

    /**
     * Sets the source prefered.
     *
     * @param sourcePrefered the new source prefered
     */
    public void setSourcePrefered(ContextualizedDescription sourcePrefered) {
        this.sourcePrefered = sourcePrefered;
    }

    /**
     * Gets the target prefered.
     *
     * @return the target prefered
     */
    public ContextualizedDescription getTargetPrefered() {
        return targetPrefered;
    }

    /**
     * Sets the target prefered.
     *
     * @param targetPref the new target prefered
     */
    public void setTargetPrefered(ContextualizedDescription targetPref) {
        this.targetPrefered = targetPref;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public I_GetConceptData getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(I_GetConceptData status) {
        this.status = status;
    }
}
