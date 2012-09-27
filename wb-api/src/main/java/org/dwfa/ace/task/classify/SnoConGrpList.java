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
package org.dwfa.ace.task.classify;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;

public class SnoConGrpList extends ArrayList<SnoConGrp> {

    private static final long serialVersionUID = 1L;

    public SnoConGrpList() {
        super();
    }

    /**
     * Counts total concepts in SnoConGrpList
     * 
     * @return <code><b>int</b></code> - total concepts
     */
    public int count() {
        int count = 0;
        int max = this.size();
        for (int i = 0; i < max; i++) {
            count += this.get(i).size();
        }
        return count;
    }

    // dump equivalent concepts to file
    public static void dumpSnoConGrpList(SnoConGrpList scgl, String fName) {
        I_TermFactory tf = Terms.get();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(fName));
            // "COMPARE" UUIDs, //NIDs, Initial Text
            int setNumber = 1;
            for (SnoConGrp scg : scgl) {
                for (SnoCon sc : scg) {
                    I_GetConceptData c = tf.getConcept(sc.id);
                    bw.write(c.getPrimUuid().toString() + "\tset=\t" + setNumber + "\t");
                    bw.write(c.getInitialText() + "\r\n");
                }
                setNumber++;
            }
            bw.flush();
            bw.close();
        } catch (TerminologyException ex) {
            Logger.getLogger(SnoConGrpList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SnoConGrpList.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(SnoConGrpList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
