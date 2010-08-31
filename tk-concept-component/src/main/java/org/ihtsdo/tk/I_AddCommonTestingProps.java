package org.ihtsdo.tk;

public interface I_AddCommonTestingProps {
	
	public Boolean isLastVersion();
	public void setLastVersion(Boolean published);
	
	public Boolean isPublished();
	public void setPublished(Boolean published);
	
	public Boolean isUncommitted();
	public void setUncommitted(Boolean newComponent);
	
	public Boolean isNewComponent();
	public void setNewComponent(Boolean changedComponent);
	
	public Boolean isChangedComponent();
	public void setChangedComponent(Boolean changedComponent);
	
	public Boolean isCurrent();
	public void setCurrent(Boolean published);

	public Boolean isRetired();
	public void setRetired(Boolean retired);

}
