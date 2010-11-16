package org.ihtsdo.mojo.qa.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
	File outputFile;

	public PerformQA(I_GetConceptData context, File outputFile, I_ConfigAceFrame config, UUID executionUUID) {
		super();
		this.config = config;
		this.context = context;
		this.contextHelper = new RulesContextHelper(config);
		this.outputFile = outputFile;
		this.executionUUID = executionUUID;
	}

	@Override
	public void processConcept(I_GetConceptData loopConcept) throws Exception {
		ResultsCollectorWorkBench results = RulesLibrary.checkConcept(loopConcept, context, true, config, contextHelper);

		if (!results.getResultsItems().isEmpty()) {
			writeOutputFile(results, loopConcept);
		}
	}

	private void writeOutputFile(ResultsCollectorWorkBench results, I_GetConceptData concept) throws Exception {
		// Add results to output file
		FileOutputStream executionFos = new FileOutputStream(outputFile);
		OutputStreamWriter executionOsw = new OutputStreamWriter(executionFos, "UTF-8");
		PrintWriter findingPw = new PrintWriter(executionOsw);
		//TODO: add header titles
		findingPw.println("uuid" + "\t" + "execution" + "\t" + "rule" + "\t" + "component uuid" + "\t" + "component name" + "\t" + "error message");
		
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
		} finally {
			findingPw.flush();
			findingPw.close();
		}
	}

}
