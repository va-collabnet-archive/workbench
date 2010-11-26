package org.ihtsdo.qa.store;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.Execution;
import org.ihtsdo.qa.store.model.Finding;
import org.ihtsdo.qa.store.model.QACase;
import org.ihtsdo.qa.store.model.QACaseVersion;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.qa.store.model.view.RulesReportLine;

public class QAStoreStubImpl implements QAStoreBI {

	@Override
	public TerminologyComponent getComponent(UUID componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rule getRule(UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Finding getFinding(UUID findingUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Execution getExecution(UUID ExecutionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QACase getQACase(UUID qaCaseUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Finding> getFindingsForExecution(UUID executionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Finding> getFindingsForComponent(QACoordinate coordinate,
			UUID componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Finding> getFindingsForPeriod(QACoordinate coordinate,
			Date dateStart, Date dateEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getAllQACases(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForComponent(QACoordinate coordinate,
			UUID componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForStatus(QACoordinate coordinate,
			boolean isActive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForDispositionStatus(QACoordinate coordinate,
			UUID dispositionStatusUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForRule(QACoordinate coordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACaseVersion> getQACaseVersions(QACoordinate coordinate,
			UUID qaCaseUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Execution> getAllExecutions(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Execution> getExecutionsForPeriod(QACoordinate coordinate,
			Date dateStart, Date dateEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Rule> getAllRules() {
		Rule rule1 = new Rule();
		rule1.setErrorCode(11);
		rule1.setName("FSN must be unique");
		rule1.setSeverity(3);

		Rule rule2 = new Rule();
		rule2.setErrorCode(25);
		rule2.setName("Retired concept should not have defining roles");
		rule2.setSeverity(3);

		Rule rule3 = new Rule();
		rule3.setErrorCode(46);
		rule3.setName("Tex should not have double spaces");
		rule3.setSeverity(1);

		List<Rule> rules = new ArrayList<Rule>();
		rules.add(rule1);
		rules.add(rule2);
		rules.add(rule3);
		
		return rules;
	}

	@Override
	public List<TerminologyComponent> getAllComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void persistComponent(TerminologyComponent component) {
		// TODO Auto-generated method stub

	}

	@Override
	public void persistRule(Rule rule) {
		// TODO Auto-generated method stub

	}

	@Override
	public void persistFinding(Finding finding) {
		// TODO Auto-generated method stub

	}

	@Override
	public void persistExecution(Execution execution) {
		// TODO Auto-generated method stub

	}

	@Override
	public void persistQACase(QACase qaCase) {
		// TODO Auto-generated method stub

	}

	@Override
	public DispositionStatus getDispositionStatus(UUID dispositionStatusUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllDatabases() {
		List<String> databases = new ArrayList<String>();
		databases.add("production database");
		databases.add("test database");
		return databases;
	}

	@Override
	public List<TerminologyComponent> getAllPathsForDatabase(String database) {
		List<TerminologyComponent> paths = new ArrayList<TerminologyComponent>();
		paths.add(new TerminologyComponent(UUID.fromString("2694ed93-f8ce-11df-98cf-0800200c9a66"), "SNOMED CT Core path", null));
		paths.add(new TerminologyComponent(UUID.fromString("2694ed94-f8ce-11df-98cf-0800200c9a66"), "US Drugs Extension path", null));
		return paths;
	}

	@Override
	public List<DispositionStatus> getAllDispositionStatus() {
		List<DispositionStatus> result = new ArrayList<DispositionStatus>();
		result.add(new DispositionStatus(UUID.fromString("2694ed90-f8ce-11df-98cf-0800200c9a66"), "Cleared"));
		result.add(new DispositionStatus(UUID.fromString("2694ed91-f8ce-11df-98cf-0800200c9a66"), "Escalated"));
		result.add(new DispositionStatus(UUID.fromString("2694ed92-f8ce-11df-98cf-0800200c9a66"), "Deferred"));
		result.add(new DispositionStatus(UUID.fromString("2694ed95-f8ce-11df-98cf-0800200c9a66"), "In discussion"));
		return result;
	}

	@Override
	public List<String> getAllTimesForPath(String database, UUID pathUuid) {
		List<String> dates = new ArrayList<String>();
		dates.add("latest");
		return dates;
	}

	@Override
	public List<String> getAllDatabasesForPath(UUID pathUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TerminologyComponent> getAllPaths() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<UUID, Integer> getDispositionStatusCountsForRule(
			QACoordinate coordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Boolean, Integer> getStatusCountsForRule(
			QACoordinate coordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getRuleLastExecutionTime(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RulesReportLine> getRulesReportLines(QACoordinate qaCoordinate) {
		List<RulesReportLine> lines = new ArrayList<RulesReportLine>();
		
		for (Rule loopRule : getAllRules()) {
			Random randomGenerator = new Random();
			HashMap<Boolean, Integer> statusResults = new HashMap<Boolean, Integer>();
			statusResults.put(true, randomGenerator.nextInt(1000));
			statusResults.put(false, randomGenerator.nextInt(1000));
			
			HashMap<UUID, Integer> dispositionStatusResults = new HashMap<UUID, Integer>();
			for (DispositionStatus loopStatus : getAllDispositionStatus()) {
				dispositionStatusResults.put(loopStatus.getDispositionStatusUuid(), randomGenerator.nextInt(1000));
			}
			
			Date time = Calendar.getInstance().getTime();
			
			RulesReportLine line = new RulesReportLine(loopRule, statusResults, dispositionStatusResults, time);
			lines.add(line);
		}


		return lines;
	}

}
