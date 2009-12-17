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
package org.dwfa.vodb.conflict;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;

public class MockTuple implements I_AmTuple {

    int version;
    int id;
    private int pathId;
    private final String value;

    public MockTuple(int id, int pathId, int version, String value) {
        this.version = version;
        this.id = id;
        this.pathId = pathId;
        this.value = value;
    }

    public I_AmTermComponent getFixedPart() {
        return new I_AmTermComponent() {

            public int getTermComponentId() {
                return id;
            }

        };
    }

    public I_AmPart getPart() {
        throw new UnsupportedOperationException();
    }

    public ArrayIntList getPartComponentNids() {
        throw new UnsupportedOperationException();
    }

    public int getPathId() {
        return pathId;
    }

    public int getStatusId() {
        throw new UnsupportedOperationException();
    }

    public int getVersion() {
        return version;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    public void setStatusId(int idStatus) {
        throw new UnsupportedOperationException();
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public I_AmPart duplicate() {
        return new MockTuple(id, pathId, version, value);
    }

    public int getFixedPartId() {
        return id;
    }

    public boolean equals(Object o) {
        if (o instanceof MockTuple) {
            MockTuple testTuple = ((MockTuple) o);
            return value.equals(testTuple.value) && id == testTuple.id && pathId == testTuple.pathId
                && version == testTuple.version;
        }
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return "TestTuple{id=" + id + ",pathid=" + pathId + ",version=" + version + ",value='" + value + "'}";
    }

}
