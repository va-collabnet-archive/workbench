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

public class Rf1HistoryTableReference implements Comparable<Object> {

    long componentSid; // SNOMED ID
    int referenceType;
    long referencedSid; // SNOMED ID

    int status; // current or retired

    private static final String TAB_CHARACTER = "\t";

    public Rf1HistoryTableReference(long cSid, int rType, long rSid) {
        this.componentSid = cSid;
        this.referenceType = rType;
        this.referencedSid = rSid;

        this.status = 0; // CURRENT is 0
    }

    @Override
    public int compareTo(Object o) {
        Rf1HistoryTableReference tmp = (Rf1HistoryTableReference) o;

        if (this.componentSid < tmp.componentSid) {
            return -1; // instance less than received
        } else if (this.componentSid > tmp.componentSid) {
            return 1; // instance greater than received
        } else {
            if (this.referencedSid < tmp.referencedSid) {
                return -1; // instance less than received
            } else if (this.referencedSid > tmp.referencedSid) {
                return 1; // instance greater than received
            } else {
                if (this.referenceType < tmp.referenceType) {
                    return -1; // instance less than received
                } else if (this.referenceType > tmp.referenceType) {
                    return 1; // instance greater than received
                } else {
                    return 0; // instance == received
                }
            }
        }
    }

    public static Rf1HistoryTableReference[] parseFile(RF1File rf1) throws IOException,
            MojoFailureException {

        int lineCount = RF1File.countFileLines(rf1);
        Rf1HistoryTableReference[] a = new Rf1HistoryTableReference[lineCount];

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf1.file),
                "UTF-8"));
        int members = 0;

        int COMPONENT_SID = 0;
        int REFERENCE_TYPE = 1;
        int REFERENCED_SID = 2;

        // Header row
        br.readLine();

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);
            long tmpComponentSid = Long.parseLong(line[COMPONENT_SID]);
            int tmpReferenceType = Integer.parseInt(line[REFERENCE_TYPE]);
            long tmpReferencedSid = Long.parseLong(line[REFERENCED_SID]);

            a[members] = new Rf1HistoryTableReference(tmpComponentSid, tmpReferenceType,
                    tmpReferencedSid);

            members++;
        }
        br.close();

        Arrays.sort(a);
        return a;
    }

}
