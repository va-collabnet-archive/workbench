package org.ihtsdo.tk.helper.templates;

public class ConceptTemplate extends AbstractTemplate {
	
	private boolean defined;

	public ConceptTemplate() {
		setType(TemplateType.CONCEPT);
	}

	public boolean isDefined() {
		return defined;
	}

	public void setDefined(boolean defined) {
		this.defined = defined;
	}

}
