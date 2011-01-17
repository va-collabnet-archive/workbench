package org.ihtsdo.qa.store;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.qa.store.model.Category;
import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.Execution;
import org.ihtsdo.qa.store.model.Finding;
import org.ihtsdo.qa.store.model.QACase;
import org.ihtsdo.qa.store.model.QACaseVersion;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.QADatabase;
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.Severity;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.qa.store.model.view.QACasesReportColumn;
import org.ihtsdo.qa.store.model.view.QACasesReportLine;
import org.ihtsdo.qa.store.model.view.QACasesReportPage;
import org.ihtsdo.qa.store.model.view.RulesReportColumn;
import org.ihtsdo.qa.store.model.view.RulesReportLine;
import org.ihtsdo.qa.store.model.view.RulesReportPage;

public interface QAStoreBI {
	
	public TerminologyComponent getComponent(UUID componentUuid);
	public Rule getRule(UUID ruleUuid);
	public Finding getFinding(UUID findingUuid);
	public Execution getExecution(UUID ExecutionUuid);
	public QACase getQACase(UUID qaCaseUuid);
	public DispositionStatus getDispositionStatus(UUID dispositionStatusUuid);
	public QADatabase getQADatabase(UUID databaseUuid);
	
	public List<QADatabase> getAllDatabases();
	public List<QADatabase> getAllDatabasesForPath(UUID pathUuid);
	public List<TerminologyComponent> getAllPaths();
	public List<TerminologyComponent> getAllPathsForDatabase(UUID databaseUuid);
	public List<String> getAllTimesForPath(UUID databaseUuid, UUID pathUuid);
	
	public List<Finding> getFindingsForExecution(UUID executionUuid);
	public List<Finding> getFindingsForComponent(QACoordinate coordinate, UUID componentUuid);
	public List<Finding> getFindingsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd);
	
	public List<Severity> getAllSeverities();
	public Severity getSeverity(UUID severityUuid);

	public List<Category> getAllCategories();
	public Category getCategory(UUID categoryUuid);
	
	public List<QACase> getAllQACases(QACoordinate coordinate);
	public List<QACase> getQACasesForComponent(QACoordinate coordinate, UUID componentUuid);
	public List<QACase> getQACasesForStatus(QACoordinate coordinate, boolean isActive);
	public List<QACase> getQACasesForDispositionStatus(QACoordinate coordinate, UUID dispositionStatusUuid);
	public List<QACase> getQACasesForRule(QACoordinate coordinate, UUID ruleUuid);
	
	public List<DispositionStatus> getAllDispositionStatus();
	
	public List<QACaseVersion> getQACaseVersions(QACoordinate coordinate, UUID qaCaseUuid);
	
	public List<Execution> getAllExecutions(QACoordinate coordinate);
	public List<Execution> getExecutionsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd);
	
	public List<Rule> getAllRules();
	public HashMap<UUID,Integer> getDispositionStatusCountsForRule(QACoordinate coordinate, UUID ruleUuid);
	public HashMap<Boolean,Integer> getStatusCountsForRule(QACoordinate coordinate, UUID ruleUuid);
	public Date getRuleLastExecutionTime(QACoordinate coordinate);
	
	public List<RulesReportLine> getRulesReportLines(QACoordinate qaCoordinate);
	public RulesReportPage getRulesReportLinesByPage(QACoordinate qaCoordinate, LinkedHashMap<RulesReportColumn,Boolean> sortBy, HashMap<RulesReportColumn, Object> filter, int startLine, int pageLenght);
	public List<QACasesReportLine> getQACasesReportLines(QACoordinate qaCoordinate, UUID ruleUuid);
	public QACasesReportPage getQACasesReportLinesByPage(QACoordinate qaCoordinate, UUID ruleUuid, LinkedHashMap<QACasesReportColumn,Boolean> sortBy, HashMap<QACasesReportColumn, Object> filter, int startLine, int pageLenght);
	
	public List<TerminologyComponent> getAllComponents();
	
	public String getExecutionRulesDetails(UUID executionUuid);
	public String getExecutionOutcomeDetails(UUID executionUuid);
	
	public void persistComponent(TerminologyComponent component);
	public void persistRule(Rule rule);
	public void persistFinding(Finding finding);
	public void persistExecution(Execution execution);
	public void persistQACase(QACase qaCase);
	public void persistQADatabase(QADatabase database);
	
}
