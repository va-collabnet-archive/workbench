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

package org.ihtsdo.translation.ui.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.translation.ui.ConfigTranslationModule;

/**
 * The Class TranslationIssuesRepositoryIdsPanel.
 *
 * @author Guillermo Reynoso
 */
public class TranslationIssuesRepositoryIdsPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8201368694726535525L;
	
	/** The conf trans. */
	private ConfigTranslationModule confTrans;
	
	/**
	 * Instantiates a new translation issues repository ids panel.
	 *
	 * @param config the config
	 * @param confTrans the conf trans
	 */
	public TranslationIssuesRepositoryIdsPanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans) {
		super();
		this.confTrans = confTrans;
		initComponents();
		initCustomComponents();
	}


	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {

		sourceRepoContainer.setMaximumSize(new Dimension(450,100));
		projectRepoContainer.setMaximumSize(new Dimension(450,100));
		
		try {
			I_TermFactory tf = Terms.get();

			if (confTrans.getProjectIssuesRepositoryIds() != null) {
				I_GetConceptData projectRepoConcept = tf.getConcept(
						confTrans.getProjectIssuesRepositoryIds());

				IssueRepository projectRepo = IssueRepositoryDAO.getIssueRepository(projectRepoConcept);

				projectRepoName.setText(projectRepo.getName());
				projectRepoId.setText(projectRepo.getRepositoryId());
				projectRepoUrl.setText(projectRepo.getUrl());
			}

			if (confTrans.getSourceIssuesRepositoryIds() != null) {
				I_GetConceptData sourceRepoConcept = tf.getConcept(
						confTrans.getSourceIssuesRepositoryIds());

				IssueRepository sourceRepo = IssueRepositoryDAO.getIssueRepository(sourceRepoConcept);

				sourceRepoName.setText(sourceRepo.getName());
				sourceRepoId.setText(sourceRepo.getRepositoryId());
				sourceRepoUrl.setText(sourceRepo.getUrl());
			}
			
		} catch (Exception e1) {
			e1.printStackTrace();
			error.setText("Problems initializing configuration see the logfile for more details");
		}
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		errorContainer = new JPanel();
		error = new JLabel();
		panel1 = new JPanel();
		projectRepoContainer = new JPanel();
		projNameLabel = new JLabel();
		projectRepoName = new JLabel();
		projIdLabel = new JLabel();
		projectRepoId = new JLabel();
		projUrlLabel = new JLabel();
		projectRepoUrl = new JLabel();
		sourceRepoContainer = new JPanel();
		srcNameLabel = new JLabel();
		sourceRepoName = new JLabel();
		srcIdLabel = new JLabel();
		sourceRepoId = new JLabel();
		srcUrlLabel = new JLabel();
		sourceRepoUrl = new JLabel();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		//======== errorContainer ========
		{
			errorContainer.setLayout(new BorderLayout(5, 5));

			//---- error ----
			error.setForeground(UIManager.getColor("Button.light"));
			errorContainer.add(error, BorderLayout.SOUTH);

			//======== panel1 ========
			{
				panel1.setBorder(new EmptyBorder(5, 5, 5, 5));
				panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));

				//======== projectRepoContainer ========
				{
					projectRepoContainer.setBorder(new CompoundBorder(
						new TitledBorder("Project issue repository"),
						new EmptyBorder(5, 5, 5, 5)));
					projectRepoContainer.setLayout(new GridBagLayout());
					((GridBagLayout)projectRepoContainer.getLayout()).columnWidths = new int[] {125, 125, 120, 0};
					((GridBagLayout)projectRepoContainer.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
					((GridBagLayout)projectRepoContainer.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)projectRepoContainer.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

					//---- projNameLabel ----
					projNameLabel.setText("Name");
					projNameLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					projectRepoContainer.add(projNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					projectRepoContainer.add(projectRepoName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- projIdLabel ----
					projIdLabel.setText("Id");
					projIdLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					projectRepoContainer.add(projIdLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					projectRepoContainer.add(projectRepoId, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- projUrlLabel ----
					projUrlLabel.setText("Url");
					projUrlLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					projectRepoContainer.add(projUrlLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					projectRepoContainer.add(projectRepoUrl, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel1.add(projectRepoContainer);

				//======== sourceRepoContainer ========
				{
					sourceRepoContainer.setBorder(new CompoundBorder(
						new TitledBorder("Source issue repository"),
						new EmptyBorder(5, 5, 5, 5)));
					sourceRepoContainer.setLayout(new GridBagLayout());
					((GridBagLayout)sourceRepoContainer.getLayout()).columnWidths = new int[] {125, 240, 0};
					((GridBagLayout)sourceRepoContainer.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
					((GridBagLayout)sourceRepoContainer.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)sourceRepoContainer.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

					//---- srcNameLabel ----
					srcNameLabel.setText("Name");
					srcNameLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					sourceRepoContainer.add(srcNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					sourceRepoContainer.add(sourceRepoName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- srcIdLabel ----
					srcIdLabel.setText("Id");
					srcIdLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					sourceRepoContainer.add(srcIdLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					sourceRepoContainer.add(sourceRepoId, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- srcUrlLabel ----
					srcUrlLabel.setText("Url");
					srcUrlLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					sourceRepoContainer.add(srcUrlLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					sourceRepoContainer.add(sourceRepoUrl, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel1.add(sourceRepoContainer);
			}
			errorContainer.add(panel1, BorderLayout.CENTER);
		}
		add(errorContainer, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The error container. */
	private JPanel errorContainer;
	
	/** The error. */
	private JLabel error;
	
	/** The panel1. */
	private JPanel panel1;
	
	/** The project repo container. */
	private JPanel projectRepoContainer;
	
	/** The proj name label. */
	private JLabel projNameLabel;
	
	/** The project repo name. */
	private JLabel projectRepoName;
	
	/** The proj id label. */
	private JLabel projIdLabel;
	
	/** The project repo id. */
	private JLabel projectRepoId;
	
	/** The proj url label. */
	private JLabel projUrlLabel;
	
	/** The project repo url. */
	private JLabel projectRepoUrl;
	
	/** The source repo container. */
	private JPanel sourceRepoContainer;
	
	/** The src name label. */
	private JLabel srcNameLabel;
	
	/** The source repo name. */
	private JLabel sourceRepoName;
	
	/** The src id label. */
	private JLabel srcIdLabel;
	
	/** The source repo id. */
	private JLabel sourceRepoId;
	
	/** The src url label. */
	private JLabel srcUrlLabel;
	
	/** The source repo url. */
	private JLabel sourceRepoUrl;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
