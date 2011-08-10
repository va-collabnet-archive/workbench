package org.ihtsdo.qadb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.ibatis.jdbc.SqlBuilder;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.ihtsdo.qadb.data.Category;
import org.ihtsdo.qadb.data.DispStatusCount;
import org.ihtsdo.qadb.data.DispositionStatus;
import org.ihtsdo.qadb.data.Execution;
import org.ihtsdo.qadb.data.Finding;
import org.ihtsdo.qadb.data.QACase;
import org.ihtsdo.qadb.data.QACaseVersion;
import org.ihtsdo.qadb.data.QAComment;
import org.ihtsdo.qadb.data.QACoordinate;
import org.ihtsdo.qadb.data.QADatabase;
import org.ihtsdo.qadb.data.Rule;
import org.ihtsdo.qadb.data.RuleFilterCoords;
import org.ihtsdo.qadb.data.Severity;
import org.ihtsdo.qadb.data.TerminologyComponent;
import org.ihtsdo.qadb.data.view.QACasesReportColumn;
import org.ihtsdo.qadb.data.view.QACasesReportLine;
import org.ihtsdo.qadb.data.view.QACasesReportPage;
import org.ihtsdo.qadb.data.view.RulesReportColumn;
import org.ihtsdo.qadb.data.view.RulesReportLine;
import org.ihtsdo.qadb.data.view.RulesReportPage;
import org.ihtsdo.qadb.helper.CaseReportLineComparator;
import org.ihtsdo.qadb.helper.MyBatisUtil;
import org.ihtsdo.qadb.helper.RuleReportLineComparator;

public class QAStoreImpl implements QAStoreBI {

	private final static Logger logger = Logger.getLogger(QAStoreImpl.class);

	private SqlSession sqlSession = null;

	public QAStoreImpl(SqlSession sqlSession) {
		super();
		logger.debug("Creating new instance of QAStoreImpl");
		this.sqlSession = sqlSession;
	}

	@Override
	public TerminologyComponent getComponent(String componentUuid) {
		logger.debug("Getting Component for UUID: " + componentUuid);
		TerminologyComponent termComp = (TerminologyComponent) sqlSession.selectOne("org.ihtsdo.qadb.data.TerminologyComponentMapper.selectTermComponentByUUID", componentUuid);
		return termComp;
	}

	@Override
	public Rule getRule(String ruleUuid) {
		Rule rule = new Rule();
		rule.setRuleUuid(ruleUuid);

		rule = (Rule) sqlSession.selectOne("org.ihtsdo.qadb.data.RuleMapper.selectRuleByUUID", rule);

		return rule;
	}

	@Override
	public Finding getFinding(String findingUuid) {
		Finding finding = new Finding();
		finding.setFindingUuid(findingUuid);
		finding = (Finding) sqlSession.selectOne("org.ihtsdo.qadb.data.FindingMapper.selectFindingByUUID", finding);

		return finding;
	}

	@Override
	public Execution getExecution(String executionUuid) {
		Execution execution = new Execution();
		execution.setExecutionUuid(executionUuid);
		execution = (Execution) sqlSession.selectOne("org.ihtsdo.qadb.data.ExecutionMapper.selectExecutionByUUID", execution);

		return execution;
	}

	@Override
	public QACase getQACase(String qaCaseUuid) {
		QACase qaCase = new QACase();
		qaCase.setCaseUuid(qaCaseUuid);
		qaCase = (QACase) sqlSession.selectOne("org.ihtsdo.qadb.data.QACaseMapper.selectQACaseByUUID", qaCase);

		return qaCase;
	}

	@Override
	public DispositionStatus getDispositionStatus(String dispositionStatusUuid) {
		DispositionStatus dispStatus = new DispositionStatus();
		dispStatus = (DispositionStatus) sqlSession.selectOne("org.ihtsdo.qadb.data.DispositionStatus.selectDispositionStatusByUUID", dispositionStatusUuid);
		return dispStatus;
	}

	@Override
	public QADatabase getQADatabase(String databaseUuid) {
		QADatabase result = (QADatabase) sqlSession.selectOne("org.ihtsdo.qadb.data.QADatabaseMapper.selectDatabaseByUUID", databaseUuid);
		return result;
	}

	@Override
	public List<QADatabase> getAllDatabases() {
		@SuppressWarnings("unchecked")
		List<QADatabase> databases = (List<QADatabase>) sqlSession.selectList("org.ihtsdo.qadb.data.QADatabaseMapper.selectAllDatabases");
		return databases;
	}

	@Override
	public List<QADatabase> getAllDatabasesForPath(String pathUuid) {
		List<QADatabase> databases = new ArrayList<QADatabase>();

		@SuppressWarnings("unchecked")
		List<String> databaseUuids = sqlSession.selectList("org.ihtsdo.qadb.data.ExecutionMapper.selectDistinctDatabaseUuidsForPaht", pathUuid.toString());
		for (String databaseUuid : databaseUuids) {
			QADatabase database = getQADatabase(databaseUuid);
			databases.add(database);
		}
		return databases;
	}

	@Override
	public List<TerminologyComponent> getAllPaths() {
		List<TerminologyComponent> paths = new ArrayList<TerminologyComponent>();

		@SuppressWarnings("unchecked")
		List<Execution> pathUuids = sqlSession.selectList("org.ihtsdo.qadb.data.ExecutionMapper.selectDistinctPaths");

		for (Execution exec : pathUuids) {
			TerminologyComponent termComp = new TerminologyComponent(exec.getPathUuid(), exec.getPathName(), null);
			paths.add(termComp);
		}
		return paths;
	}

	@Override
	public List<TerminologyComponent> getAllPathsForDatabase(String databaseUuid) {
		List<TerminologyComponent> paths = new ArrayList<TerminologyComponent>();

		@SuppressWarnings("unchecked")
		List<Execution> pathUuids = sqlSession.selectList("org.ihtsdo.qadb.data.ExecutionMapper.selectDistinctPathsForDatabase", databaseUuid);

		for (Execution execution : pathUuids) {
			TerminologyComponent termComp = new TerminologyComponent();
			termComp.setComponentName(execution.getPathName());
			termComp.setComponentUuid(execution.getPathUuid());
			paths.add(termComp);
		}
		return paths;
	}

	@Override
	public List<String> getAllTimesForPath(String databaseUuid, String pathUuid) {
		List<String> dates = new ArrayList<String>();
		dates.add("latest");
		return dates;
	}

	@Override
	public List<Finding> getFindingsForExecution(String executionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Finding> getFindingsForComponent(QACoordinate coordinate, String componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Finding> getFindingsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getAllQACases(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForComponent(QACoordinate coordinate, String componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForStatus(QACoordinate coordinate, boolean isActive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForDispositionStatus(QACoordinate coordinate, String dispositionStatusUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<QACase> getQACasesForRule(QACoordinate coordinate, String ruleUuid, List<RulesReportColumn> sortBy, HashMap<RulesReportColumn, Object> filter) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<QACase> qaCases = null;

		try {
			QACase param = new QACase();
			param.setPathUuid(coordinate.getPathUuid());
			param.setDatabaseUuid(coordinate.getDatabaseUuid());
		
			Date effectiveTime = sdf.parse(coordinate.getViewPointTime());
			Calendar effectiveCal = new GregorianCalendar();
			effectiveCal.setTime(effectiveTime);
			param.setEffectiveTime(effectiveCal);
			param.setRuleUuid(ruleUuid);

			qaCases = (List<QACase>) sqlSession.selectList("org.ihtsdo.qadb.data.QACaseMapper.selectCasesByCoordAndRule", param);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return qaCases;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<QACase> getQACasesForRule(QACoordinate coordinate, String ruleUuid) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<QACase> qaCases = null;

		try {
			QACase param = new QACase();
			param.setPathUuid(coordinate.getPathUuid());
			param.setDatabaseUuid(coordinate.getDatabaseUuid());
			
			Date effectiveTime = sdf.parse(coordinate.getViewPointTime());
			Calendar effectiveCal = new GregorianCalendar();
			effectiveCal.setTime(effectiveTime);
			param.setEffectiveTime(effectiveCal);
			param.setRuleUuid(ruleUuid);

			qaCases = (List<QACase>) sqlSession.selectList("org.ihtsdo.qadb.data.QACaseMapper.selectCasesByCoordAndRule", param);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return qaCases;
	}

	@Override
	public List<DispositionStatus> getAllDispositionStatus() {
		List<DispositionStatus> dispStatuses = null;
		try {
			dispStatuses = (List<DispositionStatus>) sqlSession.selectList("org.ihtsdo.qadb.data.DispositionStatusMapper.selectAllDispositionStatuses");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dispStatuses;
	}

	@Override
	public List<QACaseVersion> getQACaseVersions(QACoordinate coordinate, String qaCaseUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Execution> getAllExecutions(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Execution> getExecutionsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Rule> getAllRules() {
		List<Rule> rule = null;

		rule = (List<Rule>) sqlSession.selectList("org.ihtsdo.qadb.data.RuleMapper.selectAllRules");

		return rule;
	}

	@Override
	public HashMap<String, Integer> getDispositionStatusCountsForRule(QACoordinate coordinate, String ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Boolean, Integer> getStatusCountsForRule(QACoordinate coordinate, String ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getRuleLastExecutionTime(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<RulesReportLine> getRulesReportLines(QACoordinate qaCoordinate, LinkedHashMap<Integer, Boolean> sortBy, HashMap<Integer, Object> filter, int startLine, int pageLenght) {
		logger.debug("Getting rules report lines...");
		List<RulesReportLine> lines = null;
		try {
			RuleFilterCoords coords = new RuleFilterCoords();
			coords.setDatabaseUuid(qaCoordinate.getDatabaseUuid().toString());
			coords.setPathUuid(qaCoordinate.getPathUuid().toString());
			coords.setViewPointTime(qaCoordinate.getViewPointTime());
			List<DispositionStatus> existingDispStatuses = getAllDispositionStatus();
			if (filter != null) {
				if (filter.containsKey(RulesReportColumn.RULE_NAME)) {
					coords.setName("%" + filter.get(RulesReportColumn.RULE_NAME).toString() + "%");
				}
				if (filter.containsKey(RulesReportColumn.RULE_CODE)) {
					coords.setRuleCode("%" + filter.get(RulesReportColumn.RULE_CODE).toString() + "%");
				}
				if (filter.containsKey(RulesReportColumn.CATEGORY)) {
					coords.setRuleCategory(filter.get(RulesReportColumn.CATEGORY).toString());
				}
				if (filter.containsKey(RulesReportColumn.SEVERITY)) {
					coords.setSeverity(filter.get(RulesReportColumn.SEVERITY).toString());
				}
			}
			@SuppressWarnings("unchecked")
			List<Rule> rules = sqlSession.selectList("org.ihtsdo.qadb.data.RuleMapper.selectRulesByCoords", coords);
			logger.debug("rules selected by coordinates...");
			lines = new LinkedList<RulesReportLine>();
			Set<Integer> filterset = filter.keySet();
			for (Integer integer : filterset) {
				logger.info("Filter " + integer + " " + filter.get(integer));
			}
			for (Rule rule : rules) {
				try {
					UUID.fromString(rule.getRuleUuid());
				} catch (IllegalArgumentException e) {
					logger.error(e.getCause());
					continue;
				}
				logger.debug(rule.getSeverity());
				RulesReportLine line = new RulesReportLine();
				coords.setRuleUuid(rule.getRuleUuid().toString());
				coords.setStatus("1");
				Integer openStatus = (Integer) sqlSession.selectOne("org.ihtsdo.qadb.data.RuleMapper.selectStatusCount", coords);
				coords.setStatus("0");
				Integer closedStatus = (Integer) sqlSession.selectOne("org.ihtsdo.qadb.data.RuleMapper.selectStatusCount", coords);
				logger.debug("Open status count: " + openStatus);
				logger.debug("Closed status count: " + closedStatus);
				if (filter != null) {
					if (filter.containsKey(RulesReportColumn.STATUS)) {
						String statusFilter = filter.get(RulesReportColumn.STATUS).toString();
						logger.debug("Status object " + filter.get(RulesReportColumn.STATUS).toString());
						if (statusFilter.equalsIgnoreCase("open cases")) {
							if (openStatus == null) {
								continue;
							}
						} else if (statusFilter.equalsIgnoreCase("no open cases")) {
							if (openStatus != null) {
								continue;
							}
						} else if (statusFilter.equalsIgnoreCase("closed cases")) {
							if (closedStatus == null) {
								continue;
							}
						} else if (statusFilter.equalsIgnoreCase("no closed cases")) {
							if (closedStatus != null) {
								continue;
							}
						}
					}
				}

				@SuppressWarnings("unchecked")
				List<DispStatusCount> dispStatusCounts = sqlSession.selectList("org.ihtsdo.qadb.data.RuleMapper.selectDispositionStatusCounts", coords);
				boolean filterExists = false;
				if (filter != null) {
					if (filter.containsKey(new Integer(RulesReportColumn.DISPOSITION_STATUS))) {
						String dispStatusFiltr = filter.get(RulesReportColumn.DISPOSITION_STATUS).toString();
						for (DispStatusCount dispStatusCount : dispStatusCounts) {
							if (dispStatusFiltr.equals(dispStatusCount.getDispStatus()) && dispStatusCount.getStatusCount().intValue() > 0) {
								filterExists = true;
							}
						}
						if (!filterExists) {
							continue;
						}
					}
				}

				HashMap<Boolean, Integer> statusCounts = new HashMap<Boolean, Integer>();
				if (closedStatus != null) {
					statusCounts.put(false, closedStatus);
				} else {
					statusCounts.put(false, new Integer(0));
				}

				if (openStatus != null) {
					statusCounts.put(true, openStatus);
				} else {
					statusCounts.put(true, new Integer(0));
				}

				HashMap<String, Integer> statusesResult = new HashMap<String, Integer>();
				for (DispositionStatus dispositionStatus : existingDispStatuses) {
					statusesResult.put(dispositionStatus.getDispositionStatusUuid().toString(), new Integer(0));
				}
				for (DispStatusCount dspStatus : dispStatusCounts) {
					statusesResult.put(dspStatus.getDispStatus(), dspStatus.getStatusCount());
				}

				line.setDispositionStatusCount(statusesResult);
				line.setStatusCount(statusCounts);
				line.setRule(rule);
				lines.add(line);
			}

			sortLines(lines, sortBy);
			logger.debug("Total: " + lines.size());
			logger.debug("Start Line: " + startLine);
			logger.debug("End Line: " + (startLine + pageLenght));
			logger.debug("Page Lenght: " + pageLenght);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lines;
	}

	private List<RulesReportLine> reduceLines(List<RulesReportLine> lines, int startLine, int pageLenght) {
		int total = lines.size();

		if (startLine + pageLenght - 1 > total) {
			return lines.subList(startLine - 1, total);
		}
		return lines.subList(startLine - 1, startLine + pageLenght - 1);
	}

	private void sortLines(List<RulesReportLine> lines, LinkedHashMap<Integer, Boolean> sortBy) {
		Collections.sort(lines, new RuleReportLineComparator(sortBy, getAllDispositionStatus()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TerminologyComponent> getAllComponents() {
		List<TerminologyComponent> components = (List<TerminologyComponent>) sqlSession.selectList("org.ihtsdo.qadb.data.TerminologyComponentMapper.selectAllComponents");

		return components;
	}

	@Override
	public String getExecutionRulesDetails(String executionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExecutionOutcomeDetails(String executionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void persistComponent(TerminologyComponent component) {
		// TODO Auto-generated method stub

	}

	@Override
	public void persistCommentList(List<QAComment> componentList) {
		for (QAComment qaComment : componentList) {
			sqlSession.update("org.ihtsdo.mybatis.gen.QACommentMapper.insertComment", qaComment);
		}
	}

	@Override
	public void persistComment(QAComment comment) {
		if(comment.getCommentUuid() == null){
			sqlSession.update("org.ihtsdo.mybatis.gen.QACommentMapper.insertComment", comment);
		}
	}

	@Override
	public void persistRule(Rule rule) {
		logger.debug("PERSISTING RULE" + rule);
		Rule exists = getRule(rule.getRuleUuid());
		if (exists != null) {
			sqlSession.update("org.ihtsdo.qadb.data.RuleMapper.updateQaRule", rule);
		}
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
		logger.debug("PERSISTING QA CASE" + qaCase);
		QACase exists = getQACase(qaCase.getCaseUuid());
		if (exists != null) {
			logger.debug("QACASE EXISTS");
			sqlSession.update("org.ihtsdo.qadb.data.QACaseMapper.updateQaCase", qaCase);
			logger.debug("QACASE UPDATED WAITING FOR COMMIT..");
		}
	}

	@Override
	public void persistQADatabase(QADatabase database) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Severity> getAllSeverities() {
		logger.debug("Getting all severities..");
		List<Severity> components = (List<Severity>) sqlSession.selectList("org.ihtsdo.qadb.data.Severity.selectAllSeverities");
		logger.debug("total severities: " + components.size());
		return components;
	}

	@Override
	public Severity getSeverity(String severityUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RulesReportPage getRulesReportLinesByPage(QACoordinate qaCoordinate, LinkedHashMap<Integer, Boolean> sortBy, HashMap<Integer, Object> filter, int startLine, int pageLenght) {
		List<RulesReportLine> lines = new ArrayList<RulesReportLine>();
		Integer totalLines = 0;
		List<RulesReportLine> reducedLines = new ArrayList<RulesReportLine>();
		try {
			lines.addAll(getRulesReportLines(qaCoordinate, sortBy, filter, startLine, pageLenght));
			totalLines = lines.size();
			reducedLines = reduceLines(lines, startLine, pageLenght);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new RulesReportPage(reducedLines, sortBy, filter, startLine, startLine + reducedLines.size() - 1, totalLines);
	}
	
	@Override
	public List<QACasesReportLine> getQACasesReportLines(QACoordinate qaCoordinate, String ruleUuid) {
		List<QACasesReportLine> lines = new ArrayList<QACasesReportLine>();

		RuleFilterCoords coords = new RuleFilterCoords();
		coords.setDatabaseUuid(qaCoordinate.getDatabaseUuid());
		coords.setPathUuid(qaCoordinate.getPathUuid());
		coords.setRuleUuid(ruleUuid);
		coords.setViewPointTime(qaCoordinate.getViewPointTime());
		long queryStartTime = System.currentTimeMillis();
		List<QACase> ruleCases = sqlSession.selectList("org.ihtsdo.qadb.data.QACaseMapper.selectRuleCases", coords);
		long queryEndTime = System.currentTimeMillis();
		logger.info(ruleCases.size() + " Rule cases selected in: " + ((queryEndTime - queryStartTime) / 1000) + " Seconds");

		for (QACase qaCase : ruleCases) {
			QACasesReportLine loopLine = new QACasesReportLine(qaCase, qaCase.getComponentUuid(), qaCase.getDispositionStatus());
			lines.add(loopLine);
		}
		return lines;
	}

	public List<QACasesReportLine> getQACasesReportLines(QACoordinate qaCoordinate, String ruleUuid, HashMap<Integer, Object> filter, int startLine, int pageLenght) {
		List<QACasesReportLine> lines = new ArrayList<QACasesReportLine>();

		RuleFilterCoords coords = new RuleFilterCoords();
		coords.setDatabaseUuid(qaCoordinate.getDatabaseUuid());
		coords.setPathUuid(qaCoordinate.getPathUuid());
		coords.setRuleUuid(ruleUuid);
		coords.setViewPointTime(qaCoordinate.getViewPointTime());
		coords.setStartLine(startLine);
		coords.setPageLenght(pageLenght);
		if (filter != null && filter.containsKey(QACasesReportColumn.CONCEPT_NAME.getColumnNumber())) {
			coords.setName("%" + filter.get(QACasesReportColumn.CONCEPT_NAME.getColumnNumber()).toString() + "%");
		}

		long queryStartTime = System.currentTimeMillis();
		List<QACase> ruleCases = sqlSession.selectList("org.ihtsdo.qadb.data.QACaseMapper.selectRuleCases", coords);
		long queryEndTime = System.currentTimeMillis();
		logger.info(ruleCases.size() + " Rule cases selected in: " + ((queryEndTime - queryStartTime) / 1000) + " Seconds");

		if (filter != null && (filter.containsKey(QACasesReportColumn.DISPOSITION.getColumnNumber()) 
				|| filter.containsKey(QACasesReportColumn.STATUS.getColumnNumber()) 
				|| filter.containsKey(QACasesReportColumn.ASSIGNED_TO.getColumnNumber()))) {
			Object dispoFilterValue = filter.get(QACasesReportColumn.DISPOSITION.getColumnNumber());
			Object statusFilterValue = filter.get(QACasesReportColumn.STATUS.getColumnNumber());
			Object assignedToFilter = filter.get(QACasesReportColumn.ASSIGNED_TO.getColumnNumber());
			logger.debug("Disposition Status Filter " + dispoFilterValue);
			logger.debug("Status Filter" + statusFilterValue);
			for (QACase qaCase : ruleCases) {
				if (dispoFilterValue != null) {
					logger.info("DISPO STATUS FILTER");
					logger.info(qaCase.getDispositionStatus().getDispositionStatusUuid());
					logger.info(dispoFilterValue.toString());
					logger.info(!qaCase.getDispositionStatus().getDispositionStatusUuid().equals(dispoFilterValue.toString()));
					if (!qaCase.getDispositionStatus().getDispositionStatusUuid().equals(dispoFilterValue.toString())) {
						logger.debug("Ignoring disposition status filtered case");
						continue;
					}
				}
				if (statusFilterValue != null) {
					logger.info("STATUS FILTER");
					logger.info(qaCase.isActive() && statusFilterValue.toString().equalsIgnoreCase("Closed"));
					logger.info(qaCase.isActive());
					logger.info(statusFilterValue.toString());
					if (qaCase.isActive() && statusFilterValue.toString().equalsIgnoreCase("Closed")) {
						logger.debug("Ignoring status filtered case");
						continue;
					} else if (!qaCase.isActive() && statusFilterValue.toString().equalsIgnoreCase("Open")) {
						logger.debug("Ignoring status filtered case");
						continue;
					}
				}
				if(assignedToFilter != null){
					if(qaCase.getAssignedTo() == null || !qaCase.getAssignedTo().equals(assignedToFilter.toString())){
						continue;
					}
				}
				QACasesReportLine loopLine = new QACasesReportLine(qaCase, qaCase.getComponentUuid(), qaCase.getDispositionStatus());
				lines.add(loopLine);
			}
		} else {
			for (QACase qaCase : ruleCases) {
				QACasesReportLine loopLine = new QACasesReportLine(qaCase, qaCase.getComponentUuid(), qaCase.getDispositionStatus());
				lines.add(loopLine);
			}
		}
		logger.info("Returning " + lines.size() + " Lines");
		return lines;
	}

	public static void main(String[] args) {
		try {
			ResultSet x = MyBatisUtil.getSessionFactory().openSession().getConnection().createStatement().executeQuery(getDynamicSql());
			x.next();

			System.out.println(x.getObject(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getDynamicSql() {
		SqlBuilder.BEGIN();
		// Clears ThreadLocal variable
		SqlBuilder.SELECT("*");
		SqlBuilder.FROM("qa_case");
		return SqlBuilder.SQL();
	}

	@Override
	public List<RulesReportLine> getRulesReportLines(QACoordinate qaCoordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QACasesReportPage getQACasesReportLinesByPage(QACoordinate qaCoordinate, String ruleUuid, LinkedHashMap<Integer, Boolean> sortBy, HashMap<Integer, Object> filter, int startLine,
			int pageLenght) {
		long startTime = System.currentTimeMillis();
		logger.info("Getting qa cases report lines by page...");
		List<QACasesReportLine> lines = getQACasesReportLines(qaCoordinate, ruleUuid, filter,startLine, pageLenght);
		List<QACasesReportLine> reduced = null;
		int totalLines = lines.size();
		logger.debug("############################## QA CASE REPORT PAGE");
		logger.debug("Total Lines: " + totalLines);
		logger.debug("Start Line: " + startLine);
		logger.debug("Page Lenght: " + pageLenght);
		if (sortBy != null && !sortBy.isEmpty()) {
			logger.debug("************SORT BY**************");
			Set<Integer> keyset = sortBy.keySet();
			for (Integer integer : keyset) {
				logger.debug(integer + " " + sortBy.get(integer));
			}
			long sortStartTime = System.currentTimeMillis();
			Collections.sort(lines, new CaseReportLineComparator(sortBy));
			long sortEndTime = System.currentTimeMillis();
			logger.info("Sorting lines finished in: " + ((sortEndTime - sortStartTime) / 1000) + " Seconds");
		}
		if (startLine + pageLenght - 1 > totalLines) {
			reduced = lines.subList(startLine - 1, totalLines);
		} else {
			reduced = lines.subList(startLine - 1, startLine + pageLenght - 1);
		}
		logger.debug("Reduced Lines: " + reduced.size());

		QACasesReportPage result = new QACasesReportPage(reduced, sortBy, filter, startLine, startLine + reduced.size() - 1, totalLines);
		long endTime = System.currentTimeMillis();
		logger.info("QA cases page found in: " + ((endTime - startTime) / 1000) + " Seconds");
		return result;
	}

	@Override
	public List<Category> getAllCategories() {
		logger.debug("Getting all severities..");
		List<Category> categories = (List<Category>) sqlSession.selectList("org.ihtsdo.qadb.data.Category.selectAllCategories");
		logger.debug("total severities: " + categories.size());
		return categories;
	}

	@Override
	public Category getCategory(String categoryUuid) {
		// TODO Auto-generated method stub
		return null;
	}

}
