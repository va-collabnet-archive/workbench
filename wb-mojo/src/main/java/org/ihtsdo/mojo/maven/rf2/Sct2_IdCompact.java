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
package org.ihtsdo.mojo.maven.rf2;

import java.io.Serializable;
import static org.ihtsdo.mojo.maven.rf2.Sct2_IdRecord.LINE_TERMINATOR;
import static org.ihtsdo.mojo.maven.rf2.Sct2_IdRecord.TAB_CHARACTER;

/**
 *
 * @author logger
 */
public class Sct2_IdCompact implements Comparable<Sct2_IdCompact>, Serializable {

    private static final long serialVersionUID = 1L;
    public long uuidMsbL;
    public long uuidLsbL;
    public long sctIdL;
    public long time;

    public Sct2_IdCompact(long uuidMsbL, long uuidLsbL, long sctIdL, long time) {
        this.uuidMsbL = uuidMsbL;
        this.uuidLsbL = uuidLsbL;
        this.sctIdL = sctIdL;
        this.time = time; //assuming that path, module, user will all be the same (for now)
    }

    /**
     * Sort order:  SCTID, UUID long
     * @param o
     * @return
     */
    @Override
    public int compareTo(Sct2_IdCompact o) {
        if (this.sctIdL < o.sctIdL) {
            return -1; // instance less than received
        } else if (this.sctIdL > o.sctIdL) {
            return 1; // instance greater than received
        } else {
            if (this.uuidMsbL < o.uuidMsbL) {
                return -1; // instance less than received
            } else if (this.uuidMsbL > o.uuidMsbL) {
                return 1; // instance greater than received
            } else {
                if (this.uuidLsbL < o.uuidLsbL) {
                    return -1; // instance less than received
                } else if (this.uuidLsbL > o.uuidLsbL) {
                    return 1; // instance greater than received
                }
            }
            return 0; // instance == received
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sctIdL);
        sb.append(TAB_CHARACTER);
        sb.append(uuidLsbL);
        sb.append(TAB_CHARACTER);
        sb.append(uuidMsbL);
        sb.append(TAB_CHARACTER);
        sb.append(time);
        sb.append(LINE_TERMINATOR);
        return sb.toString();
    }

}
