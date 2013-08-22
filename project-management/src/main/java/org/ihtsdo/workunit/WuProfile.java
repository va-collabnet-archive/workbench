package org.ihtsdo.workunit;

import java.util.UUID;

public class WuProfile {
	
	private UUID editModuleId;
	private boolean restrictEditsToEditModule;
	private UUID editPathId;
	private boolean attributesEditable;
	private boolean descriptionsEditable;
	private boolean relationshipsEditable;
	private boolean membersEditable;

	public WuProfile() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the editModuleId
	 */
	public UUID getEditModuleId() {
		return editModuleId;
	}

	/**
	 * @param editModuleId the editModuleId to set
	 */
	public void setEditModuleId(UUID editModuleId) {
		this.editModuleId = editModuleId;
	}

	/**
	 * @return the restrictEditsToEditModule
	 */
	public boolean isRestrictEditsToEditModule() {
		return restrictEditsToEditModule;
	}

	/**
	 * @param restrictEditsToEditModule the restrictEditsToEditModule to set
	 */
	public void setRestrictEditsToEditModule(boolean restrictEditsToEditModule) {
		this.restrictEditsToEditModule = restrictEditsToEditModule;
	}

	/**
	 * @return the editPathId
	 */
	public UUID getEditPathId() {
		return editPathId;
	}

	/**
	 * @param editPathId the editPathId to set
	 */
	public void setEditPathId(UUID editPathId) {
		this.editPathId = editPathId;
	}

	/**
	 * @return the attributesEditable
	 */
	public boolean isAttributesEditable() {
		return attributesEditable;
	}

	/**
	 * @param attributesEditable the attributesEditable to set
	 */
	public void setAttributesEditable(boolean attributesEditable) {
		this.attributesEditable = attributesEditable;
	}

	/**
	 * @return the descriptionsEditable
	 */
	public boolean isDescriptionsEditable() {
		return descriptionsEditable;
	}

	/**
	 * @param descriptionsEditable the descriptionsEditable to set
	 */
	public void setDescriptionsEditable(boolean descriptionsEditable) {
		this.descriptionsEditable = descriptionsEditable;
	}

	/**
	 * @return the relationshipsEditable
	 */
	public boolean isRelationshipsEditable() {
		return relationshipsEditable;
	}

	/**
	 * @param relationshipsEditable the relationshipsEditable to set
	 */
	public void setRelationshipsEditable(boolean relationshipsEditable) {
		this.relationshipsEditable = relationshipsEditable;
	}

	/**
	 * @return the membersEditable
	 */
	public boolean isMembersEditable() {
		return membersEditable;
	}

	/**
	 * @param membersEditable the membersEditable to set
	 */
	public void setMembersEditable(boolean membersEditable) {
		this.membersEditable = membersEditable;
	}

}
