package org.dwfa.ace.api;


public class TerminologyHelper {

    public static String conceptToString(int nid) {
        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            if (!termFactory.hasConcept(nid)) {
                return "[Concept does not exist (native id " + nid + ")]";
            }
            return termFactory.getConcept(nid).getInitialText();
        } catch (Exception e) {
            e.printStackTrace();
            return "[Unable to get a name for the concept (native id "+ nid + ")]";
        }
    }
    
}
