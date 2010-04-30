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
package org.ihtsdo.mojo.mojo.memrefset.mojo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChangeSet {

    private Long time;
    private UUID pathUUID;
    private UUID refsetUUID;
    private String refsetName;
    private final List<RefSet> refSetList;

    public ChangeSet() {
        refSetList = new ArrayList<RefSet>();
    }

    public void setPathUUID(final UUID pathUUID) {
        this.pathUUID = pathUUID;
    }

    public void setRefsetUUID(final UUID refsetUUID) {
        this.refsetUUID = refsetUUID;
    }

    public void add(final RefSet rs) {
        refSetList.add(rs);
    }

    public UUID getPathUUID() {
        return pathUUID;
    }

    public UUID getRefsetUUID() {
        return refsetUUID;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(final Long time) {
        this.time = time;
    }

    public List<RefSet> getRefsetList() {
        return refSetList;
    }

    public String getRefsetName() {
        return refsetName;
    }

    public void setRefsetName(final String refsetName) {
        this.refsetName = refsetName;
    }

    @Override
    public String toString() {
        return "ChangeSet{" + "time=" + time + ", pathUUID=" + pathUUID + ", refsetUUID=" + refsetUUID
            + ", refsetName='" + refsetName + '\'' + ", refSetList=" + refSetList + '}';
    }
}
