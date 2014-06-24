/*
 * Created by JFormDesigner on Wed Feb 08 14:27:07 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.TableColumnModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.Comment;
import org.ihtsdo.project.refset.CommentsRefset;
import org.ihtsdo.project.refset.WorkflowRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.translation.model.CommentTableModel;
import org.ihtsdo.translation.ui.EditorPaneRenderer;

/**
 * @author Guillermo Reynoso
 */
public class CommentsManagerPanel extends JPanel {

	private static final long serialVersionUID = 2095808123319210358L;
	/** The Constant HEADER_SEPARATOR. */
	private static final String HEADER_SEPARATOR = " // ";
	/** The Constant COMMENT_HEADER_SEP. */
	private static final String COMMENT_HEADER_SEP = ": -";

	/** The Constant REFSET_COMMENT_NAME. */
	private static final Object REFSET_COMMENT_NAME = "Language comment";

	/** The Constant WORKLIST_COMMENT_NAME. */
	private static final Object WORKLIST_COMMENT_NAME = "Worklist comment";

	/** The html footer. */
	private String htmlFooter = "</body></html>";

	/** The html header. */
	private String htmlHeader = "<html><body><font style='color:blue'>";

	/** The end p. */
	private String endP = "</font>";

	/** The comment table model */
	private CommentTableModel commentsTableModel;

	/** The formatter. */
	private SimpleDateFormat formatter;
	private WorkListMember worklistMember;
	/** The role. */
	private I_GetConceptData role;
	private WorkflowRefset targetLangRefset;
	private I_ConfigAceFrame config;
	private boolean readonly;

	public CommentsManagerPanel() {
		super();
		initComponents();

		cmbTarComm.addItem(REFSET_COMMENT_NAME);
		cmbTarComm.addItem(WORKLIST_COMMENT_NAME);

		commentsTableModel = new CommentTableModel();
		tblComm.setModel(commentsTableModel);
		tblComm.setSelectionBackground(Color.YELLOW);

		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");

		refTable.setContentType("text/html");
		refTable.setEditable(false);
		refTable.setOpaque(false);

		label17.setIcon(IconUtilities.helpIcon);
		label17.setText("");
	}

	public void updateCommentsPanel(I_GetConceptData role, WorkflowRefset targetLangRefset, WorkListMember worklistMember) {
		this.worklistMember = worklistMember;
		this.targetLangRefset = targetLangRefset;
		this.role = role;

		getWebReferences();
		getPreviousComments();
	}

	public void setReadOnlyMode(boolean readonly) {
		this.readonly = readonly;
		bAddComent.setEnabled(true);
	}

	/**
	 * Gets the web references.
	 * 
	 * @return the web references
	 */
	private void getWebReferences() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			HashMap<URL, String> urls = new HashMap<URL, String>();

			StringBuffer sb = new StringBuffer("");
			sb.append("<html><body>");
			int urlCount = 0;
			if (targetLangRefset != null) {
				urls = targetLangRefset.getCommentsRefset(config).getUrls(worklistMember.getConcept().getConceptNid());
				urlCount = urls.size();
				for (URL url : urls.keySet()) {
					sb.append("<a href=\"");
					sb.append(url.toString());
					sb.append("\">");
					sb.append(url.toString());
					sb.append("</a><br>");
				}
			}
			urls = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config).getCommentsRefset(config).getUrls(worklistMember.getConcept().getConceptNid());

			urlCount += urls.size();
			for (URL url : urls.keySet()) {
				sb.append("<a href=\"");
				sb.append(url.toString());
				sb.append("\">");
				sb.append(url.toString());
				sb.append("</a><br>");
			}
			sb.append("</body></html>");

			refTable.setText(sb.toString());
			if (urlCount > 0) {
				tabbedPane1.setTitleAt(1, "<html>Web references <b><font color='red'>(" + urlCount + ")</font></b></html>");
			} else {
				tabbedPane1.getTitleAt(1);
				tabbedPane1.setTitleAt(1, "Web references (0)");
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * View comment.
	 */
	private void viewComment() {
		int row = tblComm.getSelectedRow();
		if (row > -1) {
			CommentPanel cp = new CommentPanel();
			String comm = (String) tblComm.getValueAt(row, 0) + "  -  " + tblComm.getValueAt(row, 1);
			String tmpDate = (String) tblComm.getValueAt(row, 1);
			String[] arrTmpDate = tmpDate.split(HEADER_SEPARATOR);
			String date = "";
			date = arrTmpDate[0];
			comm = comm.replace(htmlHeader, "");
			comm = comm.replace(htmlFooter, "");
			comm = comm.replace(endP, "");
			String[] arrComm = comm.split(COMMENT_HEADER_SEP);
			String header = arrComm[0];
			String[] headerComp = header.split(HEADER_SEPARATOR);
			String from = "";
			String role = "";
			String source = "";
			if (headerComp.length > 0) {
				source = headerComp[0];
				if (headerComp.length > 1) {
					role = headerComp[1];
					if (headerComp.length > 2) {
						from = headerComp[2];
					} else {
						from = headerComp[0];
					}
				} else {
					from = headerComp[0];
				}
			}
			cp.setFrom(from);
			cp.setRole(role);
			cp.setDate(date);
			cp.setSource(source);
			int index = comm.indexOf(COMMENT_HEADER_SEP);
			cp.setComment(comm.substring(index + COMMENT_HEADER_SEP.length()));

			JOptionPane.showMessageDialog(null, cp, "Comment", JOptionPane.INFORMATION_MESSAGE);

			this.requestFocus();

		}

	}

	/**
	 * Show new comment panel.
	 */
	public void showNewCommentPanel() {

		NewCommentPanel cPanel;
		cPanel = new NewCommentPanel();

		int action = JOptionPane.showOptionDialog(null, cPanel, "Enter new comment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		this.requestFocus();

		if (action == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (cPanel.getNewComment().trim().equals("")) {
			message("Cannot add a blank comment.");
			return;
		}
		saveComment(cPanel.getNewComment().trim(), cPanel.getCommentType(), cPanel.getCommentSubType());
	}

	/**
	 * Gets the previous comments.
	 * 
	 * @return the previous comments
	 */
	private void getPreviousComments() {
		I_ConfigAceFrame config;
		try {
			List<Comment> commentsList = new ArrayList<Comment>();
			config = Terms.get().getActiveAceFrameConfig();
			commentsTableModel.clearTable();
			// RowSorter<TableModel> sorter = new
			// TableRowSorter<TableModel>(commentsTableModel);

			if (targetLangRefset != null) {
				commentsList = targetLangRefset.getCommentsRefset(config).getFullComments(worklistMember.getConcept().getConceptNid());
				for (int i = commentsList.size() - 1; i > -1; i--) {
					commentsList.get(i).setCommentName("Language Refset: ");
				}
			}

			LinkedList<Comment> fullComments = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config).getCommentsRefset(config).getFullComments(this.worklistMember.getConcept().getConceptNid());
			for (int i = fullComments.size() - 1; i > -1; i--) {
				fullComments.get(i).setCommentName("Worklist: ");
			}
			if (fullComments != null) {
				commentsList.addAll(fullComments);
			}
			Collections.sort(commentsList);
			for (int i = commentsList.size() - 1; i > -1; i--) {
				String commentName = commentsList.get(i).getCommentName();
				if (commentsList.get(i).getTypeCid() == commentsList.get(i).getSubTypeCid()) {
					commentsTableModel.addRow(new Object[] { commentName + Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "", formatComment(commentsList.get(i).getComment()), commentsList.get(i) });
				} else {
					commentsTableModel.addRow(new Object[] { commentName + Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "/" + Terms.get().getConcept(commentsList.get(i).getSubTypeCid()), formatComment(commentsList.get(i).getComment()), commentsList.get(i) });
				}
			}

			TableColumnModel cmodel = tblComm.getColumnModel();
			cmodel.getColumn(0).setMinWidth(120);
			cmodel.getColumn(0).setMaxWidth(145);
			EditorPaneRenderer textAreaRenderer = new EditorPaneRenderer();
			cmodel.getColumn(0).setCellRenderer(textAreaRenderer);
			cmodel.getColumn(1).setCellRenderer(textAreaRenderer);
			tblComm.setRowHeight(65);
			tblComm.revalidate();
			tblComm.repaint();
			commentsTableModel.fireTableDataChanged();
			if (tblComm.getRowCount() > 0) {
				tabbedPane1.setTitleAt(0, "<html>Comments <b><font color='red'>(" + tblComm.getRowCount() + ")</font></b></html>");
			} else {
				tabbedPane1.setTitleAt(0, "<html>Comments (0)</font></b></html>");
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clear comments.
	 */
	public void clearComments() {
		commentsTableModel.clearTable();
		tabbedPane1.setTitleAt(0, "<html>Comments</font></b></html>");
	}

	/**
	 * Format comment.
	 * 
	 * @param comment
	 *            the comment
	 * @return the string
	 */
	private String formatComment(String comment) {
		long thickVer;
		thickVer = Long.parseLong(comment.substring(comment.trim().lastIndexOf(" ") + 1));
		String strDate = formatter.format(thickVer);
		String tmp = comment.substring(0, comment.lastIndexOf(" - Time:"));
		if (tmp.indexOf(COMMENT_HEADER_SEP) > -1) {
			tmp = tmp.replace(COMMENT_HEADER_SEP, endP + COMMENT_HEADER_SEP) + htmlFooter;
			return htmlHeader + "<I>" + strDate + "</I>" + HEADER_SEPARATOR + tmp;
		}
		return htmlHeader + "<I>" + strDate + "</I>" + COMMENT_HEADER_SEP + tmp;

	}

	/**
	 * Save comment.
	 * 
	 * @param comment
	 *            the comment
	 * @param commentType
	 *            the comment type
	 * @param commentSubType
	 *            the comment sub type
	 */
	private void saveComment(String comment, I_GetConceptData commentType, I_GetConceptData commentSubType) {
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			String targetComm = (String) cmbTarComm.getSelectedItem();
			if (targetComm.equals(WORKLIST_COMMENT_NAME)) {
				String fullName = config.getDbConfig().getFullName();
				WorkList workList = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config);

				CommentsRefset commentsRefset = workList.getCommentsRefset(config);
				if (commentSubType != null) {
					commentsRefset.addComment(worklistMember.getId(), commentType.getConceptNid(), commentSubType.getConceptNid(), role.toString() + HEADER_SEPARATOR + "<b>" + fullName + "</b>" + COMMENT_HEADER_SEP + comment);
				} else {
					commentsRefset.addComment(worklistMember.getId(), commentType.getConceptNid(), role.toString() + HEADER_SEPARATOR + "<b>" + fullName + "</b>" + COMMENT_HEADER_SEP + comment);
				}

			} else {
				CommentsRefset commRefset = targetLangRefset.getCommentsRefset(config);
				String fullName = config.getDbConfig().getFullName();
				if (commentSubType != null) {
					commRefset.addComment(this.worklistMember.getConcept().getConceptNid(), commentType.getConceptNid(), commentSubType.getConceptNid(), role.toString() + HEADER_SEPARATOR + "<b>" + fullName + "</b>" + COMMENT_HEADER_SEP + comment);
				} else {
					commRefset.addComment(this.worklistMember.getConcept().getConceptNid(), commentType.getConceptNid(), role.toString() + HEADER_SEPARATOR + "<b>" + fullName + "</b>" + COMMENT_HEADER_SEP + comment);
				}
			}
			// Terms.get().commit();

		} catch (TerminologyException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		getPreviousComments();
		getWebReferences();
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

	private void bAddComentActionPerformed() {
		showNewCommentPanel();
	}

	private void label17MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("COMMENTS");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void tblCommMouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (e.getClickCount() == 2) {
				viewComment();
			}
		} else if (SwingUtilities.isRightMouseButton(e) && !readonly) {
			// get the coordinates of the mouse click
			Point p = e.getPoint();

			// get the row index that contains that coordinate
			int rowNumber = tblComm.rowAtPoint(p);

			// Get the ListSelectionModel of the JTable
			ListSelectionModel model = tblComm.getSelectionModel();

			// set the selected interval of rows. Using the "rowNumber"
			// variable for the beginning and end selects only that one row.
			model.setSelectionInterval(rowNumber, rowNumber);
			popupMenu1.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private void refTableHyperlinkUpdate(HyperlinkEvent hle) {
		if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
			System.out.println("Opening: " + hle.getURL());
			System.out.println("Path: " + hle.getURL().getHost() + hle.getURL().getPath());
			try {
				Desktop desktop = null;
				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					desktop.browse(new URI(hle.getURL().toString()));
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

		}
	}

	private void viewCommentActionPerformed(ActionEvent e) {
		viewComment();
	}

	private void deleteCommentActionPerformed(ActionEvent e) {
		try {
			int selectedRow = tblComm.getSelectedRow();
			Comment selectedComment = (Comment) tblComm.getModel().getValueAt(selectedRow, 2);

			int rows = tblComm.getModel().getRowCount();
			if (selectedRow != 0) {
				for (int i = selectedRow - 1; i < rows; i--) {
					Comment comment = (Comment) tblComm.getModel().getValueAt(i, 2);
					if (!comment.getComment().contains(config.getDbConfig().getFullName())) {
						JOptionPane.showMessageDialog(this, "<html><body>This comment can no longer be deleted.", "Message", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}

			Collection<? extends IdBI> ids = null;
			config = Terms.get().getActiveAceFrameConfig();
			if (!selectedComment.getComment().contains(config.getDbConfig().getFullName())) {
				JOptionPane.showMessageDialog(this, "<html><body>This comment can no longer be deleted.", "Message", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (selectedComment != null) {
				CommentsRefset.retireCommentsMember(selectedComment.getExtension());
			}
			getPreviousComments();
		} catch (TerminologyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		panel17 = new JPanel();
		bAddComent = new JButton();
		cmbTarComm = new JComboBox();
		label17 = new JLabel();
		tabbedPane1 = new JTabbedPane();
		scrollPane3 = new JScrollPane();
		tblComm = new JTable();
		scrollPane8 = new JScrollPane();
		refTable = new JEditorPane();
		popupMenu1 = new JPopupMenu();
		menuItem2 = new JMenuItem();
		menuItem3 = new JMenuItem();

		// ======== this ========
		setLayout(new BorderLayout());

		// ======== dialogPane ========
		{
			dialogPane.setLayout(new BorderLayout());

			// ======== panel17 ========
			{
				panel17.setLayout(new GridBagLayout());
				((GridBagLayout) panel17.getLayout()).columnWidths = new int[] { 0, 0, 0, 0 };
				((GridBagLayout) panel17.getLayout()).rowHeights = new int[] { 0, 0 };
				((GridBagLayout) panel17.getLayout()).columnWeights = new double[] { 0.0, 0.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel17.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

				// ---- bAddComent ----
				bAddComent.setText("[A]dd Comment");
				bAddComent.setMnemonic('A');
				bAddComent.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				bAddComent.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bAddComentActionPerformed();
					}
				});
				panel17.add(bAddComent, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- cmbTarComm ----
				cmbTarComm.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel17.add(cmbTarComm, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- label17 ----
				label17.setText("text");
				label17.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				label17.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label17MouseClicked(e);
					}
				});
				panel17.add(label17, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(panel17, BorderLayout.NORTH);

			// ======== tabbedPane1 ========
			{

				// ======== scrollPane3 ========
				{

					// ---- tblComm ----
					tblComm.setRowHeight(36);
					tblComm.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							tblCommMouseClicked(e);
						}
					});
					scrollPane3.setViewportView(tblComm);
				}
				tabbedPane1.addTab("Comments", scrollPane3);

				// ======== scrollPane8 ========
				{

					// ---- refTable ----
					refTable.setEditable(false);
					refTable.setMinimumSize(new Dimension(10, 16));
					refTable.setPreferredSize(new Dimension(10, 16));
					refTable.addHyperlinkListener(new HyperlinkListener() {
						@Override
						public void hyperlinkUpdate(HyperlinkEvent e) {
							refTableHyperlinkUpdate(e);
						}
					});
					scrollPane8.setViewportView(refTable);
				}
				tabbedPane1.addTab("Web references", scrollPane8);

			}
			dialogPane.add(tabbedPane1, BorderLayout.CENTER);
		}
		add(dialogPane, BorderLayout.CENTER);

		// ======== popupMenu1 ========
		{

			// ---- menuItem2 ----
			menuItem2.setText("Delete");
			menuItem2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteCommentActionPerformed(e);
				}
			});
			popupMenu1.add(menuItem2);

			// ---- menuItem3 ----
			menuItem3.setText("View Comment");
			menuItem3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					viewCommentActionPerformed(e);
				}
			});
			popupMenu1.add(menuItem3);
		}
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel panel17;
	private JButton bAddComent;
	private JComboBox cmbTarComm;
	private JLabel label17;
	private JTabbedPane tabbedPane1;
	private JScrollPane scrollPane3;
	private JTable tblComm;
	private JScrollPane scrollPane8;
	private JEditorPane refTable;
	private JPopupMenu popupMenu1;
	private JMenuItem menuItem2;
	private JMenuItem menuItem3;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
