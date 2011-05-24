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
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;

/**
 *
 * @author kec
 */
public class ConAttrAB extends CreateOrAmendBlueprint {

    public boolean defined;

    public ConAttrAB(
            int conceptNid, boolean defined)
            throws IOException {
        this(Ts.get().getComponent(conceptNid).getPrimUuid(),
                defined);
    }

    public ConAttrAB(
            UUID componentUuid, boolean defined) throws IOException {
        super(componentUuid);
        this.defined = defined;
    }


    public boolean validate(ConAttrVersionBI version) throws IOException {
        if (version.getStatusNid() != getStatusNid()) {
            return false;
        }
        if (version.getNid() != getComponentNid()) {
            return false;
        }
        if (version.isDefined() != defined) {
            return false;
        }
        return true;
    }
}


