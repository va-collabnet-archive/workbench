package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPartConcept extends I_ThinExtByRefPart {

   public int getConceptId();

   public void setConceptId(int conceptId);

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

}