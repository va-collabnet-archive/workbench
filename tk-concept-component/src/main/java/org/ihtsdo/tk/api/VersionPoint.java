/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api;

import org.ihtsdo.tk.Ts;

// TODO: Auto-generated Javadoc
/**
 * The Class VersionPoint.
 *
 * @author kec
 */
public class VersionPoint implements VersionPointBI {
    
    /** The stamp. */
    private int stamp;

    /**
     * Instantiates a new version point.
     *
     * @param stamp the stamp
     */
    public VersionPoint(int stamp) {
        this.stamp = stamp;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.VersionPointBI#getTime()
     */
    @Override
    public long getTime() {
        return Ts.get().getTimeForStampNid(stamp);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.VersionPointBI#getPathNid()
     */
    @Override
    public int getPathNid() {
        return Ts.get().getPathNidForStampNid(stamp);
    }

    /**
     * Gets the status nid.
     *
     * @return the status nid
     */
    public int getStatusNid() {
        return Ts.get().getStatusNidForStampNid(stamp);
    }
}
