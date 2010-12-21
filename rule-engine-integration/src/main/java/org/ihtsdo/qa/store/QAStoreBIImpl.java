package org.ihtsdo.qa.store;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.axis2.AxisFault;
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
import org.ihtsdo.qadb.ws.QadbServiceStub;
import org.ihtsdo.qadb.ws.data.AllComponentsResponse;
import org.ihtsdo.qadb.ws.data.AllDatabasesResponse;
import org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse;
import org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest;
import org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse;
import org.ihtsdo.qadb.ws.data.AllPathsResponse;
import org.ihtsdo.qadb.ws.data.AllRulesResponse;
import org.ihtsdo.qadb.ws.data.AllSeveritiesResponse;
import org.ihtsdo.qadb.ws.data.Component;
import org.ihtsdo.qadb.ws.data.ComponentRequest;
import org.ihtsdo.qadb.ws.data.ComponentResponse;
import org.ihtsdo.qadb.ws.data.Database;
import org.ihtsdo.qadb.ws.data.DispositionStatusCount_type0;
import org.ihtsdo.qadb.ws.data.IntBoolKeyValue;
import org.ihtsdo.qadb.ws.data.IntStrKeyValue;
import org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest;
import org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse;
import org.ihtsdo.qadb.ws.data.QADatabaseRequest;
import org.ihtsdo.qadb.ws.data.QADatabaseResponse;
import org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest;
import org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse;
import org.ihtsdo.qadb.ws.data.StatusCount_type0;
import org.ihtsdo.qadb.ws.data.WsQACasesReportLine;

public class QAStoreBIImpl implements QAStoreBI {
	
	private String url = "http://localhost:8080/axis2/services/qadb-service";
	
	public QAStoreBIImpl(String url) {
		this.url = url;
	}

	public QAStoreBIImpl() {
		super();
	}

	@Override
	public TerminologyComponent getComponent(UUID componentUuid) {
		TerminologyComponent result = null;
		QadbServiceStub service = null;
		try {
			service = new QadbServiceStub(url);

			ComponentRequest componentRequest = new ComponentRequest();
			componentRequest.setComponentUuid(componentUuid.toString());
			ComponentResponse response = service.getComponent(componentRequest);

			result = WsClientDataConverter.wsComponentToTerminologyComponent(response.getComponentResponse());

		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return result;
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
	public DispositionStatus getDispositionStatus(UUID dispositionStatusUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QADatabase getQADatabase(UUID databaseUuid) {
		QADatabase result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			
			QADatabaseRequest request = new QADatabaseRequest();
			request.setDatabaseUuid(databaseUuid.toString());
			QADatabaseResponse response = service.getQADatabase(request);
			Database wsDatabase = response.getDatabase();
			result = new QADatabase(UUID.fromString(wsDatabase.getDatabaseUuid()), wsDatabase.getName());
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public List<QADatabase> getAllDatabases() {
		List<QADatabase> result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			AllDatabasesResponse response = service.getAllDatabases();
			Database[] wsDatabases = response.getDatabase();
			result = new ArrayList<QADatabase>();
			for (Database wsDatabase : wsDatabases) {
				QADatabase dataase = new QADatabase(UUID.fromString(wsDatabase.getDatabaseUuid()), wsDatabase.getName());
				result.add(dataase);
			}
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public List<QADatabase> getAllDatabasesForPath(UUID pathUuid) {
		return getAllDatabases();
	}

	@Override
	public List<TerminologyComponent> getAllPaths() {
		List<TerminologyComponent> result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			
			AllPathsResponse response = service.getAllPaths();
			Component[] paths = response.getPaths();
			result = new ArrayList<TerminologyComponent>();
			for (Component component : paths) {
				TerminologyComponent termComp = WsClientDataConverter.wsComponentToTerminologyComponent(component);
				result.add(termComp);
			}
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<TerminologyComponent> getAllPathsForDatabase(UUID databaseUuid) {
		List<TerminologyComponent> result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			
			AllPathsForDatabaseRequest request = new AllPathsForDatabaseRequest();
			request.setDatabaseUuid(databaseUuid.toString());
			AllPathsForDatabaseResponse response = service.getAllPathsForDatabase(request);
			Component[] paths = response.getPaths();
			result = new ArrayList<TerminologyComponent>();
			for (Component component : paths) {
				TerminologyComponent termComp = WsClientDataConverter.wsComponentToTerminologyComponent(component);
				result.add(termComp);
			}
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<String> getAllTimesForPath(UUID databaseUuid, UUID pathUuid) {
		List<String> dates = new ArrayList<String>();
		dates.add("latest");
		return dates;
	}

	@Override
	public List<Finding> getFindingsForExecution(UUID executionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Finding> getFindingsForComponent(QACoordinate coordinate, UUID componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Finding> getFindingsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Severity> getAllSeverities() {
		List<Severity> result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			
			AllSeveritiesResponse response = service.getAllSeverities();
			org.ihtsdo.qadb.ws.data.Severity[] severities = response.getSeverity();
			result = new ArrayList<Severity>();
			for (org.ihtsdo.qadb.ws.data.Severity wsSeverity : severities) {
				Severity severity = WsClientDataConverter.wsSeverityToSeverty(wsSeverity);
				result.add(severity);
			}
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Severity getSeverity(UUID severityUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getAllQACases(QACoordinate coordinate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForComponent(QACoordinate coordinate, UUID componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForStatus(QACoordinate coordinate, boolean isActive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForDispositionStatus(QACoordinate coordinate, UUID dispositionStatusUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QACase> getQACasesForRule(QACoordinate coordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DispositionStatus> getAllDispositionStatus() {
		List<DispositionStatus> result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			AllDispositionStatusResponse response = service.getAllDispositionStatus();
			org.ihtsdo.qadb.ws.data.DispositionStatus[] wsDispStatuses = response.getDispositionStatus();
			result = new ArrayList<DispositionStatus>();
			for (org.ihtsdo.qadb.ws.data.DispositionStatus wsDispStatus : wsDispStatuses) {
				DispositionStatus dispStatus = new DispositionStatus();
				dispStatus = WsClientDataConverter.wsDipsStatusToDispStatus(wsDispStatus);
				result.add(dispStatus);
			}

		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public List<QACaseVersion> getQACaseVersions(QACoordinate coordinate, UUID qaCaseUuid) {
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
		List<Rule> result = null;
		QadbServiceStub service;
		try {
			service = new QadbServiceStub(url);
			AllRulesResponse reponse = service.getAllRules();
			org.ihtsdo.qadb.ws.data.Rule[] rules = reponse.getRule();
			result = new ArrayList<Rule>();
			for (org.ihtsdo.qadb.ws.data.Rule wsRule : rules) {
				Rule rule = new Rule();
				rule = WsClientDataConverter.wsRuleToRule(wsRule);
				result.add(rule);
			}
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public HashMap<UUID, Integer> getDispositionStatusCountsForRule(QACoordinate coordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Boolean, Integer> getStatusCountsForRule(QACoordinate coordinate, UUID ruleUuid) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RulesReportPage getRulesReportLinesByPage(QACoordinate qaCoordinate, LinkedHashMap<RulesReportColumn, Boolean> sortBy, HashMap<RulesReportColumn, Object> filter, int startLine,
			int pageLenght) {
		RulesReportPage result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);

			RulesReportLinesByPageRequest request = new RulesReportLinesByPageRequest();

			org.ihtsdo.qadb.ws.data.QACoordinate wsCoordinate = new org.ihtsdo.qadb.ws.data.QACoordinate();
			WsClientDataConverter.qaCoordinateToWsQaCoordinate(qaCoordinate, wsCoordinate);
			request.setQaCoordinate(wsCoordinate);

			IntBoolKeyValue[] wsSorteBy = null;
			wsSorteBy = WsClientDataConverter.sortByToWsSortBy(sortBy, wsSorteBy);
			request.setSortedBy(wsSorteBy);

			IntStrKeyValue[] wsFilter = null;
			wsFilter = WsClientDataConverter.filterToWsFilter(filter, wsFilter);
			request.setFilter(wsFilter);

			request.setStartLine(startLine);
			request.setPageLenght(pageLenght);

			RulesReportLinesByPageResponse response = service.getRulesReportLinesByPage(request);

			result = new RulesReportPage();

			result.setFilter(filter);
			result.setSortBy(sortBy);
			result.setInitialLine(startLine);
			result.setFinalLine(response.getFinalLine());
			result.setTotalLines(response.getTotalLines());

			List<RulesReportLine> lines = new ArrayList<RulesReportLine>();
			org.ihtsdo.qadb.ws.data.RulesReportLine[] wsReportLines = response.getRulesReportLine();
			for (org.ihtsdo.qadb.ws.data.RulesReportLine wsRulesReportLine : wsReportLines) {
				RulesReportLine ruleReportLine = new RulesReportLine();

				HashMap<UUID, Integer> dispositionStatusCounts = new HashMap<UUID, Integer>();
				DispositionStatusCount_type0[] wsDispositionStatusCounts = wsRulesReportLine.getDispositionStatusCount();
				for (DispositionStatusCount_type0 wsDispositionStatusCount : wsDispositionStatusCounts) {
					dispositionStatusCounts.put(UUID.fromString(wsDispositionStatusCount.getDispositionStatusUuid()), wsDispositionStatusCount.getDispositionStatusCount());
				}
				ruleReportLine.setDispositionStatusCount(dispositionStatusCounts);

				HashMap<Boolean, Integer> statusCounts = new HashMap<Boolean, Integer>();
				StatusCount_type0[] wsStatusCounts = wsRulesReportLine.getStatusCount();
				for (StatusCount_type0 wsStatusCount : wsStatusCounts) {
					statusCounts.put(wsStatusCount.getStatus(), wsStatusCount.getCount());
				}
				ruleReportLine.setStatusCount(statusCounts);

				Rule rule = null;
				org.ihtsdo.qadb.ws.data.Rule wsRule = wsRulesReportLine.getRule();

				rule = WsClientDataConverter.wsRuleToRule(wsRule);

				ruleReportLine.setRule(rule);
				lines.add(ruleReportLine);
			}
			result.setLines(lines);

		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<QACasesReportLine> getQACasesReportLines(QACoordinate qaCoordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QACasesReportPage getQACasesReportLinesByPage(QACoordinate qaCoordinate, UUID ruleUuid, LinkedHashMap<QACasesReportColumn, Boolean> sortBy, HashMap<QACasesReportColumn, Object> filter,
			int startLine, int pageLenght) {
		QACasesReportPage reportPage = null;
		try{
			QadbServiceStub service = new QadbServiceStub(url);
			
			QACasesReportLinesByPageRequest request = new QACasesReportLinesByPageRequest();
			IntStrKeyValue[] wsFilter = null;
			WsClientDataConverter.qaCaseFilterToWsQaCaseFilter(filter, wsFilter);
			IntBoolKeyValue[] wsSorteBy = null;
			WsClientDataConverter.qaCaseSortToWsQaCaseSort(sortBy, wsSorteBy);
			
			org.ihtsdo.qadb.ws.data.QACoordinate wsQaCoord = new org.ihtsdo.qadb.ws.data.QACoordinate(); 
			WsClientDataConverter.qaCoordinateToWsQaCoordinate(qaCoordinate, wsQaCoord);

			request.setFilter(wsFilter);
			request.setQaCoordinate(wsQaCoord);
			request.setSorteBy(wsSorteBy);
			request.setStartLine(startLine);
			request.setPageLenght(pageLenght);
			request.setRuleUuid(ruleUuid.toString());
			QACasesReportLinesByPageResponse wsResponse = service.getQACasesReportLinesByPage(request);
			
			reportPage = new QACasesReportPage();
			reportPage.setFilter(filter);
			reportPage.setSortBy(sortBy);
			reportPage.setFinalLine(wsResponse.getFinalLine());
			reportPage.setTotalLines(wsResponse.getTotalLines());
			reportPage.setInitialLine(wsResponse.getInitialLine());
			WsQACasesReportLine[] wslines = wsResponse.getLines();
			List<QACasesReportLine> lines = new ArrayList<QACasesReportLine>();
			if(wslines != null){
				for (WsQACasesReportLine wsQACasesReportLine : wslines) {
					QACase qaCase = WsClientDataConverter.wsCaseToCase(wsQACasesReportLine.getQaCase());
					TerminologyComponent component = WsClientDataConverter.wsComponentToTerminologyComponent(wsQACasesReportLine.getComponent());
					DispositionStatus disposition = WsClientDataConverter.wsDipsStatusToDispStatus(wsQACasesReportLine.getDispositionStatus());
					QACasesReportLine line = new QACasesReportLine(qaCase, component, disposition);
					lines.add(line);
					
				}
			}
			reportPage.setLines(lines);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return reportPage;
	}

	@Override
	public List<TerminologyComponent> getAllComponents() {
		List<TerminologyComponent> result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			AllComponentsResponse wsResponse = service.getAllComponents();
			Component[] wsComponents = wsResponse.getComponent();
			result = new ArrayList<TerminologyComponent>();
			for (Component wsComponent : wsComponents) {
				TerminologyComponent component = new TerminologyComponent();
				component = WsClientDataConverter.wsComponentToTerminologyComponent(wsComponent);
				result.add(component);
			}
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String getExecutionRulesDetails(UUID executionUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExecutionOutcomeDetails(UUID executionUuid) {
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
	public void persistQADatabase(QADatabase database) {
		// TODO Auto-generated method stub

	}

}
