/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.dwfa.ace.reporting;

import java.util.ArrayList;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;

/**
 * 
 * @goal change-report
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class ChangeReport extends ChangeReportBase {
    
    public ChangeReport(String v1, String v2){
        super(v1, v2);
    }

    @Override
    protected void processConcepts() throws Exception {
        I_TermFactory tf = Terms.get();
        AceLog.getAppLog().info("Getting concepts in DFS order.");
        ArrayList<Integer> all_concepts = getAllConcepts();
        AceLog.getAppLog().info("Processing: " + all_concepts.size());
        long beg = System.currentTimeMillis();
        int i = 0;
        for (int id : all_concepts) {
            I_GetConceptData c = tf.getConcept(id);
            i++;
            processConcept(c, i, beg);
        }
    }
}
