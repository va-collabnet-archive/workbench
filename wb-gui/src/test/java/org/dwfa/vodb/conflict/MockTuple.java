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

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class MockTuple implements I_AmTuple {

    int version;
    int id;
    private int pathId;
    private int statusId = 0;
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

            public int getNid() {
                return id;
            }


            @Override
            public boolean promote(I_Position viewPosition, PathSetReadOnly pomotionPaths, I_IntSet allowedStatus,
                    PRECEDENCE precedence) throws IOException, TerminologyException {
                throw new UnsupportedOperationException();
            }

        };
    }

    public I_AmPart getMutablePart() {
        throw new UnsupportedOperationException();
    }

    public ArrayIntList getPartComponentNids() {
        throw new UnsupportedOperationException();
    }

    public int getPathId() {
        return pathId;
    }

    public int getStatusId() {
        return this.statusId;
    }

    public int getVersion() {
        return version;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    public void setStatusId(int idStatus) {
        this.statusId = idStatus;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public I_AmPart duplicate() {
        return new MockTuple(id, pathId, version, value);
    }

    public int getNid() {
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

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { id, pathId, version });
    }

    public String toString() {
        return "TestTuple{id=" + id + ",pathid=" + pathId + ",version=" + version + ",value='" + value + "'}";
    }

    public int getPositionId() {
        throw new UnsupportedOperationException();
    }

    public void setPositionId(int pid) {
        throw new UnsupportedOperationException();
    }

	@Override
	public long getTime() {
		return ThinVersionHelper.convert(getVersion());
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		I_AmPart newPart = duplicate();
		newPart.setStatusId(statusNid);
		newPart.setPathId(pathNid);
		newPart.setTime(time);
		return newPart;
	}

	@Override
	public void setTime(long value) {
	     throw new UnsupportedOperationException();
	}

}
