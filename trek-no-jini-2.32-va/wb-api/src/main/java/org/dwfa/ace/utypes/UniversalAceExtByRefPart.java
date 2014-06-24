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
package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPart implements Serializable, I_VersionComponent {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private Collection<UUID> pathUid;
    private long time;
    private Collection<UUID> statusUid;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(pathUid);
        out.writeLong(time);
        out.writeObject(statusUid);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            pathUid = (Collection<UUID>) in.readObject();
            time = in.readLong();
            statusUid = (Collection<UUID>) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public Collection<UUID> getPathUid() {
        return pathUid;
    }

    public void setPathUid(Collection<UUID> pathUid) {
        this.pathUid = pathUid;
    }

    public Collection<UUID> getStatusUid() {
        return statusUid;
    }

    public void setStatusUid(Collection<UUID> statusUid) {
        this.statusUid = statusUid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Collection<UUID> getPathId() {
        return getPathUid();
    }

    public Collection<UUID> getStatusId() {
        return getStatusUid();
    }

    public void setPathId(Collection<UUID> pathId) {
        setPathUid(pathId);
    }

    public void setStatusId(Collection<UUID> status) {
        setStatusUid(status);
    }

}
