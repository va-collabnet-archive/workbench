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

package org.ihtsdo.project.view.details;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.issue.manager.implementation.CollabnetIssueManager;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.I_TerminologyProject.Type;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.PromotionRefset;

/**
 * The Class WorklistMemberLogPanel.
 * 
 * @author Guillermo Reynoso
 */
public class WorklistMemberLogPanel extends JPanel {

	/** The member. */
	private WorkListMember member;

	/** The formatter. */
	private SimpleDateFormat formatter;

	/** The hash list. */
	private TreeMap<String, List<LogObjectContainer>> hashList;

	/** The hash var col. */
	private HashMap<Integer, Integer> hashVarCol;

	/** The translation project. */
	private I_TerminologyProject project;

	/** The repo. */
	private IssueRepository repo;

	/** The regis. */
	private IssueRepoRegistration regis;

	/**
	 * Instantiates a new worklist member log panel.
	 */
	public WorklistMemberLogPanel() {
		initComponents();
		new JTable() {
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component returnComp = super.prepareRenderer(renderer, row, column);
				Color alternateColor = new Color(223, 228, 242);
				Color whiteColor = Color.WHITE;
				if (!returnComp.getBackground().equals(getSelectionBackground())) {
					Color bg = (row % 2 == 0 ? alternateColor : whiteColor);
					returnComp.setBackground(bg);
					bg = null;
				}
				return returnComp;
			}
		};
		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	}

	/**
	 * Show member changes.
	 * 
	 * @param member
	 *            the member
	 * @param translationProject
	 *            the translation project
	 * @param repo
	 *            the repo
	 * @param regis
	 *            the regis
	 */
	public void showMemberChanges(WorkListMember member, I_TerminologyProject translationProject, IssueRepository repo, IssueRepoRegistration regis) {
		this.member = member;
		this.project = translationProject;
		if (repo == null || regis == null) {
			try {

				if (translationProject.getProjectIssueRepo() != null) {
					this.repo = IssueRepositoryDAO.getIssueRepository(translationProject.getProjectIssueRepo());

					this.regis = IssueRepositoryDAO.getRepositoryRegistration(this.repo.getUuid(), Terms.get().getActiveAceFrameConfig());

				}
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				AceLog.getAppLog().alertAndLogException(e);
			}

		} else {
			this.repo = repo;
			this.regis = regis;
		}

		hashList = new TreeMap<String, List<LogObjectContainer>>();
		getPromotionStatuses();
		getDescriptions();
		getComments();
		if (this.repo != null && regis != null) {
			getIssues();
		}
		completeResultTable();
	}

	/**
	 * Gets the site user password.
	 * 
	 * @return the site user password
	 */
	private String getSiteUserPassword() {
		return regis.getPassword();
	}

	/**
	 * Gets the site user name.
	 * 
	 * @return the site user name
	 */
	private String getSiteUserName() {
		return regis.getUserId();
	}

	/**
	 * Gets the issues.
	 * 
	 * @return the issues
	 */
	private void getIssues() {
		if (repo != null && regis != null) {
			List<Issue> issueL = new ArrayList<Issue>();
			I_GetConceptData concept = this.member.getConcept();
			String strDate;
			if (member.getConcept() != null) {
				if (repo.getType() == IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()) {
					CollabnetIssueManager cIM = new CollabnetIssueManager();

					try {
						cIM.openRepository(repo, getSiteUserName(), getSiteUserPassword());
					} catch (Exception e) {
						AceLog.getAppLog().alertAndLogException(e);
						message("Sorry, cannot connect to repository.\n" + e.getMessage());
						return;
					}
					try {
						issueL = cIM.getIssuesForComponentId(concept.getUids().iterator().next().toString());
						for (Issue issue : issueL) {
							strDate = formatter.format(issue.getLastModifiedDate());
							addIssueTolist(issue.getUser(), strDate, issue.getTitle() + "\n" + issue.getDescription());

						}
					} catch (Exception e) {
						AceLog.getAppLog().alertAndLogException(e);
						message("Sorry, cannot retrieve comments for this issue.\n" + e.getMessage());
						return;
					}
				}
			}
		}
	}

	/**
	 * Adds the issue tolist.
	 * 
	 * @param userName
	 *            the user name
	 * @param version
	 *            the version
	 * @param stringIssue
	 *            the string issue
	 */
	private void addIssueTolist(String userName, String version, String stringIssue) {
		List<LogObjectContainer> objList;

		LogObjectContainer loc = new LogObjectContainer(LogObjectContainer.PARTS.ISSUE_PART, stringIssue, userName);
		if (hashList.containsKey(version)) {
			objList = hashList.get(version);
		} else {
			objList = new ArrayList<LogObjectContainer>();
		}
		objList.add(loc);
		hashList.put(version, objList);
	}

	/**
	 * Complete result table.
	 */
	private void completeResultTable() {
		// String[] columnNames = {"Author","Date",
		// "Prom.Status","Comment", "Issue"};

		// DefaultTableModel tableModel = new DefaultTableModel(data,
		// columnNames) {
		// private static final long serialVersionUID = 1L;
		//
		// public boolean isCellEditable(int x, int y) {
		// return false;
		// }
		// };
		List<String> lstHeaders = new ArrayList<String>();
		lstHeaders.add("Author");
		lstHeaders.add("Date");
		lstHeaders.add("Promotion Status");
		lstHeaders.add("Comment");
		lstHeaders.add("Issue");

		String[] lastObjectStringdata = new String[lstHeaders.size()];
		for (Integer did : hashVarCol.keySet()) {
			hashVarCol.put(did, lstHeaders.size());
			lstHeaders.add("Desc Id: " + did);
			// lstHeaders.add("Lang Refset for Desc Id: " + did);
		}
		int[] colWidth = new int[lstHeaders.size()];
		int[] rowHeight = new int[hashList.keySet().size()];
		List<String[]> dataVecList = new ArrayList<String[]>();
		List<LogObjectContainer> locList;

		Font fontTmp = table1.getFont();
		FontMetrics fontMetrics = new FontMetrics(fontTmp) {
		};
		Rectangle2D bounds;
		int tmpWidth;
		int tmpHeight;
		int rowCount = 0;
		for (String key : hashList.keySet()) {
			locList = hashList.get(key);
			String[] objStringdata = new String[lstHeaders.size()];
			objStringdata[1] = key;
			bounds = fontMetrics.getStringBounds(objStringdata[1], null);
			tmpWidth = (int) bounds.getWidth();
			if (colWidth[1] < tmpWidth) {
				colWidth[1] = tmpWidth;
			}
			tmpHeight = (int) bounds.getHeight();
			if (rowHeight[rowCount] < tmpHeight) {
				rowHeight[rowCount] = tmpHeight;
			}
			for (LogObjectContainer loc : locList) {
				Integer index;
				I_GetConceptData status;
				String text;
				switch (loc.getPartType()) {
				case PROMOTION_PART:
					try {
						I_ExtendByRefPartCid pval = (I_ExtendByRefPartCid) loc.getPart();
						status = Terms.get().getConcept(pval.getC1id());
						objStringdata[0] = Terms.get().getConcept(pval.getAuthorNid()).toString();
						bounds = fontMetrics.getStringBounds(objStringdata[0], null);
						tmpWidth = (int) bounds.getWidth();
						if (colWidth[0] < tmpWidth) {
							colWidth[0] = tmpWidth;
						}
						tmpHeight = (int) bounds.getHeight();
						if (rowHeight[rowCount] < tmpHeight) {
							rowHeight[rowCount] = tmpHeight;
						}
						objStringdata[2] = status.toString();
						bounds = fontMetrics.getStringBounds(objStringdata[2], null);
						tmpWidth = (int) bounds.getWidth();
						if (colWidth[2] < tmpWidth) {
							colWidth[2] = tmpWidth;
						}
						tmpHeight = (int) bounds.getHeight();
						if (rowHeight[rowCount] < tmpHeight) {
							rowHeight[rowCount] = tmpHeight;
						}
					} catch (TerminologyException e) {
						AceLog.getAppLog().alertAndLogException(e);
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLogException(e);
					}
					break;

				case COMMENTS_PART:
					text = loc.getStringPart();
					objStringdata[3] = text;

					bounds = fontMetrics.getStringBounds(objStringdata[3], null);
					tmpWidth = (int) bounds.getWidth();
					if (colWidth[3] < tmpWidth) {
						colWidth[3] = tmpWidth;
					}
					// tmpHeight = (int) bounds.getHeight();
					if (rowHeight[rowCount] < 66) {
						rowHeight[rowCount] = 66;
					}
					break;

				case ISSUE_PART:
					objStringdata[0] = loc.getUserName();
					bounds = fontMetrics.getStringBounds(objStringdata[0], null);
					tmpWidth = (int) bounds.getWidth();
					if (colWidth[0] < tmpWidth) {
						colWidth[0] = tmpWidth;
					}
					tmpHeight = (int) bounds.getHeight();
					if (rowHeight[rowCount] < tmpHeight) {
						rowHeight[rowCount] = tmpHeight;
					}
					text = loc.getStringPart();
					objStringdata[4] = text;
					bounds = fontMetrics.getStringBounds(objStringdata[4], null);
					tmpWidth = (int) bounds.getWidth();
					if (colWidth[4] < tmpWidth) {
						colWidth[4] = tmpWidth;
					}
					// tmpHeight = (int) bounds.getHeight();
					if (rowHeight[rowCount] < 66) {
						rowHeight[rowCount] = 66;
					}
					break;

				case DESCRIPTION_PART:
					try {
						I_DescriptionPart dval = (I_DescriptionPart) loc.getPart();
						index = hashVarCol.get(loc.getDid());

						objStringdata[0] = Terms.get().getConcept(dval.getAuthorNid()).toString();
						bounds = fontMetrics.getStringBounds(objStringdata[0], null);
						tmpWidth = (int) bounds.getWidth();
						if (colWidth[0] < tmpWidth) {
							colWidth[0] = tmpWidth;
						}
						tmpHeight = (int) bounds.getHeight();
						if (rowHeight[rowCount] < tmpHeight) {
							rowHeight[rowCount] = tmpHeight;
						}
						if (objStringdata[index] != null && !objStringdata[index].equals(""))
							objStringdata[index] += "\n";
						else
							objStringdata[index] = "";

						objStringdata[index] += dval.getText();
						I_GetConceptData type = Terms.get().getConcept(dval.getTypeId());
						objStringdata[index] += "\nType: " + type.toString();
						status = Terms.get().getConcept(dval.getStatusId());
						objStringdata[index] += "\nDesc.Status: " + status.toString();
						status = Terms.get().getConcept(dval.getStatusId());
						objStringdata[index] += "\nICS: " + dval.isInitialCaseSignificant();

						bounds = fontMetrics.getStringBounds(objStringdata[index], null);
						tmpWidth = (int) bounds.getWidth();
						if (colWidth[index] < tmpWidth) {
							colWidth[index] = tmpWidth;
						}
						// tmpHeight = (int) bounds.getHeight();
						if (rowHeight[rowCount] < 66) {
							rowHeight[rowCount] = 66;
						}
					} catch (TerminologyException e) {
						// TODO Auto-generated catch block
						AceLog.getAppLog().alertAndLogException(e);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						AceLog.getAppLog().alertAndLogException(e);
					}
					break;

				case LANG_REFSET_PART:
					try {
						I_ExtendByRefPartCid rval = (I_ExtendByRefPartCid) loc.getPart();
						objStringdata[0] = Terms.get().getConcept(rval.getAuthorNid()).toString();
						bounds = fontMetrics.getStringBounds(objStringdata[0], null);
						tmpWidth = (int) bounds.getWidth();
						if (colWidth[0] < tmpWidth) {
							colWidth[0] = tmpWidth;
						}
						tmpHeight = (int) bounds.getHeight();
						if (rowHeight[rowCount] < tmpHeight) {
							rowHeight[rowCount] = tmpHeight;
						}
						index = hashVarCol.get(loc.getDid());
						I_GetConceptData accept = Terms.get().getConcept(rval.getC1id());
						if (objStringdata[index] != null && !objStringdata[index].equals(""))
							objStringdata[index] += "\n";
						else
							objStringdata[index] = "";

						objStringdata[index] += "Acceptability: " + accept.toString();

						status = Terms.get().getConcept(rval.getStatusId());
						objStringdata[index] += "\nLang.Refset Status: " + status.toString();

						bounds = fontMetrics.getStringBounds(objStringdata[index], null);
						tmpWidth = (int) bounds.getWidth();
						if (colWidth[index] < tmpWidth) {
							colWidth[index] = tmpWidth;
						}
						// tmpHeight = (int) bounds.getHeight();
						if (rowHeight[rowCount] < 100) {
							rowHeight[rowCount] = 100;
						}

					} catch (TerminologyException e) {
						// TODO Auto-generated catch block
						AceLog.getAppLog().alertAndLogException(e);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						AceLog.getAppLog().alertAndLogException(e);
					}
					break;

				}
			}
			boolean emptyRow = true;
			for (int i = 0; i < lastObjectStringdata.length; i++) {
				if (lastObjectStringdata[i] != null) {
					emptyRow = false;
				}
			}
			if (emptyRow) {
				lastObjectStringdata = objStringdata;
			} else if (lastObjectStringdata[0] != null && objStringdata[0] != null && lastObjectStringdata[2] != null && objStringdata[2] != null && lastObjectStringdata[0].equals(objStringdata[0]) && lastObjectStringdata[2].equals(objStringdata[2])) {
				lastObjectStringdata = objStringdata;
			} else {
				dataVecList.add(lastObjectStringdata);
				rowCount++;
				lastObjectStringdata = objStringdata;
			}
		}
		dataVecList.add(lastObjectStringdata);
		rowCount++;

		String[][] data = new String[dataVecList.size()][lstHeaders.size()];
		dataVecList.toArray(data);
		String[] columnNames = new String[lstHeaders.size()];
		lstHeaders.toArray(columnNames);

		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		table1.setModel(tableModel);

		for (String[] string : data) {
			tableModel.addRow(string);
		}

		TableColumnModel cmodel = table1.getColumnModel();
		LogAreaRenderer logAreaRenderer = new LogAreaRenderer();
		for (int i = 0; i < cmodel.getColumnCount(); i++) {
			cmodel.getColumn(i).setCellRenderer(logAreaRenderer);
			if (colWidth[i] == 0) {
				cmodel.getColumn(i).setWidth(60);
			} else if (colWidth[i] > 350) {
				cmodel.getColumn(i).setPreferredWidth(350);
			} else {
				cmodel.getColumn(i).setPreferredWidth(colWidth[i] + 10);
			}
			// cmodel.getColumn(i).sizeWidthToFit();
		}

		for (int i = 0; i < tableModel.getRowCount(); i++) {
			table1.setRowHeight(i, rowHeight[i] + 5);
		}
		// table1.setRowHeight();
		table1.revalidate();
		table1.repaint();
		tableModel.fireTableDataChanged();

	}

	/**
	 * Gets the comments.
	 * 
	 * @return the comments
	 */
	private void getComments() {
		if (project.getProjectType().equals(Type.TRANSLATION)) {
			TranslationProject translationProject = (TranslationProject) project;
			I_ConfigAceFrame config;
			try {
				config = Terms.get().getActiveAceFrameConfig();
				I_GetConceptData langRefsetConcept = translationProject.getTargetLanguageRefset();
				LanguageMembershipRefset targetLangRefset = new LanguageMembershipRefset(langRefsetConcept, config);
				long thickVer;
				String strDate;

				List<String> comments = new ArrayList<String>();
				if (targetLangRefset != null) {
					comments.addAll(targetLangRefset.getCommentsRefset(config).getComments(this.member.getId()).values());
					for (int i = comments.size() - 1; i > -1; i--) {
						thickVer = Long.valueOf(comments.get(i).substring(comments.get(i).trim().lastIndexOf(" ") + 1));
						strDate = formatter.format(thickVer);
						addStringTolist(strDate, "Language refset comment: " + comments.get(i).substring(0, comments.get(i).lastIndexOf(" - Time:")));

					}
				}
				comments = new ArrayList<String>();
				comments.addAll(TerminologyProjectDAO.getWorkList(Terms.get().getConcept(this.member.getWorkListUUID()), config).getCommentsRefset(config).getComments(this.member.getId()).values());

				for (int i = comments.size() - 1; i > -1; i--) {
					thickVer = Long.valueOf(comments.get(i).substring(comments.get(i).trim().lastIndexOf(" ") + 1));
					strDate = formatter.format(thickVer);
					addStringTolist(strDate, "Worklist comment: " + comments.get(i).substring(0, comments.get(i).lastIndexOf(" - Time:")));

				}
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else {

		}

	}

	/**
	 * Gets the descriptions.
	 * 
	 * @return the descriptions
	 */
	private void getDescriptions() {
		if (project.getProjectType().equals(Type.TRANSLATION)) {
			try {
				hashVarCol = new HashMap<Integer, Integer>();
				TranslationProject translationProject = (TranslationProject) project;
				I_GetConceptData langRefset = translationProject.getTargetLanguageRefset();
				List<ContextualizedDescription> descriptions = ContextualizedDescription.getContextualizedDescriptions(member.getId(), langRefset.getConceptNid(), true);
				for (ContextualizedDescription description : descriptions) {
					if (description.getLanguageExtension() != null) {
						Integer did = description.getDescId();
						if (!hashVarCol.containsKey(did)) {
							hashVarCol.put(did, 0);
						}
						List<? extends I_DescriptionPart> parts = description.getDescriptionParts();
						if (parts != null) {
							for (I_AmPart part : parts) {
								addPartTolist(part, LogObjectContainer.PARTS.DESCRIPTION_PART, did);
							}
						}
						List<? extends I_ExtendByRefPart> lParts = description.getLanguageRefsetParts();

						if (lParts != null) {
							for (I_AmPart part : lParts) {
								addPartTolist(part, LogObjectContainer.PARTS.LANG_REFSET_PART, did);
							}
						}
					}
				}
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}

	/**
	 * Gets the promotion statuses.
	 * 
	 * @return the promotion statuses
	 */
	private void getPromotionStatuses() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			WorkList worklist = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(this.member.getWorkListUUID()), config);
			PromotionRefset promoRefset = worklist.getPromotionRefset(config);
			for (I_ExtendByRef promotionMember : Terms.get().getAllExtensionsForComponent(member.getId(), true)) {
				if (promotionMember.getRefsetId() == promoRefset.getRefsetId()) {
					List<? extends I_ExtendByRefPart> loopParts = promotionMember.getMutableParts();
					for (I_ExtendByRefPart loopPart : loopParts) {
						addPartTolist(loopPart, LogObjectContainer.PARTS.PROMOTION_PART, null);
					}
					return;
				}
			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Adds the string tolist.
	 * 
	 * @param version
	 *            the version
	 * @param stringPart
	 *            the string part
	 */
	private void addStringTolist(String version, String stringPart) {
		List<LogObjectContainer> objList;

		LogObjectContainer loc = new LogObjectContainer(LogObjectContainer.PARTS.COMMENTS_PART, stringPart);
		if (hashList.containsKey(version)) {
			objList = hashList.get(version);
		} else {
			objList = new ArrayList<LogObjectContainer>();
		}
		objList.add(loc);
		hashList.put(version, objList);
	}

	/**
	 * Adds the part tolist.
	 * 
	 * @param part
	 *            the part
	 * @param partType
	 *            the part type
	 * @param did
	 *            the did
	 */
	private void addPartTolist(I_AmPart part, LogObjectContainer.PARTS partType, Integer did) {
		String version;
		long thickVer;
		List<LogObjectContainer> objList;

		thickVer = Terms.get().convertToThickVersion(part.getVersion());

		version = formatter.format(thickVer);
		LogObjectContainer loc = new LogObjectContainer(partType, part, did);
		if (hashList.containsKey(version)) {
			objList = hashList.get(version);
		} else {
			objList = new ArrayList<LogObjectContainer>();
		}
		objList.add(loc);
		hashList.put(version, objList);
	}

	/**
	 * The Class LogAreaRenderer.
	 */
	class LogAreaRenderer extends JTextArea implements TableCellRenderer {

		/** The renderer. */
		private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

		// Column heights are placed in this Map
		/** The tablecell sizes. */

		/**
		 * Instantiates a new text area renderer.
		 */
		public LogAreaRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			// set the Font, Color, etc.
			renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setForeground(renderer.getForeground());
			setBackground(renderer.getBackground());
			setBorder(renderer.getBorder());
			setFont(renderer.getFont());
			String text = renderer.getText();
			setText(text);

			setToolTipText(text);
			int height_wanted = (int) getPreferredSize().getHeight();
			if (height_wanted > 48) {
				setRows(6);
			}

			// TableColumnModel columnModel = table.getColumnModel();
			// setSize(columnModel.getColumn(column).getWidth(), 0);
			// int height_wanted = (int) getPreferredSize().getHeight();
			// addSize(table, row, column, height_wanted);
			// height_wanted = findTotalMaximumRowSize(table, row);
			// if (height_wanted != table.getRowHeight(row)) {
			// table.setRowHeight(row, height_wanted);
			// }
			return this;
		}
	}

	/**
	 * Message.
	 * 
	 * @param string
	 *            the string
	 */
	private void message(String string) {

		JOptionPane.showOptionDialog(this, string, "Information", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
	}

	/**
	 * Button1 action performed.
	 */
	private void button1ActionPerformed() {
		showMemberChanges(this.member, this.project, this.repo, this.regis);
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		button1 = new JButton();
		scrollPane1 = new JScrollPane();
		table1 = new ZebraJTable();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 1.0, 0.0, 1.0E-4 };

		// ======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0E-4 };
			((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- button1 ----
			button1.setText("refresh");
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed();
				}
			});
			panel1.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== scrollPane1 ========
		{
			scrollPane1.setViewportView(table1);
		}
		add(scrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JButton button1;
	private JScrollPane scrollPane1;
	private ZebraJTable table1;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
