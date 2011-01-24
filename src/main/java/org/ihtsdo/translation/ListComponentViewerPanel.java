/*
 * Created by JFormDesigner on Wed Nov 03 18:55:55 GMT-03:00 2010
 */

package org.ihtsdo.translation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import javax.security.auth.login.LoginException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import net.jini.config.ConfigurationException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.ListItemBean;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.TranslationSearchHelper;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.translation.ui.TranslationConceptEditorRO;
import org.ihtsdo.translation.ui.ZebraJTable;

/**
 * @author Guillermo Reynoso
 */
public class ListComponentViewerPanel extends JPanel {

	private static final long serialVersionUID = -6140825319002970607L;
	private DefaultComboBoxModel projectComboModel;
	private I_ConfigAceFrame config;
	private DefaultTableModel lstTableModel;
	private String[] columns;

	public ListComponentViewerPanel(I_ConfigAceFrame config) {
		this.config = config;
		initComponents();
		initCustomComponents();

	}

	class MenuItemListener implements ActionListener{

		private HashSet<Integer> nodes;
		private ActionEvent accEvent;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (nodes!=null){

				try {
				final I_Work tworker;
				if (config.getWorker().isExecuting()) {
						tworker = config.getWorker().getTransactionIndependentClone();
				} else {

					tworker = config.getWorker();						
				}

				this.accEvent=e;
				Runnable r=new Runnable(){
					public void run(){
						I_ConfigAceFrame config;
						try {
							config = Terms.get().getActiveAceFrameConfig();
//							 Worker worker = (Worker)config.getWorker().getTransactionIndependentClone();
							I_EncodeBusinessProcess process=null;
							if (accEvent.getActionCommand().equals("Send to translation")){
								process=TerminologyProjectDAO.getBusinessProcess(new File("sampleProcesses/SendToTranslFromAS.bp"));
								process.writeAttachment("HASHED_CONCEPTS", nodes);
								

				                tworker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), config);
								tworker.execute(process);		
								return;
							}

						} catch (TerminologyException e) {

							e.printStackTrace();
						} catch (IOException e) {

							e.printStackTrace();
						} catch (TaskFailedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				};
                new Thread(r).start();

				} catch (LoginException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (PrivilegedActionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}
		public void setItem(HashSet<Integer> descSet){
			this.nodes=descSet;
		}

	}
	private void initCustomComponents() {

		columns = new String[] { "Source fsn", "Source preferred", "Target preferred", "Status" };

		String[][] data = null;
		lstTableModel = new DefaultTableModel(data, columns) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		listTable.setModel(lstTableModel);

		listTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			
			private MenuItemListener mItemListener; 
			private JPopupMenu menu;
			private JMenuItem mItem;
			private int xPoint;
			private int yPoint;
			
			private JPopupMenu getMenu(){
				
				if (menu==null){
					menu=new JPopupMenu();
					mItem=new JMenuItem();
					mItem.setText("Send to translation");
					mItem.setActionCommand("Send to translation");
					mItem.addActionListener(mItemListener);
					menu.add(mItem);
				}
				return menu;

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				try {	
					if (e.getButton()==e.BUTTON3){

						xPoint = e.getX();
						yPoint = e.getY();
						int row=listTable.rowAtPoint(new Point(xPoint,yPoint));
						if (row>-1){
							DefaultTableModel model=(DefaultTableModel)listTable.getModel();
							int[] selRows=listTable.getSelectedRows();
							HashSet<Integer> descSet=new HashSet<Integer> ();
							if (selRows.length<2){
								int rowModel=listTable.convertRowIndexToModel(row);
								ContextualizedDescription node = (ContextualizedDescription)model.getValueAt(rowModel,0);				
								descSet.add(node.getConceptId());
							}else{
								for (int i=0;i<selRows.length;i++){
									int rowModel=listTable.convertRowIndexToModel(selRows[i]);
									ContextualizedDescription node = (ContextualizedDescription)model.getValueAt(rowModel,0);				
									descSet.add(node.getConceptId());
								}
							}
							if (mItemListener==null){

								mItemListener=new MenuItemListener();
							}
							mItemListener.setItem(descSet);
							getMenu();
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									menu.show(listTable,xPoint, yPoint);
								}
							});
						}
					}
					if (e.getClickCount() == 2) {
						ContextualizedDescription item;
						if (listTable.getSelectedRow() > 0) {
							if (lstTableModel.getValueAt(listTable.getSelectedRow(), 0) != null) {
								item = (ContextualizedDescription) lstTableModel.getValueAt(listTable.getSelectedRow(), 0);
							} else if (lstTableModel.getValueAt(listTable.getSelectedRow(), 1) != null) {
								item = (ContextualizedDescription) lstTableModel.getValueAt(listTable.getSelectedRow(), 1);
							} else {
								item = (ContextualizedDescription) lstTableModel.getValueAt(listTable.getSelectedRow(), 2);
							}
						} else {
							return;
						}

						I_TerminologyProject project = (I_TerminologyProject) projectComboModel.getSelectedItem();
						WorkListMember worklistMember = new WorkListMember(item.getText(), item.getConceptId(), item.getConcept().getUUIDs(), null, null,
								ArchitectonicAuxiliary.Concept.ACTIVE.getUids().iterator().next(), new GregorianCalendar().getTimeInMillis());

						if (worklistMember != null) {
							TranslationConceptEditorRO editorRO = new TranslationConceptEditorRO();

							JDialog ro = new JDialog();
							ro.setContentPane(editorRO);
							ro.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
							ro.setSize(new Dimension(800, 650));
							Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
							Dimension ventana = ro.getSize();
							ro.setLocation((pantalla.width - ventana.width) / 2, (pantalla.height - ventana.height) / 2);

							worklistMember.setId(item.getConceptId());
							editorRO.updateUI((TranslationProject) project, worklistMember);
							ro.setVisible(true);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		projectComboModel = new DefaultComboBoxModel();
		projectCombo.setModel(projectComboModel);

		projectCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				messageLabel.setText("");
			}
		});

		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		projectComboModel.addElement("");
		for (I_TerminologyProject iTerminologyProject : projects) {
			projectComboModel.addElement(iTerminologyProject);
		}

	}

	private void refreshButtonActionPerformed(ActionEvent e) {
		try {
			String[][] data = null;
			lstTableModel = new DefaultTableModel(data, columns) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int x, int y) {
					return false;
				}
			};
			listTable.setModel(lstTableModel);

			// Populate table
			if (projectCombo.getSelectedItem() instanceof TranslationProject) {
				List<ListItemBean> list = TranslationSearchHelper.getListItemsForProject((TranslationProject) projectCombo.getSelectedItem());
				for (ListItemBean listItemBean : list) {
					lstTableModel.addRow(new Object[] { listItemBean.getSourceFsn(), listItemBean.getSourcePrefered(), listItemBean.getTargetPrefered(), listItemBean.getStatus() });
				}
			} else {
				messageLabel.setForeground(Color.RED);
				messageLabel.setText("No project selected.");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void closeButtonActionPerformed(ActionEvent e) {
		TranslationHelperPanel thp;
		try {
			thp = PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp = thp.getTabbedPanel();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.LIST_COMPONENT_VIEWER_NAME)) {
						tp.remove(i);
						tp.revalidate();
						tp.repaint();
					}
				}
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		buttonCont = new JPanel();
		projectCombo = new JComboBox();
		refreshButton = new JButton();
		tableScroll = new JScrollPane();
		listTable = new ZebraJTable();
		panel1 = new JPanel();
		messageLabel = new JLabel();
		closeButton = new JButton();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {133, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//======== buttonCont ========
		{
			buttonCont.setLayout(new GridBagLayout());
			((GridBagLayout)buttonCont.getLayout()).columnWidths = new int[] {199, 0, 0};
			((GridBagLayout)buttonCont.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)buttonCont.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
			((GridBagLayout)buttonCont.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
			buttonCont.add(projectCombo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- refreshButton ----
			refreshButton.setText("refresh");
			refreshButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					refreshButtonActionPerformed(e);
				}
			});
			buttonCont.add(refreshButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(buttonCont, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== tableScroll ========
		{
			tableScroll.setViewportView(listTable);
		}
		add(tableScroll, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- messageLabel ----
			messageLabel.setForeground(UIManager.getColor("Desktop.background"));
			panel1.add(messageLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- closeButton ----
			closeButton.setText("close");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeButtonActionPerformed(e);
				}
			});
			panel1.add(closeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel buttonCont;
	private JComboBox projectCombo;
	private JButton refreshButton;
	private JScrollPane tableScroll;
	private ZebraJTable listTable;
	private JPanel panel1;
	private JLabel messageLabel;
	private JButton closeButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
