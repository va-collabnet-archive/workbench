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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.qa.store.model.Category;
import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.QACase;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.QaCaseComment;
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.Severity;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.qa.store.model.view.QACasesReportColumn;
import org.ihtsdo.qa.store.model.view.RulesReportColumn;
import org.ihtsdo.qadb.ws.data.Case;
import org.ihtsdo.qadb.ws.data.Component;
import org.ihtsdo.qadb.ws.data.IntBoolKeyValue;
import org.ihtsdo.qadb.ws.data.IntStrKeyValue;
import org.ihtsdo.qadb.ws.data.QACaseComment;
import org.ihtsdo.qadb.ws.data.WsCategory;

/**
 * The Class WsClientDataConverter.
 */
public class WsClientDataConverter {
	
	/**
	 * Filter to ws filter.
	 *
	 * @param filter the filter
	 * @return the int str key value[]
	 */
	public static IntStrKeyValue[] filterToWsFilter(Map<RulesReportColumn, Object> filter) {
		IntStrKeyValue[] wsFilter = new IntStrKeyValue[filter.size()];
		if(filter != null && !filter.isEmpty()){
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
	
	/**
	 * Sort by to ws sort by.
	 *
	 * @param sortBy the sort by
	 * @param wsSorteBy the ws sorte by
	 * @return the int bool key value[]
	 */
	public static IntBoolKeyValue[] sortByToWsSortBy(Map<RulesReportColumn, Boolean> sortBy, IntBoolKeyValue[] wsSorteBy) {
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
	
	/**
	 * Qa coordinate to ws qa coordinate.
	 *
	 * @param qaCoordinate the qa coordinate
	 * @param wsCoordinate the ws coordinate
	 */
	public static void qaCoordinateToWsQaCoordinate(QACoordinate qaCoordinate, org.ihtsdo.qadb.ws.data.QACoordinate wsCoordinate) {
		wsCoordinate.setDatabaseUuid(qaCoordinate.getDatabaseUuid().toString());
		wsCoordinate.setPathUuid(qaCoordinate.getPathUuid().toString());
		wsCoordinate.setViewPointTime(qaCoordinate.getViewPointTime());
	}

	/**
	 * Ws rule to rule.
	 *
	 * @param wsRule the ws rule
	 * @return the rule
	 */
	public static Rule wsRuleToRule(org.ihtsdo.qadb.ws.data.Rule wsRule) {
		Rule result = new Rule();
		result.setRuleCode(wsRule.getRuleCode());
		if(wsRule.getRuleUuid() != null){
			result.setRuleUuid(UUID.fromString(wsRule.getRuleUuid()));
		}
		result.setName(wsRule.getName());
		result.setCategory(wsRule.getCategory());
		org.ihtsdo.qadb.ws.data.Severity wsSeverity = wsRule.getSeverity();
		if(wsSeverity != null && wsSeverity.getSeverityUuid() != null){
			Severity severity = new Severity(UUID.fromString(wsSeverity.getSeverityUuid()),wsSeverity.getName(),null);
			result.setSeverity(severity);
		}
		result.setDescription(wsRule.getDescription());
		result.setDitaDocumentationLinkUuid(wsRule.getDitaDocumentationLinkUuid());
		if(wsRule.getEffectiveTime() != null){
			result.setEffectiveTime(wsRule.getEffectiveTime().getTime());
		}
		result.setDitaGeneratedTopicUuid(wsRule.getDitaGeneratedTopicUuid());
		result.setDocumentationUrl(wsRule.getDocumentationUrl());
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

	/**
	 * Ws component to terminology component.
	 *
	 * @param component the component
	 * @return the terminology component
	 */
	public static TerminologyComponent wsComponentToTerminologyComponent(Component component) {
		TerminologyComponent result = new TerminologyComponent();
		result.setComponentName(component.getComponentName());
		result.setComponentUuid(UUID.fromString(component.getComponentUuid()));
		if(component.getSctid() != null && !component.getSctid().equals("null")){
			result.setSctid(Long.valueOf(component.getSctid()));
		}
		return result;
	}

	/**
	 * Ws dips status to disp status.
	 *
	 * @param wsDispStatus the ws disp status
	 * @return the disposition status
	 */
	public static DispositionStatus wsDipsStatusToDispStatus(org.ihtsdo.qadb.ws.data.DispositionStatus wsDispStatus) {
		DispositionStatus result = new DispositionStatus();
		result.setDispositionStatusUuid(UUID.fromString(wsDispStatus.getDispositionStatusUuid()));
		result.setName(wsDispStatus.getName());
		return result;
	}

	/**
	 * Ws severity to severty.
	 *
	 * @param wsSeverity the ws severity
	 * @return the severity
	 */
	public static Severity wsSeverityToSeverty(org.ihtsdo.qadb.ws.data.Severity wsSeverity) {
		Severity result = new Severity();
		result.setDescription(wsSeverity.getDescription());
		result.setName(wsSeverity.getName());
		result.setSeverityUuid(UUID.fromString(wsSeverity.getSeverityUuid()));
		return result;
	}

	/**
	 * Qa case filter to ws qa case filter.
	 *
	 * @param filter the filter
	 * @return the int str key value[]
	 */
	public static IntStrKeyValue[] qaCaseFilterToWsQaCaseFilter(HashMap<QACasesReportColumn, Object> filter) {
		IntStrKeyValue[] wsFilter = new IntStrKeyValue[filter.size()];
		Iterable<QACasesReportColumn> keySet = filter.keySet();
		int j = 0;
		for (QACasesReportColumn qaCasesReportColumn : keySet) {
			IntStrKeyValue f = new IntStrKeyValue();
			f.setKey(qaCasesReportColumn.getColumnNumber());
			f.setValue(filter.get(qaCasesReportColumn).toString());
			wsFilter[j] = f;
			j++;
		}
		return wsFilter;
	}

	/**
	 * Qa case sort to ws qa case sort.
	 *
	 * @param sorteBy the sorte by
	 * @return the int bool key value[]
	 */
	public static IntBoolKeyValue[]  qaCaseSortToWsQaCaseSort(LinkedHashMap<QACasesReportColumn, Boolean> sorteBy) {
		IntBoolKeyValue[] result = new IntBoolKeyValue[sorteBy.size()];
		Set<QACasesReportColumn> keySet = sorteBy.keySet();
		int j = 0;
		for (QACasesReportColumn qaCasesReportColumn : keySet) {
			IntBoolKeyValue sort = new IntBoolKeyValue();
			sort.setKey(qaCasesReportColumn.getColumnNumber());
			sort.setValue(sorteBy.get(qaCasesReportColumn));
			result[j] = sort;
			j++;
		}
		return result;
	}

	/**
	 * Ws case to case.
	 *
	 * @param qaCase the qa case
	 * @return the qA case
	 */
	public static QACase wsCaseToCase(Case qaCase) {
		QACase result = new QACase();
		result.setCaseUuid(UUID.fromString(qaCase.getCaseUuid()));
		result.setActive(qaCase.getIsActive());
		result.setAssignedTo(qaCase.getAssignedTo());
		if(qaCase.getComponent() != null){
			result.setComponentUuid(UUID.fromString(qaCase.getComponent().getComponentUuid()));
		}
		if(qaCase.getDatabaseUuid() != null){
			result.setDatabaseUuid(UUID.fromString(qaCase.getDatabaseUuid()));
		}
		result.setDetail(qaCase.getDetail());
		result.setDispositionAnnotation(qaCase.getDispositionAnnotation());
		try{
			result.setDispositionReasonUuid(UUID.fromString(qaCase.getDispositionReasonUuid()));
		}catch (Exception e) {}
		result.setDispositionStatusDate(qaCase.getDispositionStatusDate());
		result.setDispositionStatusEditor(qaCase.getDispositionStatusEditor());
		result.setDispositionStatusUuid(UUID.fromString(qaCase.getDispositionStatus().getDispositionStatusUuid()));
		result.setPathUuid(UUID.fromString(qaCase.getPathUuid()));
		result.setRuleUuid(UUID.fromString(qaCase.getRule().getRuleUuid()));
		result.setEffectiveTime(qaCase.getEffectiveTime());
		result.setLastChangedState(qaCase.getLastStatusChanged());
		result.setActive(qaCase.getIsActive());
		result.setAssignmentEditor(qaCase.getAssignmentEditor());
		result.setAssignmentDate(qaCase.getAssignmentDate());
		List<QaCaseComment> comments = null;
		QACaseComment[] wsCaseComments = qaCase.getComments();
		if(wsCaseComments != null && wsCaseComments.length > 0){
			comments = new ArrayList<QaCaseComment>();
			for (QACaseComment qaCaseComment : wsCaseComments) {
				QaCaseComment comment = wsCommentToComment(qaCaseComment);
				comments.add(comment);
			}
		}
		result.setComments(comments);
		
		return result;
	}

	/**
	 * Ws comment to comment.
	 *
	 * @param qaCaseComment the qa case comment
	 * @return the qa case comment
	 */
	public static QaCaseComment wsCommentToComment(QACaseComment qaCaseComment) {
		QaCaseComment comment = new QaCaseComment();
		comment.setAuthor(qaCaseComment.getAuthor());
		comment.setComment(qaCaseComment.getComment());
		if(qaCaseComment.getCaseUuid()!= null){
			comment.setCaseUuid(UUID.fromString(qaCaseComment.getCaseUuid()));
		}
		if(qaCaseComment.getCommentUuid()!= null){
			comment.setCommentUuid(UUID.fromString(qaCaseComment.getCommentUuid()));
		}
		if(qaCaseComment.getEffectiveTime() != null){
			comment.setEffectiveTime(qaCaseComment.getEffectiveTime().getTime());
		}
		if(qaCaseComment.getStatus() != null){
			comment.setStatus(qaCaseComment.getStatus().intValue());
		}
		
		return comment;
	}

	/**
	 * Case to ws case.
	 *
	 * @param qaCase the qa case
	 * @return the case
	 */
	public static Case caseToWsCase(QACase qaCase) {
		Case result = new Case();
		result.setAssignedTo(qaCase.getAssignedTo());
		result.setCaseUuid(qaCase.getCaseUuid().toString());
		
		Component component = new Component();
		if(qaCase.getComponentUuid() != null){
			component.setComponentUuid(qaCase.getComponentUuid().toString());
		}
		result.setComponent(component );
		if(qaCase.getDatabaseUuid() != null){
			result.setDatabaseUuid(qaCase.getDatabaseUuid().toString());
		}
		result.setDetail(qaCase.getDetail());
		result.setDispositionAnnotation(qaCase.getDispositionAnnotation());
		if(qaCase.getDispositionReasonUuid() != null){
			result.setDispositionReasonUuid(qaCase.getDispositionReasonUuid().toString());
		}
		org.ihtsdo.qadb.ws.data.DispositionStatus dispStatus = new org.ihtsdo.qadb.ws.data.DispositionStatus();
		if(qaCase.getDispositionStatusUuid() != null){
			dispStatus.setDispositionStatusUuid(qaCase.getDispositionStatusUuid().toString());
		}
		result.setDispositionStatus(dispStatus );
		result.setDispositionStatusDate(qaCase.getDispositionStatusDate());
		result.setDispositionStatusEditor(qaCase.getDispositionStatusEditor());
		result.setEffectiveTime(qaCase.getEffectiveTime());
		result.setAssignmentDate(qaCase.getAssignmentDate());
		result.setAssignmentEditor(qaCase.getAssignmentEditor());
		result.setIsActive(qaCase.isActive());
		result.setPathUuid(qaCase.getPathUuid().toString());
		org.ihtsdo.qadb.ws.data.Rule rule = new org.ihtsdo.qadb.ws.data.Rule();
		if(qaCase.getRuleUuid() != null){
			rule.setRuleUuid(qaCase.getRuleUuid().toString());
		}
		result.setRule(rule );
		return result;
	}

	/**
	 * Ws category to category.
	 *
	 * @param wsCategory the ws category
	 * @return the category
	 */
	public static Category wsCategoryToCategory(WsCategory wsCategory) {
		Category result = new Category();
		result.setDescription(wsCategory.getDescription());
		result.setName(wsCategory.getName());
		result.setCategoryUuid(UUID.fromString(wsCategory.getCategoryUuid()));
		return result;
	}

	/**
	 * Rule to wsdl rule.
	 *
	 * @param rule the rule
	 * @return the org.ihtsdo.qadb.ws.data. rule
	 */
	public static org.ihtsdo.qadb.ws.data.Rule ruleToWsdlRule(Rule rule) {
		org.ihtsdo.qadb.ws.data.Rule wsRule = new org.ihtsdo.qadb.ws.data.Rule();
		wsRule.setCategory(rule.getCategory());
		wsRule.setDescription(rule.getDescription());
		wsRule.setDitaDocumentationLinkUuid(rule.getDitaDocumentationLinkUuid());
		wsRule.setDitaGeneratedTopicUuid(rule.getDitaGeneratedTopicUuid());
		wsRule.setDocumentationUrl(rule.getDocumentationUrl());
		if (rule.getDitaUuid() != null) {
			wsRule.setDitaUuid(rule.getDitaUuid().toString());
		}
		wsRule.setExample(rule.getExample());
		wsRule.setExpectedResult(rule.getExpectedResult());
		wsRule.setIsWhitelistAllowed(rule.isWhitelistAllowed());
		wsRule.setIsWhitelistResetAllowed(rule.isWhitelistResetAllowed());
		wsRule.setIsWhitelistResetWhenClosed(rule.isWhitelistResetWhenClosed());
		wsRule.setModifiedBy(rule.getModifiedBy());
		wsRule.setName(rule.getName());
		wsRule.setPackageName(rule.getPackageName());
		wsRule.setPackageUrl(rule.getPackageUrl());
		wsRule.setRuleCode(rule.getRuleCode());
		wsRule.setRuleUuid(rule.getRuleUuid().toString());
		if(rule.getSeverity() != null){
			org.ihtsdo.qadb.ws.data.Severity severity = new org.ihtsdo.qadb.ws.data.Severity();
			severity.setName(rule.getSeverity().getName());
			UUID severityUuid = rule.getSeverity().getSeverityUuid();
			if(severityUuid!= null){
				severity.setSeverityUuid(severityUuid.toString());
			}
			wsRule.setSeverity(severity);
		}
		wsRule.setStandingIssues(rule.getStandingIssues());
		wsRule.setStatus(rule.getStatus());
		wsRule.setSuggestedResolution(rule.getSuggestedResolution());
		return wsRule;
	}

	/**
	 * Comment to ws comment.
	 *
	 * @param comment the comment
	 * @return the qA case comment
	 */
	public static QACaseComment commentToWsComment(QaCaseComment comment) {
		QACaseComment result = new QACaseComment();
		result.setAuthor(comment.getAuthor());
		if(comment.getCaseUuid() != null){
			result.setCaseUuid(comment.getCaseUuid().toString());
		}
		result.setComment(comment.getComment());
		if(comment.getCommentUuid() != null){
			result.setCommentUuid(comment.getCommentUuid().toString());
		}
		if(comment.getStatus() != null){
			result.setStatus(BigInteger.valueOf(comment.getStatus()));
		}
		return result;
	}

}
