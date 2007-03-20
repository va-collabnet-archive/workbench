package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinDescTuple;
import org.dwfa.vodb.types.ThinRelTuple;

import com.sleepycat.je.DatabaseException;

public class ConflictPanel extends JPanel implements ActionListener {

	public static class ConflictColors {
		private List<Color> conflictColors = new ArrayList<Color>();
		int currentColor = 0;
		public ConflictColors() {
			super();
			// Link for colors
			// http://www.w3schools.com/html/html_colornames.asp
			conflictColors.add(new Color(0x5F9EA0));
			conflictColors.add(new Color(0x7FFF00));
			conflictColors.add(new Color(0xD2691E));
			conflictColors.add(new Color(0x6495ED));
			conflictColors.add(new Color(0xDC143C));
			conflictColors.add(new Color(0xB8860B));
			conflictColors.add(new Color(0xFF8C00));
			conflictColors.add(new Color(0x8FBC8F));
			conflictColors.add(new Color(0x483D8B));
			conflictColors.add(new Color(0x1E90FF));
			conflictColors.add(new Color(0xFFD700));
			conflictColors.add(new Color(0xF0E68C));
			conflictColors.add(new Color(0x90EE90));
			conflictColors.add(new Color(0x8470FF));
		}

		public Color getColor() {
			if (currentColor == conflictColors.size()) {
				reset();
			}
			return conflictColors.get(currentColor++);
		}
		
		public void reset() {
			currentColor = 0;
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConflictColors colors = new ConflictColors();
	private JPanel commonPanel;
	private JPanel differencePanel;
	private JPanel versionPanel;
	Dimension maxPartPanelSize = new Dimension(TermLabelMaker.LABEL_WIDTH + 20, 4000);
	private JCheckBox showStatus = new JCheckBox("status");
	private JCheckBox longForm = new JCheckBox("long form");
	private ConceptBean cb;
	private AceFrameConfig config;
	


	public ConflictPanel() {
		super();
		initWithGridBagLayout();
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3), 
				BorderFactory.createLineBorder(Color.GRAY)));
		showStatus.addActionListener(this);
		longForm.addActionListener(this);
	}
	private void initWithGridBagLayout() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 1;
		c.gridy++;
		JLabel label = new JLabel("Conflicts:");
		label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
		add(label, c);
		c.gridx++;
		c.gridx++;
		add(longForm, c);
		c.gridx++;
		add(showStatus, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		
		commonPanel = new JPanel(new GridLayout(0,1));
		commonPanel.setName("commonPanel");
		commonPanel.setBorder(BorderFactory.createTitledBorder("Common: "));
		add(commonPanel, c);
		c.gridx = c.gridx + c.gridwidth;
		differencePanel = new JPanel(new GridLayout(0,1));
		differencePanel.setName("difference panel");
		differencePanel.setBorder(BorderFactory.createTitledBorder("Differences: "));
		add(differencePanel, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		versionPanel = new JPanel(new GridBagLayout());
		versionPanel.setName("versionPanel");
		versionPanel.setBorder(BorderFactory.createTitledBorder("Versions: "));
		JScrollPane differenceScroller = new JScrollPane(versionPanel);
		differenceScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		add(differenceScroller, c);
	}
	
	public void setConcept(ConceptBean cb, AceFrameConfig config) throws DatabaseException {
		this.cb = cb;
		this.config = config;
		commonPanel.removeAll();
		differencePanel.removeAll();
		versionPanel.removeAll();
		if (cb != null) {
			List<JLabel> commonLabels = cb.getCommonLabels(longForm.isSelected(), showStatus.isSelected(), config);
			JPanel commonPartPanel = new JPanel();
			commonPartPanel.setLayout(new BoxLayout(commonPartPanel, BoxLayout.Y_AXIS));
			for (JLabel l: commonLabels) {
				commonPartPanel.add(l);
			}
			commonPanel.add(commonPartPanel);
			
			Map<ThinDescTuple, Color> desColorMap= new HashMap<ThinDescTuple, Color>();
			Map<ThinRelTuple, Color> relColorMap= new HashMap<ThinRelTuple, Color>();
			colors.reset();
			Collection<JLabel> conflictingLabels = cb.getConflictingLabels(longForm.isSelected(), 
					showStatus.isSelected(), config, colors, desColorMap, relColorMap);
			JPanel conflictPartPanel = new JPanel();
			conflictPartPanel.setLayout(new BoxLayout(conflictPartPanel, BoxLayout.Y_AXIS));
			for (JLabel l: conflictingLabels) {
				conflictPartPanel.add(l);
			}
			differencePanel.add(conflictPartPanel);

			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.VERTICAL;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.weightx = 0;
			c.weighty = 0;
			c.gridx = 0;
			c.gridy = 0;

			for (Position p: config.getViewPositionSet()) {
				JPanel statePanel = cb.getVersionView(p, config, desColorMap, relColorMap);
				versionPanel.add(statePanel, c);
				c.gridx++;
			}
			c.weightx = 1.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			versionPanel.add(new JPanel(), c);
		}
	}
	public void actionPerformed(ActionEvent e) {
		try {
			setConcept(cb, config);
			Component parent = this.getParent();
			while (parent != null) {
				parent.invalidate();
				parent = this.getParent();
			}
		} catch (DatabaseException e1) {
			AceLog.alertAndLog(this, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
		}
	}	
}
