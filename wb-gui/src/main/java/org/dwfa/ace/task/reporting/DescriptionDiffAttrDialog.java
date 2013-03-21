/*
 * Created by JFormDesigner on Tue Jan 31 14:45:35 GMT-03:00 2012
 */

package org.dwfa.ace.task.reporting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.util.DatePicker;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.gui.ObjectTransferHandler;
import org.ihtsdo.tk.api.ContradictionException;

/**
 * @author Guillermo Reynoso
 */
public class DescriptionDiffAttrDialog extends JDialog {
	private static final long serialVersionUID = 393357623017080226L;
	private I_ConfigAceFrame config;
	private boolean canceled = false;
	private String initTime;
	private String laterTime;
	private UUID uuid1;
	private UUID uuid2;

	private TimeZone cstTime = TimeZone.getTimeZone("CST");
	private DateFormat cstFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	private DefaultListModel pathListModel;
	private CreateDescriptionsDiffRefsets createDescriptionsDiffRefsets;

	public DescriptionDiffAttrDialog(CreateDescriptionsDiffRefsets createDescriptionsDiffRefsets) {
		super();
		initComponents();
		try {
			errorLabel.setText("");
			errorLabel.setVisible(false);
			config = Terms.get().getActiveAceFrameConfig();
			this.createDescriptionsDiffRefsets = createDescriptionsDiffRefsets;
			cstFormat.setTimeZone(cstTime);
			initCustomComponents();
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					canceled = true;
					close();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createAndShowGui() {
		pack();
		setVisible(true);
	}

	private void close() {
		dispose();
	}

	private void initCustomComponents() {
		try {
//			// Set release dates
//			for (int i = 2002; i <= 2012; i++) {
//				Calendar c0 = new GregorianCalendar(cstTime);
//				Calendar c3 = new GregorianCalendar(cstTime);
//				Calendar c6 = new GregorianCalendar(cstTime);
//				Calendar c9 = new GregorianCalendar(cstTime);
//				c0.set(i, 0, 31, 23, 59, 59);
//				c3.set(i, 3, 30, 23, 59, 59);
//				c6.set(i, 6, 31, 23, 59, 59);
//				c9.set(i, 9, 31, 23, 59, 59);
//				
//				initTimeCombo.addItem(new TimeItem(c0));
//				initTimeCombo.addItem(new TimeItem(c3));
//				initTimeCombo.addItem(new TimeItem(c6));
//				initTimeCombo.addItem(new TimeItem(c9));
//				laterTimeCombo.addItem(new TimeItem(c0));
//				laterTimeCombo.addItem(new TimeItem(c3));
//				laterTimeCombo.addItem(new TimeItem(c6));
//				laterTimeCombo.addItem(new TimeItem(c9));
//			}

			ObjectTransferHandler oth = new ObjectTransferHandler(config, null);
			pathListModel = new DefaultListModel();
			pathListModel.addListDataListener(new ListDataListener() {
				@Override
				public void intervalRemoved(ListDataEvent arg0) {
				}

				@Override
				public void intervalAdded(ListDataEvent arg0) {
					I_GetConceptData addedRfst = (I_GetConceptData) pathListModel.get(0);
					try {
						if (!validateAsPathRefset(addedRfst.getConceptNid(), config)) {
							pathListModel.remove(0);
							JOptionPane.showMessageDialog(DescriptionDiffAttrDialog.this, "The selected refset is not a valid Path", "Warning", JOptionPane.WARNING_MESSAGE);
							pathListModel.addElement(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SNOMED_CORE.localize().getNid()));
						}
					} catch (HeadlessException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (TerminologyException e) {
						e.printStackTrace();
					} catch (ContradictionException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void contentsChanged(ListDataEvent arg0) {
				}
			});
			pathList.setModel(pathListModel);
			pathList.setName(ObjectTransferHandler.TARGET_LIST_NAME);
			pathListModel.addElement(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SNOMED_CORE.localize().getNid()));
			pathList.setTransferHandler(oth);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean validateAsPathRefset(int pathRefsetId, I_ConfigAceFrame config) throws IOException, TerminologyException, ContradictionException {
		I_TermFactory tf = Terms.get();
		I_GetConceptData languageRefsetConcept = tf.getConcept(pathRefsetId);
		I_GetConceptData pathRefset = tf.getConcept(ArchitectonicAuxiliary.Concept.PATH.localize().getNid());
		I_IntSet allowedTypes = tf.newIntSet();
		// allowedTypes.add(ArchitectonicAuxiliary.Concept.HAS_RELEASE_PATH_REFSET_ATTRIBUTE.localize().getNid());
		allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

		return pathRefset.isParentOf(languageRefsetConcept, config.getAllowedStatus(), config.getDestRelTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
	}

	public String getInitTime() {
		return this.initTime;
	}

	public String getLaterTime() {
		return this.laterTime;
	}

	public UUID getUUID1() {
		return this.uuid1;
	}

	public UUID getUUID2() {
		return this.uuid2;
	}

	private void okButtonActionPerformed(ActionEvent e) {
		if (!isDatesOk()) {
			errorLabel.setText("End date must be after start date.");
			errorLabel.setVisible(true);
			return;
		}
		Calendar initTimeItem = initTimeCombo.getSelectedDate();
		Calendar endTimeItem = laterTimeCombo.getSelectedDate();
		this.initTime = cstFormat.format(initTimeItem.getTime()) + " CST";
		this.laterTime = cstFormat.format(endTimeItem.getTime()) + " CST";
		I_GetConceptData pathConcept = (I_GetConceptData) pathListModel.getElementAt(0);
		this.uuid1 = pathConcept.getPrimUuid();
		this.uuid2 = pathConcept.getPrimUuid();
		createDescriptionsDiffRefsets.ok();
		close();
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		canceled = true;
		createDescriptionsDiffRefsets.cancel();
		close();
	}

	private void laterTimeComboItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			isDatesOk();
		}
	}

	private boolean isDatesOk() {
		if (initTimeCombo.getSelectedDate() != null && laterTimeCombo.getSelectedDate() != null) {
			Calendar initTimeItem = initTimeCombo.getSelectedDate();
			Calendar endTimeItem = laterTimeCombo.getSelectedDate();
			if (initTimeItem.after(endTimeItem) || initTimeItem.equals(endTimeItem)) {
				return false;
			}
		}
		errorLabel.setText("");
		errorLabel.setVisible(false);
		return true;
	}

	private void initTimeComboItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			isDatesOk();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		buttonBar = new JPanel();
		errorLabel = new JLabel();
		okButton = new JButton();
		cancelButton = new JButton();
		panel1 = new JPanel();
		label1 = new JLabel();
		GregorianCalendar minDate = new GregorianCalendar();
		minDate.setTimeInMillis(1);
		// date picker
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		initTimeCombo = new DatePicker(minDate, Calendar.getInstance(), dateFormat);
		label2 = new JLabel();
		laterTimeCombo = new DatePicker(minDate, Calendar.getInstance(), dateFormat);
		label3 = new JLabel();
		scrollPane1 = new JScrollPane();
		pathList = new JList();

		// ======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// ======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			// ======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] { 0, 85, 80 };
				((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] { 1.0, 0.0, 0.0 };

				// ---- errorLabel ----
				errorLabel.setText("text");
				errorLabel.setForeground(Color.red);
				buttonBar.add(errorLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.PAGE_END);

			// ======== panel1 ========
			{
				panel1.setLayout(new GridBagLayout());
				((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 0, 0 };
				((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
				((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };

				// ---- label1 ----
				label1.setText("Release start date");
				panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- initTimeCombo ----
				panel1.add(initTimeCombo, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

				// ---- label2 ----
				label2.setText("Release end date");
				panel1.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- laterTimeCombo ----
				panel1.add(laterTimeCombo, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

				// ---- label3 ----
				label3.setText("Path");
				panel1.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ======== scrollPane1 ========
				{
					scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

					// ---- pathList ----
					pathList.setVisibleRowCount(1);
					scrollPane1.setViewportView(pathList);
				}
				panel1.add(scrollPane1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(panel1, BorderLayout.CENTER);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel buttonBar;
	private JLabel errorLabel;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel panel1;
	private JLabel label1;
	private DatePicker initTimeCombo;
	private JLabel label2;
	private DatePicker laterTimeCombo;
	private JLabel label3;
	private JScrollPane scrollPane1;
	private JList pathList;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	class TimeItem {
		Calendar c;

		public Calendar getC() {
			return c;
		}

		public void setC(Calendar c) {
			this.c = c;
		}

		public TimeItem(Calendar c) {
			this.c = c;
		}

		@Override
		public String toString() {
			return cstFormat.format(c.getTime()) + " CST";
		}
	}

}
