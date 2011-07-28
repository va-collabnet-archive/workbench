package org.ihtsdo.qadb.ws;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
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
import org.ihtsdo.qadb.data.view.RulesReportLine;
import org.ihtsdo.qadb.data.view.RulesReportPage;
import org.ihtsdo.qadb.ws.data.Case;
import org.ihtsdo.qadb.ws.data.Component;
import org.ihtsdo.qadb.ws.data.Database;
import org.ihtsdo.qadb.ws.data.DispositionStatusCount_type0;
import org.ihtsdo.qadb.ws.data.Execution;
import org.ihtsdo.qadb.ws.data.IntBoolKeyValue;
import org.ihtsdo.qadb.ws.data.IntStrKeyValue;
import org.ihtsdo.qadb.ws.data.QACaseComment;
import org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse;
import org.ihtsdo.qadb.ws.data.StatusCount_type0;
import org.ihtsdo.qadb.ws.data.WsCategory;
import org.ihtsdo.qadb.ws.data.WsQACasesReportLine;

public class WsConverter {
	
	private static final Logger logger = Logger.getLogger(WsConverter.class);

	public static QACase wsCaseToCase(Case qaCase) {
		QACase result = new QACase();
		result.setCaseUuid(qaCase.getCaseUuid());
		result.setActive(qaCase.getIsActive());
		result.setAssignedTo(qaCase.getAssignedTo());
		
		TerminologyComponent componentUuid = new TerminologyComponent();
		componentUuid.setComponentUuid(qaCase.getComponent().getComponentUuid());
		if(qaCase.getComponent() != null){
			result.setComponentUuid(componentUuid);
		}
		if(qaCase.getDatabaseUuid() != null){
			result.setDatabaseUuid(qaCase.getDatabaseUuid());
		}
		result.setDetail(qaCase.getDetail());
		result.setDispositionAnnotation(qaCase.getDispositionAnnotation());
		try{
			result.setDispositionReasonUuid(qaCase.getDispositionReasonUuid());
		}catch (Exception e) {}
		result.setDispositionStatusDate(qaCase.getDispositionStatusDate());
		logger.debug("############## " + qaCase.getDispositionStatusDate());
		result.setDispositionStatusEditor(qaCase.getDispositionStatusEditor());
		DispositionStatus dispositionStatus = new DispositionStatus();
		dispositionStatus.setDispositionStatusUuid(qaCase.getDispositionStatus().getDispositionStatusUuid());
		result.setDispositionStatus(dispositionStatus);
		result.setPathUuid(qaCase.getPathUuid());
		result.setRuleUuid(qaCase.getRule().getRuleUuid());
		result.setEffectiveTime(qaCase.getEffectiveTime());
		result.setActive(qaCase.getIsActive());
		List<QAComment> comments = null;
		if(qaCase.getComments() != null && qaCase.getComments().length != 0){
			comments = new ArrayList<QAComment>();
			for(QACaseComment wsComment : qaCase.getComments()){
				QAComment comment = wsCommentToComment(wsComment);
				comments.add(comment);
			}
		}
		result.setComments(comments );
		result.setAssignmentDate(qaCase.getAssignmentDate());
		result.setAssignmentEditor(qaCase.getAssignmentEditor());
		return result;
	}

	public static QAComment wsCommentToComment(QACaseComment wsComment) {
		QAComment comment = new QAComment();
		comment.setComment(wsComment.getComment());
		comment.setAuthor(wsComment.getAuthor());
		comment.setCaseUuid(wsComment.getCaseUuid());
		comment.setCommentUuid(wsComment.getCommentUuid());
		comment.setEffectiveTime(wsComment.getEffectiveTime());
		if(wsComment.getStatus() != null){
			comment.setStatus(wsComment.getStatus().intValue());
		}
		return comment;
	}
	
	public static RulesReportLinesByPageResponse convertRuleReportPage(RulesReportPage result) {
		logger.debug("Convert rule report page");
		RulesReportLinesByPageResponse response = new RulesReportLinesByPageResponse();

		HashMap<Integer, Object> filter = result.getFilter();
		IntStrKeyValue[] filterArr = null;
		if (filter != null && filter.size() != 0) {
			filterArr = new IntStrKeyValue[result.getFilter().size()];
			Set<Integer> keySet = filter.keySet();
			int i = 0;
			for (Integer rulesReportColumn : keySet) {
				IntStrKeyValue s = new IntStrKeyValue();
				s.setKey(rulesReportColumn);
				s.setValue(filter.get(rulesReportColumn).toString());
				filterArr[i] = s;
				i++;
			}
		}

		response.setFilter(filterArr);

		// Sort by
		LinkedHashMap<Integer, Boolean> sortByResult = result.getSortBy();
		IntBoolKeyValue[] sortArr = null;
		if (sortByResult != null && sortByResult.size() != 0) {
			Set<Integer> sortKeySet = sortByResult.keySet();
			sortArr = new IntBoolKeyValue[sortKeySet.size()];
			int j = 0;
			for (Integer integer : sortKeySet) {
				IntBoolKeyValue b = new IntBoolKeyValue();
				b.setKey(integer);
				b.setValue(sortByResult.get(integer));
				sortArr[j] = b;
				j++;
			}
		}

		response.setSortedBy(sortArr);

		List<RulesReportLine> resultLines = result.getLines();
		org.ihtsdo.qadb.ws.data.RulesReportLine[] responsLines = null;
		if (resultLines != null && !resultLines.isEmpty()) {
			responsLines = new org.ihtsdo.qadb.ws.data.RulesReportLine[resultLines.size()];
			int responseLineCount = 0;
			for (RulesReportLine rulesReportLine : resultLines) {
				org.ihtsdo.qadb.ws.data.RulesReportLine line = new org.ihtsdo.qadb.ws.data.RulesReportLine();

				// DispStatus counts
				HashMap<String, Integer> dispStatusCountResult = rulesReportLine.getDispositionStatusCount();
				DispositionStatusCount_type0[] dispStatusCountArray = new DispositionStatusCount_type0[dispStatusCountResult.size()];
				Set<String> dispStatusCountKeySet = dispStatusCountResult.keySet();
				int dc = 0;
				for (String string : dispStatusCountKeySet) {
					DispositionStatusCount_type0 dispStatusCount = new DispositionStatusCount_type0();
					dispStatusCount.setDispositionStatusCount(dispStatusCountResult.get(string));
					dispStatusCount.setDispositionStatusUuid(string);
					dispStatusCountArray[dc] = dispStatusCount;
					dc++;
				}
				
				line.setDispositionStatusCount(dispStatusCountArray);

				// Status
				HashMap<Boolean, Integer> statusResult = rulesReportLine.getStatusCount();
				StatusCount_type0[] statusCountResp = new StatusCount_type0[statusResult.size()];
				StatusCount_type0 statusCoutnFalse = new StatusCount_type0();
				statusCoutnFalse.setCount(statusResult.get(new Boolean(false)));
				statusCoutnFalse.setStatus(new Boolean(false));
				statusCountResp[0] = statusCoutnFalse;
				StatusCount_type0 statusCoutnTrue = new StatusCount_type0();
				statusCoutnTrue.setCount(statusResult.get(new Boolean(true)));
				statusCoutnTrue.setStatus(new Boolean(true));
				statusCountResp[1] = statusCoutnTrue;

				line.setStatusCount(statusCountResp);

				Rule ruelResult = rulesReportLine.getRule();
				org.ihtsdo.qadb.ws.data.Rule ruleResponse = convertRule(ruelResult);

				line.setRule(ruleResponse);

				responsLines[responseLineCount] = line;
				responseLineCount++;
				// dispStatusCount_type0.setDispositionStatusCount(param)

			}
		}

		response.setRulesReportLine(responsLines);

		return response;
	}

	public static org.ihtsdo.qadb.ws.data.Rule convertRule(Rule ruleResult) {
		org.ihtsdo.qadb.ws.data.Rule response = new org.ihtsdo.qadb.ws.data.Rule();
		response.setRuleUuid(ruleResult.getRuleUuid());
		response.setName(ruleResult.getName());
		response.setCategory(ruleResult.getCategory());
		logger.debug(ruleResult.getSeverity());
		if(ruleResult.getSeverity() != null){
			org.ihtsdo.qadb.ws.data.Severity severity = new org.ihtsdo.qadb.ws.data.Severity();
			severity.setName(ruleResult.getSeverity().getSeverityName());
			severity.setSeverityUuid(ruleResult.getSeverity().getSeverityUuid());
			severity.setStatus(ruleResult.getSeverity().getSeverityStatus());
			response.setSeverity(severity);
		}
		response.setRuleCode(ruleResult.getRuleCode());
		return response;
	}
	
	public static org.ihtsdo.qadb.ws.data.Rule ruleToWsRule(Rule rule) {
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
		wsRule.setEffectiveTime(rule.getEffectiveTime());
		if(rule.getSeverity() != null){
			org.ihtsdo.qadb.ws.data.Severity severity = new org.ihtsdo.qadb.ws.data.Severity();
			severity.setName(rule.getSeverity().getSeverityName());
			severity.setSeverityUuid(rule.getSeverity().getSeverityUuid());
			severity.setStatus(rule.getSeverity().getSeverityStatus());
			wsRule.setSeverity(severity);
		}
		wsRule.setStandingIssues(rule.getStandingIssues());
		wsRule.setStatus(rule.getStatus());
		wsRule.setSuggestedResolution(rule.getSuggestedResolution());
		return wsRule;
	}

	public static Component componentToWsComponent(TerminologyComponent component) {
		Component result = null;
		if(component != null){
			result = new Component();
			result.setComponentName(component.getComponentName());
			result.setComponentUuid(component.getComponentUuid());
			result.setSctid(""+component.getSctid());
		}
		return result;
	}

	public static Case caseToWsCase(QACase qaCase) {
		Case result = new Case();
		result.setCaseUuid(qaCase.getCaseUuid());
		result.setDispositionReasonUuid(qaCase.getDispositionReasonUuid());
		result.setDispositionStatusDate(qaCase.getDispositionStatusDate());
		result.setDispositionStatusEditor(qaCase.getDispositionStatusEditor());
		result.setDispositionAnnotation(qaCase.getDispositionAnnotation());
		result.setIsActive(qaCase.isActive());
		result.setEffectiveTime(qaCase.getEffectiveTime());
		result.setAssignedTo(qaCase.getAssignedTo());
		
		Component component = new Component();
		component.setComponentUuid(qaCase.getComponentUuid().getComponentUuid());
		result.setComponent(component );
		result.setPathUuid(qaCase.getPathUuid());
		result.setDatabaseUuid(qaCase.getDatabaseUuid());
		result.setDetail(qaCase.getDetail());
		
		org.ihtsdo.qadb.ws.data.DispositionStatus dispStatus = new org.ihtsdo.qadb.ws.data.DispositionStatus();
		dispStatus.setDispositionStatusUuid(qaCase.getDispositionStatus().getDispositionStatusUuid());
		result.setDispositionStatus(dispStatus );
		
		org.ihtsdo.qadb.ws.data.Rule rule = new org.ihtsdo.qadb.ws.data.Rule();
		rule.setRuleUuid(qaCase.getRuleUuid());
		result.setRule(rule);
		QACaseComment[] wsComments = null;
		if(qaCase.getComments() != null && ! qaCase.getComments().isEmpty()){
			wsComments = new QACaseComment[qaCase.getComments().size()];
			int i = 0;
			for (QAComment comment : qaCase.getComments()) {
				QACaseComment wsComment = commentToWsComment(comment);
				wsComments[i] = wsComment;
				i++;
			}
		}
		result.setComments(wsComments);
		result.setAssignmentEditor(qaCase.getAssignmentEditor());
		result.setAssignmentDate(qaCase.getAssignmentDate());
		return result;
	}

	public static QACaseComment commentToWsComment(QAComment comment) {
		QACaseComment wsComment = new QACaseComment();
		wsComment.setAuthor(comment.getAuthor());
		wsComment.setComment(comment.getComment());
		wsComment.setCommentUuid(comment.getCommentUuid());
		wsComment.setEffectiveTime(comment.getEffectiveTime());
		wsComment.setCaseUuid(comment.getCaseUuid());
		if(comment.getStatus() != null){
			wsComment.setStatus(BigInteger.valueOf(comment.getStatus()));
		}
		return wsComment;
	}

	public static Execution executionToWsExecution(org.ihtsdo.qadb.data.Execution execution) {
		Execution result = new Execution();
		result.setContextName(execution.getContextName());
		result.setDate(execution.getDate());
		result.setDescription(execution.getDescription());
		result.setEndTime(execution.getEndTime());
		result.setExecutionUuid(execution.getExecutionUuid());
		result.setName(execution.getName());
		result.setStartTime(execution.getStartTime());
		result.setPathUuid(execution.getPathUuid());
		result.setViewPointTime(execution.getViewPointTime());
		return result;
	}

	public static org.ihtsdo.qadb.ws.data.Finding findingToWsFinding(Finding finding) {
		org.ihtsdo.qadb.ws.data.Finding result = new org.ihtsdo.qadb.ws.data.Finding();
		
		return null;
	}

	public static org.ihtsdo.qadb.ws.data.DispositionStatus dispositionStatusToWsDispositionStatus(DispositionStatus dispositionStatus) {
		org.ihtsdo.qadb.ws.data.DispositionStatus result = new org.ihtsdo.qadb.ws.data.DispositionStatus();
		result.setDispositionStatusUuid(dispositionStatus.getDispositionStatusUuid());
		result.setName(dispositionStatus.getName());
		return result;
	}

	public static Database databaseToWsDatabase(QADatabase qaDb) {
		Database result = new Database();
		result.setDatabaseUuid(qaDb.getDatabaseUuid());
		result.setName(qaDb.getName());
		return result;
	}

	public static org.ihtsdo.qadb.ws.data.Severity severityToWsSeverity(Severity severity) {
		org.ihtsdo.qadb.ws.data.Severity result = new org.ihtsdo.qadb.ws.data.Severity();
		result.setAuthor(severity.getSeverityAuthor());
		result.setDescription(severity.getSeverityDescription());
		result.setName(severity.getSeverityName());
		result.setSeverityUuid(severity.getSeverityUuid());
		result.setStatus(severity.getSeverityStatus());
		return result;
	}

	public static QACoordinate qaCoordToWsCoord(org.ihtsdo.qadb.ws.data.QACoordinate qaCoordinate) {
		QACoordinate result = new QACoordinate(qaCoordinate.getDatabaseUuid(), qaCoordinate.getPathUuid(), qaCoordinate.getViewPointTime());
		return result;
	}

	public static LinkedHashMap<Integer, Boolean> wsSortByToSortBy(IntBoolKeyValue[] sorteBy) {
		LinkedHashMap<Integer, Boolean> result = null;
		if(sorteBy != null && sorteBy.length != 0){
			result = new LinkedHashMap<Integer, Boolean>();
			for (IntBoolKeyValue intBoolKeyValue : sorteBy) {
				result.put(intBoolKeyValue.getKey(), intBoolKeyValue.getValue());
			}
		}
		return result;
	}

	public static HashMap<Integer, Object> wsFilterToFilter(IntStrKeyValue[] filter) {
		HashMap<Integer, Object> result = null;
		if(filter != null && filter.length != 0){
			result = new HashMap<Integer, Object>();
			for (IntStrKeyValue element : filter) {
				logger.info("Filter: " + element.getKey() + " Value: " + element.getValue());
				result.put(element.getKey(), element.getValue());
			}
		}
		return result;
	}

	public static WsQACasesReportLine reportLineToWsReportLine(QACasesReportLine qaCasesReportLine) {
		WsQACasesReportLine result = new WsQACasesReportLine();
		Component component = componentToWsComponent(qaCasesReportLine.getComponent());
		result.setComponent(component);
		org.ihtsdo.qadb.ws.data.DispositionStatus dispStatus = dispositionStatusToWsDispositionStatus(qaCasesReportLine.getDisposition());
		result.setDispositionStatus(dispStatus );
		Case qaCase = caseToWsCase(qaCasesReportLine.getQaCase());
		result.setQaCase(qaCase );
		
		return result;
	}

	public static WsCategory categoryToWsCategory(Category category) {
		WsCategory wsCategory = new WsCategory();
		wsCategory.setCategoryUuid(""+category.getCategoryUuid());
		wsCategory.setDescription(category.getDescription());
		wsCategory.setName(category.getName());
		return wsCategory;
	}

	public static Rule wsRuleToRule(org.ihtsdo.qadb.ws.data.Rule rule) {
		Rule result = new Rule();
		result.setCategory(rule.getCategory());
		result.setDescription(rule.getDescription());
		result.setDitaDocumentationLinkUuid(rule.getDitaDocumentationLinkUuid());
		result.setDitaGeneratedTopicUuid(rule.getDitaGeneratedTopicUuid());
		result.setDocumentationUrl(rule.getDocumentationUrl());
		result.setDitaUuid(rule.getDitaUuid());
		result.setExample(rule.getExample());
		result.setExpectedResult(rule.getExpectedResult());
		result.setModifiedBy(rule.getModifiedBy());
		result.setName(rule.getName());
		result.setPackageName(rule.getPackageName());
		result.setPackageUrl(rule.getPackageUrl());
		result.setEffectiveTime(rule.getEffectiveTime());
		result.setRuleCode(rule.getRuleCode());
		result.setRuleUuid(rule.getRuleUuid());
		Severity severity = new Severity();
		if(rule.getSeverity() != null){
			severity.setSeverityAuthor(rule.getSeverity().getAuthor());
			severity.setSeverityDescription(rule.getSeverity().getDescription());
			severity.setSeverityName(rule.getSeverity().getName());
			severity.setSeverityStatus(rule.getSeverity().getStatus());
			severity.setSeverityUuid(rule.getSeverity().getSeverityUuid());
		}
		result.setSeverity(severity);
		result.setStandingIssues(rule.getStandingIssues());
		result.setStatus(rule.getStatus());
		result.setSuggestedResolution(rule.getSuggestedResolution());
		result.setWhitelistAllowed(rule.getIsWhitelistAllowed());
		result.setWhitelistResetAllowed(rule.getIsWhitelistResetAllowed());
		result.setWhitelistResetWhenClosed(rule.getIsWhitelistResetWhenClosed());
		return result;
	}


}
