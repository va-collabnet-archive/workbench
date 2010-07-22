package org.ihtsdo.tk;

public interface I_AddCommonTestingProps {
	
	public Boolean isPublished();
	public void setPublished(Boolean published);
	
	public Boolean isNewComponent();
	public void setNewComponent(Boolean newComponent);
	
	public Boolean isChangedComponent();
	public void setChangedComponent(Boolean changedComponent);

	public Boolean isRetired();
	public void setRetired(Boolean retired);

}
