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
package org.dwfa.ace.api;

import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;

public interface I_HostConceptPlugins extends I_ContainTermComponent {

    public static final String SHOW_HISTORY = "showHistory";

    public static final String USE_PREFS = "usePrefs";

    public enum HOST_ENUM {
        CONCEPT_PANEL_L1, CONCEPT_PANEL_R1, CONCEPT_PANEL_R2, CONCEPT_PANEL_R3, CONCEPT_PANEL_R4, CONCEPT_PANEL_LIST_VIEW, CONCPET_PANEL_DATA_CHECK, REFSET_SPEC;
    }

    public enum TOGGLES {
        ID(UUID.fromString("f0e28620-7495-11de-8a39-0800200c9a66"), 1), ATTRIBUTES(UUID.fromString("f0e28621-7495-11de-8a39-0800200c9a66"), 1), AU_DIALECT(UUID.fromString("f0e28622-7495-11de-8a39-0800200c9a66"), 1), UK_DIALECT(UUID.fromString("f0e28623-7495-11de-8a39-0800200c9a66"), 1), USA_DIALECT(UUID.fromString("f0e28624-7495-11de-8a39-0800200c9a66"), 1), NZ_DIALECT(UUID.fromString("f0e28625-7495-11de-8a39-0800200c9a66"), 1), CA_DIALECT(UUID.fromString("f0e28626-7495-11de-8a39-0800200c9a66"), 1), DESCRIPTIONS(UUID.fromString("f0e28627-7495-11de-8a39-0800200c9a66"), 1), SOURCE_RELS(UUID.fromString("f0e28628-7495-11de-8a39-0800200c9a66"), 1), DEST_RELS(UUID.fromString("f0e28629-7495-11de-8a39-0800200c9a66"), 1), LINEAGE(UUID.fromString("f0e2862a-7495-11de-8a39-0800200c9a66"), 1), LINEAGE_GRAPH(UUID.fromString("f0e2862b-7495-11de-8a39-0800200c9a66"), 1), IMAGE(UUID.fromString("f0e2862c-7495-11de-8a39-0800200c9a66"), 1), CONFLICT(UUID.fromString("f0e2862d-7495-11de-8a39-0800200c9a66"), 1), STATED_INFERRED(UUID.fromString("f0e2862e-7495-11de-8a39-0800200c9a66"), 1), PREFERENCES(UUID.fromString("f0e28631-7495-11de-8a39-0800200c9a66"), 1), HISTORY(UUID.fromString("f0e28632-7495-11de-8a39-0800200c9a66"), 1), REFSETS(UUID.fromString("f0e28633-7495-11de-8a39-0800200c9a66"), 1);

        UUID pluginId;
        private int controlId;

        public UUID getPluginId() {
            return pluginId;
        }

        public int getControlId() {
            return controlId;
        }

        private TOGGLES(UUID pluginId, int controlId) {
            this.pluginId = pluginId;
            this.controlId = controlId;
        }

        public static TOGGLES fromId(UUID id) {
            for (TOGGLES t : values()) {
                if (t.pluginId == id) {
                    return t;
                }
            }
            return null;
        }
    };

    public enum REFSET_TYPES {
        BOOLEAN("boolean", I_ExtendByRefPartBoolean.class), 
        CONCEPT("concept", I_ExtendByRefPartCid.class), 
        CON_INT("con int", I_ExtendByRefPartCidInt.class), 
        STRING("string", I_ExtendByRefPartStr.class), 
        INTEGER("integer", I_ExtendByRefPartInt.class);

        private String interfaceName;

        private Class<? extends I_ExtendByRefPart> partClass;

        private REFSET_TYPES(String interfaceName, Class<? extends I_ExtendByRefPart> partClass) {
            this.interfaceName = interfaceName;
            this.partClass = partClass;
        }
        
        public String getInterfaceName() {
            return interfaceName;
        }

        public Class<? extends I_ExtendByRefPart> getPartClass() {
            return partClass;
        }

    
    }

    public enum LINK_TYPE {
        UNLINKED, SEARCH_LINK, TREE_LINK, DATA_CHECK_LINK, LIST_LINK
    };

    public boolean getShowHistory();

    public boolean getShowRefsets();

    public boolean getUsePrefs();

    public I_GetConceptData getHierarchySelection();

    public void unlink();

    public void setToggleState(TOGGLES toggle, boolean state);

    public boolean getToggleState(TOGGLES toggle);

    public void setAllTogglesToState(boolean state);

    public void setLinkType(LINK_TYPE link);

}
