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
package org.ihtsdo.mojo.maven.sct;

import java.io.Serializable;

class SctXConRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final String TAB_CHARACTER = "\t";

    // RECORD FIELDS
    long id; // CONCEPTID
    int status; // CONCEPTSTATUS
    String ctv3id; // CTV3ID
    String snomedrtid; // SNOMEDID (SNOMED RT ID)
    int isprimitive; // ISPRIMITIVE
    int xPath;
    int xRevision;

    public SctXConRecord(long i, int s, String ctv, String rt, int p) {
        id = i;
        status = s;
        ctv3id = ctv;
        snomedrtid = rt;
        isprimitive = p;
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctXConRecord tmp = (SctXConRecord) obj;
        if (this.id < tmp.id) {
            return -1; // instance less than received
        } else if (this.id > tmp.id) {
            return 1; // instance greater than received
        } else {
            if (this.xPath < tmp.xPath) {
                return -1; // instance less than received
            } else if (this.xPath > tmp.xPath) {
                return 1; // instance greater than received
            } else {
                if (this.xRevision < tmp.xRevision) {
                    return -1; // instance less than received
                } else if (this.xRevision > tmp.xRevision) {
                    return 1; // instance greater than received
                } else {
                    return 0; // instance == received
                }
            }
        }
    }

    // Create string to show some input fields for exception reporting
    public String toString() {
        return id + TAB_CHARACTER + status + TAB_CHARACTER + isprimitive;
    }
}
