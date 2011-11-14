package org.ihtsdo.mojo.qa.batch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.computer.kindof.IsaCache;
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.rules.testmodel.TerminologyHelperDroolsWorkbench;
import org.ihtsdo.tk.api.coordinate.IsaCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.helper.ResultsItem;

public class PerformQA implements I_ProcessConcepts { 
	I_ConfigAceFrame config;
	I_GetConceptData context;
	UUID executionUUID;
	RulesContextHelper contextHelper;
	PrintWriter findingPw;
	I_GetConceptData snomedRoot;
	I_IntSet destRels;
	TerminologyHelperDroolsWorkbench terminologyHelperCache;
	int count;
	int skippedCount;
	long start;

	HashMap<String,Long> traceElapsedTimes;
	HashMap<String,Integer> traceCounts;
	HashMap<String,Integer> uniqueFsnMap;
	Set<Integer> duplicatesSet;

	int fsnNid;
	private String databaseUuid;
	private String testPathUuid;
	private IsaCache isaCache;
	private long elapsedTotal;
	private int estimatedNumberOfConcepts;
	private HashMap<String, String> allRules;
	private Set<String> rulesAlerted;
	private List<UUID> monitoredUuids;

	public PerformQA(I_GetConceptData context, PrintWriter findingPw, I_ConfigAceFrame config, UUID executionUUID,
			RulesContextHelper contextHelper, String databaseUuid, String testPathUuid, HashMap<String, String> allRules) throws Exception {
		super();
		this.config = config;
		this.context = context;
		this.contextHelper = contextHelper;
		this.findingPw = findingPw;
		this.executionUUID = executionUUID;
		this.databaseUuid=databaseUuid;
		this.testPathUuid=testPathUuid;
		this.count = 0;
		this.skippedCount = 0;
		this.start = Calendar.getInstance().getTimeInMillis();
		this.allRules=allRules;
		traceElapsedTimes = new HashMap<String,Long>();
		traceCounts = new HashMap<String,Integer>();
		uniqueFsnMap = new HashMap<String,Integer>();
		duplicatesSet = new HashSet<Integer>();
		rulesAlerted = new HashSet<String>();
		monitoredUuids = new ArrayList<UUID>();
		
		monitoredUuids.add(UUID.fromString("298ce677-9986-3d03-8d55-28af02be7892"));
		monitoredUuids.add(UUID.fromString("e98ee24f-6586-3e20-9ccb-fa36a4ea0b40"));
		monitoredUuids.add(UUID.fromString("8003005b-e3ff-3ca6-ad4b-1cd0555fcd79"));
		monitoredUuids.add(UUID.fromString("a70da6df-e8b1-3fb6-ba87-b2bed8298121"));
		monitoredUuids.add(UUID.fromString("1cb37872-4476-3358-aea1-800fa4d97b5f"));
		monitoredUuids.add(UUID.fromString("813562d6-5725-3da1-9d77-0a546061242d"));
		try {
			destRels = Terms.get().newIntSet();
			destRels.add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			snomedRoot = Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			fsnNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
			terminologyHelperCache = RulesLibrary.getTerminologyHelper(); // pointer to avoid garbage collection during qa
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Setting up Is-a cache...");
		
		if (config.getViewCoordinate().getIsaCoordinates().size() != 1) {
			throw new Exception("wrong number of view coordinates: " + config.getViewCoordinate().getIsaCoordinates());
		}
		ViewCoordinate cacheViewCoordinate = new ViewCoordinate(config.getViewCoordinate());
		//cacheViewCoordinate.setPositionSet(positionSet)
		for (IsaCoordinate isac: cacheViewCoordinate.getIsaCoordinates() ) {
			KindOfComputer.setupIsaCacheAndWait(isac);
		}
		System.out.println("Is-a created OK...");
		isaCache = KindOfComputer.getIsaCacheMap().get(config.getViewCoordinate().getIsaCoordinates().iterator().next());
		if (isaCache == null) {
			throw new Exception("Error: No isa cache for ViewCoordinate.");
		}
		elapsedTotal = 0;
		estimatedNumberOfConcepts = 400000;
	}

	@Override
	public void processConcept(I_GetConceptData loopConcept) throws Exception {
		if (monitoredUuids.contains(loopConcept.getPrimUuid())) {
			System.out.println("Monitored concept detected: " + loopConcept);
		}
		if (isaCache.isKindOf(loopConcept.getNid(), snomedRoot.getNid())) {
			long individualStart = Calendar.getInstance().getTimeInMillis();
			ResultsCollectorWorkBench results = RulesLibrary.checkConcept(loopConcept, context, true, config, contextHelper, INFERRED_VIEW_ORIGIN.INFERRED);
			if (monitoredUuids.contains(loopConcept.getPrimUuid())) {
				System.out.println("Results for Monitored concept detected: " + results.getResultsItems().size());
				for (ResultsItem loopItem : results.getResultsItems()) {
					System.out.println("-- " + loopItem.getMessage() + " - " + loopItem.getRuleUuid());
				}
				
				try {
				    BufferedWriter out = new BufferedWriter(new FileWriter(loopConcept.getPrimUuid() + ".txt"));
				    out.write(loopConcept.toLongString());
				    out.close();
				} catch (IOException e) {
				}
			}
			long individualElapsed = Calendar.getInstance().getTimeInMillis()-individualStart;

//			String fsn = "";
//			for (I_DescriptionTuple loopTuple : loopConcept.getDescriptionTuples(config.getAllowedStatus(),
//					config.getDescTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(),
//					config.getConflictResolutionStrategy())) {
//				if (loopTuple.getTypeNid() == fsnNid && loopTuple.getLang().equals("en")) {
//					fsn = loopTuple.getText();
//				}
//			}
//
//			if (uniqueFsnMap.containsKey(fsn)) {
//				if (!duplicatesSet.contains(loopConcept.getNid())) {
//					ResultsItem r1 = new ResultsItem();
//					r1.setErrorCode(4);
//					r1.setMessage("FSN Duplicated:" + fsn);
//					r1.setRuleUuid("d4d60d70-0733-11e1-be50-0800200c9a66");
//					r1.setSeverity("f9545a20-12cf-11e0-ac64-0800200c9a66");
//					List<ResultsItem> r1List = new ArrayList<ResultsItem>();
//					r1List.add(r1);
//					ResultsCollectorWorkBench tmpResults1 = new ResultsCollectorWorkBench();
//					tmpResults1.setAlertList(new ArrayList<AlertToDataConstraintFailure>());
//					tmpResults1.setResultsItems(r1List);
//					writeOutputFile(tmpResults1, loopConcept);
//					duplicatesSet.add(loopConcept.getNid());
//				}
//
//				I_GetConceptData duplicateConcept = Terms.get().getConcept(uniqueFsnMap.get(fsn));
//				if (!duplicatesSet.contains(duplicateConcept.getNid())) {
//					ResultsItem r2 = new ResultsItem();
//					r2.setErrorCode(4);
//					r2.setMessage("FSN Duplicated:" + fsn);
//					r2.setRuleUuid("d4d60d70-0733-11e1-be50-0800200c9a66");
//					r2.setSeverity("f9545a20-12cf-11e0-ac64-0800200c9a66");
//					List<ResultsItem> r2List = new ArrayList<ResultsItem>();
//					r2List.add(r2);
//					ResultsCollectorWorkBench tmpResults2 = new ResultsCollectorWorkBench();
//					tmpResults2.setAlertList(new ArrayList<AlertToDataConstraintFailure>());
//					tmpResults2.setResultsItems(r2List);
//					writeOutputFile(tmpResults2, duplicateConcept);
//					duplicatesSet.add(duplicateConcept.getNid());
//				}
//			} else {
//				uniqueFsnMap.put(fsn, loopConcept.getNid());
//			}

			//System.out.println("Individual loop for " + loopConcept.toString() + " in " + individualElapsed + " ms.");
			if (individualElapsed > 6000) {
				System.out.println("Specially long check: " + loopConcept.toString() + " took " + individualElapsed + " ms.");
			}

			count++;
			if (count % 10000 == 0 || count == 100 || count == 1000) {
				long elapsedInterval = Calendar.getInstance().getTimeInMillis()-start;
				elapsedTotal = elapsedTotal + elapsedInterval;
				System.out.println("Checked " + count + " effective concepts in " + elapsedInterval + " ms.");
				String elpasedString = String.format("%d hours, %d min, %d sec",
						TimeUnit.MILLISECONDS.toHours(elapsedTotal),
						TimeUnit.MILLISECONDS.toMinutes(elapsedTotal) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTotal)),
						TimeUnit.MILLISECONDS.toSeconds(elapsedTotal) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTotal))
				);
				long predictedTime = (estimatedNumberOfConcepts * elapsedTotal)/count;
				String predictedString = String.format("%d hours, %d min, %d sec",
						TimeUnit.MILLISECONDS.toHours(predictedTime),
						TimeUnit.MILLISECONDS.toMinutes(predictedTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(predictedTime)),
						TimeUnit.MILLISECONDS.toSeconds(predictedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(predictedTime))
				);
				System.out.println("Elapsed until now: " + elpasedString + ", predicted time: " + predictedString + " (average: " + elapsedTotal/count + ")");
				System.out.println("");
				start = Calendar.getInstance().getTimeInMillis();
			}
			if (!results.getResultsItems().isEmpty()) {
				writeOutputFile(results, loopConcept);
			}
		} else {
			skippedCount++;
			if (skippedCount % 1000 == 0) {
				System.out.println("Skipped concepts: " + skippedCount);
				System.out.println("");
			}
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
				String ruleUuid = resultItem.getRuleUuid();
				String conceptName = concept.toUserString();
				UUID conceptUUID = concept.getUids().get(0);
				if (!allRules.containsKey(ruleUuid)){
					if (!rulesAlerted.contains(ruleUuid)) {
						rulesAlerted.add(ruleUuid);
						System.out.println("RULE DOESN'T EXIST IN RULE FILE - UUID:" + ruleUuid +  " error code:" + errorCode +  " Message:" + message);
						//throw new Exception("RULE DOESN'T EXIST IN RULE FILE - UUID:" + ruleUuid +  " error code:" + errorCode +  " Message:" + message);
					}
				}
				//Write data to file
				findingPw.print(findingUUID + "\t");
				findingPw.print(databaseUuid + "\t");
				findingPw.print(testPathUuid + "\t");
				findingPw.print(executionUUID + "\t");
				findingPw.print(ruleUuid + "\t");// Rule
				findingPw.print(conceptUUID + "\t");
				findingPw.print(errorCode + "-" + message + "\t");
				findingPw.print(conceptName);
				findingPw.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
