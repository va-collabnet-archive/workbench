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
package org.ihtsdo.mojo.maven.rf1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.maven.plugin.MojoFailureException;

public class Rf1TextDef implements Comparable<Object> {

    private static final String TAB_CHARACTER = "\t";

    long conceptSid;
    String snomedId;
    String fsn;
    String definition;

    int status; // current or retired

    public Rf1TextDef(long tmpConceptSid, String tmpSnomedId, String tmpFsn, String tmpDefinition) {
        this.conceptSid = tmpConceptSid;
        this.snomedId = tmpSnomedId;
        this.fsn = tmpFsn;
        this.definition = tmpDefinition;

        this.status = 0; // CURRENT is 0 
    }

    @Override
    public int compareTo(Object o) {
        Rf1TextDef tmp = (Rf1TextDef) o;

        if (this.conceptSid < tmp.conceptSid)
            return -1; // instance less than received
        else if (this.conceptSid > tmp.conceptSid)
            return 1; // instance greater than received
        else if (this.definition.compareTo(tmp.definition) < 0)
            return -1; // instance less than received
        else if (this.definition.compareTo(tmp.definition) > 0)
            return 1; // instance greater than received
        return 0; // instance == received
    }

    public static Rf1TextDef[] parseFile(RF1File rf1) throws IOException, MojoFailureException {

        int lineCount = RF1File.countFileLines(rf1);
        Rf1TextDef[] a = new Rf1TextDef[lineCount];

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf1.file),
                "UTF-8"));
        int members = 0;

        int CONCEPT_SID = 0;
        int SNOMED_ID = 1;
        int FSM = 2;
        int DEFINITION = 3;

        // Header row
        br.readLine();

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);
            long tmpConceptSid = Long.parseLong(line[CONCEPT_SID]);
            String tmpSnomedId = line[SNOMED_ID];
            String tmpFsn = line[FSM];
            String tmpDefinition = line[DEFINITION];
            
            a[members] = new Rf1TextDef(tmpConceptSid, tmpSnomedId, tmpFsn, tmpDefinition);

            members++;
        }
        br.close();

        Arrays.sort(a);
        
        return a;
    }
    
    public String toString() {
        return ( this.conceptSid + " :: " + this.definition);
    }

}
