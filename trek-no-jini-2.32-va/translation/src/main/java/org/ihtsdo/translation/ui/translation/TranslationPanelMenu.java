/*
 * Created by JFormDesigner on Mon Mar 05 20:28:21 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.event.AddDescriptionEvent;
import org.ihtsdo.translation.ui.event.AddFsnEvent;
import org.ihtsdo.translation.ui.event.AddPreferedDescriptionEvent;
import org.ihtsdo.translation.ui.event.FireSaveEvent;
import org.ihtsdo.translation.ui.event.HistoryEvent;
import org.ihtsdo.translation.ui.event.LogEvent;
import org.ihtsdo.translation.ui.event.SearchDocumentEvent;
import org.ihtsdo.translation.ui.event.SendToConceptViewerR1Event;
import org.ihtsdo.translation.ui.event.SpellcheckEvent;

/**
 * @author Guillermo Reynoso
 */
public class TranslationPanelMenu extends JPanel {
	private static final long serialVersionUID = 5076266671860562956L;

	public TranslationPanelMenu() {
		initComponents();
	}

	public void updateTranslationPanelMenue(boolean readOnlyMode, ConfigTranslationModule translConfig) {
		if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)) {
			bAddFSN.setEnabled(false);
			mAddDesc.setEnabled(false);
			mAddPref.setEnabled(true && !readOnlyMode);
		} else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.SYNONYMS_EDITOR)) {
			bAddFSN.setEnabled(false);
			mAddDesc.setEnabled(true && !readOnlyMode);
			mAddPref.setEnabled(false);
		} else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.FULL_EDITOR)) {
			bAddFSN.setEnabled(true && !readOnlyMode);
			mAddDesc.setEnabled(true && !readOnlyMode);
			mAddPref.setEnabled(true && !readOnlyMode);
		}
	}

	private void bAddFSNActionPerformed() {
		EventMediator.getInstance().fireEvent(new AddFsnEvent());
	}

	private void mAddPrefActionPerformed() {
		EventMediator.getInstance().fireEvent(new AddPreferedDescriptionEvent());
	}

	private void mAddDescActionPerformed() {
		EventMediator.getInstance().fireEvent(new AddDescriptionEvent());
	}

	private void mSpellChkActionPerformed() {
		EventMediator.getInstance().fireEvent(new SpellcheckEvent());
	}

	private void mHistActionPerformed() {
		EventMediator.getInstance().fireEvent(new HistoryEvent());
	}

	private void mLogActionPerformed() {
		EventMediator.getInstance().fireEvent(new LogEvent());
	}

	private void searchDocumentsActionPerformed(ActionEvent e) {
		EventMediator.getInstance().fireEvent(new SearchDocumentEvent());
	}

	private void sendMenuItemActionPerformed(ActionEvent e) {
		EventMediator.getInstance().fireEvent(new FireSaveEvent());
	}

	private void csendToConceptViewerMenuActionPerformed(ActionEvent e) {
		EventMediator.getInstance().fireEvent(new SendToConceptViewerR1Event());
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		menuBar1 = new JMenuBar();
		menu1 = new JMenu();
		bAddFSN = new JMenuItem();
		mAddPref = new JMenuItem();
		mAddDesc = new JMenuItem();
		menu3 = new JMenu();
		mSpellChk = new JMenuItem();
		csendToConceptViewerMenu = new JMenuItem();
		menu2 = new JMenu();
		mHist = new JMenuItem();
		mLog = new JMenuItem();
		menu4 = new JMenu();
		menuItem1 = new JMenuItem();
		actionMenu = new JMenu();
		sendMenuItem = new JMenuItem();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

		//======== menuBar1 ========
		{

			//======== menu1 ========
			{
				menu1.setText("E[d]it");
				menu1.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				menu1.setMnemonic('D');

				//---- bAddFSN ----
				bAddFSN.setText("Add FSN");
				bAddFSN.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				bAddFSN.setMnemonic('F');
				bAddFSN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				bAddFSN.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bAddFSNActionPerformed();
					}
				});
				menu1.add(bAddFSN);

				//---- mAddPref ----
				mAddPref.setText("Add Preferred");
				mAddPref.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				mAddPref.setMnemonic('P');
				mAddPref.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				mAddPref.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mAddPrefActionPerformed();
					}
				});
				menu1.add(mAddPref);

				//---- mAddDesc ----
				mAddDesc.setText("Add Description");
				mAddDesc.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				mAddDesc.setMnemonic('D');
				mAddDesc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				mAddDesc.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mAddDescActionPerformed();
					}
				});
				menu1.add(mAddDesc);
			}
			menuBar1.add(menu1);

			//======== menu3 ========
			{
				menu3.setText("[T]ools");
				menu3.setSelectedIcon(null);
				menu3.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				menu3.setMnemonic('T');

				//---- mSpellChk ----
				mSpellChk.setText("Spellcheck");
				mSpellChk.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				mSpellChk.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				mSpellChk.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mSpellChkActionPerformed();
					}
				});
				menu3.add(mSpellChk);

				//---- csendToConceptViewerMenu ----
				csendToConceptViewerMenu.setText("Send to concept viewer R-1");
				csendToConceptViewerMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				csendToConceptViewerMenu.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						csendToConceptViewerMenuActionPerformed(e);
					}
				});
				menu3.add(csendToConceptViewerMenu);
			}
			menuBar1.add(menu3);

			//======== menu2 ========
			{
				menu2.setText("[V]iew");
				menu2.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				menu2.setMnemonic('V');

				//---- mHist ----
				mHist.setText("History");
				mHist.setMnemonic('Y');
				mHist.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				mHist.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				mHist.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mHistActionPerformed();
					}
				});
				menu2.add(mHist);

				//---- mLog ----
				mLog.setText("Log");
				mLog.setMnemonic('G');
				mLog.setIcon(null);
				mLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				mLog.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				mLog.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mLogActionPerformed();
					}
				});
				menu2.add(mLog);
			}
			menuBar1.add(menu2);

			//======== menu4 ========
			{
				menu4.setText("[S]earch");
				menu4.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				menu4.setMnemonic('S');

				//---- menuItem1 ----
				menuItem1.setText("Search Documents");
				menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				menuItem1.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
				menuItem1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						searchDocumentsActionPerformed(e);
					}
				});
				menu4.add(menuItem1);
			}
			menuBar1.add(menu4);

			//======== actionMenu ========
			{
				actionMenu.setText("Act[i]on");
				actionMenu.setMnemonic('I');

				//---- sendMenuItem ----
				sendMenuItem.setText("Send");
				sendMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				sendMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						sendMenuItemActionPerformed(e);
					}
				});
				actionMenu.add(sendMenuItem);
				actionMenu.addSeparator();
			}
			menuBar1.add(actionMenu);
		}
		add(menuBar1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JMenuBar menuBar1;
	private JMenu menu1;
	private JMenuItem bAddFSN;
	private JMenuItem mAddPref;
	private JMenuItem mAddDesc;
	private JMenu menu3;
	private JMenuItem mSpellChk;
	private JMenuItem csendToConceptViewerMenu;
	private JMenu menu2;
	private JMenuItem mHist;
	private JMenuItem mLog;
	private JMenu menu4;
	private JMenuItem menuItem1;
	private JMenu actionMenu;
	private JMenuItem sendMenuItem;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
