package org.ihtsdo.translation.ui.translation;

import org.ihtsdo.project.ContextualizedDescription;

public class TranslationTermRow {
	private String langCode;

	public TranslationTermRow(ContextualizedDescription contextualizedDescription) {
		this.langCode = contextualizedDescription.getLang();
	}

}
