package org.ihtsdo.qadb.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.ihtsdo.qadb.QAStoreBI;
import org.ihtsdo.qadb.QAStoreImpl;
import org.ihtsdo.qadb.data.Category;
import org.ihtsdo.qadb.data.DispositionStatus;
import org.ihtsdo.qadb.data.Finding;
import org.ihtsdo.qadb.data.QACase;
import org.ihtsdo.qadb.data.QAComment;
import org.ihtsdo.qadb.data.QACoordinate;
import org.ihtsdo.qadb.data.QADatabase;
import org.ihtsdo.qadb.data.Rule;
import org.ihtsdo.qadb.data.Severity;
import org.ihtsdo.qadb.data.TerminologyComponent;
import org.ihtsdo.qadb.data.view.QACasesReportLine;
import org.ihtsdo.qadb.data.view.QACasesReportPage;
import org.ihtsdo.qadb.data.view.RulesReportPage;
import org.ihtsdo.qadb.helper.MyBatisUtil;
import org.ihtsdo.qadb.ws.data.AllCategoriesResponse;
import org.ihtsdo.qadb.ws.data.AllComponentsResponse;
import org.ihtsdo.qadb.ws.data.AllDatabasesResponse;
import org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse;
import org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest;
import org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse;
import org.ihtsdo.qadb.ws.data.AllPathsResponse;
import org.ihtsdo.qadb.ws.data.AllRulesResponse;
import org.ihtsdo.qadb.ws.data.AllSeveritiesResponse;
import org.ihtsdo.qadb.ws.data.Case;
import org.ihtsdo.qadb.ws.data.CaseRequest;
import org.ihtsdo.qadb.ws.data.Component;
import org.ihtsdo.qadb.ws.data.ComponentRequest;
import org.ihtsdo.qadb.ws.data.ComponentResponse;
import org.ihtsdo.qadb.ws.data.Database;
import org.ihtsdo.qadb.ws.data.ExecutionRequest;
import org.ihtsdo.qadb.ws.data.ExecutionResponse;
import org.ihtsdo.qadb.ws.data.FindingRequest;
import org.ihtsdo.qadb.ws.data.FindingResponse;
import org.ihtsdo.qadb.ws.data.GetCaseResponse;
import org.ihtsdo.qadb.ws.data.IntBoolKeyValue;
import org.ihtsdo.qadb.ws.data.IntStrKeyValue;
import org.ihtsdo.qadb.ws.data.PersistQACaseList;
import org.ihtsdo.qadb.ws.data.PersistQACaseRequest;
import org.ihtsdo.qadb.ws.data.PersistQACommentRequest;
import org.ihtsdo.qadb.ws.data.PersistsQARuleRequest;
import org.ihtsdo.qadb.ws.data.QACaseComment;
import org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest;
import org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse;
import org.ihtsdo.qadb.ws.data.QADatabaseRequest;
import org.ihtsdo.qadb.ws.data.QADatabaseResponse;
import org.ihtsdo.qadb.ws.data.RuleRequest;
import org.ihtsdo.qadb.ws.data.RuleResponse;
import org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest;
import org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse;
import org.ihtsdo.qadb.ws.data.WsCategory;
import org.ihtsdo.qadb.ws.data.WsQACasesReportLine;

public class QadbServiceImpl implements QadbServiceSkeletonInterface {
	private static final Logger logger = Logger.getLogger(QadbServiceImpl.class);

	public ComponentResponse getComponent(ComponentRequest request) {
		logger.debug("Getting component for UUID: " + request.getComponentUuid());
		ComponentResponse resp = new ComponentResponse();
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			TerminologyComponent component = qaStore.getComponent(request.getComponentUuid());

			Component wsComponent = WsConverter.componentToWsComponent(component);
			resp.setComponentResponse(wsComponent);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public GetCaseResponse getCase(CaseRequest caseRequest) {
		GetCaseResponse resp = new GetCaseResponse();
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			QACase qaCase = qaStore.getQACase(caseRequest.getCaseUuid());

			org.ihtsdo.qadb.ws.data.Case param = WsConverter.caseToWsCase(qaCase);
			resp.setGetCaseResponse(param);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public AllRulesResponse getAllRules() {
		AllRulesResponse resp = new AllRulesResponse();
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			List<Rule> rules = qaStore.getAllRules();
			List<org.ihtsdo.qadb.ws.data.Rule> wsRuleList = new ArrayList<org.ihtsdo.qadb.ws.data.Rule>();
			for (Rule rule : rules) {
				org.ihtsdo.qadb.ws.data.Rule wsRule = WsConverter.ruleToWsRule(rule);
				wsRuleList.add(wsRule);
			}

			resp.setRule((org.ihtsdo.qadb.ws.data.Rule[]) wsRuleList.toArray());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public ExecutionResponse getExecution(ExecutionRequest executionRequest) {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		ExecutionResponse resp = new ExecutionResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			org.ihtsdo.qadb.data.Execution execution = qaStore.getExecution(executionRequest.getExecutionUuid());
			org.ihtsdo.qadb.ws.data.Execution wsExecution = null;
			wsExecution = WsConverter.executionToWsExecution(execution);
			resp.setExecutionResponse(wsExecution);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	@Override
	public RuleResponse getRule(RuleRequest ruleRequest) {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		RuleResponse resp = new RuleResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			Rule rule = qaStore.getRule(ruleRequest.getRuleUuid());
			org.ihtsdo.qadb.ws.data.Rule wsRule = null;
			wsRule = WsConverter.ruleToWsRule(rule);
			resp.setRuleResponse(wsRule);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public FindingResponse getFinding(FindingRequest findingRequest) {
		// TODO: TA VERDE.. a terminar despues
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		FindingResponse resp = new FindingResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			Finding finding = qaStore.getFinding(findingRequest.getFindingUuid());
			org.ihtsdo.qadb.ws.data.Finding wsFinding = null;
			wsFinding = WsConverter.findingToWsFinding(finding);
			resp.setFindingResponse(wsFinding);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}

		return resp;
	}

	@Override
	public AllDispositionStatusResponse getAllDispositionStatus() {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		AllDispositionStatusResponse resp = new AllDispositionStatusResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			List<DispositionStatus> dispStatuses = qaStore.getAllDispositionStatus();
			org.ihtsdo.qadb.ws.data.DispositionStatus[] wsDispStatuses = new org.ihtsdo.qadb.ws.data.DispositionStatus[dispStatuses.size()];
			int i = 0;
			for (DispositionStatus dispositionStatus : dispStatuses) {
				org.ihtsdo.qadb.ws.data.DispositionStatus wsDispStatus = WsConverter.dispositionStatusToWsDispositionStatus(dispositionStatus);
				wsDispStatuses[i] = wsDispStatus;
				i++;
			}
			resp.setDispositionStatus(wsDispStatuses);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public AllDatabasesResponse getAllDatabases() {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		AllDatabasesResponse resp = new AllDatabasesResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			List<QADatabase> qaDatabases = qaStore.getAllDatabases();
			Database[] wsDatabases = new Database[qaDatabases.size()];
			int i = 0;
			for (QADatabase qaDb : qaDatabases) {
				Database database = WsConverter.databaseToWsDatabase(qaDb);
				wsDatabases[i] = database;
				i++;
			}
			resp.setDatabase(wsDatabases);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public AllSeveritiesResponse getAllSeverities() {
		logger.debug("Getting all severities...");
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		AllSeveritiesResponse resp = new AllSeveritiesResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			List<Severity> dbSeverities = qaStore.getAllSeverities();
			org.ihtsdo.qadb.ws.data.Severity[] wsSeverities = new org.ihtsdo.qadb.ws.data.Severity[dbSeverities.size()];
			int i = 0;
			for (Severity severity : dbSeverities) {
				org.ihtsdo.qadb.ws.data.Severity wsSeverity = WsConverter.severityToWsSeverity(severity);
				wsSeverities[i] = wsSeverity;
				i++;
			}
			resp.setSeverity(wsSeverities);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public RulesReportLinesByPageResponse getRulesReportLinesByPage(RulesReportLinesByPageRequest rulesReportLinesByPageRequest) {
		logger.debug("Getting Rules Report lines by page ");
		RulesReportLinesByPageResponse response = null;
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		try {
			// Coordenates
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			String databaseUuid = rulesReportLinesByPageRequest.getQaCoordinate().getDatabaseUuid();
			String pathUuid = rulesReportLinesByPageRequest.getQaCoordinate().getPathUuid();
			String viewPointTime = rulesReportLinesByPageRequest.getQaCoordinate().getViewPointTime();

			// Sort
			LinkedHashMap<Integer, Boolean> sorteBy = new LinkedHashMap<Integer, Boolean>();
			IntBoolKeyValue[] sortedBy = rulesReportLinesByPageRequest.getSortedBy();
			if (sortedBy != null && sortedBy.length != 0) {
				for (IntBoolKeyValue strBoolKeyValue : sortedBy) {
					sorteBy.put(strBoolKeyValue.getKey(), strBoolKeyValue.getValue());
				}
			}

			// Filters
			LinkedHashMap<Integer, Object> filters = new LinkedHashMap<Integer, Object>();
			IntStrKeyValue[] reqFilters = rulesReportLinesByPageRequest.getFilter();
			if (reqFilters != null && reqFilters.length != 0) {
				for (IntStrKeyValue strStrKeyValue : reqFilters) {
					filters.put(strStrKeyValue.getKey(), strStrKeyValue.getValue());
				}
			}

			QACoordinate qaCoordinate = new QACoordinate(databaseUuid, pathUuid, viewPointTime);
			RulesReportPage result = qaStore.getRulesReportLinesByPage(qaCoordinate, sorteBy, filters, rulesReportLinesByPageRequest.getStartLine(),
					rulesReportLinesByPageRequest.getPageLenght());

			response = WsConverter.convertRuleReportPage(result);
			response.setInitialLine(result.getInitialLine());
			response.setFinalLine(result.getFinalLine());
			response.setTotalLines(result.getTotalLines());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}

		return response;
	}

	@Override
	public AllComponentsResponse getAllComponents() {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		AllComponentsResponse response = new AllComponentsResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			List<TerminologyComponent> components = qaStore.getAllComponents();

			Component[] wsComponents = null;
			if (components != null && !components.isEmpty()) {
				wsComponents = new Component[components.size()];
				int i = 0;
				for (TerminologyComponent component : components) {
					Component wsCom = new Component();
					wsCom = WsConverter.componentToWsComponent(component);
					wsComponents[i] = wsCom;
					i++;
				}
			}
			response.setComponent(wsComponents);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return response;
	}

	@Override
	public AllPathsForDatabaseResponse getAllPathsForDatabase(AllPathsForDatabaseRequest allPathsForDatabaseRequest) {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		AllPathsForDatabaseResponse resp = new AllPathsForDatabaseResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			List<TerminologyComponent> terminologyComponents = qaStore.getAllPathsForDatabase(allPathsForDatabaseRequest.getDatabaseUuid());
			Component[] wsComponents = new Component[terminologyComponents.size()];
			int i = 0;
			for (TerminologyComponent component : terminologyComponents) {
				Component wsComponent = WsConverter.componentToWsComponent(component);
				wsComponents[i] = wsComponent;
				i++;
			}
			resp.setPaths(wsComponents);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public AllPathsResponse getAllPaths() {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		AllPathsResponse resp = new AllPathsResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			List<TerminologyComponent> terminologyComponents = qaStore.getAllPaths();
			Component[] wsComponents = new Component[terminologyComponents.size()];
			int i = 0;
			for (TerminologyComponent component : terminologyComponents) {
				Component wsComponent = WsConverter.componentToWsComponent(component);
				wsComponents[i] = wsComponent;
				i++;
			}
			resp.setPaths(wsComponents);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public QADatabaseResponse getQADatabase(QADatabaseRequest qADatabaseRequest) {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		QADatabaseResponse resp = new QADatabaseResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			QADatabase database = qaStore.getQADatabase(qADatabaseRequest.getDatabaseUuid());

			Database wsDatabase = WsConverter.databaseToWsDatabase(database);
			resp.setDatabase(wsDatabase);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public QACasesReportLinesByPageResponse getQACasesReportLinesByPage(QACasesReportLinesByPageRequest qACasesReportLinesByPageRequest) {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		QACasesReportLinesByPageResponse resp = new QACasesReportLinesByPageResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			String ruleUuid = qACasesReportLinesByPageRequest.getRuleUuid();

			QACoordinate qaCoordinate = WsConverter.qaCoordToWsCoord(qACasesReportLinesByPageRequest.getQaCoordinate());

			LinkedHashMap<Integer, Boolean> sortBy = WsConverter.wsSortByToSortBy(qACasesReportLinesByPageRequest.getSorteBy());
			HashMap<Integer, Object> filter = WsConverter.wsFilterToFilter(qACasesReportLinesByPageRequest.getFilter());
			QACasesReportPage casesReport = qaStore.getQACasesReportLinesByPage(qaCoordinate, ruleUuid, sortBy, filter, qACasesReportLinesByPageRequest.getStartLine(),
					qACasesReportLinesByPageRequest.getPageLenght());
			resp.setFilter(qACasesReportLinesByPageRequest.getFilter());
			resp.setSortBy(qACasesReportLinesByPageRequest.getSorteBy());
			resp.setInitialLine(qACasesReportLinesByPageRequest.getStartLine());
			resp.setFinalLine(casesReport.getFinalLine());
			resp.setTotalLines(casesReport.getTotalLines());

			WsQACasesReportLine[] wsLines = null;
			List<QACasesReportLine> lines = casesReport.getLines();

			if (lines != null && !lines.isEmpty()) {
				logger.debug("Service returned lines size: " + lines.size());
				wsLines = new WsQACasesReportLine[lines.size()];
				int j = 0;
				for (QACasesReportLine qaCasesReportLine : lines) {
					WsQACasesReportLine wsLine = WsConverter.reportLineToWsReportLine(qaCasesReportLine);
					wsLines[j] = wsLine;
					j++;
				}
			}
			resp.setLines(wsLines);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public void persistQACase(PersistQACaseRequest persistQACaseRequest) {
		Case requestCase = persistQACaseRequest.getQaCase();
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		QAStoreBI qaStore = new QAStoreImpl(sqlSession);
		logger.debug(requestCase);
		QACase qaCase = WsConverter.wsCaseToCase(requestCase);
		logger.debug(qaCase);
		qaStore.persistQACase(qaCase);
		logger.debug("Commiting qacase update");
		sqlSession.commit();
		logger.debug("COMMITED SUCCESFULLY");
		sqlSession.close();
		logger.debug("SESSION CLOSED");
	}

	@Override
	public AllCategoriesResponse getAllCategories() {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		AllCategoriesResponse resp = new AllCategoriesResponse();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			List<Category> categories = qaStore.getAllCategories();
			WsCategory[] wsCategories = new WsCategory[categories.size()];
			int i = 0;
			for (Category category : categories) {
				WsCategory wsCategory = WsConverter.categoryToWsCategory(category);
				wsCategories[i] = wsCategory;
				i++;
			}
			resp.setCategories(wsCategories);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlSession.close();
		}
		return resp;
	}

	@Override
	public void persistsQARule(PersistsQARuleRequest persistsQARuleRequest) throws PersistsQARuleFaultException {
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		try {
			QAStoreBI qaStore = new QAStoreImpl(sqlSession);
			Rule rule = WsConverter.wsRuleToRule(persistsQARuleRequest.getRule());
			qaStore.persistRule(rule);
			sqlSession.commit();
			sqlSession.close();
		} catch (Exception e) {
			sqlSession.rollback();
			logger.error(e);
			throw new PersistsQARuleFaultException(e.getMessage(), e);
		} finally {
			sqlSession.close();
		}

	}

	@Override
	public void persistQACaseList(PersistQACaseList persistQACaseList) throws PersistQACaseListFaultException {
		Case[] requestCaseList = persistQACaseList.getQaCaseList();
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		QAStoreBI qaStore = new QAStoreImpl(sqlSession);
		for (Case requestCase : requestCaseList) {
			QACase qaCase = WsConverter.wsCaseToCase(requestCase);
			qaStore.persistQACase(qaCase);
		}
		sqlSession.commit();
		sqlSession.close();
	}

	@Override
	public void persistQAComment(PersistQACommentRequest persistQACommentRequest) throws PersistQACommentFaultException {
		QACaseComment qaCaseComment = persistQACommentRequest.getQaComment();
		SqlSession sqlSession = MyBatisUtil.getSessionFactory().openSession();
		QAStoreBI qaStore = new QAStoreImpl(sqlSession);
		QAComment comment = WsConverter.wsCommentToComment(qaCaseComment);
		qaStore.persistComment(comment);
		sqlSession.commit();
		sqlSession.close();
	}

}
