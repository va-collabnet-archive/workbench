package org.ihtsdo.workunit.sif;

import java.util.List;

public abstract class SifTerminologyComponent {
	
	private List<SifIdentifier> identifiers;
	
	private SifIdentifier moduleId;
	private long effectiveTime;
	private long plannedEffectiveTime;
	private long changeTime;
	private boolean active;
	
	private List<SifRefsetMemberSimpleType> simpleTypeMemberships;
	private List<SifRefsetMemberCidType> cidTypeMemberships;

	public SifTerminologyComponent() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the identifiers
	 */
	public List<SifIdentifier> getIdentifiers() {
		return identifiers;
	}

	/**
	 * @param identifiers the identifiers to set
	 */
	public void setIdentifiers(List<SifIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	/**
	 * @return the moduleId
	 */
	public SifIdentifier getModuleId() {
		return moduleId;
	}

	/**
	 * @param moduleId the moduleId to set
	 */
	public void setModuleId(SifIdentifier moduleId) {
		this.moduleId = moduleId;
	}

	/**
	 * @return the effectiveTime
	 */
	public long getEffectiveTime() {
		return effectiveTime;
	}

	/**
	 * @param effectiveTime the effectiveTime to set
	 */
	public void setEffectiveTime(long effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	/**
	 * @return the plannedEffectiveTime
	 */
	public long getPlannedEffectiveTime() {
		return plannedEffectiveTime;
	}

	/**
	 * @param plannedEffectiveTime the plannedEffectiveTime to set
	 */
	public void setPlannedEffectiveTime(long plannedEffectiveTime) {
		this.plannedEffectiveTime = plannedEffectiveTime;
	}

	/**
	 * @return the changeTime
	 */
	public long getChangeTime() {
		return changeTime;
	}

	/**
	 * @param changeTime the changeTime to set
	 */
	public void setChangeTime(long changeTime) {
		this.changeTime = changeTime;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the simpleTypeMemberships
	 */
	public List<SifRefsetMemberSimpleType> getSimpleTypeMemberships() {
		return simpleTypeMemberships;
	}

	/**
	 * @param simpleTypeMemberships the simpleTypeMemberships to set
	 */
	public void setSimpleTypeMemberships(
			List<SifRefsetMemberSimpleType> simpleTypeMemberships) {
		this.simpleTypeMemberships = simpleTypeMemberships;
	}

	/**
	 * @return the cidTypeMemberships
	 */
	public List<SifRefsetMemberCidType> getCidTypeMemberships() {
		return cidTypeMemberships;
	}

	/**
	 * @param cidTypeMemberships the cidTypeMemberships to set
	 */
	public void setCidTypeMemberships(
			List<SifRefsetMemberCidType> cidTypeMemberships) {
		this.cidTypeMemberships = cidTypeMemberships;
	}

}
