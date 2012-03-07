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
package org.ihtsdo.helper.bdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;

/**
 *
 * @author marc
 */
public class MultiEditorContradictionCase {

    private int cNid; // concept with contradiction
    private ArrayList<String> cases; // reported cases
    // DETAILS
    private HashMap<UUID, String> authTimeMapComputed; // computed from getAllSapNids()
    private HashMap<UUID, String> authTimeMapMissing; // missing from getAllSapNids()

    public MultiEditorContradictionCase(int cNid, ArrayList<String> cases) {
        this.cNid = cNid;
        this.cases = cases;
    }

    public int getConceptNid() {
        return cNid;
    }

    public void setAuthTimeMapComputed(HashMap<UUID, String> authTimeMapComputed) {
        this.authTimeMapComputed = authTimeMapComputed;
    }

    public void setAuthTimeMapMissing(HashMap<UUID, String> authTimeMapMissing) {
        this.authTimeMapMissing = authTimeMapMissing;
    }

    public ArrayList<String> getCases() {
        return cases;
    }

    @Override
    public String toString() {
        TerminologyStoreDI ts = Ts.get();
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(ts.getConcept(cNid).toUserString());


            return sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(MultiEditorContradictionCase.class.getName()).log(Level.SEVERE, null, ex);
            return sb.toString();
        }
    }

    public String toStringLong() {
        TerminologyStoreDI ts = Ts.get();
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("\r\n*** CONTRADICTION CASE ***\r\n   Concept: ");
            sb.append(ts.getConcept(cNid).getPrimUuid().toString());
            sb.append(" ");
            sb.append(ts.getConcept(cNid).toUserString());


            return sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(MultiEditorContradictionCase.class.getName()).log(Level.SEVERE, null, ex);
            return sb.toString();
        }
    }
}
