package org.ihtsdo.mojo.qa.batch;

import java.io.File;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;

public class PerformQA implements I_ProcessConcepts {
	I_ConfigAceFrame config;
	I_GetConceptData context;
	RulesContextHelper contextHelper;
	File outputFile;

	public PerformQA(I_GetConceptData context, File outputFile, I_ConfigAceFrame config) {
		super();
		this.config = config;
		this.context = context;
		this.contextHelper = new RulesContextHelper(config);
		this.outputFile = outputFile;
	}

	@Override
	public void processConcept(I_GetConceptData loopConcept) throws Exception {
		ResultsCollectorWorkBench results = 
			RulesLibrary.checkConcept(loopConcept, context, true, config, contextHelper);
		
		if (!results.getResultsItems().isEmpty()) {
			writeOutputFile(results);
		}
	}

	private void writeOutputFile(ResultsCollectorWorkBench results) {
		// TODO Auto-generated method stub
		// Add results to output file
		
	}

}
