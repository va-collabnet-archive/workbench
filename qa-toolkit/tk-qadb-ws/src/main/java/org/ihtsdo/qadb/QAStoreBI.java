package org.ihtsdo.qadb;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.ihtsdo.qadb.data.Category;
import org.ihtsdo.qadb.data.DispositionStatus;
import org.ihtsdo.qadb.data.Execution;
import org.ihtsdo.qadb.data.Finding;
import org.ihtsdo.qadb.data.QACase;
import org.ihtsdo.qadb.data.QACaseVersion;
import org.ihtsdo.qadb.data.QAComment;
import org.ihtsdo.qadb.data.QACoordinate;
import org.ihtsdo.qadb.data.QADatabase;
import org.ihtsdo.qadb.data.Rule;
import org.ihtsdo.qadb.data.Severity;
import org.ihtsdo.qadb.data.TerminologyComponent;
import org.ihtsdo.qadb.data.view.QACasesReportLine;
import org.ihtsdo.qadb.data.view.QACasesReportPage;
import org.ihtsdo.qadb.data.view.RulesReportLine;
import org.ihtsdo.qadb.data.view.RulesReportPage;

public interface QAStoreBI {

	public TerminologyComponent getComponent(String componentUuid);

	public Rule getRule(String ruleUuid);

	public Finding getFinding(String string);

	public Execution getExecution(String ExecutionUuid);

	public QACase getQACase(String qaCaseUuid);

	public DispositionStatus getDispositionStatus(String dispositionStatusUuid);

	public QADatabase getQADatabase(String databaseUuid);

	public List<QADatabase> getAllDatabases();

	public List<QADatabase> getAllDatabasesForPath(String pathUuid);

	public List<TerminologyComponent> getAllPaths();

	public List<TerminologyComponent> getAllPathsForDatabase(String databaseUuid);

	public List<String> getAllTimesForPath(String databaseUuid, String pathUuid);

	public List<Finding> getFindingsForExecution(String executionUuid);

	public List<Finding> getFindingsForComponent(QACoordinate coordinate, String componentUuid);

	public List<Finding> getFindingsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd);

	public List<Severity> getAllSeverities();
	
	public List<Category> getAllCategories();
	
	public Category getCategory(String categoryUuid);

	public Severity getSeverity(String severityUuid);

	public List<QACase> getAllQACases(QACoordinate coordinate);

	public List<QACase> getQACasesForComponent(QACoordinate coordinate, String componentUuid);

	public List<QACase> getQACasesForStatus(QACoordinate coordinate, boolean isActive);

	public List<QACase> getQACasesForDispositionStatus(QACoordinate coordinate, String dispositionStatusUuid);

	public List<QACase> getQACasesForRule(QACoordinate coordinate, String ruleUuid);

	public List<DispositionStatus> getAllDispositionStatus();

	public List<QACaseVersion> getQACaseVersions(QACoordinate coordinate, String qaCaseUuid);

	public List<Execution> getAllExecutions(QACoordinate coordinate);

	public List<Execution> getExecutionsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd);

	public List<Rule> getAllRules();

	public HashMap<String, Integer> getDispositionStatusCountsForRule(QACoordinate coordinate, String ruleUuid);

	public HashMap<Boolean, Integer> getStatusCountsForRule(QACoordinate coordinate, String ruleUuid);

	public Date getRuleLastExecutionTime(QACoordinate coordinate);

	public List<RulesReportLine> getRulesReportLines(QACoordinate qaCoordinate);

	public RulesReportPage getRulesReportLinesByPage(QACoordinate qaCoordinate, LinkedHashMap<Integer, Boolean> sorteBy, 
			HashMap<Integer, Object> filters, int startLine,int pageLenght);

	public List<QACasesReportLine> getQACasesReportLines(QACoordinate qaCoordinate, String ruleUuid);

	public QACasesReportPage getQACasesReportLinesByPage(QACoordinate qaCoordinate, String ruleUuid, LinkedHashMap<Integer, Boolean> sortBy,
			HashMap<Integer, Object> filter, int startLine, int pageLenght);

	public List<TerminologyComponent> getAllComponents();

	public String getExecutionRulesDetails(String executionUuid);

	public String getExecutionOutcomeDetails(String executionUuid);

	public void persistComponent(TerminologyComponent component);

	public void persistRule(Rule rule);

	public void persistFinding(Finding finding);

	public void persistExecution(Execution execution);

	public void persistQACase(QACase qaCase);

	public void persistQADatabase(QADatabase database);

	void persistCommentList(List<QAComment> componentList);

	void persistComment(QAComment comment);

}
