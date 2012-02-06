/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.qa.store;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.ihtsdo.qa.store.model.Category;
import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.Execution;
import org.ihtsdo.qa.store.model.Finding;
import org.ihtsdo.qa.store.model.QACase;
import org.ihtsdo.qa.store.model.QACaseVersion;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.QADatabase;
import org.ihtsdo.qa.store.model.QaCaseComment;
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.Severity;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.qa.store.model.view.QACasesReportColumn;
import org.ihtsdo.qa.store.model.view.QACasesReportLine;
import org.ihtsdo.qa.store.model.view.QACasesReportPage;
import org.ihtsdo.qa.store.model.view.RulesReportColumn;
import org.ihtsdo.qa.store.model.view.RulesReportLine;
import org.ihtsdo.qa.store.model.view.RulesReportPage;

/**
 * The Class QAStoreStubImpl.
 */
public class QAStoreStubImpl implements QAStoreBI {
	
	/**
	 * Gets the sample components.
	 *
	 * @return the sample components
	 */
	public List<TerminologyComponent> getSampleComponents() {
		List<TerminologyComponent> paths = new ArrayList<TerminologyComponent>();
		paths.add(new TerminologyComponent(UUID.fromString("2694ed01-f8ce-11df-98cf-0800200c9a66"), "asthma", new Long(12345002)));
		paths.add(new TerminologyComponent(UUID.fromString("2694ed02-f8ce-11df-98cf-0800200c9a66"), "appendix removal", new Long(534634003)));
		paths.add(new TerminologyComponent(UUID.fromString("2694ed03-f8ce-11df-98cf-0800200c9a66"), "bilateral pneumonia", new Long(656756007)));
		paths.add(new TerminologyComponent(UUID.fromString("2694ed04-f8ce-11df-98cf-0800200c9a66"), "escherichia colli", new Long(354645009)));
		paths.add(new TerminologyComponent(UUID.fromString("2694ed05-f8ce-11df-98cf-0800200c9a66"), "acne", new Long(34455001)));
		return paths;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getComponent(java.util.UUID)
	 */
	@Override
	public TerminologyComponent getComponent(UUID componentUuid) {
		for (TerminologyComponent loopComponent : getAllComponents()) {
			if (loopComponent.getComponentUuid().equals(componentUuid)) {
				return loopComponent;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getRule(java.util.UUID)
	 */
	@Override
	public Rule getRule(UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getFinding(java.util.UUID)
	 */
	@Override
	public Finding getFinding(UUID findingUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getExecution(java.util.UUID)
	 */
	@Override
	public Execution getExecution(UUID ExecutionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACase(java.util.UUID)
	 */
	@Override
	public QACase getQACase(UUID qaCaseUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getFindingsForExecution(java.util.UUID)
	 */
	@Override
	public List<Finding> getFindingsForExecution(UUID executionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getFindingsForComponent(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public List<Finding> getFindingsForComponent(QACoordinate coordinate,
			UUID componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getFindingsForPeriod(org.ihtsdo.qa.store.model.QACoordinate, java.util.Date, java.util.Date)
	 */
	@Override
	public List<Finding> getFindingsForPeriod(QACoordinate coordinate,
			Date dateStart, Date dateEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllQACases(org.ihtsdo.qa.store.model.QACoordinate)
	 */
	@Override
	public List<QACase> getAllQACases(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesForComponent(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public List<QACase> getQACasesForComponent(QACoordinate coordinate,
			UUID componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesForStatus(org.ihtsdo.qa.store.model.QACoordinate, boolean)
	 */
	@Override
	public List<QACase> getQACasesForStatus(QACoordinate coordinate,
			boolean isActive) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesForDispositionStatus(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public List<QACase> getQACasesForDispositionStatus(QACoordinate coordinate,
			UUID dispositionStatusUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesForRule(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public List<QACase> getQACasesForRule(QACoordinate coordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACaseVersions(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public List<QACaseVersion> getQACaseVersions(QACoordinate coordinate,
			UUID qaCaseUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllExecutions(org.ihtsdo.qa.store.model.QACoordinate)
	 */
	@Override
	public List<Execution> getAllExecutions(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getExecutionsForPeriod(org.ihtsdo.qa.store.model.QACoordinate, java.util.Date, java.util.Date)
	 */
	@Override
	public List<Execution> getExecutionsForPeriod(QACoordinate coordinate,
			Date dateStart, Date dateEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllRules()
	 */
	@Override
	public List<Rule> getAllRules() {
		Rule rule1 = new Rule();
		rule1.setRuleCode("11");
		rule1.setName("FSN must be unique");
		rule1.setSeverity(getAllSeverities().get(1));
		rule1.setCategory("Descriptions model");
		rule1.setRuleUuid(UUID.fromString("2694ed98-f8ce-11df-98cf-0800200c9a66"));

		Rule rule2 = new Rule();
		rule2.setRuleCode("25");
		rule2.setName("Retired concept should not have defining roles");
		rule2.setSeverity(getAllSeverities().get(2));
		rule2.setCategory("Concept model");
		rule2.setRuleUuid(UUID.fromString("2694ed99-f8ce-11df-98cf-0800200c9a66"));

		Rule rule3 = new Rule();
		rule3.setRuleCode("46");
		rule3.setName("Tex should not have double spaces");
		rule3.setSeverity(getAllSeverities().get(0));
		rule3.setCategory("Descriptions model");
		rule3.setRuleUuid(UUID.fromString("2694ed00-f8ce-11df-98cf-0800200c9a66"));

		List<Rule> rules = new ArrayList<Rule>();
		rules.add(rule1);
		rules.add(rule2);
		rules.add(rule3);

		return rules;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllComponents()
	 */
	@Override
	public List<TerminologyComponent> getAllComponents() {
		List<TerminologyComponent> components = new ArrayList<TerminologyComponent>();
		components.addAll(getAllPaths());
		components.addAll(getSampleComponents());
		return components;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistComponent(org.ihtsdo.qa.store.model.TerminologyComponent)
	 */
	@Override
	public void persistComponent(TerminologyComponent component) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistRule(org.ihtsdo.qa.store.model.Rule)
	 */
	@Override
	public void persistRule(Rule rule) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistFinding(org.ihtsdo.qa.store.model.Finding)
	 */
	@Override
	public void persistFinding(Finding finding) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistExecution(org.ihtsdo.qa.store.model.Execution)
	 */
	@Override
	public void persistExecution(Execution execution) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistQACase(org.ihtsdo.qa.store.model.QACase)
	 */
	@Override
	public void persistQACase(QACase qaCase) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getDispositionStatus(java.util.UUID)
	 */
	@Override
	public DispositionStatus getDispositionStatus(UUID dispositionStatusUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllDatabases()
	 */
	@Override
	public List<QADatabase> getAllDatabases() {
		List<QADatabase> databases = new ArrayList<QADatabase>();
		databases.add(new QADatabase(UUID.fromString("2694ed96-f8ce-11df-98cf-0800200c9a66"), "production database"));
		databases.add(new QADatabase(UUID.fromString("2694ed97-f8ce-11df-98cf-0800200c9a66"), "test database"));
		return databases;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllPathsForDatabase(java.util.UUID)
	 */
	@Override
	public List<TerminologyComponent> getAllPathsForDatabase(UUID databaseUid) {
		List<TerminologyComponent> paths = new ArrayList<TerminologyComponent>();
		paths.addAll(getAllPaths());
		return paths;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllDispositionStatus()
	 */
	@Override
	public List<DispositionStatus> getAllDispositionStatus() {
		List<DispositionStatus> result = new ArrayList<DispositionStatus>();
		result.add(new DispositionStatus(UUID.fromString("2694ed90-f8ce-11df-98cf-0800200c9a66"), "Cleared"));
		result.add(new DispositionStatus(UUID.fromString("2694ed91-f8ce-11df-98cf-0800200c9a66"), "Escalated"));
		result.add(new DispositionStatus(UUID.fromString("2694ed92-f8ce-11df-98cf-0800200c9a66"), "Deferred"));
		result.add(new DispositionStatus(UUID.fromString("2694ed95-f8ce-11df-98cf-0800200c9a66"), "In discussion"));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllTimesForPath(java.util.UUID, java.util.UUID)
	 */
	@Override
	public List<String> getAllTimesForPath(UUID databaseUuid, UUID pathUuid) {
		List<String> dates = new ArrayList<String>();
		dates.add("latest");
		return dates;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllDatabasesForPath(java.util.UUID)
	 */
	@Override
	public List<QADatabase> getAllDatabasesForPath(UUID pathUuid) {
		return getAllDatabases();
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllPaths()
	 */
	@Override
	public List<TerminologyComponent> getAllPaths() {
		List<TerminologyComponent> paths = new ArrayList<TerminologyComponent>();
		paths.add(new TerminologyComponent(UUID.fromString("2694ed93-f8ce-11df-98cf-0800200c9a66"), "SNOMED CT Core path", null));
		paths.add(new TerminologyComponent(UUID.fromString("2694ed94-f8ce-11df-98cf-0800200c9a66"), "US Drugs Extension path", null));
		return paths;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getDispositionStatusCountsForRule(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public HashMap<UUID, Integer> getDispositionStatusCountsForRule(
			QACoordinate coordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getStatusCountsForRule(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public HashMap<Boolean, Integer> getStatusCountsForRule(
			QACoordinate coordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getRuleLastExecutionTime(org.ihtsdo.qa.store.model.QACoordinate)
	 */
	@Override
	public Date getRuleLastExecutionTime(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getRulesReportLines(org.ihtsdo.qa.store.model.QACoordinate)
	 */
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

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getExecutionRulesDetails(java.util.UUID)
	 */
	@Override
	public String getExecutionRulesDetails(UUID executionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getExecutionOutcomeDetails(java.util.UUID)
	 */
	@Override
	public String getExecutionOutcomeDetails(UUID executionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQADatabase(java.util.UUID)
	 */
	@Override
	public QADatabase getQADatabase(UUID databaseUuid) {
		for (QADatabase loopDatabase : getAllDatabases()) {
			if (loopDatabase.getDatabaseUuid().equals(databaseUuid)) {
				return loopDatabase;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistQADatabase(org.ihtsdo.qa.store.model.QADatabase)
	 */
	@Override
	public void persistQADatabase(QADatabase database) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesReportLines(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public List<QACasesReportLine> getQACasesReportLines(
			QACoordinate qaCoordinate, UUID ruleUuid) {
		List<QACasesReportLine> lines = new ArrayList<QACasesReportLine>();
		Calendar time = Calendar.getInstance();
		Random randomGenerator = new Random();
		for (TerminologyComponent loopComponent : getSampleComponents()) {
			QACase loopCase = new QACase();
			DispositionStatus loopStatus = getAllDispositionStatus().get(randomGenerator.nextInt(3));
			loopCase.setActive(true);
			loopCase.setCaseUuid(UUID.randomUUID());
			loopCase.setDatabaseUuid(qaCoordinate.getDatabaseUuid());
			loopCase.setComponentUuid(loopComponent.getComponentUuid());
			loopCase.setDetail("This are the details");
			loopCase.setDispositionStatusUuid(loopStatus.getDispositionStatusUuid());
			loopCase.setPathUuid(qaCoordinate.getPathUuid());
			loopCase.setRuleUuid(ruleUuid);
			loopCase.setViewPointTime(qaCoordinate.getViewPointTime());
			loopCase.setEffectiveTime(time);
			loopCase.setAssignedTo("Patricia");
			QACasesReportLine loopLine = new QACasesReportLine(loopCase, loopComponent, loopStatus);
			lines.add(loopLine);
		}
		
		return lines;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getRulesReportLinesByPage(org.ihtsdo.qa.store.model.QACoordinate, java.util.LinkedHashMap, java.util.HashMap, int, int)
	 */
	@Override
	public RulesReportPage getRulesReportLinesByPage(
			QACoordinate qaCoordinate, Map<RulesReportColumn,Boolean> sortBy,
			Map<RulesReportColumn, Object> filter, int startLine, int pageLenght) {
		List<RulesReportLine> lines = new ArrayList<RulesReportLine>();
		for (int i = 1; i < pageLenght; i++) {
			lines.addAll(getRulesReportLines(qaCoordinate));
		}
		return new RulesReportPage(lines, sortBy, filter, startLine, startLine + pageLenght -1, 150);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesReportLinesByPage(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID, java.util.LinkedHashMap, java.util.HashMap, int, int)
	 */
	@Override
	public QACasesReportPage getQACasesReportLinesByPage(
			QACoordinate qaCoordinate, UUID ruleUuid,
			LinkedHashMap<QACasesReportColumn,Boolean> sortBy, HashMap<QACasesReportColumn, Object> filter, 
			int startLine, int pageLenght) {
		List<QACasesReportLine> lines = new ArrayList<QACasesReportLine>();
		for (int i = 1; i < pageLenght; i++) {
			lines.addAll(getQACasesReportLines(qaCoordinate, ruleUuid));
		}
		return new QACasesReportPage(lines, sortBy, filter, startLine, startLine + pageLenght -1, 550);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllSeverities()
	 */
	@Override
	public List<Severity> getAllSeverities() {
		List<Severity> severities = new ArrayList<Severity>();
		severities.add(new Severity(UUID.randomUUID(), "Low", ""));
		severities.add(new Severity(UUID.randomUUID(), "Medium", ""));
		severities.add(new Severity(UUID.randomUUID(), "High", ""));
		return severities;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getSeverity(java.util.UUID)
	 */
	@Override
	public Severity getSeverity(UUID severityUuid) {
		for (Severity loopSeverity : getAllSeverities()) {
			if (loopSeverity.getSeverityUuid().equals(severityUuid)) {
				return loopSeverity;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllCategories()
	 */
	@Override
	public List<Category> getAllCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getCategory(java.util.UUID)
	 */
	@Override
	public Category getCategory(UUID categoryUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistQACaseList(java.util.List)
	 */
	@Override
	public void persistQACaseList(List<QACase> qaCaseList) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistQAComment(org.ihtsdo.qa.store.model.QaCaseComment)
	 */
	@Override
	public void persistQAComment(QaCaseComment comment) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
