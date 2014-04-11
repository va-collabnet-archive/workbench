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

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.rules.testmodel.TerminologyHelperDroolsWorkbench;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
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
	HashMap<UUID,Integer> uniqueFsnMap;
	Set<Integer> duplicatesSet;

	int fsnNid;
	int activeNid;
	private String databaseUuid;
	private String testPathUuid;
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
		uniqueFsnMap = new HashMap<UUID,Integer>();
		duplicatesSet = new HashSet<Integer>();
		rulesAlerted = new HashSet<String>();
		monitoredUuids = new ArrayList<UUID>();

		//		monitoredUuids.add(UUID.fromString("48b0c96d-06a9-34f8-9479-6a278c74e87c"));
		//		monitoredUuids.add(UUID.fromString("57a97fd5-5278-358c-a9d5-16deb1a587d1"));
		//		monitoredUuids.add(UUID.fromString("1eb658fd-6f5c-3170-b736-56459b35490e"));

		try {
			destRels = Terms.get().newIntSet();
			destRels.add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			snomedRoot = Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			fsnNid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
			activeNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
			terminologyHelperCache = RulesLibrary.getTerminologyHelper(); // pointer to avoid garbage collection during qa
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		elapsedTotal = 0;
		estimatedNumberOfConcepts = 400000;
	}

	public void printConceptParents() {

	}

	@Override
	public void processConcept(I_GetConceptData loopConcept) throws Exception {
		if (monitoredUuids.contains(loopConcept.getPrimUuid())) {
			System.out.println("Monitored concept detected: " + loopConcept);
			System.out.println(loopConcept.toLongString());
		}
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


			if (results.getResultsItems().size() > 0) {
				TerminologyHelperDroolsWorkbench thdw = new TerminologyHelperDroolsWorkbench();

				List<I_GetConceptData> parents = new ArrayList<I_GetConceptData>();
				parents.add(Terms.get().getConcept(UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072")));
				parents.add(Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")));
				parents.add(Terms.get().getConcept(UUID.fromString("5adbed85-55d8-3304-a404-4bebab660fff")));
				parents.add(Terms.get().getConcept(UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c")));
				parents.add(Terms.get().getConcept(UUID.fromString("a5db42d4-6d94-33b7-92e7-d4a1d0f0d814")));
				parents.add(Terms.get().getConcept(UUID.fromString("d4227098-db7a-331e-8f00-9d1e27626fc5")));
				parents.add(Terms.get().getConcept(UUID.fromString("f267fc6f-7c4d-3a79-9f17-88b82b42709a")));
				parents.add(Terms.get().getConcept(UUID.fromString("0c7b717a-3e41-372b-be57-621befb9b3ee")));
				parents.add(Terms.get().getConcept(UUID.fromString("e730d11f-e155-3482-a423-9637db3bc1a2")));
				parents.add(Terms.get().getConcept(UUID.fromString("d96c5d7d-3314-3048-919e-4b866225c6c6")));
				parents.add(Terms.get().getConcept(UUID.fromString("d8a42cc5-05dd-3fcf-a1f7-62856e38874a")));
				parents.add(Terms.get().getConcept(UUID.fromString("bfbced4b-ad7d-30aa-ae5c-f848ccebd45b")));
				parents.add(Terms.get().getConcept(UUID.fromString("a0db7e17-c6b2-3acc-811d-8a523274e869")));

				List<I_GetConceptData> domains = new ArrayList<I_GetConceptData>();
				domains.add(Terms.get().getConcept(UUID.fromString("0acd09e5-a5f1-4880-82b2-2a5c273dca29")));
				domains.add(Terms.get().getConcept(UUID.fromString("a8883b08-9920-4531-9eb5-2ea578402157")));
				domains.add(Terms.get().getConcept(UUID.fromString("46c55be1-5a10-48e4-ab5a-ef579e1990b8")));
				domains.add(Terms.get().getConcept(UUID.fromString("272bcda1-766d-41e7-a711-b8d8e75fa5b2")));
				domains.add(Terms.get().getConcept(UUID.fromString("767e3525-ebd4-43f9-9640-952e31589e47")));
				domains.add(Terms.get().getConcept(UUID.fromString("674f7913-a5c3-4d92-834c-6ba879dc7423")));
				domains.add(Terms.get().getConcept(UUID.fromString("2e6d715a-aa77-4473-9720-93455d34c64c")));
				domains.add(Terms.get().getConcept(UUID.fromString("2465beac-65b5-47ef-8082-6e42934a4b18")));
				domains.add(Terms.get().getConcept(UUID.fromString("8d0f383e-fa0e-4492-843e-bc2003037dfa")));
				domains.add(Terms.get().getConcept(UUID.fromString("aebf2ccd-181d-45b9-bea8-8511c07a7fb7")));
				domains.add(Terms.get().getConcept(UUID.fromString("a0ff836c-bf65-4c06-a11b-f5ebe0eea5e9")));

				List<I_GetConceptData> ranges = new ArrayList<I_GetConceptData>();
				ranges.add(Terms.get().getConcept(UUID.fromString("af9e9019-136e-4816-80ab-801df9c61a6b")));
				ranges.add(Terms.get().getConcept(UUID.fromString("7785e4d0-ef89-411e-bb53-a39cb1a7dd01")));
				ranges.add(Terms.get().getConcept(UUID.fromString("a985589e-5ecb-42ad-a04d-9cb5be711979")));
				ranges.add(Terms.get().getConcept(UUID.fromString("3b753d9c-8ded-4def-9318-07b3b80a1a4e")));
				ranges.add(Terms.get().getConcept(UUID.fromString("efe80992-2c82-4adc-b91a-aa259772a370")));
				ranges.add(Terms.get().getConcept(UUID.fromString("329228ca-5f69-44d3-aa7e-91c798a6a717")));
				ranges.add(Terms.get().getConcept(UUID.fromString("de0ba6ce-bfc6-44ea-bfe8-c8adc5ff4f54")));
				ranges.add(Terms.get().getConcept(UUID.fromString("8a6f1b4d-0965-4216-b7b3-a38eaf364acf")));
				ranges.add(Terms.get().getConcept(UUID.fromString("034c1f09-2356-497a-94cf-57401fdf0210")));
				ranges.add(Terms.get().getConcept(UUID.fromString("27baf8b8-0012-4132-95b3-2089d89e1622")));
				ranges.add(Terms.get().getConcept(UUID.fromString("98582c9c-8a08-4a12-aaf3-48c90b38f6c1")));
				ranges.add(Terms.get().getConcept(UUID.fromString("b3c2919f-4cc9-45e6-a499-437e94bbb452")));
				ranges.add(Terms.get().getConcept(UUID.fromString("f0133e8f-547f-4fc6-8c10-6c6d1be0f531")));
				ranges.add(Terms.get().getConcept(UUID.fromString("c658f166-301f-45f6-b5aa-c52989e8e829")));
				ranges.add(Terms.get().getConcept(UUID.fromString("4f3404b7-5da3-4092-bb90-778c65df4f31")));
				ranges.add(Terms.get().getConcept(UUID.fromString("bedc628b-1e0b-4359-aa7d-7a3078e3bee0")));
				ranges.add(Terms.get().getConcept(UUID.fromString("81a14dc2-ae8f-48fc-a43b-b445ae89df44")));
				ranges.add(Terms.get().getConcept(UUID.fromString("f76ece36-c3f6-49fd-90ec-7ec57e5cafb9")));
				ranges.add(Terms.get().getConcept(UUID.fromString("e9fc0f33-0d5c-46f4-bd39-2807cb04080b")));
				ranges.add(Terms.get().getConcept(UUID.fromString("87fe35ac-458d-41c1-8588-129d1bab3951")));
				ranges.add(Terms.get().getConcept(UUID.fromString("3d2a28e3-67bd-470e-a570-991bbce52d1d")));
				ranges.add(Terms.get().getConcept(UUID.fromString("753e3b8f-14de-40e8-aad3-d07aaa6ebf18")));
				ranges.add(Terms.get().getConcept(UUID.fromString("32318346-fbe8-467c-90b5-9ae8f65a348b")));
				ranges.add(Terms.get().getConcept(UUID.fromString("c8ab369c-fe56-4737-be32-5e5859a16d79")));
				ranges.add(Terms.get().getConcept(UUID.fromString("6f169a0b-36fc-4341-b355-65a2f293eccf")));
				ranges.add(Terms.get().getConcept(UUID.fromString("66d14afd-7457-488c-9ae4-47697b1700eb")));
				ranges.add(Terms.get().getConcept(UUID.fromString("128946c4-9181-4f69-97f4-03d50625737d")));
				ranges.add(Terms.get().getConcept(UUID.fromString("24d21983-2158-4c58-bd6c-59ac73417096")));
				ranges.add(Terms.get().getConcept(UUID.fromString("c6559269-cdec-478c-80f8-a69a6c979b05")));
				ranges.add(Terms.get().getConcept(UUID.fromString("593dae07-8e86-40f4-8c04-fc5804d1ac45")));
				ranges.add(Terms.get().getConcept(UUID.fromString("c0111487-cb37-4afb-b2c3-ec8d09850b78")));
				ranges.add(Terms.get().getConcept(UUID.fromString("cb7b1a32-f558-406b-9348-c0dcaa039051")));
				ranges.add(Terms.get().getConcept(UUID.fromString("91359120-2b7f-40ad-bc68-5520ed5ae4e0")));
				ranges.add(Terms.get().getConcept(UUID.fromString("e0806cb8-62dd-4680-a2c4-6838bd619f3c")));
				ranges.add(Terms.get().getConcept(UUID.fromString("255226b9-4fe8-4947-af3f-3003f53d0a22")));
				ranges.add(Terms.get().getConcept(UUID.fromString("26d816df-bfb0-47b7-aa56-d4f516356f07")));
				ranges.add(Terms.get().getConcept(UUID.fromString("c79b5385-ec6d-44ed-ad6a-ba71d67dfaf1")));
				ranges.add(Terms.get().getConcept(UUID.fromString("1dbed7d0-b2be-4435-9487-28e1b8981a03")));
				ranges.add(Terms.get().getConcept(UUID.fromString("ac3f9c9c-1f6e-4b31-972d-8a99286df03a")));
				ranges.add(Terms.get().getConcept(UUID.fromString("a9e4c5a7-a753-42ad-b395-d5e5f41fb326")));
				ranges.add(Terms.get().getConcept(UUID.fromString("9a7c4424-cdd8-4442-a570-55a1da760854")));
				ranges.add(Terms.get().getConcept(UUID.fromString("cf2df8a2-88e4-417d-ba5c-cd33a1292add")));
				ranges.add(Terms.get().getConcept(UUID.fromString("78d5f6ea-f5db-429f-9d4f-3fd291eb4302")));
				ranges.add(Terms.get().getConcept(UUID.fromString("b3d62f01-cae1-4a4d-8a1e-1302cca565d9")));
				ranges.add(Terms.get().getConcept(UUID.fromString("256875d1-f29b-4ea4-ad10-811ecd87a74d")));
				ranges.add(Terms.get().getConcept(UUID.fromString("a744e178-ec29-45e8-ae0a-63d85927da2b")));
				ranges.add(Terms.get().getConcept(UUID.fromString("0c41774c-14af-4195-853f-29e8e3826ae3")));
				ranges.add(Terms.get().getConcept(UUID.fromString("224770fd-4c0b-486c-b053-1b07ade4293b")));
				ranges.add(Terms.get().getConcept(UUID.fromString("59e7e1be-9b91-431d-b1c8-479bf536e56a")));
				ranges.add(Terms.get().getConcept(UUID.fromString("abbbb4cf-8072-4348-8252-fd8f2ef1c039")));
				ranges.add(Terms.get().getConcept(UUID.fromString("7325a456-9d3d-4c9c-8964-467893c72f2b")));
				ranges.add(Terms.get().getConcept(UUID.fromString("6d682c29-346e-40f5-b3af-6bd4cb6fa3ee")));
				ranges.add(Terms.get().getConcept(UUID.fromString("2a190f65-9905-4b15-9f84-8bcb2538e947")));
				ranges.add(Terms.get().getConcept(UUID.fromString("91e9272a-182f-44cf-b788-44bb654c37b5")));
				ranges.add(Terms.get().getConcept(UUID.fromString("0e65d464-c379-42b4-999f-5448580e61a2")));
				ranges.add(Terms.get().getConcept(UUID.fromString("f57ad8fc-2ca7-4632-8c9e-74b7e71914d7")));
				ranges.add(Terms.get().getConcept(UUID.fromString("5734a7a3-3926-43d0-a005-32ee4bddc423")));
				ranges.add(Terms.get().getConcept(UUID.fromString("9058754a-4262-4f07-9096-9b8f92fa8503")));

				I_GetConceptData descendant = loopConcept;
				for (I_GetConceptData loopParent : parents) {
					if (thdw.isParentOfOrEqualTo(loopParent.getPrimUuid().toString(), 
							descendant.getPrimUuid().toString())) {
						System.out.println("Parent: " + loopParent + " Descendant: " + descendant + " *** result = TRUE");
					} else {
						System.out.println("Parent: " + loopParent + " Descendant: " + descendant + " *** result = FALSE");
					}
				}
				System.out.println("");
				System.out.println("");
				System.out.println("");
				for (I_GetConceptData loopdomain : domains) {
					if (thdw.isMemberOf(descendant.getPrimUuid().toString(),
							loopdomain.getPrimUuid().toString())) {
						System.out.println("Domain: " + loopdomain + " Member: " + descendant + " *** result = TRUE");
					} else {
						System.out.println("Domain: " + loopdomain + " Member: " + descendant + " *** result = FALSE");
					}
				}
				System.out.println("");
				System.out.println("");
				System.out.println("");
				for (I_GetConceptData looprange : ranges) {
					if (thdw.isMemberOf(descendant.getPrimUuid().toString(),
							looprange.getPrimUuid().toString())) {
						System.out.println("Range: " + looprange + " Member: " + descendant + " *** result = TRUE");
					} else {
						System.out.println("Range: " + looprange + " Member: " + descendant + " *** result = FALSE");
					}
				}
				System.out.println("");
				System.out.println("");
				System.out.println("");
			}
		}

		long individualElapsed = Calendar.getInstance().getTimeInMillis()-individualStart;

		String fsn = "";
		int loopConceptStatusNid = 0;
		List<? extends I_ConceptAttributeTuple> statusTuples = loopConcept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy());
		if (statusTuples.size() > 0 ) {
			loopConceptStatusNid = statusTuples.iterator().next().getStatusNid();
		}

		if (loopConceptStatusNid == activeNid) {
			for (I_DescriptionTuple loopTuple : loopConcept.getDescriptionTuples(config.getAllowedStatus(),
					config.getDescTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(),
					config.getConflictResolutionStrategy())) {
				if (loopTuple.getTypeNid() == fsnNid && loopTuple.getLang().equals("en")) {
					fsn = loopTuple.getText();
				}
			}

			if (uniqueFsnMap.containsKey(Type5UuidFactory.get(fsn))) {
				if (!duplicatesSet.contains(loopConcept.getNid())) {
					ResultsItem r1 = new ResultsItem();
					r1.setErrorCode(4);
					r1.setMessage("FSN Duplicated:" + fsn);
					r1.setRuleUuid("d4d60d70-0733-11e1-be50-0800200c9a66");
					r1.setSeverity("f9545a20-12cf-11e0-ac64-0800200c9a66");
					List<ResultsItem> r1List = new ArrayList<ResultsItem>();
					r1List.add(r1);
					ResultsCollectorWorkBench tmpResults1 = new ResultsCollectorWorkBench();
					tmpResults1.setAlertList(new ArrayList<AlertToDataConstraintFailure>());
					tmpResults1.setResultsItems(r1List);
					writeOutputFile(tmpResults1, loopConcept);
					duplicatesSet.add(loopConcept.getNid());
				}

				I_GetConceptData duplicateConcept = Terms.get().getConcept(uniqueFsnMap.get(Type5UuidFactory.get(fsn)));
				if (!duplicatesSet.contains(duplicateConcept.getNid())) {
					ResultsItem r2 = new ResultsItem();
					r2.setErrorCode(4);
					r2.setMessage("FSN Duplicated:" + fsn);
					r2.setRuleUuid("d4d60d70-0733-11e1-be50-0800200c9a66");
					r2.setSeverity("f9545a20-12cf-11e0-ac64-0800200c9a66");
					List<ResultsItem> r2List = new ArrayList<ResultsItem>();
					r2List.add(r2);
					ResultsCollectorWorkBench tmpResults2 = new ResultsCollectorWorkBench();
					tmpResults2.setAlertList(new ArrayList<AlertToDataConstraintFailure>());
					tmpResults2.setResultsItems(r2List);
					writeOutputFile(tmpResults2, duplicateConcept);
					duplicatesSet.add(duplicateConcept.getNid());
				}
			} else {
				uniqueFsnMap.put(Type5UuidFactory.get(fsn), loopConcept.getNid());
			}
		}
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
