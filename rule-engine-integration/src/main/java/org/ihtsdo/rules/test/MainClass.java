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
package org.ihtsdo.rules.test;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.ace.log.AceLog;

/**
 * The Class MainClass.
 */
public class MainClass {

	/** The tabbed pane. */
	public static MemoriousJTabbedPane tabbedPane;
	
	/** The count. */
	public static int count = 0;

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String args[]) throws Exception {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		tabbedPane = new MemoriousJTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		String titles[] = { "A", "B", "C", "D", "E", "F" };
		int mnemonic[] = { KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E,
				KeyEvent.VK_F };
		for (int i = 0, n = titles.length; i < n; i++) {
			add(tabbedPane, titles[i], mnemonic[i]);
			count++;
		}

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
				int index = sourceTabbedPane.getSelectedIndex();
				AceLog.getAppLog().info("Tab changed to: " + sourceTabbedPane.getTitleAt(index));
			}
		};
		tabbedPane.addChangeListener(changeListener);

		JButton add = new JButton("Add");
		add.setActionCommand("add");
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				count++;
				JButton button = new JButton("agregado " + count);
				tabbedPane.add("agregado" + count, button);
			}
		});


		JButton remove = new JButton("Remove");
		remove.setActionCommand("remove");
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabbedPane.remove(tabbedPane.getSelectedIndex());
			}
		});


		frame.add(tabbedPane, BorderLayout.CENTER);
		frame.add(add, BorderLayout.BEFORE_LINE_BEGINS);
		frame.add(remove, BorderLayout.AFTER_LINE_ENDS);
		frame.setSize(400, 150);
		frame.setVisible(true);

	}

	/**
	 * Adds the.
	 *
	 * @param tabbedPane the tabbed pane
	 * @param label the label
	 * @param mnemonic the mnemonic
	 */
	static void add(JTabbedPane tabbedPane, String label, int mnemonic) {
		int count = tabbedPane.getTabCount();
		JButton button = new JButton(label);
		tabbedPane.addTab(label, button);
		tabbedPane.setMnemonicAt(count, mnemonic);
	}

}