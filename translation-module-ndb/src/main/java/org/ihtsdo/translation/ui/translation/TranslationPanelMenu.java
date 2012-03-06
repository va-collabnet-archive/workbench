/*
 * Created by JFormDesigner on Mon Mar 05 20:28:21 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author Guillermo Reynoso
 */
public class TranslationPanelMenu extends JPanel {
	public TranslationPanelMenu() {
		initComponents();
	}

	private void bAddFSNActionPerformed() {
		// TODO add your code here
	}

	private void mAddPrefActionPerformed() {
		// TODO add your code here
	}

	private void mAddDescActionPerformed() {
		// TODO add your code here
	}

	private void saveSimpleActionPerformed(ActionEvent e) {
		// TODO add your code here
	}

	private void saveAndAddActionPerformed(ActionEvent e) {
		// TODO add your code here
	}

	private void mSpellChkActionPerformed() {
		// TODO add your code here
	}

	private void mHistActionPerformed() {
		// TODO add your code here
	}

	private void mLogActionPerformed() {
		// TODO add your code here
	}

	private void searchDocumentsActionPerformed(ActionEvent e) {
		// TODO add your code here
	}

	private void sendMenuItemActionPerformed(ActionEvent e) {
		// TODO add your code here
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		menuBar1 = new JMenuBar();
		menu1 = new JMenu();
		bAddFSN = new JMenuItem();
		mAddPref = new JMenuItem();
		mAddDesc = new JMenuItem();
		saveSimple = new JMenuItem();
		saveAndAdd = new JMenuItem();
		menu3 = new JMenu();
		mSpellChk = new JMenuItem();
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

				//---- saveSimple ----
				saveSimple.setText("Save");
				saveSimple.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveSimpleActionPerformed(e);
					}
				});
				menu1.add(saveSimple);

				//---- saveAndAdd ----
				saveAndAdd.setText("Save And Add Description");
				saveAndAdd.setMnemonic('A');
				saveAndAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
				saveAndAdd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveAndAddActionPerformed(e);
					}
				});
				menu1.add(saveAndAdd);
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
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JMenuBar menuBar1;
	private JMenu menu1;
	private JMenuItem bAddFSN;
	private JMenuItem mAddPref;
	private JMenuItem mAddDesc;
	private JMenuItem saveSimple;
	private JMenuItem saveAndAdd;
	private JMenu menu3;
	private JMenuItem mSpellChk;
	private JMenu menu2;
	private JMenuItem mHist;
	private JMenuItem mLog;
	private JMenu menu4;
	private JMenuItem menuItem1;
	private JMenu actionMenu;
	private JMenuItem sendMenuItem;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
