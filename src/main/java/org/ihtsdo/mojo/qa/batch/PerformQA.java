package org.ihtsdo.mojo.qa.batch;

import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.tk.helper.ResultsItem;

public class PerformQA implements I_ProcessConcepts {
	I_ConfigAceFrame config;
	I_GetConceptData context;
	UUID executionUUID;
	RulesContextHelper contextHelper;
	PrintWriter findingPw;

	public PerformQA(I_GetConceptData context, PrintWriter findingPw, I_ConfigAceFrame config, UUID executionUUID,
			RulesContextHelper contextHelper) {
		super();
		this.config = config;
		this.context = context;
		this.contextHelper = contextHelper;
		this.findingPw = findingPw;
		this.executionUUID = executionUUID;
	}

	@Override
	public void processConcept(I_GetConceptData loopConcept) throws Exception {
		System.out.println("Processing Concept: " + loopConcept);
		ResultsCollectorWorkBench results = RulesLibrary.checkConcept(loopConcept, context, true, config, contextHelper);

		if (!results.getResultsItems().isEmpty()) {
			writeOutputFile(results, loopConcept);
		}
	}

	private void writeOutputFile(ResultsCollectorWorkBench results, I_GetConceptData concept) throws Exception {
		
		try {
			List<AlertToDataConstraintFailure> alertList = results.getAlertList();

			for (AlertToDataConstraintFailure alertToDataConstraintFailure : alertList) {
				alertToDataConstraintFailure.getAlertType();
				alertToDataConstraintFailure.getAlertMessage();
			}

			List<ResultsItem> resultItems = results.getResultsItems();
			for (ResultsItem resultItem : resultItems) {
				//Get finding data
				UUID findingUUID = UUID.randomUUID();
				int errorCode = resultItem.getErrorCode();//Rule
				String message = resultItem.getMessage();
				String conceptName = concept.toUserString();
				UUID conceptUUID = concept.getUids().get(0);
				
				//Write data to file
				findingPw.print(findingUUID + "\t");
				findingPw.print(executionUUID + "\t");
				findingPw.print(errorCode + "\t");// Rule
				findingPw.print(conceptName + "\t");
				findingPw.print(conceptUUID + "\t");
				findingPw.print(message);
				findingPw.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
