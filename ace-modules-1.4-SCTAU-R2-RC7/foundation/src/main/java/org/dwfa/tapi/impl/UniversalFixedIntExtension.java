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
package org.dwfa.tapi.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_ExtendLocally;
import org.dwfa.tapi.I_ExtendUniversally;
import org.dwfa.tapi.I_ExtendWithInteger;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;

public class UniversalFixedIntExtension implements I_ExtendWithInteger, I_ExtendUniversally {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Collection<UUID> memberUids;
    private int intExtension;

    public UniversalFixedIntExtension(Collection<UUID> memberUids, int intExtension) {
        super();
        this.memberUids = memberUids;
        this.intExtension = intExtension;
    }

    public int getIntExtension() {
        return intExtension;
    }

    public Collection<UUID> getUids() {
        return memberUids;
    }

    public I_ExtendLocally localize() throws IOException, TerminologyException {
        return new LocalFixedIntExtension(LocalFixedTerminology.getStore().getNid(memberUids), intExtension);
    }

    public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType,
            I_StoreUniversalFixedTerminology extensionServer) throws IOException, TerminologyException {
        return extensionServer.getUniversalExtension(this, extensionType);
    }

    public boolean isUniversal() {
        return true;
    }

    public PropertyDescriptor[] getDataDescriptors() throws IntrospectionException {
        return new PropertyDescriptor[] { new PropertyDescriptor("intExtension", this.getClass(), "getIntExtension",
            null) };
    }

}
