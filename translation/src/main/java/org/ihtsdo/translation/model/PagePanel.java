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

package org.ihtsdo.translation.model;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * The Class PagePanel.
 *
 * @author Vahram Manukyan
 */
public class PagePanel extends JPanel implements MouseListener {
	
	/** The Constant BOLD. */
	private static final String BOLD = "<B>";
	
	/** The Constant BLUE_PREFIX. */
	private static final String BLUE_PREFIX = "<HTML><font color=\"blue\">";
	
	/** The Constant BLACK_PREFIX. */
	private static final String BLACK_PREFIX = "<HTML><font color=\"black\">";
	
	/** The Constant PAGE_CHANGED. */
	public static final String PAGE_CHANGED = "PAGE_CHANGED";
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7461559950374162592L;
	
	/** The new page. */
	private JLabel newPage;
	
	/** The page number str. */
	private String pageNumberStr;
	
	/** The layout. */
	private GridBagLayout layout;
	
	/** The page size. */
	private Integer pageSize;
	
	/** The total items. */
	private Integer totalItems;
	
	/** The last page. */
	private Integer lastPage;

	/**
	 * Instantiates a new page panel.
	 */
	public PagePanel() {
		initComponents();
		initCustomComponents();
	}

	/**
	 * Instantiates a new page panel.
	 *
	 * @param pageSize the page size
	 * @param totalItems the total items
	 */
	public PagePanel(Integer pageSize, Integer totalItems) {
		initComponents();
		this.totalItems = totalItems;
		this.pageSize = pageSize;
		this.lastPage = this.totalItems / this.pageSize;
		initCustomComponents();
	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		pageNumberStr = "1";
		setNewPage(page1Label);

		page1Label.addMouseListener(this);
		page2Label.addMouseListener(this);
		page3Label.addMouseListener(this);
		page4Label.addMouseListener(this);
		page5Label.addMouseListener(this);
		nextLabel.addMouseListener(this);
		previousLabel.addMouseListener(this);
		layout = (GridBagLayout) pagePanel.getLayout();

	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame mainFrame = new JFrame();
				mainFrame.setContentPane(new PagePanel(20, 200));
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainFrame.pack();
				mainFrame.setVisible(true);
			}
		});
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		pagePanel = new JPanel();
		previousLabel = new JLabel();
		page1Label = new JLabel();
		page2Label = new JLabel();
		page3Label = new JLabel();
		page4Label = new JLabel();
		page5Label = new JLabel();
		etcLabel = new JLabel();
		nextLabel = new JLabel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		//======== pagePanel ========
		{
			pagePanel.setLayout(new GridBagLayout());
			((GridBagLayout)pagePanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)pagePanel.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)pagePanel.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0E-4};
			((GridBagLayout)pagePanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- previousLabel ----
			previousLabel.setText("<HTML><font color=\"blue\">&lt&lt");
			previousLabel.setHorizontalAlignment(SwingConstants.CENTER);
			previousLabel.setPreferredSize(new Dimension(30, 15));
			pagePanel.add(previousLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

			//---- page1Label ----
			page1Label.setText("<HTML><font color=\"black\"><B>1");
			page1Label.setHorizontalAlignment(SwingConstants.CENTER);
			page1Label.setMinimumSize(new Dimension(20, 15));
			page1Label.setPreferredSize(new Dimension(20, 15));
			pagePanel.add(page1Label, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

			//---- page2Label ----
			page2Label.setText("<HTML><font color=\"blue\">2");
			page2Label.setHorizontalAlignment(SwingConstants.CENTER);
			page2Label.setPreferredSize(new Dimension(20, 15));
			page2Label.setMinimumSize(new Dimension(20, 15));
			pagePanel.add(page2Label, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

			//---- page3Label ----
			page3Label.setText("<HTML><font color=\"blue\">3");
			page3Label.setHorizontalAlignment(SwingConstants.CENTER);
			page3Label.setMinimumSize(new Dimension(20, 15));
			page3Label.setPreferredSize(new Dimension(20, 15));
			pagePanel.add(page3Label, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

			//---- page4Label ----
			page4Label.setText("<HTML><font color=\"blue\">4");
			page4Label.setHorizontalAlignment(SwingConstants.CENTER);
			page4Label.setMinimumSize(new Dimension(20, 15));
			page4Label.setPreferredSize(new Dimension(20, 15));
			page4Label.setVisible(false);
			pagePanel.add(page4Label, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

			//---- page5Label ----
			page5Label.setText("<HTML><font color=\"blue\">5");
			page5Label.setHorizontalAlignment(SwingConstants.CENTER);
			page5Label.setMinimumSize(new Dimension(20, 15));
			page5Label.setPreferredSize(new Dimension(20, 15));
			page5Label.setVisible(false);
			pagePanel.add(page5Label, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

			//---- etcLabel ----
			etcLabel.setText("<HTML><font color=\"blue\">...");
			etcLabel.setHorizontalAlignment(SwingConstants.CENTER);
			etcLabel.setMinimumSize(new Dimension(20, 15));
			etcLabel.setPreferredSize(new Dimension(20, 15));
			pagePanel.add(etcLabel, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

			//---- nextLabel ----
			nextLabel.setText("<HTML><font color=\"blue\">&gt&gt");
			nextLabel.setHorizontalAlignment(SwingConstants.CENTER);
			nextLabel.setPreferredSize(new Dimension(30, 15));
			pagePanel.add(nextLabel, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(pagePanel, BorderLayout.CENTER);
		// //GEN-END:initComponents
	}

	/**
	 * Sets the new page.
	 *
	 * @param newPage the new new page
	 */
	public void setNewPage(JLabel newPage) {
		if (this.newPage != null) {
			this.newPage.setText(this.newPage.getText().replace(BOLD, ""));
			this.newPage.setText(this.newPage.getText().replace("black", "blue"));
			newPage.setText(BLACK_PREFIX + BOLD + pageNumberStr);
			firePropertyChange(PAGE_CHANGED, this.newPage, new Integer(pageNumberStr));
			newPage.repaint();
			this.newPage.repaint();
			this.newPage = newPage;
		} else {
			newPage.setText(BLACK_PREFIX + BOLD + pageNumberStr);
			firePropertyChange(PAGE_CHANGED, this.newPage, new Integer(pageNumberStr));
			newPage.revalidate();
			this.newPage = newPage;
		}
	}

	/**
	 * Gets the new page.
	 *
	 * @return the new page
	 */
	public JLabel getNewPage() {
		return newPage;
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The page panel. */
	private JPanel pagePanel;
	
	/** The previous label. */
	private JLabel previousLabel;
	
	/** The page1 label. */
	private JLabel page1Label;
	
	/** The page2 label. */
	private JLabel page2Label;
	
	/** The page3 label. */
	private JLabel page3Label;
	
	/** The page4 label. */
	private JLabel page4Label;
	
	/** The page5 label. */
	private JLabel page5Label;
	
	/** The etc label. */
	private JLabel etcLabel;
	
	/** The next label. */
	private JLabel nextLabel;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		if (source instanceof JLabel && !source.equals(newPage)) {
			if (e.getSource().equals(nextLabel)) {
				nextButtonClicked();
			} else if (e.getSource().equals(previousLabel)) {
				previousButtonClicked();
			} else {
				numberClicked(source);
			}
		}
	}

	/**
	 * Number clicked.
	 *
	 * @param source the source
	 */
	private void numberClicked(Object source) {
		JLabel lab = (JLabel) source;
		if (!lab.getText().contains("...")) {
			pageNumberStr = lab.getText().split(BLUE_PREFIX)[1];
			Integer actualNum = new Integer(pageNumberStr);
			if (actualNum == 1) {
				page4Label.setVisible(false);
				layout.columnWeights[4] = 0.0;
				page5Label.setVisible(false);
				layout.columnWeights[5] = 0.0;
				page2Label.setText(BLUE_PREFIX + "2");
				page3Label.setText(BLUE_PREFIX + "3");
				setNewPage(lab);
			} else if (actualNum == 2) {
				page2Label.setText(BLUE_PREFIX + "2");
				page4Label.setText(BLUE_PREFIX + "4");
				page5Label.setText(BLUE_PREFIX + "5");
				setNewPage(lab);
				page4Label.setVisible(false);
				layout.columnWeights[4] = 0.0;
				page5Label.setVisible(false);
				layout.columnWeights[5] = 0.0;
			} else if (actualNum == 3) {
				page2Label.setText(BLUE_PREFIX + "2");
				page4Label.setText(BLUE_PREFIX + "4");
				page5Label.setText(BLUE_PREFIX + "5");
				page4Label.setVisible(true);
				layout.columnWeights[4] = 1.0;
				setNewPage(lab);
			} else if (actualNum == 4) {
				page2Label.setText(BLUE_PREFIX + "2");
				page3Label.setText(BLUE_PREFIX + "3");
				page5Label.setText(BLUE_PREFIX + "5");
				page5Label.setVisible(true);
				layout.columnWeights[5] = 1.0;
				page4Label.setVisible(true);
				layout.columnWeights[4] = 1.0;
				page5Label.setText(BLUE_PREFIX + (actualNum + 1));
				setNewPage(page4Label);
			} else if (actualNum == lastPage && actualNum >= 5) {
				System.out.println("lastPage");
			} else if (actualNum >= 5) {
				page2Label.setText(BLUE_PREFIX + "...");
				page3Label.setText(BLUE_PREFIX + (actualNum - 1));
				page5Label.setText(BLUE_PREFIX + (actualNum + 1));
				setNewPage(page4Label);
			}
		}
	}

	/**
	 * Previous button clicked.
	 */
	private void previousButtonClicked() {
		pageNumberStr = newPage.getText().split(BLACK_PREFIX + BOLD)[1];
		Integer actualNum = new Integer(pageNumberStr);
		System.out.println("Actual Number" + actualNum);
		pageNumberStr = (new Integer(actualNum - 1)).toString();
		if (actualNum == 2) {
			setNewPage(page1Label);
			page4Label.setVisible(false);
			layout.columnWeights[4] = 0.0;
		} else if (actualNum == 3) {
			page5Label.setVisible(false);
			layout.columnWeights[5] = 0.0;
			setNewPage(page2Label);
		} else if (actualNum == 4) {
			page2Label.setText(BLUE_PREFIX + "2");
			page3Label.setText(BLUE_PREFIX + "3");
			page5Label.setText(BLUE_PREFIX + "5");
			setNewPage(page3Label);
		} else if (actualNum == 5) {
			page2Label.setText(BLUE_PREFIX + "2");
			page3Label.setText(BLUE_PREFIX + "3");
			page5Label.setText(BLUE_PREFIX + "5");
			setNewPage(page4Label);
		} else if (actualNum > 5) {
			page2Label.setText(BLUE_PREFIX + "...");
			page3Label.setText(BLUE_PREFIX + (actualNum - 2));
			page5Label.setText(BLUE_PREFIX + (actualNum));
			setNewPage(page4Label);
		}
	}

	/**
	 * Next button clicked.
	 */
	private void nextButtonClicked() {
		pageNumberStr = newPage.getText().split(BLACK_PREFIX + BOLD)[1];
		Integer actualNum = new Integer(pageNumberStr);
		pageNumberStr = (new Integer(actualNum + 1)).toString();
		// Primera Pagina
		if (actualNum == 1) {
			// saltamos a segunda pagina
			setNewPage(page2Label);
		} else if (actualNum == 2) {
			// saltamos a tercera pagina
			setNewPage(page3Label);
			// aparece pagina 4
			page4Label.setVisible(true);
			layout.columnWeights[4] = 1.0;
		} else if (actualNum == 3) {
			setNewPage(page4Label);
			page5Label.setVisible(true);
			layout.columnWeights[5] = 1.0;
		} else if (actualNum >= 4) {
			page2Label.setText(BLUE_PREFIX + "...");
			page3Label.setText(BLUE_PREFIX + actualNum);
			page5Label.setText(BLUE_PREFIX + (actualNum + 2));
			setNewPage(page4Label);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}
}
