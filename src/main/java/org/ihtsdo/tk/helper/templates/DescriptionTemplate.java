package org.ihtsdo.tk.helper.templates;

public class DescriptionTemplate extends AbstractTemplate {
	
	private String text;
	private String langCode;
	private boolean initialCaseSignificant;

	public DescriptionTemplate() {
		setType(TemplateType.DESCRIPTION);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	public boolean isInitialCaseSignificant() {
		return initialCaseSignificant;
	}

	public void setInitialCaseSignificant(boolean initialCaseSignificant) {
		this.initialCaseSignificant = initialCaseSignificant;
	}
	
}
