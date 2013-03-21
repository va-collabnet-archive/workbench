/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.ihtsdo.translation.ui.ConfigTranslationModule.CompletionMode;
import org.ihtsdo.translation.ui.config.TranslatorDefaultEditorModePanel;

/**
 * The Class ConfigTranslationModule.
 */
public class ConfigTranslationModule implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The auto open next inbox item. */
	private boolean autoOpenNextInboxItem;

	/** The enable spell checker. */
	private boolean enableSpellChecker;

	/** The is project default configuration. */
	private boolean isProjectDefaultConfiguration;

	/** The translator roles. */
	private HashMap<UUID, EditorMode> translatorRoles;

	/** The source tree components. */
	private LinkedHashSet<TreeComponent> sourceTreeComponents;

	/** The target tree components. */
	private LinkedHashSet<TreeComponent> targetTreeComponents;

	/** The Columns displayed in inbox. */
	private LinkedHashSet<InboxColumn> ColumnsDisplayedInInbox;

	/** The selected editor mode. */
	private EditorMode selectedEditorMode;

	/** The editing panel open mode. */
	private EditingPanelOpenMode editingPanelOpenMode;

	/** The default similarity search option. */
	private DefaultSimilaritySearchOption defaultSimilaritySearchOption;

	/** The selected pref term default. */
	private PreferredTermDefault selectedPrefTermDefault;

	/** The selected fsn gen strategy. */
	private FsnGenerationStrategy selectedFsnGenStrategy;

	/** The selected ics generation strategy. */
	private IcsGenerationStrategy selectedIcsGenerationStrategy;

	/** The project issues repository ids. */
	private List<UUID> projectIssuesRepositoryIds;

	/** The source issues repository ids. */
	private List<UUID> sourceIssuesRepositoryIds;

	/** The source issues repository ids. */
	private CompletionMode completionMode;

	/**
	 * Instantiates a new config translation module.
	 */
	public ConfigTranslationModule() {
		super();
		this.autoOpenNextInboxItem = true;
		this.enableSpellChecker = false;

		LinkedHashSet<TreeComponent> treeComponentsPreference = new LinkedHashSet<TreeComponent>();
		treeComponentsPreference.add(TreeComponent.FSN);
		treeComponentsPreference.add(TreeComponent.PREFERRED);
		treeComponentsPreference.add(TreeComponent.SYNONYM);

		this.sourceTreeComponents = treeComponentsPreference;
		this.targetTreeComponents = treeComponentsPreference;

		this.selectedEditorMode = EditorMode.PREFERRED_TERM_EDITOR;
		this.editingPanelOpenMode = EditingPanelOpenMode.FSN_TERM_MODE;
		this.defaultSimilaritySearchOption = DefaultSimilaritySearchOption.FSN;
		this.selectedFsnGenStrategy = FsnGenerationStrategy.NONE;
		this.selectedIcsGenerationStrategy = IcsGenerationStrategy.NONE;
		this.selectedPrefTermDefault = PreferredTermDefault.BLANK;
		this.completionMode = CompletionMode.INCOMPLETE_INSTACES;

		LinkedHashSet<InboxColumn> columns = new LinkedHashSet<InboxColumn>();
		columns.add(InboxColumn.DEFAULT_DESCRIPTION);
		columns.add(InboxColumn.STATUS);
		this.ColumnsDisplayedInInbox = columns;
		this.translatorRoles = new HashMap<UUID, EditorMode>();
		try {
			Set<I_GetConceptData> roles = TranslatorDefaultEditorModePanel.getTranslationRoles();
			for (I_GetConceptData role : roles) {
				translatorRoles.put(role.getUids().iterator().next(), EditorMode.PREFERRED_TERM_EDITOR);
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * The Enum FsnGenerationStrategy.
	 */
	public enum FsnGenerationStrategy implements Serializable {

		/** The NONE. */
		NONE("None"),
		/** The SAM e_ a s_ preferred. */
		SAME_AS_PREFERRED("Same as preferred"),
		/** The COP y_ sourc e_ language. */
		COPY_SOURCE_LANGUAGE("Copy source language"),
		/** The LIN k_ sourc e_ language. */
		LINK_SOURCE_LANGUAGE("Link source language");

		/** The name. */
		private final String name;

		/**
		 * Instantiates a new fsn generation strategy.
		 * 
		 * @param name
		 *            the name
		 */
		private FsnGenerationStrategy(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return this.name;
		}

	}

	/**
	 * The Enum DefaultSimilaritySearchOption.
	 */
	public enum DefaultSimilaritySearchOption implements Serializable {

		/** The FSN. */
		FSN("FSN"),
		/** The PREFERRED. */
		PREFERRED("Preferred"),
		/** The BOTH. */
		BOTH("Both");

		/** The name. */
		private final String name;

		/**
		 * Instantiates a new default similarity search option.
		 * 
		 * @param name
		 *            the name
		 */
		private DefaultSimilaritySearchOption(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return this.name;
		}

	}

	/**
	 * The Enum IcsGenerationStrategy.
	 */
	public enum IcsGenerationStrategy implements Serializable {

		NONE("None"), COPY_FROM_SOURCE("Copy from source");

		private final String name;

		private IcsGenerationStrategy(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}
	}

	/**
	 * The Enum EditingPanelOpenMode.
	 */
	public enum EditingPanelOpenMode implements Serializable {

		/** The FS n_ ter m_ mode. */
		FSN_TERM_MODE("FSN term mode"),
		/** The PREFFER d_ ter m_ mode. */
		PREFFERD_TERM_MODE("Preferred term mode");

		/** The name. */
		private final String name;

		/**
		 * Instantiates a new editing panel open mode.
		 * 
		 * @param name
		 *            the name
		 */
		private EditingPanelOpenMode(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return this.name;
		}
	}

	/**
	 * The Enum EditorMode.
	 */
	public enum CompletionMode implements Serializable {
		
		ALL_INSTANCES("All Instances"),
		COMPLETE_INSTANCES("Complete Instances"),
		INCOMPLETE_INSTACES("Incomplete Instances");
		
		/** The name. */
		private final String name;
		
		/**
		 * Instantiates a new editor mode.
		 * 
		 * @param name
		 *            the name
		 */
		private CompletionMode(String name) {
			this.name = name;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return this.name;
		}
		
	}
	
	/**
	 * The Enum EditorMode.
	 */
	public enum EditorMode implements Serializable {

		/** The FUL l_ editor. */
		FULL_EDITOR("Full editor"),
		/** The PREFERRE d_ ter m_ editor. */
		PREFERRED_TERM_EDITOR("Preferred term editor"),
		/** The SYNONYM s_ editor. */
		SYNONYMS_EDITOR("Synonyms editor"),
		/** The REA d_ only. */
		READ_ONLY("Read only");

		/** The name. */
		private final String name;

		/**
		 * Instantiates a new editor mode.
		 * 
		 * @param name
		 *            the name
		 */
		private EditorMode(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return this.name;
		}

	}

	/**
	 * The Enum PreferredTermDefault.
	 */
	public enum PreferredTermDefault implements Serializable {

		/** The BLANK. */
		BLANK("Blank"),
		/** The SOURCE. */
		SOURCE("Source"),
		/** The BES t_ similarit y_ match. */
		BEST_SIMILARITY_MATCH("Best similarity match");

		/** The name. */
		private final String name;

		/**
		 * Instantiates a new preferred term default.
		 * 
		 * @param name
		 *            the name
		 */
		private PreferredTermDefault(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return this.name;
		}

	}

	/**
	 * The Enum TreeComponent.
	 */
	public enum TreeComponent implements Serializable {

		/** The FSN. */
		FSN("Fully Specified Name"),
		/** The PREFERRED. */
		PREFERRED("Preferred"),
		/** The SYNONYM. */
		SYNONYM("Synonym"),
		/** The RETIRED. */
		RETIRED("Retired descriptions"),
		/** The AUTHO r_ path. */
		AUTHOR_PATH("Author path");

		/** The name. */
		private final String name;

		/**
		 * Instantiates a new tree component.
		 * 
		 * @param name
		 *            the name
		 */
		private TreeComponent(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return this.name;
		}

	}

	/**
	 * The Enum InboxColumn.
	 */
	public enum InboxColumn implements Serializable {

		/** The SOURC e_ preferred. */
		SOURCE_PREFERRED(String.class, "Source preferred", 0),

		/** The TARGE t_ preferred. */
		TARGET_PREFERRED(String.class, "Target preferred", 1),

		/** The TARGE t_ fsn. */
		TARGET_FSN(String.class, "Target FSN", 2),

		/** The WORKLIST. */
		WORKLIST(String.class, "Worklist", 3),

		/** The DESTINATION. */
		DESTINATION(String.class, "Destination", 4),

		/** The STATUS. */
		STATUS(String.class, "Status", 5),
		
		/** The default description. */
		DEFAULT_DESCRIPTION(String.class, "Default description", 6);
		// STATUS_DATE(String.class,"Date",6);

		/** The editor class. */
		private final Class editorClass;

		/** The column name. */
		private final String columnName;

		/** The column number. */
		private final Integer columnNumber;

		/**
		 * Instantiates a new inbox column.
		 * 
		 * @param editorClass
		 *            the editor class
		 * @param columnName
		 *            the column name
		 * @param columnNumber
		 *            the column number
		 */
		InboxColumn(Class editorClass, String columnName, Integer columnNumber) {
			this.editorClass = editorClass;
			this.columnName = columnName;
			this.columnNumber = columnNumber;
		}

		/**
		 * Gets the column number.
		 * 
		 * @return the column number
		 */
		public Integer getColumnNumber() {
			return columnNumber;
		}

		/**
		 * Gets the column name.
		 * 
		 * @return the column name
		 */
		public String getColumnName() {
			return columnName;
		}

		/**
		 * Gets the editor class.
		 * 
		 * @return the editor class
		 */
		public Class getEditorClass() {
			return editorClass;
		}

	}

	/**
	 * Checks if is enable spell checker.
	 * 
	 * @return true, if is enable spell checker
	 */
	public boolean isEnableSpellChecker() {
		return enableSpellChecker;
	}

	/**
	 * Sets the enable spell checker.
	 * 
	 * @param enableSpellChecker
	 *            the new enable spell checker
	 */
	public void setEnableSpellChecker(boolean enableSpellChecker) {
		this.enableSpellChecker = enableSpellChecker;
	}

	/**
	 * Checks if is auto open next inbox item.
	 * 
	 * @return true, if is auto open next inbox item
	 */
	public boolean isAutoOpenNextInboxItem() {
		return autoOpenNextInboxItem;
	}

	/**
	 * Sets the auto open next inbox item.
	 * 
	 * @param autoOpenNextInboxItem
	 *            the new auto open next inbox item
	 */
	public void setAutoOpenNextInboxItem(boolean autoOpenNextInboxItem) {
		this.autoOpenNextInboxItem = autoOpenNextInboxItem;
	}

	/**
	 * Gets the source tree components.
	 * 
	 * @return the source tree components
	 */
	public LinkedHashSet<TreeComponent> getSourceTreeComponents() {
		return sourceTreeComponents;
	}

	/**
	 * Sets the source tree components.
	 * 
	 * @param sourceTreeComponents
	 *            the new source tree components
	 */
	public void setSourceTreeComponents(LinkedHashSet<TreeComponent> sourceTreeComponents) {
		this.sourceTreeComponents = sourceTreeComponents;
	}

	/**
	 * Gets the target tree components.
	 * 
	 * @return the target tree components
	 */
	public LinkedHashSet<TreeComponent> getTargetTreeComponents() {
		return targetTreeComponents;
	}

	/**
	 * Sets the target tree components.
	 * 
	 * @param targetTreeComponents
	 *            the new target tree components
	 */
	public void setTargetTreeComponents(LinkedHashSet<TreeComponent> targetTreeComponents) {
		this.targetTreeComponents = targetTreeComponents;
	}

	/**
	 * Gets the columns displayed in inbox.
	 * 
	 * @return the columns displayed in inbox
	 */
	public LinkedHashSet<InboxColumn> getColumnsDisplayedInInbox() {
		return ColumnsDisplayedInInbox;
	}

	/**
	 * Sets the columns displayed in inbox.
	 * 
	 * @param columnsDisplayedInInbox
	 *            the new columns displayed in inbox
	 */
	public void setColumnsDisplayedInInbox(LinkedHashSet<InboxColumn> columnsDisplayedInInbox) {
		ColumnsDisplayedInInbox = columnsDisplayedInInbox;
	}

	/**
	 * Gets the selected editor mode.
	 * 
	 * @return the selected editor mode
	 */
	public EditorMode getSelectedEditorMode() {
		return selectedEditorMode;
	}

	/**
	 * Sets the selected editor mode.
	 * 
	 * @param selectedEditorMode
	 *            the new selected editor mode
	 */
	public void setSelectedEditorMode(EditorMode selectedEditorMode) {
		this.selectedEditorMode = selectedEditorMode;
	}

	/**
	 * Gets the selected pref term default.
	 * 
	 * @return the selected pref term default
	 */
	public PreferredTermDefault getSelectedPrefTermDefault() {
		return selectedPrefTermDefault;
	}

	/**
	 * Sets the selected pref term default.
	 * 
	 * @param selectedPrefTermDefault
	 *            the new selected pref term default
	 */
	public void setSelectedPrefTermDefault(PreferredTermDefault selectedPrefTermDefault) {
		this.selectedPrefTermDefault = selectedPrefTermDefault;
	}

	
	
	public CompletionMode getCompletionMode() {
		return completionMode;
	}

	public void setCompletionMode(CompletionMode completionMode) {
		this.completionMode = completionMode;
	}

	/**
	 * Gets the selected fsn gen strategy.
	 * 
	 * @return the selected fsn gen strategy
	 */
	public FsnGenerationStrategy getSelectedFsnGenStrategy() {
		return selectedFsnGenStrategy;
	}

	/**
	 * Sets the selected fsn gen strategy.
	 * 
	 * @param selectedFsnGenStrategy
	 *            the new selected fsn gen strategy
	 */
	public void setSelectedFsnGenStrategy(FsnGenerationStrategy selectedFsnGenStrategy) {
		this.selectedFsnGenStrategy = selectedFsnGenStrategy;
	}

	/**
	 * Gets the selected ics generation strategy.
	 * 
	 * @return the selected ics generation strategy
	 */
	public IcsGenerationStrategy getSelectedIcsGenerationStrategy() {
		return selectedIcsGenerationStrategy;
	}

	/**
	 * Sets the selected ics generation strategy.
	 * 
	 * @param selectedIcsGenerationStrategy
	 *            the new selected ics generation strategy
	 */
	public void setSelectedIcsGenerationStrategy(IcsGenerationStrategy selectedIcsGenerationStrategy) {
		this.selectedIcsGenerationStrategy = selectedIcsGenerationStrategy;
	}

	/**
	 * Gets the project issues repository ids.
	 * 
	 * @return the project issues repository ids
	 */
	public List<UUID> getProjectIssuesRepositoryIds() {
		return projectIssuesRepositoryIds;
	}

	/**
	 * Sets the project issues repository ids.
	 * 
	 * @param projectIssuesRepositoryIds
	 *            the new project issues repository ids
	 */
	public void setProjectIssuesRepositoryIds(List<UUID> projectIssuesRepositoryIds) {
		this.projectIssuesRepositoryIds = projectIssuesRepositoryIds;
	}

	/**
	 * Gets the source issues repository ids.
	 * 
	 * @return the source issues repository ids
	 */
	public List<UUID> getSourceIssuesRepositoryIds() {
		return sourceIssuesRepositoryIds;
	}

	/**
	 * Sets the source issues repository ids.
	 * 
	 * @param sourceIssuesRepositoryIds
	 *            the new source issues repository ids
	 */
	public void setSourceIssuesRepositoryIds(List<UUID> sourceIssuesRepositoryIds) {
		this.sourceIssuesRepositoryIds = sourceIssuesRepositoryIds;
	}

	/**
	 * Sets the project default configuration.
	 * 
	 * @param isProjectDefaultConfiguration
	 *            the new project default configuration
	 */
	public void setProjectDefaultConfiguration(boolean isProjectDefaultConfiguration) {
		this.isProjectDefaultConfiguration = isProjectDefaultConfiguration;
	}

	/**
	 * Checks if is project default configuration.
	 * 
	 * @return true, if is project default configuration
	 */
	public boolean isProjectDefaultConfiguration() {
		return isProjectDefaultConfiguration;
	}

	/**
	 * Sets the translator roles.
	 * 
	 * @param translatorRoles
	 *            the translator roles
	 */
	public void setTranslatorRoles(HashMap<UUID, EditorMode> translatorRoles) {
		this.translatorRoles = translatorRoles;
	}

	/**
	 * Gets the translator roles.
	 * 
	 * @return the translator roles
	 */
	public HashMap<UUID, EditorMode> getTranslatorRoles() {
		return translatorRoles;
	}

	/**
	 * Sets the default similarity search option.
	 * 
	 * @param defaultSimilaritySearchOption
	 *            the new default similarity search option
	 */
	public void setDefaultSimilaritySearchOption(DefaultSimilaritySearchOption defaultSimilaritySearchOption) {
		this.defaultSimilaritySearchOption = defaultSimilaritySearchOption;
	}

	/**
	 * Gets the default similarity search option.
	 * 
	 * @return the default similarity search option
	 */
	public DefaultSimilaritySearchOption getDefaultSimilaritySearchOption() {
		return defaultSimilaritySearchOption;
	}

	/**
	 * Sets the editing panel open mode.
	 * 
	 * @param editingPanelOpenMode
	 *            the new editing panel open mode
	 */
	public void setEditingPanelOpenMode(EditingPanelOpenMode editingPanelOpenMode) {
		this.editingPanelOpenMode = editingPanelOpenMode;
	}

	/**
	 * Gets the editing panel open mode.
	 * 
	 * @return the editing panel open mode
	 */
	public EditingPanelOpenMode getEditingPanelOpenMode() {
		return editingPanelOpenMode;
	}

}
