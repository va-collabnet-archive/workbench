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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.axis2.AxisFault;
import org.dwfa.ace.log.AceLog;
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
import org.ihtsdo.qadb.ws.PersistQACaseListFaultException;
import org.ihtsdo.qadb.ws.PersistsQARuleFaultException;
import org.ihtsdo.qadb.ws.QadbServiceStub;
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
import org.ihtsdo.qadb.ws.data.Component;
import org.ihtsdo.qadb.ws.data.ComponentRequest;
import org.ihtsdo.qadb.ws.data.ComponentResponse;
import org.ihtsdo.qadb.ws.data.Database;
import org.ihtsdo.qadb.ws.data.DispositionStatusCount_type0;
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
import org.ihtsdo.qadb.ws.data.StatusCount_type0;
import org.ihtsdo.qadb.ws.data.WsCategory;
import org.ihtsdo.qadb.ws.data.WsQACasesReportLine;

/**
 * The Class QAStoreBIImpl.
 */
public class QAStoreBIImpl implements QAStoreBI {

	/** The url. */
	private String url = "http://mgr.servers.aceworkspace.net:50008/axis2/services/qadb-service";

	/**
	 * Instantiates a new qA store bi impl.
	 *
	 * @param url the url
	 */
	public QAStoreBIImpl(String url) {
		AceLog.getAppLog().info("#######################################################");
		AceLog.getAppLog().info("");
		AceLog.getAppLog().info(url);
		AceLog.getAppLog().info("");
		AceLog.getAppLog().info("#######################################################");
		if (url != null) {
			this.url = url;
		}
	}

	/**
	 * Instantiates a new qA store bi impl.
	 */
	public QAStoreBIImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getComponent(java.util.UUID)
	 */
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getRule(java.util.UUID)
	 */
	@Override
	public Rule getRule(UUID ruleUuid) {
		Rule result = null;
		QadbServiceStub service = null;
		try {
			service = new QadbServiceStub(url);

			RuleRequest ruleRequest = new RuleRequest();
			ruleRequest.setRuleUuid(ruleUuid.toString());
			RuleResponse response = service.getRule(ruleRequest);

			result = WsClientDataConverter.wsRuleToRule(response.getRuleResponse());

		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
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
	 * @see org.ihtsdo.qa.store.QAStoreBI#getDispositionStatus(java.util.UUID)
	 */
	@Override
	public DispositionStatus getDispositionStatus(UUID dispositionStatusUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQADatabase(java.util.UUID)
	 */
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllDatabases()
	 */
	@Override
	public List<QADatabase> getAllDatabases() {
		List<QADatabase> result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			AllDatabasesResponse response = service.getAllDatabases();
			Database[] wsDatabases = response.getDatabase();
			result = new ArrayList<QADatabase>();
			if (wsDatabases != null) {
				for (Database wsDatabase : wsDatabases) {
					QADatabase dataase = new QADatabase(UUID.fromString(wsDatabase.getDatabaseUuid()), wsDatabase.getName());
					result.add(dataase);
				}
			}
		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return result;
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
		List<TerminologyComponent> result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);

			AllPathsResponse response = service.getAllPaths();
			Component[] paths = response.getPaths();
			result = new ArrayList<TerminologyComponent>();
			if (paths != null) {
				for (Component component : paths) {
					TerminologyComponent termComp = WsClientDataConverter.wsComponentToTerminologyComponent(component);
					result.add(termComp);
				}
			}
		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllPathsForDatabase(java.util.UUID)
	 */
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
			if (paths != null) {
				for (Component component : paths) {
					TerminologyComponent termComp = WsClientDataConverter.wsComponentToTerminologyComponent(component);
					result.add(termComp);
				}
			}
		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
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
	public List<Finding> getFindingsForComponent(QACoordinate coordinate, UUID componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getFindingsForPeriod(org.ihtsdo.qa.store.model.QACoordinate, java.util.Date, java.util.Date)
	 */
	@Override
	public List<Finding> getFindingsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllSeverities()
	 */
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getSeverity(java.util.UUID)
	 */
	@Override
	public Severity getSeverity(UUID severityUuid) {
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
	public List<QACase> getQACasesForComponent(QACoordinate coordinate, UUID componentUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesForStatus(org.ihtsdo.qa.store.model.QACoordinate, boolean)
	 */
	@Override
	public List<QACase> getQACasesForStatus(QACoordinate coordinate, boolean isActive) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesForDispositionStatus(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public List<QACase> getQACasesForDispositionStatus(QACoordinate coordinate, UUID dispositionStatusUuid) {
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
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllDispositionStatus()
	 */
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACaseVersions(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public List<QACaseVersion> getQACaseVersions(QACoordinate coordinate, UUID qaCaseUuid) {
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
	public List<Execution> getExecutionsForPeriod(QACoordinate coordinate, Date dateStart, Date dateEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllRules()
	 */
	@Override
	public List<Rule> getAllRules() {
		List<Rule> result = null;
		QadbServiceStub service;
		try {
			service = new QadbServiceStub(url);
			AllRulesResponse reponse = service.getAllRules();
			org.ihtsdo.qadb.ws.data.Rule[] rules = reponse.getRule();
			result = new ArrayList<Rule>();
			if (rules != null) {
				for (org.ihtsdo.qadb.ws.data.Rule wsRule : rules) {
					Rule rule = new Rule();
					rule = WsClientDataConverter.wsRuleToRule(wsRule);
					result.add(rule);
				}
			}
		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getDispositionStatusCountsForRule(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public HashMap<UUID, Integer> getDispositionStatusCountsForRule(QACoordinate coordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getStatusCountsForRule(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public HashMap<Boolean, Integer> getStatusCountsForRule(QACoordinate coordinate, UUID ruleUuid) {
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
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getRulesReportLinesByPage(org.ihtsdo.qa.store.model.QACoordinate, java.util.LinkedHashMap, java.util.HashMap, int, int)
	 */
	@Override
	public RulesReportPage getRulesReportLinesByPage(QACoordinate qaCoordinate, Map<RulesReportColumn, Boolean> sortBy, Map<RulesReportColumn, Object> filter, int startLine, int pageLenght) {
		RulesReportPage result = null;
		QadbServiceStub service = null;
		try {

			service = new QadbServiceStub(url);

			RulesReportLinesByPageRequest request = new RulesReportLinesByPageRequest();

			org.ihtsdo.qadb.ws.data.QACoordinate wsCoordinate = new org.ihtsdo.qadb.ws.data.QACoordinate();
			WsClientDataConverter.qaCoordinateToWsQaCoordinate(qaCoordinate, wsCoordinate);
			request.setQaCoordinate(wsCoordinate);

			IntBoolKeyValue[] wsSorteBy = null;
			wsSorteBy = WsClientDataConverter.sortByToWsSortBy(sortBy, wsSorteBy);
			request.setSortedBy(wsSorteBy);

			IntStrKeyValue[] wsFilter = null;
			if (filter != null && !filter.isEmpty()) {
				wsFilter = WsClientDataConverter.filterToWsFilter(filter);
			}
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
			if (wsReportLines != null) {

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
			}
			result.setLines(lines);

		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} finally {
			if (service != null) {
				try{
					service._getServiceClient().cleanup();
					service._getServiceClient().cleanupTransport();
				}catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesReportLines(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID)
	 */
	@Override
	public List<QACasesReportLine> getQACasesReportLines(QACoordinate qaCoordinate, UUID ruleUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getQACasesReportLinesByPage(org.ihtsdo.qa.store.model.QACoordinate, java.util.UUID, java.util.LinkedHashMap, java.util.HashMap, int, int)
	 */
	@Override
	public QACasesReportPage getQACasesReportLinesByPage(QACoordinate qaCoordinate, UUID ruleUuid, LinkedHashMap<QACasesReportColumn, Boolean> sortBy, HashMap<QACasesReportColumn, Object> filter, int startLine,
			int pageLenght) {
		QACasesReportPage reportPage = null;
		QadbServiceStub service = null;
		try {
			service = new QadbServiceStub(url);
			
			QACasesReportLinesByPageRequest request = new QACasesReportLinesByPageRequest();
			IntStrKeyValue[] wsFilter = null;
			if (filter != null && !filter.isEmpty()) {
				wsFilter = WsClientDataConverter.qaCaseFilterToWsQaCaseFilter(filter);
			}
			IntBoolKeyValue[] wsSorteBy = null;

			wsSorteBy = WsClientDataConverter.qaCaseSortToWsQaCaseSort(sortBy);

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
			if (wslines != null) {
				for (WsQACasesReportLine wsQACasesReportLine : wslines) {
					QACase qaCase = WsClientDataConverter.wsCaseToCase(wsQACasesReportLine.getQaCase());
					TerminologyComponent component = WsClientDataConverter.wsComponentToTerminologyComponent(wsQACasesReportLine.getComponent());
					DispositionStatus disposition = WsClientDataConverter.wsDipsStatusToDispStatus(wsQACasesReportLine.getDispositionStatus());
					QACasesReportLine line = new QACasesReportLine(qaCase, component, disposition);
					lines.add(line);
				}
			}
			reportPage.setLines(lines);

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}finally {
			if (service != null) {
				try{
					service._getServiceClient().cleanup();
					service._getServiceClient().cleanupTransport();
				}catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
		return reportPage;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllComponents()
	 */
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
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
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			PersistsQARuleRequest wsRuleRequest = new PersistsQARuleRequest();
			org.ihtsdo.qadb.ws.data.Rule wsRule = WsClientDataConverter.ruleToWsdlRule(rule);
			wsRuleRequest.setRule(wsRule);
			service.persistsQARule(wsRuleRequest);
		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (PersistsQARuleFaultException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
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
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			PersistQACaseRequest request = new PersistQACaseRequest();

			Case wsQaCase = WsClientDataConverter.caseToWsCase(qaCase);
			request.setQaCase(wsQaCase);
			service.persistQACase(request);
		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistQADatabase(org.ihtsdo.qa.store.model.QADatabase)
	 */
	@Override
	public void persistQADatabase(QADatabase database) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getAllCategories()
	 */
	@Override
	public List<Category> getAllCategories() {
		List<Category> result = null;
		try {
			QadbServiceStub service = new QadbServiceStub(url);

			AllCategoriesResponse response = service.getAllCategories();
			WsCategory[] categories = response.getCategories();
			result = new ArrayList<Category>();
			for (WsCategory wsCategory : categories) {
				Category category = WsClientDataConverter.wsCategoryToCategory(wsCategory);
				result.add(category);
			}
		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#getCategory(java.util.UUID)
	 */
	@Override
	public Category getCategory(UUID categoryUuid) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistQACaseList(java.util.List)
	 */
	@Override
	public void persistQACaseList(List<QACase> qaCaseList) throws Exception {
		try {
			QadbServiceStub service = new QadbServiceStub(url);
			PersistQACaseList qaListRequest = new PersistQACaseList();

			Case[] wsCaseList = new Case[qaCaseList.size()];
			int i = 0;
			for (QACase qaCase2 : qaCaseList) {
				Case wsQaCase = WsClientDataConverter.caseToWsCase(qaCase2);
				wsCaseList[i] = wsQaCase;
				i++;
			}
			qaListRequest.setQaCaseList(wsCaseList);
			service.persistQACaseList(qaListRequest);
		} catch (AxisFault e) {
			AceLog.getAppLog().alertAndLogException(e);
			throw e;
		} catch (RemoteException e) {
			AceLog.getAppLog().alertAndLogException(e);
			throw e;
		} catch (PersistQACaseListFaultException e) {
			AceLog.getAppLog().alertAndLogException(e);
			throw new RemoteException(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.qa.store.QAStoreBI#persistQAComment(org.ihtsdo.qa.store.model.QaCaseComment)
	 */
	@Override
	public void persistQAComment(QaCaseComment comment) throws Exception {
		QACaseComment wsComment = WsClientDataConverter.commentToWsComment(comment);
		QadbServiceStub service = new QadbServiceStub(url);
		PersistQACommentRequest persistRequest = new PersistQACommentRequest();
		persistRequest.setQaComment(wsComment);
		try {
			service.persistQAComment(persistRequest);
		} catch (Exception e) {
			throw e;
		}
	}

}
