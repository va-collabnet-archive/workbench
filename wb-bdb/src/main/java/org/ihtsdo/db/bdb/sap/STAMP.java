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
import org.ihtsdo.tk.api.StampBI;

/**
 *Holds information about a stamp nid. A stamp nid contains the information about a
 * distinct combination of a status,time, author, module, and path.
 * @author akf
 */
public class STAMP implements StampBI {
    int stampNid;

    /**
     * Constructor specifying the stamp nid.
     * @param stampNid
     */
    public STAMP(int stampNid) {
        this.stampNid = stampNid;
    }

    @Override
    public int getStatusNid() {
        return Bdb.getSapDb().getStatusNid(stampNid);
    }

    @Override
    public int getAuthorNid() {
        return Bdb.getSapDb().getAuthorNid(stampNid);
    }

    @Override
    public int getModuleNid() {
        return Bdb.getSapDb().getModuleNid(stampNid);
    }

    @Override
    public int getPathNid() {
        return Bdb.getSapDb().getPathNid(stampNid);
    }

    @Override
    public long getTime() {
        return Bdb.getSapDb().getTime(stampNid);
    }

    @Override
    public int getStampNid() {
        return stampNid;
    }
    
}
