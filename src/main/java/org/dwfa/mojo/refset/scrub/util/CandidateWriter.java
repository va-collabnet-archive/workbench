package org.dwfa.mojo.refset.scrub.util;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.I_TermFactory;

import java.util.TreeMap;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

public final class CandidateWriter {

    private final PrintWriter reportWriter;
    private final I_TermFactory termFactory;

    public CandidateWriter(final String reportFile, final I_TermFactory termFactory) throws FileNotFoundException {
        this.termFactory = termFactory;
        reportWriter = new PrintWriter(reportFile);
    }

    public void logCandidate(String refsetName, I_ThinExtByRefVersioned candidate) throws Exception {
        String conceptDesc = termFactory.getConcept(candidate.getComponentId()).getInitialText();

        // First index the version parts so we can print back in chronological order
        TreeMap<Long, PartDescription> partIndex = new TreeMap<Long, PartDescription>();
        for (I_ThinExtByRefPart part : candidate.getVersions()) {
            if (part instanceof I_ThinExtByRefPartConcept) {
                PartDescription partDesc = new PartDescription();
                int inclusionType = ((I_ThinExtByRefPartConcept)part).getConceptId();
                partDesc.typeDesc = termFactory.getConcept(inclusionType).getInitialText();
                partDesc.statusDesc = termFactory.getConcept(part.getStatus()).getInitialText();
                partDesc.pathDesc = termFactory.getConcept(part.getPathId()).getInitialText();
                Long version = termFactory.convertToThickVersion(part.getVersion());
                partIndex.put(version, partDesc);
            }
        }

        System.out.println("\tFound candidate" + ": " + conceptDesc);
        SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM yyyy HH:mm:ss z");
        for (Long version : partIndex.keySet()) {
            PartDescription partDesc = partIndex.get(version);
            String dateStr = dateFmt.format(new Date(version));
            System.out.println("\t\t" + partDesc.typeDesc + "," + partDesc.statusDesc + "," +
                    partDesc.pathDesc + "," + dateStr);
            reportWriter.println(refsetName + "\t" + conceptDesc + "\t" + partDesc.typeDesc + "\t" +
                    partDesc.statusDesc + "\t" + partDesc.pathDesc + "\t" + dateStr);
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
