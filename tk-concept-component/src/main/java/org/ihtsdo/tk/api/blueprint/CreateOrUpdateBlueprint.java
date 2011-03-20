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

/**
 *
 * @author kec
 */
public abstract class CreateOrUpdateBlueprint {


    protected String getPrimoridalUuidStr(int nid)
            throws IOException, InvalidCUB {
        ComponentBI component = Ts.get().getComponent(nid);
        if (component!= null) {
            return component.getPrimUuid().toString();
        }
        List<UUID> uuids = Ts.get().getUuidsForNid(nid);
        if (uuids.size() == 1) {
            return uuids.get(0).toString();
        }
        throw new InvalidCUB("Can't find primordialUuid for: " + component);
    }

    protected String getPrimoridalUuidStr(UUID uuid)
            throws IOException, InvalidCUB {
        ComponentBI component = Ts.get().getComponent(uuid);
        if (component!= null) {
            return component.getPrimUuid().toString();
        }
        List<UUID> uuids = Ts.get().getUuidsForNid(component.getNid());
        if (uuids.size() == 1) {
            return uuids.get(0).toString();
        }
        throw new InvalidCUB("Can't find primordialUuid for: " + component);
    }

}
