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

public class DatabaseSetupConfig {

    public enum CORE_DB_TYPE {
        LAZY, CON_DESC, CON_DESC_REL, CON_COMPDESC_REL, CON_DESCMAP_REL
    };

    public enum ID_DB_TYPE {
        UUID_MAP_PRIMARY, UUID_MAP_SECONDARY, UUID_MAP_PRIMARY_WITH_CORES
    };

    private CORE_DB_TYPE coreDbType = CORE_DB_TYPE.CON_DESCMAP_REL;

    private ID_DB_TYPE idDbType = ID_DB_TYPE.UUID_MAP_PRIMARY_WITH_CORES;

    public CORE_DB_TYPE getCoreDbType() {
        return coreDbType;
    }

    public void setCoreDbType(CORE_DB_TYPE coreDbType) {
        this.coreDbType = coreDbType;
    }

    public void setCoreDbTypeStr(String coreDbTypeStr) {
        this.coreDbType = CORE_DB_TYPE.valueOf(coreDbTypeStr);
    }

    public String getCoreDbTypeStr() {
        return coreDbType.name();
    }

    public ID_DB_TYPE getIdDbType() {
        return idDbType;
    }

    public void setIdDbType(ID_DB_TYPE idDbType) {
        this.idDbType = idDbType;
    }

    public void setIdDbTypeStr(String idDbTypeStr) {
        this.idDbType = ID_DB_TYPE.valueOf(idDbTypeStr);
    }

    public String getIdDbTypeStr() {
        return idDbType.name();
    }

}
