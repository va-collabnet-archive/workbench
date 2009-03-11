package org.dwfa.tapi;

import java.io.IOException;


public interface I_DescribeConcept {

	public boolean isInitialCapSig();

	public String getLangCode();

	public String getText();

	public I_Conceptualize getConcept() throws IOException, TerminologyException;

	public I_Conceptualize getDescType() throws IOException, TerminologyException;

	public I_Conceptualize getStatus() throws IOException, TerminologyException;


}