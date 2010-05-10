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
package org.dwfa.mojo.refset.scrub.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.util.AceDateFormat;

public final class CandidateWriter {

    private final PrintWriter reportWriter;
    private final I_TermFactory termFactory;

    public CandidateWriter(final String reportFile, final I_TermFactory termFactory) throws FileNotFoundException {
        this.termFactory = termFactory;
        reportWriter = new PrintWriter(reportFile);
    }

    public void logCandidate(String refsetName, I_ThinExtByRefVersioned candidate) throws Exception {
        String conceptDesc = termFactory.getConcept(candidate.getComponentId()).getInitialText();

        // First index the version parts so we can print back in chronological
        // order
        TreeMap<Long, PartDescription> partIndex = new TreeMap<Long, PartDescription>();
        for (I_ThinExtByRefPart part : candidate.getVersions()) {
            if (part instanceof I_ThinExtByRefPartConcept) {
                PartDescription partDesc = new PartDescription();
                int inclusionType = ((I_ThinExtByRefPartConcept) part).getConceptId();
                partDesc.typeDesc = termFactory.getConcept(inclusionType).getInitialText();
                partDesc.statusDesc = termFactory.getConcept(part.getStatus()).getInitialText();
                partDesc.pathDesc = termFactory.getConcept(part.getPathId()).getInitialText();
                Long version = termFactory.convertToThickVersion(part.getVersion());
                partIndex.put(version, partDesc);
            }
        }

        System.out.println("\tFound candidate" + ": " + conceptDesc);
        DateFormat dateFmt = AceDateFormat.getCandidateWriterDateFormat();
        for (Long version : partIndex.keySet()) {
            PartDescription partDesc = partIndex.get(version);
            String dateStr = dateFmt.format(new Date(version));
            System.out.println("\t\t" + partDesc.typeDesc + "," + partDesc.statusDesc + "," + partDesc.pathDesc + ","
                + dateStr);
            reportWriter.println(refsetName + "\t" + conceptDesc + "\t" + partDesc.typeDesc + "\t"
                + partDesc.statusDesc + "\t" + partDesc.pathDesc + "\t" + dateStr);
        }
        reportWriter.println();
    }

    public void close() {
        reportWriter.flush();
        reportWriter.close();
    }

    private class PartDescription {
        String typeDesc;
        String statusDesc;
        String pathDesc;
    }
}
