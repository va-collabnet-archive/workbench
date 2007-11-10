package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPartLanguage extends I_ThinExtByRefPart {

   public int getAcceptabilityId();

   public void setAcceptabilityId(int acceptabilityId);

   public int getCorrectnessId();

   public void setCorrectnessId(int correctnessId);

   public int getDegreeOfSynonymyId();

   public void setDegreeOfSynonymyId(int degreeOfSynonymyId);

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

   public I_ThinExtByRefPart duplicatePart();

}