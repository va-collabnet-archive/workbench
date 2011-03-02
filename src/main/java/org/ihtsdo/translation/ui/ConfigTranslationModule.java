package org.ihtsdo.translation.ui;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.translation.ui.config.TranslatorDefaultEditorModePanel;

public class ConfigTranslationModule  implements Serializable{

	private static final long serialVersionUID = 1L;
	private boolean autoOpenNextInboxItem;
	private boolean enableSpellChecker;
	private boolean isProjectDefaultConfiguration;

	private HashMap<UUID, EditorMode> translatorRoles;

	private LinkedHashSet<TreeComponent> sourceTreeComponents;
	private LinkedHashSet<TreeComponent> targetTreeComponents;
	private LinkedHashSet<InboxColumn> ColumnsDisplayedInInbox;
	private EditorMode selectedEditorMode;
	private EditingPanelOpenMode editingPanelOpenMode;
	private DefaultSimilaritySearchOption defaultSimilaritySearchOption;
	private PreferredTermDefault selectedPrefTermDefault;
	private FsnGenerationStrategy selectedFsnGenStrategy;
	private IcsGenerationStrategy selectedIcsGenerationStrategy;
	private List<UUID> projectIssuesRepositoryIds;
	private List<UUID> sourceIssuesRepositoryIds;
	
	
	public ConfigTranslationModule() {
		super();
		this.autoOpenNextInboxItem = true;
		this.enableSpellChecker = true;
		
		LinkedHashSet<TreeComponent> treeComponentsPreference = new LinkedHashSet<TreeComponent>();
		treeComponentsPreference.add(TreeComponent.FSN);
		treeComponentsPreference.add(TreeComponent.PREFERRED);
		treeComponentsPreference.add(TreeComponent.SYNONYM);
		
		this.sourceTreeComponents = treeComponentsPreference;
		this.targetTreeComponents = treeComponentsPreference;
		
		this.selectedEditorMode = EditorMode.PREFERRED_TERM_EDITOR;
		this.editingPanelOpenMode = EditingPanelOpenMode.FSN_TERM_MODE;
		this.defaultSimilaritySearchOption = DefaultSimilaritySearchOption.FSN;
		this.selectedFsnGenStrategy = FsnGenerationStrategy.COPY_SOURCE_LANGUAGE;
		this.selectedIcsGenerationStrategy = IcsGenerationStrategy.NONE;
		
		LinkedHashSet<InboxColumn> columns = new LinkedHashSet<InboxColumn>();
		columns.add(InboxColumn.SOURCE_PREFERRED);
		columns.add(InboxColumn.STATUS);
		this.ColumnsDisplayedInInbox = columns;
		this.translatorRoles = new HashMap<UUID, EditorMode>();
		try {
			Set<I_GetConceptData> roles = TranslatorDefaultEditorModePanel.getTranslationRoles();
			for (I_GetConceptData role : roles) {
				translatorRoles.put(role.getUids().iterator().next(), EditorMode.READ_ONLY);
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public enum FsnGenerationStrategy implements Serializable{
		NONE("None"), 
		SAME_AS_PREFERRED("Same as preferred"), 
		COPY_SOURCE_LANGUAGE("Copy source language"),
		LINK_SOURCE_LANGUAGE("Link source language");

		private final String name;

		private FsnGenerationStrategy(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}

	}
	
	public enum DefaultSimilaritySearchOption implements Serializable{
		FSN("FSN"), 
		PREFERRED("Preferred"), 
		BOTH("Both");
		
		private final String name;
		
		private DefaultSimilaritySearchOption(String name) {
			this.name = name;
		}
		
		public String toString() {
			return this.name;
		}
		
	}
	
	public enum IcsGenerationStrategy implements Serializable{
		NONE("None"), 
		COPY_FROM_SOURCE("Copy from source"), 
		WORDS_LIST("Words list");

		private final String name;

		private IcsGenerationStrategy(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}

	}
	
	public enum EditingPanelOpenMode implements Serializable{
		FSN_TERM_MODE("FSN term mode"),
		PREFFERD_TERM_MODE("Prefferd term mode");
		
		private final String name;

		private EditingPanelOpenMode(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}
	}

	public enum EditorMode implements Serializable{
		FULL_EDITOR("Full editor"), 
		PREFERRED_TERM_EDITOR("Preferred term editor"), 
		SYNONYMS_EDITOR("Synonyms editor"),
		READ_ONLY("Read only");
		

		private final String name;

		private EditorMode(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}

	}

	public enum PreferredTermDefault implements Serializable{
		BLANK("Blank"), 
		SOURCE("Source"), 
		BEST_SIMILARITY_MATCH("Best similarity match");

		private final String name;

		private PreferredTermDefault(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}

	}

	public enum TreeComponent implements Serializable{
		FSN("Fully Specified Name"), 
		PREFERRED("Preferred"), 
		SYNONYM("Synonym"),
		RETIRED("Retired descriptions"),
		AUTHOR_PATH("Author path");

		private final String name;

		private TreeComponent(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}

	}

	public enum InboxColumn implements Serializable {
		SOURCE_PREFERRED(String.class,"Source preferred term"),
		TARGET_PREFERRED(String.class,"Target preferred term"),
		TARGET_FSN(String.class,"Target FSN term"),
		STATUS(String.class,"Status"),
		STATUS_DATE(String.class,"Date");

		private final Class editorClass;
		private final String columnName;
		InboxColumn(Class editorClass,String columnName) {
			this.editorClass = editorClass;
			this.columnName=columnName;
		}

		public String getColumnName(){
			return columnName;
		}

		public Class getEditorClass() {
			return editorClass;
		}

	}
	
	public boolean isEnableSpellChecker() {
		return enableSpellChecker;
	}

	public void setEnableSpellChecker(boolean enableSpellChecker) {
		this.enableSpellChecker = enableSpellChecker;
	}

	public boolean isAutoOpenNextInboxItem() {
		return autoOpenNextInboxItem;
	}
	
	public void setAutoOpenNextInboxItem(boolean autoOpenNextInboxItem) {
		this.autoOpenNextInboxItem = autoOpenNextInboxItem;
	}
	
	public LinkedHashSet<TreeComponent> getSourceTreeComponents() {
		return sourceTreeComponents;
	}
	
	public void setSourceTreeComponents(
			LinkedHashSet<TreeComponent> sourceTreeComponents) {
		this.sourceTreeComponents = sourceTreeComponents;
	}
	
	public LinkedHashSet<TreeComponent> getTargetTreeComponents() {
		return targetTreeComponents;
	}
	
	public void setTargetTreeComponents(
			LinkedHashSet<TreeComponent> targetTreeComponents) {
		this.targetTreeComponents = targetTreeComponents;
	}
	
	public LinkedHashSet<InboxColumn> getColumnsDisplayedInInbox() {
		return ColumnsDisplayedInInbox;
	}
	
	public void setColumnsDisplayedInInbox(
			LinkedHashSet<InboxColumn> columnsDisplayedInInbox) {
		ColumnsDisplayedInInbox = columnsDisplayedInInbox;
	}
	
	public EditorMode getSelectedEditorMode() {
		return selectedEditorMode;
	}
	
	public void setSelectedEditorMode(EditorMode selectedEditorMode) {
		this.selectedEditorMode = selectedEditorMode;
	}
	
	public PreferredTermDefault getSelectedPrefTermDefault() {
		return selectedPrefTermDefault;
	}
	
	public void setSelectedPrefTermDefault(
			PreferredTermDefault selectedPrefTermDefault) {
		this.selectedPrefTermDefault = selectedPrefTermDefault;
	}
	
	public FsnGenerationStrategy getSelectedFsnGenStrategy() {
		return selectedFsnGenStrategy;
	}
	
	public void setSelectedFsnGenStrategy(
			FsnGenerationStrategy selectedFsnGenStrategy) {
		this.selectedFsnGenStrategy = selectedFsnGenStrategy;
	}

	public IcsGenerationStrategy getSelectedIcsGenerationStrategy() {
		return selectedIcsGenerationStrategy;
	}

	public void setSelectedIcsGenerationStrategy(
			IcsGenerationStrategy selectedIcsGenerationStrategy) {
		this.selectedIcsGenerationStrategy = selectedIcsGenerationStrategy;
	}

	public List<UUID> getProjectIssuesRepositoryIds() {
		return projectIssuesRepositoryIds;
	}

	public void setProjectIssuesRepositoryIds(List<UUID> projectIssuesRepositoryIds) {
		this.projectIssuesRepositoryIds = projectIssuesRepositoryIds;
	}

	public List<UUID> getSourceIssuesRepositoryIds() {
		return sourceIssuesRepositoryIds;
	}

	public void setSourceIssuesRepositoryIds(List<UUID> sourceIssuesRepositoryIds) {
		this.sourceIssuesRepositoryIds = sourceIssuesRepositoryIds;
	}

	public void setProjectDefaultConfiguration(boolean isProjectDefaultConfiguration) {
		this.isProjectDefaultConfiguration = isProjectDefaultConfiguration;
	}

	public boolean isProjectDefaultConfiguration() {
		return isProjectDefaultConfiguration;
	}

	public void setTranslatorRoles(HashMap<UUID, EditorMode> translatorRoles) {
		this.translatorRoles = translatorRoles;
	}

	public HashMap<UUID, EditorMode> getTranslatorRoles() {
		return translatorRoles;
	}

	public void setDefaultSimilaritySearchOption(DefaultSimilaritySearchOption defaultSimilaritySearchOption) {
		this.defaultSimilaritySearchOption = defaultSimilaritySearchOption;
	}

	public DefaultSimilaritySearchOption getDefaultSimilaritySearchOption() {
		return defaultSimilaritySearchOption;
	}

	public void setEditingPanelOpenMode(EditingPanelOpenMode editingPanelOpenMode) {
		this.editingPanelOpenMode = editingPanelOpenMode;
	}

	public EditingPanelOpenMode getEditingPanelOpenMode() {
		return editingPanelOpenMode;
	}
	
}
