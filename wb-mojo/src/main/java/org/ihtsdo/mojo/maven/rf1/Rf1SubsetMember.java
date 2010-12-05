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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.maven.plugin.MojoFailureException;

public class Rf1SubsetMember implements Comparable<Object> {

    private static final String TAB_CHARACTER = "\t";

    long origSubsetId; // SCTID original subset release id, see SubsetTable for original subset id
    // long subsetId; // SCTID subset release id, see SubsetTable for original subset id
    long memberId; // SCTID for SNOMED Concept or Description 
    int memberValue; // MEMBERSTATUS For Descriptions: 1=Preferred, 2=Synonym, 3=FSN
    //String linkedId; // SCTID, used for navigation subsets

    int status;

    public Rf1SubsetMember(long origSubsetId, long memberId, int value) {
        this.origSubsetId = origSubsetId;
        this.memberId = memberId;
        this.memberValue = value;
        this.status = 0; // CURRENT is 0
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public int getMemberValue() {
        return memberValue;
    }

    public void setMemberValue(int memberValue) {
        this.memberValue = memberValue;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int compareTo(Object o) {
        Rf1SubsetMember tmp = (Rf1SubsetMember) o;

        if (this.origSubsetId < tmp.origSubsetId) {
            return -1; // instance less than received
        } else if (this.origSubsetId > tmp.origSubsetId) {
            return 1; // instance greater than received
        } else {
            if (this.memberId < tmp.memberId) {
                return -1; // instance less than received
            } else if (this.memberId > tmp.memberId) {
                return 1; // instance greater than received
            } else {
                if (this.memberValue < tmp.memberValue) {
                    return -1; // instance less than received
                } else if (this.memberValue > tmp.memberValue) {
                    return 1; // instance greater than received
                } else {
                    return 0; // instance == received
                }
            }
        }
    }

    public static Rf1SubsetMember[] parseSubsetMembers(RF1File rf1, long originalSubsetId)
            throws IOException, MojoFailureException {

        int lineCount = RF1File.countFileLines(rf1);
        Rf1SubsetMember[] a = new Rf1SubsetMember[lineCount];

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf1.file),
                "UTF-8"));
        int members = 0;

        // int SUBSETID = 0;
        int MEMBERID = 1;
        int MEMBERSTATUS = 2; // NOTE: status is used as a "value"
        // int LINKEDID = 3;

        // Header row
        br.readLine();

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);
            // long subsetId = Long.parseLong(line[SUBSETID]);
            long memberId = Long.parseLong(line[MEMBERID]);
            int memberValue = Integer.parseInt(line[MEMBERSTATUS]);

            a[members] = new Rf1SubsetMember(originalSubsetId, memberId, memberValue);

            members++;
        }
        Arrays.sort(a);
        return a;
    }

    public static Rf1SubsetMember[] parseSubsetMembers(RF1File rf1,
            HashMap<Long, Rf1SubsetId> mapSubsetIdToOriginal) throws IOException,
            MojoFailureException {
        int lineCount = RF1File.countFileLines(rf1);
        Rf1SubsetMember[] a = new Rf1SubsetMember[lineCount];

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf1.file),
                "UTF-8"));
        int members = 0;

        int SUBSETID = 0;
        int MEMBERID = 1;
        int MEMBERSTATUS = 2; // NOTE: status is used as a "value"
        // int LINKEDID = 3;

        // Header row
        br.readLine();

        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);
            long subsetId = Long.parseLong(line[SUBSETID]);
            long memberId = Long.parseLong(line[MEMBERID]);
            int memberValue = Integer.parseInt(line[MEMBERSTATUS]);

            long originalSubsetId = mapSubsetIdToOriginal.get(subsetId).getSubsetSctIdOriginal();
            a[members] = new Rf1SubsetMember(originalSubsetId, memberId, memberValue);

            members++;
        }
        Arrays.sort(a);
        return a;
    }

}
