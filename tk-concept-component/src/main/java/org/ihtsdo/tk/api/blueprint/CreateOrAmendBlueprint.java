/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api.blueprint;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 *
 * @author kec
 */
public abstract class CreateOrAmendBlueprint {

    private static UUID currentStatusUuid = null;
    private static UUID retiredStatusUuid = null;
    private UUID componentUuid;
    private UUID statusUuid;

    public CreateOrAmendBlueprint(UUID componentUuid) {
        if (currentStatusUuid == null) {
            try {
                if (Ts.get().usesRf2Metadata()) {
                    currentStatusUuid = SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid();
                    retiredStatusUuid = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid();;
                } else {
                    currentStatusUuid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid();
                    retiredStatusUuid = SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        statusUuid = currentStatusUuid;
        this.componentUuid = componentUuid;
    }

    protected String getPrimoridalUuidStr(int nid)
            throws IOException, InvalidCAB {
        ComponentBI component = Ts.get().getComponent(nid);
        if (component != null) {
            return component.getPrimUuid().toString();
        }
        List<UUID> uuids = Ts.get().getUuidsForNid(nid);
        if (uuids.size() == 1) {
            return uuids.get(0).toString();
        }
        throw new InvalidCAB("Can't find primordialUuid for: " + component);
    }

    protected String getPrimoridalUuidStr(UUID uuid)
            throws IOException, InvalidCAB {
        ComponentBI component = Ts.get().getComponent(uuid);
        if (component != null) {
            return component.getPrimUuid().toString();
        }
        List<UUID> uuids = Ts.get().getUuidsForNid(component.getNid());
        if (uuids.size() == 1) {
            return uuids.get(0).toString();
        }
        throw new InvalidCAB("Can't find primordialUuid for: " + component);
    }

    public UUID getComponentUuid() {
        return componentUuid;
    }

    public void setComponentUuid(UUID componentUuid) {
        this.componentUuid = componentUuid;
    }

    public int getComponentNid() throws IOException {
        return Ts.get().getNidForUuids(componentUuid);
    }

    public UUID getStatusUuid() {
        return statusUuid;
    }

    public int getStatusNid() throws IOException {
        return Ts.get().getNidForUuids(statusUuid);
    }

    public void setStatusUuid(UUID statusUuid) {
        this.statusUuid = statusUuid;
    }

    public void setCurrent() {
        this.statusUuid = currentStatusUuid;
    }

    public void setRetired() {
        this.statusUuid = retiredStatusUuid;
    }
}
