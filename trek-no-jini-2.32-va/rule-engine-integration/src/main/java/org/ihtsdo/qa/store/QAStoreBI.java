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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * The Interface QAStoreBI.
 */
public interface QAStoreBI {
	
	/**
	 * Gets the component.
	 *
	 * @param componentUuid the component uuid
	 * @return the component
	 */
	public TerminologyComponent getComponent(UUID componentUuid);
	
	/**
	 * Gets the rule.
	 *
	 * @param ruleUuid the rule uuid
	 * @return the rule
	 */
	public Rule getRule(UUID ruleUuid);
	
	/**
	 * Gets the finding.
	 *
	 * @param findingUuid the finding uuid
	 * @return the finding
	 */
	public Finding getFinding(UUID findingUuid);
	
	/**
	 * Gets the execution.
	 *
	 * @param ExecutionUuid the execution uuid
	 * @return the execution
	 */
	public Execution getExecution(UUID ExecutionUuid);
	
	/**
	 * Gets the qA case.
	 *
	 * @param qaCaseUuid the qa case uuid
	 * @return the qA case
	 */
	public QACase getQACase(UUID qaCaseUuid);
	
	/**
	 * Gets the disposition status.
	 *
	 * @param dispositionStatusUuid the disposition status uuid
	 * @return the disposition status
	 */
	public DispositionStatus getDispositionStatus(UUID dispositionStatusUuid);
	
	/**
	 * Gets the qA database.
	 *
	 * @param databaseUuid the database uuid
	 * @return the qA database
	 */
	public QADatabase getQADatabase(UUID databaseUuid);
	
	/**
	 * Gets the all databases.
	 *
	 * @return the all databases
	 */
	public List<QADatabase> getAllDatabases();
	
	/**
	 * Gets the all databases for path.
	 *
	 * @param pathUuid the path uuid
	 * @return the all databases for path
	 */
	public List<QADatabase> getAllDatabasesForPath(UUID pathUuid);
	
	/**
	 * Gets the all paths.
	 *
	 * @return the all paths
	 */
	public List<TerminologyComponent> getAllPaths();
	
	/**
	 * Gets the all paths for database.
	 *
	 * @param databaseUuid the database uuid
	 * @return the all paths for database
	 */
	public List<TerminologyComponent> getAllPathsForDatabase(UUID databaseUuid);
	
	/**
	 * Gets the all times for path.
	 *
	 * @param databaseUuid the database uuid
	 * @param pathUuid the path uuid
	 * @return the all times for path
	 */
	public List<String> getAllTimesForPath(UUID databaseUuid, UUID pathUuid);
	
	/**
	 * Gets the findings for execution.
	 *
	 * @param executionUuid the execution uuid
	 * @return the findings for execution
	 */
	public List<Finding> getFindingsForExecution(UUID executionUuid);
	
	/**
	 * Gets the findings for component.
	 *
	 * @param coordinate the coordinate
	 * @param componentUuid the component uuid
	 * @return the findings for component
	 */
	public List<Finding> getFindingsForComponent(QACoordinate coordinate, UUID componentUuid);
	
	/**
	 * Gets the findings for period.
	 *
	 * @param coordinate the coordinate
	 * @param dateStart the date start
	 * @param dateEnd the date end
	 * @return the findings for period
	 */
	public List<Finding> getFindingsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd);
	
	/**
	 * Gets the all severities.
	 *
	 * @return the all severities
	 */
	public List<Severity> getAllSeverities();
	
	/**
	 * Gets the severity.
	 *
	 * @param severityUuid the severity uuid
	 * @return the severity
	 */
	public Severity getSeverity(UUID severityUuid);

	/**
	 * Gets the all categories.
	 *
	 * @return the all categories
	 */
	public List<Category> getAllCategories();
	
	/**
	 * Gets the category.
	 *
	 * @param categoryUuid the category uuid
	 * @return the category
	 */
	public Category getCategory(UUID categoryUuid);
	
	/**
	 * Gets the all qa cases.
	 *
	 * @param coordinate the coordinate
	 * @return the all qa cases
	 */
	public List<QACase> getAllQACases(QACoordinate coordinate);
	
	/**
	 * Gets the qA cases for component.
	 *
	 * @param coordinate the coordinate
	 * @param componentUuid the component uuid
	 * @return the qA cases for component
	 */
	public List<QACase> getQACasesForComponent(QACoordinate coordinate, UUID componentUuid);
	
	/**
	 * Gets the qA cases for status.
	 *
	 * @param coordinate the coordinate
	 * @param isActive the is active
	 * @return the qA cases for status
	 */
	public List<QACase> getQACasesForStatus(QACoordinate coordinate, boolean isActive);
	
	/**
	 * Gets the qA cases for disposition status.
	 *
	 * @param coordinate the coordinate
	 * @param dispositionStatusUuid the disposition status uuid
	 * @return the qA cases for disposition status
	 */
	public List<QACase> getQACasesForDispositionStatus(QACoordinate coordinate, UUID dispositionStatusUuid);
	
	/**
	 * Gets the qA cases for rule.
	 *
	 * @param coordinate the coordinate
	 * @param ruleUuid the rule uuid
	 * @return the qA cases for rule
	 */
	public List<QACase> getQACasesForRule(QACoordinate coordinate, UUID ruleUuid);
	
	/**
	 * Gets the all disposition status.
	 *
	 * @return the all disposition status
	 */
	public List<DispositionStatus> getAllDispositionStatus();
	
	/**
	 * Gets the qA case versions.
	 *
	 * @param coordinate the coordinate
	 * @param qaCaseUuid the qa case uuid
	 * @return the qA case versions
	 */
	public List<QACaseVersion> getQACaseVersions(QACoordinate coordinate, UUID qaCaseUuid);
	
	/**
	 * Gets the all executions.
	 *
	 * @param coordinate the coordinate
	 * @return the all executions
	 */
	public List<Execution> getAllExecutions(QACoordinate coordinate);
	
	/**
	 * Gets the executions for period.
	 *
	 * @param coordinate the coordinate
	 * @param dateStart the date start
	 * @param dateEnd the date end
	 * @return the executions for period
	 */
	public List<Execution> getExecutionsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd);
	
	/**
	 * Gets the all rules.
	 *
	 * @return the all rules
	 */
	public List<Rule> getAllRules();
	
	/**
	 * Gets the disposition status counts for rule.
	 *
	 * @param coordinate the coordinate
	 * @param ruleUuid the rule uuid
	 * @return the disposition status counts for rule
	 */
	public HashMap<UUID,Integer> getDispositionStatusCountsForRule(QACoordinate coordinate, UUID ruleUuid);
	
	/**
	 * Gets the status counts for rule.
	 *
	 * @param coordinate the coordinate
	 * @param ruleUuid the rule uuid
	 * @return the status counts for rule
	 */
	public HashMap<Boolean,Integer> getStatusCountsForRule(QACoordinate coordinate, UUID ruleUuid);
	
	/**
	 * Gets the rule last execution time.
	 *
	 * @param coordinate the coordinate
	 * @return the rule last execution time
	 */
	public Date getRuleLastExecutionTime(QACoordinate coordinate);
	
	/**
	 * Gets the rules report lines.
	 *
	 * @param qaCoordinate the qa coordinate
	 * @return the rules report lines
	 */
	public List<RulesReportLine> getRulesReportLines(QACoordinate qaCoordinate);
	
	/**
	 * Gets the rules report lines by page.
	 *
	 * @param qaCoordinate the qa coordinate
	 * @param sortBy the sort by
	 * @param filter the filter
	 * @param startLine the start line
	 * @param pageLenght the page lenght
	 * @return the rules report lines by page
	 */
	public RulesReportPage getRulesReportLinesByPage(QACoordinate qaCoordinate, Map<RulesReportColumn,Boolean> sortBy, Map<RulesReportColumn, Object> filter, int startLine, int pageLenght);
	
	/**
	 * Gets the qA cases report lines.
	 *
	 * @param qaCoordinate the qa coordinate
	 * @param ruleUuid the rule uuid
	 * @return the qA cases report lines
	 */
	public List<QACasesReportLine> getQACasesReportLines(QACoordinate qaCoordinate, UUID ruleUuid);
	
	/**
	 * Gets the qA cases report lines by page.
	 *
	 * @param qaCoordinate the qa coordinate
	 * @param ruleUuid the rule uuid
	 * @param sortBy the sort by
	 * @param filter the filter
	 * @param startLine the start line
	 * @param pageLenght the page lenght
	 * @return the qA cases report lines by page
	 */
	public QACasesReportPage getQACasesReportLinesByPage(QACoordinate qaCoordinate, UUID ruleUuid, LinkedHashMap<QACasesReportColumn,Boolean> sortBy, HashMap<QACasesReportColumn, Object> filter, int startLine, int pageLenght);
	
	/**
	 * Gets the all components.
	 *
	 * @return the all components
	 */
	public List<TerminologyComponent> getAllComponents();
	
	/**
	 * Gets the execution rules details.
	 *
	 * @param executionUuid the execution uuid
	 * @return the execution rules details
	 */
	public String getExecutionRulesDetails(UUID executionUuid);
	
	/**
	 * Gets the execution outcome details.
	 *
	 * @param executionUuid the execution uuid
	 * @return the execution outcome details
	 */
	public String getExecutionOutcomeDetails(UUID executionUuid);
	
	/**
	 * Persist component.
	 *
	 * @param component the component
	 */
	public void persistComponent(TerminologyComponent component);
	
	/**
	 * Persist rule.
	 *
	 * @param rule the rule
	 */
	public void persistRule(Rule rule);
	
	/**
	 * Persist finding.
	 *
	 * @param finding the finding
	 */
	public void persistFinding(Finding finding);
	
	/**
	 * Persist execution.
	 *
	 * @param execution the execution
	 */
	public void persistExecution(Execution execution);
	
	/**
	 * Persist qa case.
	 *
	 * @param qaCase the qa case
	 */
	public void persistQACase(QACase qaCase);
	
	/**
	 * Persist qa database.
	 *
	 * @param database the database
	 */
	public void persistQADatabase(QADatabase database);
	
	/**
	 * Persist qa case list.
	 *
	 * @param qaCaseList the qa case list
	 * @throws Exception the exception
	 */
	public void persistQACaseList(List<QACase> qaCaseList) throws  Exception;
	
	/**
	 * Persist qa comment.
	 *
	 * @param comment the comment
	 * @throws Exception the exception
	 */
	public void persistQAComment(QaCaseComment comment) throws Exception;
	
}
