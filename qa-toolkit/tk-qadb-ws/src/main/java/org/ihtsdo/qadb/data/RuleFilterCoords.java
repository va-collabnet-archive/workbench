package org.ihtsdo.qadb.data;

public class RuleFilterCoords {
	private String ruleUuid;
	private String status;
	private Integer statusFilter;
	private String name;
	private String ruleCode;
	private String ruleCategory;
	private String severity;
	private String databaseUuid;
	private String pathUuid;
	private String viewPointTime;
	private Boolean conceptNameOrder;
	private Boolean statusOrder;
	private Boolean dispositionOrder;
	private Integer startLine;
	private Integer pageLenght;
	private String dispStatusUuidFilter;
	private String assignedToFilter;
	private DispositionStatus dispStatusFilter;

	public Integer getStatusFilter() {
		return statusFilter;
	}

	public void setStatusFilter(Integer statusFilter) {
		this.statusFilter = statusFilter;
	}

	public String getAssignedToFilter() {
		return assignedToFilter;
	}

	public void setAssignedToFilter(String assignedToFilter) {
		this.assignedToFilter = assignedToFilter;
	}

	public String getDispStatusUuidFilter() {
		return dispStatusUuidFilter;
	}

	public void setDispStatusUuidFilter(String dispStatusUuidFilter) {
		this.dispStatusUuidFilter = dispStatusUuidFilter;
	}

	public DispositionStatus getDispStatusFilter() {
		return dispStatusFilter;
	}

	public void setDispStatusFilter(DispositionStatus dispStatusFilter) {
		this.dispStatusFilter = dispStatusFilter;
	}

	public Integer getStartLine() {
		return startLine;
	}

	public void setStartLine(Integer startLine) {
		this.startLine = startLine;
	}

	public Integer getPageLenght() {
		return pageLenght;
	}

	public void setPageLenght(Integer pageLenght) {
		this.pageLenght = pageLenght;
	}

	public Boolean getConceptNameOrder() {
		return conceptNameOrder;
	}

	public void setConceptNameOrder(Boolean conceptNameOrder) {
		this.conceptNameOrder = conceptNameOrder;
	}

	public Boolean getStatusOrder() {
		return statusOrder;
	}

	public void setStatusOrder(Boolean statusOrder) {
		this.statusOrder = statusOrder;
	}

	public Boolean getDispositionOrder() {
		return dispositionOrder;
	}

	public void setDispositionOrder(Boolean dispositionOrder) {
		this.dispositionOrder = dispositionOrder;
	}

	public String getRuleUuid() {
		return ruleUuid;
	}

	public void setRuleUuid(String ruleUuid) {
		this.ruleUuid = ruleUuid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRuleCode() {
		return ruleCode;
	}

	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}

	public String getRuleCategory() {
		return ruleCategory;
	}

	public void setRuleCategory(String ruleCategory) {
		this.ruleCategory = ruleCategory;
	}

	public String getDatabaseUuid() {
		return databaseUuid;
	}

	public void setDatabaseUuid(String databaseUuid) {
		this.databaseUuid = databaseUuid;
	}

	public String getPathUuid() {
		return pathUuid;
	}

	public void setPathUuid(String pathUuid) {
		this.pathUuid = pathUuid;
	}

	public String getViewPointTime() {
		return viewPointTime;
	}

	public void setViewPointTime(String viewPointTime) {
		this.viewPointTime = viewPointTime;
	}
}
