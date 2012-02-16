/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.db.bdb.sap;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.SapBI;

/**
 *Holds information about a sap nid. A sap nid contains the information about a
 * distinct combination of a status, author, path, time, and (eventually) module.
 * Note that module is not currently implemented.
 * @author akf
 */
public class SAP implements SapBI {
    int sapNid;

    /**
     * Constructor specifying the sap nid.
     * @param sapNid
     */
    public SAP(int sapNid) {
        this.sapNid = sapNid;
    }

    @Override
    public int getStatusNid() {
        return Bdb.getSapDb().getStatusNid(sapNid);
    }

    @Override
    public int getAuthorNid() {
        return Bdb.getSapDb().getAuthorNid(sapNid);
    }

    @Override
    public int getModuleNid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPathNid() {
        return Bdb.getSapDb().getPathNid(sapNid);
    }

    @Override
    public long getTime() {
        return Bdb.getSapDb().getTime(sapNid);
    }

    @Override
    public int getSapNid() {
        return sapNid;
    }
    
}
