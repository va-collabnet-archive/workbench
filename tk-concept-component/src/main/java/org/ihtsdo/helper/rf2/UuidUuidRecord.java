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
package org.ihtsdo.helper.rf2;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author logger
 */
public class UuidUuidRecord
        implements Comparable<UuidUuidRecord>, Serializable {

    private static final long serialVersionUID = 1L;
    public UUID uuidComputed;
    public UUID uuidDeclared;

    public UuidUuidRecord(UUID uuidComputed, UUID uuidDeclared) {
        this.uuidComputed = uuidComputed;
        this.uuidDeclared = uuidDeclared;
    }

    /**
     * Sort order: [SCTID_COMPUTED_UUID, DECLARED_ASSIGNED_UUID]
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(UuidUuidRecord o) {
        if (this.uuidComputed.compareTo(o.uuidComputed) < 0) {
            return -1; // instance less than received
        } else if (this.uuidComputed.compareTo(o.uuidComputed) > 0) {
            return 1; // instance greater than received
        } else {
            if (this.uuidDeclared.compareTo(o.uuidDeclared) < 0) {
                return -1; // instance less than received
            } else if (this.uuidDeclared.compareTo(o.uuidDeclared) > 0) {
                return 1; // instance greater than received
            } else {
                return 0; // instance == received
            }
        }
    }

}
