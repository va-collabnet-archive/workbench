package org.dwfa.tapi;

import java.io.IOException;


public interface I_RelateConcepts {

	public int getRelGrp();

	public I_Conceptualize getC1() throws IOException, TerminologyException;

	public I_Conceptualize getC2() throws IOException, TerminologyException;

	public I_Conceptualize getCharacteristic() throws IOException, TerminologyException;

	public I_Conceptualize getRefinability() throws IOException, TerminologyException;

	public I_Conceptualize getRelType() throws IOException, TerminologyException;

}