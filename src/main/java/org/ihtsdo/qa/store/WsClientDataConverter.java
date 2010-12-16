package org.ihtsdo.qa.store;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.QACase;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.Severity;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.qa.store.model.view.QACasesReportColumn;
import org.ihtsdo.qa.store.model.view.RulesReportColumn;
import org.ihtsdo.qadb.ws.data.Case;
import org.ihtsdo.qadb.ws.data.Component;
import org.ihtsdo.qadb.ws.data.IntBoolKeyValue;
import org.ihtsdo.qadb.ws.data.IntStrKeyValue;

public class WsClientDataConverter {
	public static IntStrKeyValue[] filterToWsFilter(HashMap<RulesReportColumn, Object> filter, IntStrKeyValue[] wsFilter) {
		if(filter != null && !filter.isEmpty()){
			wsFilter = new IntStrKeyValue[filter.size()];
			Set<RulesReportColumn> filterSet = filter.keySet();
			int j = 0;
			for (RulesReportColumn filterElement : filterSet) {
				IntStrKeyValue wsFilterElement = new IntStrKeyValue();
				wsFilterElement.setKey(filterElement.getColumnNumber());
				wsFilterElement.setValue(filter.get(filterElement).toString());
				wsFilter[j] = wsFilterElement;
				j++;
			}
		}
		return wsFilter;
	}
	
	public static IntBoolKeyValue[] sortByToWsSortBy(LinkedHashMap<RulesReportColumn, Boolean> sortBy, IntBoolKeyValue[] wsSorteBy) {
		if(sortBy != null && !sortBy.isEmpty()){
			wsSorteBy = new IntBoolKeyValue[sortBy.size()];
			Set<RulesReportColumn> sortBySet = sortBy.keySet();
			int i = 0;
			for (RulesReportColumn sortelement : sortBySet) {
				IntBoolKeyValue wsSortElement = new IntBoolKeyValue();
				wsSortElement.setKey(sortelement.getColumnNumber());
				wsSortElement.setValue(sortBy.get(sortelement));
				wsSorteBy[i] = wsSortElement;
				i++;
			}
		}
		return wsSorteBy;
	}
	
	public static void qaCoordinateToWsQaCoordinate(QACoordinate qaCoordinate, org.ihtsdo.qadb.ws.data.QACoordinate wsCoordinate) {
		wsCoordinate.setDatabaseUuid(qaCoordinate.getDatabaseUuid().toString());
		wsCoordinate.setPathUuid(qaCoordinate.getPathUuid().toString());
		wsCoordinate.setViewPointTime(qaCoordinate.getViewPointTime());
	}

	public static Rule wsRuleToRule(org.ihtsdo.qadb.ws.data.Rule wsRule) {
		Rule result = new Rule();
		result.setRuleCode(wsRule.getRuleCode());
		if(wsRule.getRuleUuid() != null){
			result.setRuleUuid(UUID.fromString(wsRule.getRuleUuid()));
		}
		result.setName(wsRule.getName());
		result.setCategory(wsRule.getCategory());
		Severity severity = new Severity(null, wsRule.getSeverity(), null);
		result.setSeverity(severity);
		result.setDescription(wsRule.getDescription());
		result.setDitaDocumentationLinkUuid(wsRule.getDitaDocumentationLinkUuid());
		result.setDitaGeneratedTopicUuid(wsRule.getDitaGeneratedTopicUuid());
		if(wsRule.getDitaUuid() != null && !wsRule.equals("")){
			result.setDitaUuid(UUID.fromString(wsRule.getDitaUuid()));
		}
		result.setExample(wsRule.getExample());
		result.setExpectedResult(wsRule.getExpectedResult());
		result.setModifiedBy(wsRule.getModifiedBy());
		result.setPackageName(wsRule.getPackageName());
		result.setPackageUrl(wsRule.getPackageUrl());
		result.setStandingIssues(wsRule.getStandingIssues());
		result.setStatus(wsRule.getStatus());
		result.setSuggestedResolution(wsRule.getSuggestedResolution());
		result.setWhitelistAllowed(wsRule.getIsWhitelistAllowed());
		result.setWhitelistResetAllowed(wsRule.getIsWhitelistResetAllowed());
		result.setWhitelistResetWhenClosed(wsRule.getIsWhitelistResetWhenClosed());
		return result;
	}

	public static TerminologyComponent wsComponentToTerminologyComponent(Component component) {
		TerminologyComponent result = new TerminologyComponent();
		result.setComponentName(component.getComponentName());
		result.setComponentUuid(UUID.fromString(component.getComponentUuid()));
		if(component.getSctid() != null && !component.getSctid().equals("null")){
			result.setSctid(Long.valueOf(component.getSctid()));
		}
		return result;
	}

	public static DispositionStatus wsDipsStatusToDispStatus(org.ihtsdo.qadb.ws.data.DispositionStatus wsDispStatus) {
		DispositionStatus result = new DispositionStatus();
		result.setDispositionStatusUuid(UUID.fromString(wsDispStatus.getDispositionStatusUuid()));
		result.setName(wsDispStatus.getName());
		return result;
	}

	public static Severity wsSeverityToSeverty(org.ihtsdo.qadb.ws.data.Severity wsSeverity) {
		Severity result = new Severity();
		result.setDescription(wsSeverity.getDescription());
		result.setName(wsSeverity.getName());
		result.setSeverityUuid(UUID.fromString(wsSeverity.getSeverityUuid()));
		return result;
	}

	public static void qaCaseFilterToWsQaCaseFilter(HashMap<QACasesReportColumn, Object> filter, IntStrKeyValue[] wsFilter) {
	}

	public static void qaCaseSortToWsQaCaseSort(LinkedHashMap<QACasesReportColumn, Boolean> sortBy, IntBoolKeyValue[] wsSorteBy) {
	}

	public static QACase wsCaseToCase(Case qaCase) {
		QACase result = new QACase();
		result.setAssignedTo(qaCase.getAssignedTo());
		result.setEffectiveTime(qaCase.getEffectiveTime());
		result.setActive(qaCase.getIsActive());
		return result;
	}

}
