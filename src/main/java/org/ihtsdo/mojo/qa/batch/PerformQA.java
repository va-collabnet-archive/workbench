package org.ihtsdo.mojo.qa.batch;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.tk.api.KindOfCacheBI;
import org.ihtsdo.tk.helper.ResultsItem;

public class PerformQA implements I_ProcessConcepts {
	I_ConfigAceFrame config;
	I_GetConceptData context;
	UUID executionUUID;
	RulesContextHelper contextHelper;
	PrintWriter findingPw;
	I_GetConceptData snomedRoot;
	I_IntSet destRels;
	KindOfCacheBI myStaticIsACache;
	int count;
	long start;

	public PerformQA(I_GetConceptData context, PrintWriter findingPw, I_ConfigAceFrame config, UUID executionUUID,
			RulesContextHelper contextHelper) {
		super();
		this.config = config;
		this.context = context;
		this.contextHelper = contextHelper;
		this.findingPw = findingPw;
		this.executionUUID = executionUUID;
		this.count = 0;
		this.start = Calendar.getInstance().getTimeInMillis();
		try {
			destRels = Terms.get().newIntSet();
			destRels.add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			snomedRoot = Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			myStaticIsACache = RulesLibrary.setupIsACache();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processConcept(I_GetConceptData loopConcept) throws Exception {
		if (myStaticIsACache.isKindOf(loopConcept.getConceptNid(), snomedRoot.getConceptNid())) {
			//snomedRoot.isParentOfOrEqualTo(loopConcept)
			//, config.getAllowedStatus(), 
			//destRels, config.getViewPositionSetReadOnly(), 
			//config.getPrecedence(), config.getConflictResolutionStrategy())
		ResultsCollectorWorkBench results = RulesLibrary.checkConcept(loopConcept, context, true, config, contextHelper);
		count++;
		if (count % 1000 == 0) {
			System.out.println("Checked " + count + " effective concepts in " + (Calendar.getInstance().getTimeInMillis()-start) + " ms.");
			start = Calendar.getInstance().getTimeInMillis();
		}
		if (!results.getResultsItems().isEmpty()) {
			writeOutputFile(results, loopConcept);
		}
		} else {
			//System.out.println("Skipping concept: " + loopConcept);
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
