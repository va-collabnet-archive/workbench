/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.activity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.swing.SwingTask;

public class ActivityPanel extends JPanel implements I_ShowActivity {
	
	char[] spinners = new char[] {'|', '\\', '-', '/'};
	int spinnerIndex = 0;
	public char nextSpinner() {
		if (spinnerIndex == spinners.length) {
			spinnerIndex = 0;
		}
		return spinners[spinnerIndex++];
	}
	
	private class ShowDeleteButton extends SwingTask {

		@Override
		public void doRun() {
			deleteButton.setVisible(true);			
		}
	
}
	private class ActivityProgress extends JProgressBar {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int width;
		public ActivityProgress(int width) {
			super();
			this.width = width;
		}
		@Override
		public Dimension getMaximumSize() {
			Dimension d = super.getMaximumSize();
			d.width = width;
			return d;
		}
		@Override
		public Dimension getMinimumSize() {
			Dimension d = super.getMinimumSize();
			d.width = width;
			return d;
		}
		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			d.width = width;
			return d;
		}
		
	}
	private class StopActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			complete();

		}

	}

	private class DeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ActivityViewer.removeActivity(ActivityPanel.this);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JProgressBar progressBar = new ActivityProgress(100);

	JButton stopButton = new JButton(new ImageIcon(ACE.class
			.getResource("/24x24/plain/stop.png")));

	JButton deleteButton = new JButton(new ImageIcon(ACE.class
			.getResource("/24x24/plain/delete2.png")));

	JLabel progressInfoUpper = new JLabel();

	JLabel progressInfoLower = new JLabel();
	private boolean showDelete;

	public ActivityPanel(boolean showBorder, boolean showDelete) {
		super(new GridBagLayout());
		this.showDelete = showDelete;
		progressInfoUpper.setFont(new Font("Dialog", java.awt.Font.BOLD, 10));
		progressInfoLower.setFont(new Font("Dialog", 0, 10));

		GridBagConstraints c = new GridBagConstraints();

		JPanel infoGridPanel = new JPanel(new GridLayout(2, 1));
		infoGridPanel.add(progressInfoUpper);
		infoGridPanel.add(progressInfoLower);
		infoGridPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.WEST;
		add(infoGridPanel, c);

		c.weightx = 0.0;
		c.gridx++;
		c.anchor = GridBagConstraints.EAST;
		add(progressBar, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx++;
		add(stopButton, c);
		stopButton.addActionListener(new StopActionListener());
		deleteButton.setVisible(false);
		deleteButton.addActionListener(new DeleteActionListener());
		add(deleteButton, c);
		if (showBorder) {
			setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));
		}
	}

	public JPanel getViewPanel() {
		return this;
	}

	public void setProgressInfoUpper(String text) {
		progressInfoUpper.setText(text);
	}

	public void setProgressInfoLower(String text) {
		if (text.startsWith("<html>")) {
			progressInfoLower.setText("<html>   " + text.substring(6));
		} else {
			progressInfoLower.setText("   " + text);
		}
	}

	public void setIndeterminate(boolean newValue) {
		progressBar.setIndeterminate(newValue);
	}

	public void setMaximum(final int n) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setMaximum(n);
			}
		});
	}

	public void setValue(final int n) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(n);
			}
		});
	}

	public void complete() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				stopButton.setVisible(false);
				progressBar.setVisible(false);
				if (showDelete) {
					ACE.timer.schedule(new ShowDeleteButton(), 1000);
				}
			}
		});
	}

	public void addActionListener(final ActionListener l) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				stopButton.addActionListener(l);
			}
		});
	}

	public void removeActionListener(final ActionListener l) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				stopButton.removeActionListener(l);
			}
		});
	}

}
