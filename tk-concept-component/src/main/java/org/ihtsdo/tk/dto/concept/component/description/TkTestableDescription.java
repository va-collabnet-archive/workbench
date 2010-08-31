package org.ihtsdo.tk.dto.concept.component.description;

import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.I_AddCommonTestingProps;
import org.ihtsdo.tk.api.ext.I_DescribeExternally;
import org.ihtsdo.tk.dto.concept.component.I_AmComponent;

public class TkTestableDescription implements I_AddCommonTestingProps, I_DescribeExternally, I_AmComponent {
	
	private Boolean changedComponent = false;
	private Boolean current = false;
	private Boolean lastVersion = false;
	private Boolean newComponent = false;
	private Boolean published = false;
	private Boolean retired = false;
	private Boolean uncommitted = false;
	private TkDescription description = null;
	private TkDescriptionRevision descriptionRevision = null;
	
	public TkTestableDescription(TkDescription description,
			TkDescriptionRevision descriptionRevision) {
		super();
		this.description = description;
		this.descriptionRevision = descriptionRevision;
	}
	
	public Boolean isChangedComponent() {
		return changedComponent;
	}
	public void setChangedComponent(Boolean changedComponent) {
		this.changedComponent = changedComponent;
	}
	public Boolean isCurrent() {
		return current;
	}
	public void setCurrent(Boolean current) {
		this.current = current;
	}
	public Boolean isLastVersion() {
		return lastVersion;
	}
	public void setLastVersion(Boolean lastVersion) {
		this.lastVersion = lastVersion;
	}
	public Boolean isNewComponent() {
		return newComponent;
	}
	public void setNewComponent(Boolean newComponent) {
		this.newComponent = newComponent;
	}
	public Boolean isPublished() {
		return published;
	}
	public void setPublished(Boolean published) {
		this.published = published;
	}
	public Boolean isRetired() {
		return retired;
	}
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	public Boolean isUncommitted() {
		return uncommitted;
	}
	public void setUncommitted(Boolean uncommitted) {
		this.uncommitted = uncommitted;
	}
	@Override
	public String getLang() {
		return descriptionRevision.getLang();
	}
	@Override
	public String getText() {
		return descriptionRevision.getText();
	}
	@Override
	public UUID getTypeUuid() {
		return descriptionRevision.getTypeUuid();
	}
	@Override
	public boolean isInitialCaseSignificant() {
		return descriptionRevision.isInitialCaseSignificant();
	}
	@Override
	public UUID getAuthorUuid() {
		return descriptionRevision.getAuthorUuid();
	}
	@Override
	public UUID getPathUuid() {
		return descriptionRevision.getPathUuid();
	}
	@Override
	public UUID getStatusUuid() {
		return descriptionRevision.getStatusUuid();
	}
	@Override
	public long getTime() {
		return descriptionRevision.getTime();
	}

	@Override
	public List getAdditionalIdComponents() {
		return description.getAdditionalIdComponents();
	}

	@Override
	public List getEIdentifiers() {
		return description.getEIdentifiers();
	}

	@Override
	public int getIdComponentCount() {
		return description.getIdComponentCount();
	}

	@Override
	public UUID getPrimordialComponentUuid() {
		return description.getPrimordialComponentUuid();
	}

	@Override
	public List getRevisionList() {
		return description.getRevisionList();
	}

	@Override
	public List getRevisions() {
		return description.getRevisions();
	}

	@Override
	public List getUuids() {
		return description.getUuids();
	}

	@Override
	public int getVersionCount() {
		return description.getVersionCount();
	}
}
