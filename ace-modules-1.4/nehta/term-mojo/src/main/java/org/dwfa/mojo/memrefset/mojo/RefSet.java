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
package org.dwfa.mojo.memrefset.mojo;

import java.util.UUID;

public final class RefSet {

    private UUID componentUUID;
    private UUID statusUUID;
    private UUID conceptUUID;
    private UUID memberUUID;

    private String componentDescription;
    private String conceptDescription;
    private String statusDescription;

    public void setComponentUUID(final UUID componentUUID) {
        this.componentUUID = componentUUID;
    }

    public void setStatusUUID(final UUID statusUUID) {
        this.statusUUID = statusUUID;
    }

    public void setConceptUUID(final UUID conceptUUID) {
        this.conceptUUID = conceptUUID;
    }

    public UUID getComponentUUID() {
        return componentUUID;
    }

    public UUID getStatusUUID() {
        return statusUUID;
    }

    public UUID getConceptUUID() {
        return conceptUUID;
    }

    public UUID getMemberUUID() {
        return memberUUID;
    }

    public String getComponentDescription() {
        return componentDescription;
    }

    public String getConceptDescription() {
        return conceptDescription;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setMemberUUID(final UUID memberUUID) {
        this.memberUUID = memberUUID;
    }

    public void setComponentDescription(final String componentDescription) {
        this.componentDescription = componentDescription;
    }

    public void setConceptDescription(final String conceptDescription) {
        this.conceptDescription = conceptDescription;
    }

    public void setStatusDescription(final String statusDescription) {
        this.statusDescription = statusDescription;
    }

    @Override
    public String toString() {
        return "RefSet{" + "componentUUID=" + componentUUID + ", statusUUID=" + statusUUID + ", conceptUUID="
            + conceptUUID + ", memberUUID=" + memberUUID + ", componentDescription='" + componentDescription + '\''
            + ", conceptDescription='" + conceptDescription + '\'' + ", statusDescription='" + statusDescription + '\''
            + '}';
    }
}
