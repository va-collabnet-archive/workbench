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
import java.util.UUID;

import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.util.id.Type3UuidFactory;

public class Rf1SubsetTable {
    private static final String TAB_CHARACTER = "\t";

    long subsetId; // SCTID 
    long subsetOriginalId; // SCTID
    UUID subsetOriginalUuid;

    int subsetVersion;
    String subsetName;
    int subsetType; // 1=Language, 2=Realm Concept, 3=Realm Description

    String languageCode;

    String realmId;
    String contextId;

    public Rf1SubsetTable(long subsetId, long subsetOriginalId, int subsetVersion,
            String subsetName, int subsetType, String languageCode, String realmId, String contextId) {
        super();
        this.subsetId = subsetId;
        this.subsetOriginalId = subsetOriginalId;
        this.subsetOriginalUuid = Type3UuidFactory.fromSNOMED(subsetOriginalId);
        ;
        this.subsetVersion = subsetVersion;
        this.subsetName = subsetName;
        this.subsetType = subsetType;
        this.languageCode = languageCode;
        this.realmId = realmId;
        this.contextId = contextId;

    }

    public static Rf1SubsetTable[] parseSubsetIdToOriginalUuidMap(RF1File rf1) throws IOException,
            MojoFailureException {
        int SUBSETID = 0;
        int SUBSETORIGINALID = 1;
        int SUBSETVERSION = 2;
        int SUBSETNAME = 3;
        int SUBSETTYPE = 4;
        int LANGUAGECODE = 5;
        int REALMID = 6;
        int CONTEXTID = 7;

        Rf1SubsetTable[] a = new Rf1SubsetTable[RF1File.countFileLines(rf1)];

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf1.file),
                "UTF-8"));
        // Header row
        br.readLine();

        int count = 0;
        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            // SUBSETID
            long sctIdSubset = Long.parseLong(line[SUBSETID]);
            // SUBSETORIGINALID
            long sctIdOriginal = Long.parseLong(line[SUBSETORIGINALID]);
            // SUBSETVERSION
            int subsetVersion = Integer.parseInt(line[SUBSETVERSION]);
            // SUBSETNAME
            String subsetName = line[SUBSETNAME];
            // SUBSETTYPE
            int subsetType = Integer.parseInt(line[SUBSETTYPE]);
            // LANGUAGECODE
            String languageCode = line[LANGUAGECODE];
            // REALMID
            String realmId = line[REALMID];
            // CONTEXTID
            String contextId = line[CONTEXTID];

            a[count] = new Rf1SubsetTable(sctIdSubset, sctIdOriginal, subsetVersion, subsetName,
                    subsetType, languageCode, realmId, contextId);
            count++;
        }

        br.close();

        return a;
    } // setupSubsetIdToOriginalUuidMap

}
